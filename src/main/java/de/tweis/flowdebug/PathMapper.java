package de.tweis.flowdebug;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.jetbrains.php.debug.template.PhpTemplateDebugStateService;
import com.jetbrains.php.debug.template.PhpTemplateLanguagePathMapper;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.util.pathmapper.PhpPathMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

@SuppressWarnings("UnstableApiUsage")
public class PathMapper extends PhpTemplateLanguagePathMapper {
    private static final Logger LOG = Logger.getInstance(PathMapper.class);

    @Override
    public boolean isTemplateFileType(@NotNull FileType fileType) {
        // This path mapper considers the original class to be the template file. A complete
        // distinction between original classes and proxy classes is not possible at this point,
        // as only information about the file type gets passed as a parameter and this is the
        // same for all PHP classes. The concrete decision whether a mapping is necessary must
        // be done afterwards in the method mapToPhp(), which has more information about the file.

        LOG.info(String.format("isTemplateFileType() was called: %s", fileType.getDefaultExtension()));

        return fileType.getDefaultExtension().equals(PhpFileType.INSTANCE.getDefaultExtension());
    }

    @Override
    public boolean isGeneratedFile(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        // This method is called by PhpTemplateLanguagePathMapper.getInstance(). Before the
        // method is called, the calling method already checks for the file extension for PHP
        // and plain text files. So the use of isTemplateFileType() for qualifying the class
        // as a mapper for the original class (PHP files) is not possible. Therefore, the
        // method must return a true-ish result for both proxy and original classes when it
        // is called by PhpTemplateLanguagePathMapper.getInstance().

        LOG.info(String.format("isGeneratedFile() was called: %s", virtualFile.getPath()));

        // Check if virtualFile is a generated Flow proxy class
        if (MappingUtility.isProxyClass(virtualFile)) {
            LOG.info(String.format("Flow proxy class was detected: %s", virtualFile.getPath()));
            return true;
        }

        // Check if virtualFile is an original class that has a corresponding
        // generated Flow proxy class
        VirtualFile proxyClass = MappingUtility.getProxyClassByOriginalClass(virtualFile, project);
        if (proxyClass != null) {
            LOG.info(String.format("Original class with corresponding Flow proxy class was detected: %s", virtualFile.getPath()));
            LOG.info(String.format("The corresponding Flow proxy class is: %s", proxyClass.getPath()));
            return true;
        }

        return false;
    }

    @Override
    public XSourcePosition mapToPhp(
            @NotNull XSourcePosition xSourcePosition,
            @NotNull Project project,
            @NotNull PhpPathMapper phpPathMapper
    ) {
        // original class => proxy class
        LOG.info(String.format("mapToPhp() was called: %s", xSourcePosition.getFile().getPath()));

        // Check whether the xSourcePosition file is a proxy class. If so, the user
        // explicitly set a breakpoint in the proxy class.
        if (MappingUtility.isProxyClass(xSourcePosition.getFile())) {
            LOG.info(String.format(
                    "xSourcePosition file is an proxy class => no mapping: %s",
                    xSourcePosition.getFile().getPath()
            ));

            VirtualFile originalClassFile = MappingUtility.getOriginalClassByProxyClass(
                    xSourcePosition.getFile(),
                    phpPathMapper
            );

            if (originalClassFile == null) {
                LOG.warn(String.format(
                        "xSourcePosition file is a proxy class, but no original class could be found => possibly not a valid breakpoint: %s",
                        xSourcePosition.getFile().getPath()
                ));
                return xSourcePosition;
            }

            // Placing breakpoints in proxy classes only makes sense if the breakpoints are in
            // proxy class exclusive code. If we detect that the breakpoint is not in exclusive
            // code, we return to the original breakpoint. When returning the original breakpoint,
            // PhpLineBreakpointHandler.registerBreakpoint deactivates the breakpoint as not mappable.
            if (!MappingUtility.isProxyClassExclusiveLine(xSourcePosition, originalClassFile)) {
                LOG.info(String.format(
                        "xSourcePosition file is an proxy class, but line is in non exclusive code => no valid breakpoint: %s",
                        originalClassFile.getPath()
                ));
                return xSourcePosition;
            }

            // If we return the original breakpoint, the breakpoint of PhpLineBreakpointHandler.registerBreakpoint
            // will be disabled as not assignable. To prevent this, we need to use Reflection to change the file
            // reference to the original class.
            VirtualFile backupXSourcePositionFile = xSourcePosition.getFile();

            try {
                Field myFileField = XSourcePositionImpl.class.getDeclaredField("myFile");
                myFileField.setAccessible(true);
                myFileField.set(xSourcePosition, originalClassFile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOG.warn(String.format(
                        "xSourcePosition file is an proxy class and line is exclusive code => valid breakpoint, but reflection error: %s",
                        e.getMessage()
                ));
            }

            return XDebuggerUtil.getInstance().createPosition(backupXSourcePositionFile, xSourcePosition.getLine());
        }

        // The xSourcePosition file should be an original class, so we should try
        // to map it to the corresponding proxy class.
        VirtualFile localFile = MappingUtility.getProxyClassByOriginalClass(xSourcePosition.getFile(), project);
        if (localFile != null) {
            return XDebuggerUtil.getInstance().createPosition(localFile, xSourcePosition.getLine());
        }

        return xSourcePosition;
    }

    @Override
    public XSourcePosition mapToTemplate(
            @Nullable XSourcePosition xSourcePosition,
            @NotNull Project project,
            @NotNull PhpPathMapper phpPathMapper
    ) {
        // proxy class => original class
        LOG.info(String.format(
                "mapToTemplate() was called: %s",
                (xSourcePosition != null) ? xSourcePosition.getFile().getPath() : "null"
        ));

        if (xSourcePosition == null) {
            return null;
        }

        VirtualFile originalClassFile = MappingUtility.getOriginalClassByProxyClass(
                xSourcePosition.getFile(),
                phpPathMapper
        );

        if (originalClassFile == null) {
            LOG.info(String.format("No original class could be found for proxy class: %s", xSourcePosition.getFile().getPath()));
            return null;
        }

        if (MappingUtility.isProxyClassExclusiveLine(xSourcePosition, originalClassFile)) {
            LOG.info(String.format("xSourcePosition line is greater than original class line count => no mapping: %s", originalClassFile.getPath()));
            return xSourcePosition;
        }

        return XDebuggerUtil.getInstance().createPosition(originalClassFile, xSourcePosition.getLine());
    }

    @Override
    protected PhpTemplateDebugStateService getTemplateDebugInstance(@NotNull Project project) {
        return null;
    }
}
