package de.tweis.flowdebug;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.util.pathmapper.PhpPathMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingUtility {
    private static final Logger LOG = Logger.getInstance(MappingUtility.class);

    private static final Pattern PATH_AND_FILENAME_PATTERN = Pattern.compile("(?m)^# PathAndFilename: (.*)$");

    public static boolean isProxyClass(@NotNull VirtualFile virtualFile) {
        // TODO: Improve heuristics by checking for more characteristics of the path of a proxy class
        return virtualFile.getPath().contains("Flow_Object_Classes");
    }

    public static boolean isProxyClassExclusiveLine(
            @NotNull XSourcePosition proxyClassSourcePosition,
            @NotNull VirtualFile originalClassFile
    ) {
        Document document = ReadAction.compute(() -> FileDocumentManager.getInstance().getDocument(originalClassFile));
        int originalFileLinesCount = (document != null) ? document.getLineCount() : 0;
        return (proxyClassSourcePosition.getLine() > originalFileLinesCount);
    }

    public static @Nullable VirtualFile getOriginalClassByProxyClass(
            @NotNull VirtualFile proxyClassFile,
            @NotNull PhpPathMapper phpPathMapper
    ) {
        try {
            String fileContent = VfsUtilCore.loadText(proxyClassFile);
            Matcher matcher = PATH_AND_FILENAME_PATTERN.matcher(fileContent);

            if (matcher.find()) {
                String originalFilePath = matcher.group(1);
                return phpPathMapper.getLocalFile(originalFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static VirtualFile getProxyClassByOriginalClass(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        StateService stateService = StateService.getInstance(project);
        if (stateService == null || !stateService.isWellConfigured()) {
            LOG.warn("No valid path to the context specific folder in Data/Temporary is set!");
            return null;
        }

        PsiManager psiManager = PsiManager.getInstance(project);

        PsiFile psiFile = psiManager.findFile(virtualFile);
        if (psiFile == null) {
            LOG.warn(String.format("No PSI file could be found for the virtual file: %s", virtualFile.getPath()));
            return null;
        }

        Collection<PhpClass> allClasses = PhpPsiUtil.findAllClasses(psiFile);
        if (allClasses.size() == 0) {
            LOG.warn(String.format("PSI file does not contain any PHP classes: %s", psiFile.getVirtualFile().getPath()));
            return null;
        }
        if (allClasses.size() > 1) {
            // This is a violation against PSR-1 3. and the associated autoloading PSRs (PSR-0 and PSR-4).
            LOG.warn(String.format("PSI file contain more than one PHP class: %s", psiFile.getVirtualFile().getPath()));
        }

        PhpClass originalClass = allClasses.iterator().next();

        String proxyClassFQN = originalClass.getFQN()
                .replaceAll("^\\\\", "")
                .replace('\\', '_');

        LOG.info(String.format("Generated proxy class FQN \"%s\" for original class FQN \"%s\"", proxyClassFQN, originalClass.getFQN()));

        StateService.State state = stateService.getState();
        String proxyClassCachePath = String.format(
                "%s/Cache/Code/Flow_Object_Classes/%s.php",
                StateService.calculateContextSpecificDataTemporaryPath(
                        state.getFlowContext(),
                        state.getFlowDataTemporaryPath()
                ),
                proxyClassFQN
        );

        return LocalFileSystem.getInstance().findFileByPath(proxyClassCachePath);
    }
}
