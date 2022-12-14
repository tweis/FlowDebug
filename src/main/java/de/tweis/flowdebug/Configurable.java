package de.tweis.flowdebug;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.jetbrains.php.util.PhpConfigurationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class Configurable implements com.intellij.openapi.options.Configurable, SearchableConfigurable {
    private final @NotNull Project project;

    private JPanel panel;
    private JTextField flowContextTextField;
    private TextFieldWithBrowseButton flowDataTemporaryPathTextField;

    public Configurable(@NotNull Project project) {
        this.project = project;
        setupUI();
        this.flowDataTemporaryPathTextField.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>(
                "Flow Data Temporary Path",
                "Specify path to Flow data temporary directory",
                this.flowDataTemporaryPathTextField,
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        ));
    }

    private void setupUI() {
        panel = new JPanel();
        panel.setLayout(new VerticalFlowLayout(true, false));

        JLabel flowContextLabel = new JLabel();
        flowContextLabel.setText("Flow Context:");
        panel.add(flowContextLabel);

        flowContextTextField = new JTextField();
        panel.add(flowContextTextField);

        JLabel flowDataTemporaryPathLabel = new JLabel();
        flowDataTemporaryPathLabel.setText("Flow Data Temporary Path:");
        panel.add(flowDataTemporaryPathLabel);

        flowDataTemporaryPathTextField = new TextFieldWithBrowseButton();
        panel.add(flowDataTemporaryPathTextField);
    }

    @Override
    public String getDisplayName() {
        return "FlowDebug";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return this.panel;
    }

    @Override
    public boolean isModified() {
        StateService.State state = StateService.getInstance(this.project).getState();

        return PhpConfigurationUtil.isModified(
                this.flowContextTextField,
                state.getFlowContext()
        ) || PhpConfigurationUtil.isModified(
                this.flowDataTemporaryPathTextField,
                state.getFlowDataTemporaryPath()
        );
    }

    @Override
    public void apply() throws ConfigurationException {
        StateService.State state = StateService.getInstance(this.project).getState();
        validate(this.flowContextTextField.getText(), this.flowDataTemporaryPathTextField.getText());
        state.setFlowContext(this.flowContextTextField.getText());
        state.setFlowDataTemporaryPath(this.flowDataTemporaryPathTextField.getText());
    }

    public static void validate(@Nullable String flowContext, @Nullable String flowDataTemporaryPath) throws ConfigurationException {
        if (!ValidationUtility.isValidFlowContext(flowContext)) {
            throw new ConfigurationException("Flow Context is not specified or invalid.");
        }

        if (!ValidationUtility.isValidFlowDataTemporaryPath(flowDataTemporaryPath)) {
            throw new ConfigurationException("Flow Data Temporary Path is not specified or invalid.");
        }
    }

    @Override
    public void reset() {
        StateService.State flowDebugState = StateService.getInstance(this.project).getState();
        this.flowContextTextField.setText(flowDebugState.getFlowContext());
        this.flowDataTemporaryPathTextField.setText(flowDebugState.getSerializedFlowDataTemporaryPath());
    }

    @Override
    public @Nullable String getHelpTopic() {
        return null;
    }

    public @NotNull String getId() {
        return "de.tweis.flowdebug.Configurable";
    }

    public Runnable enableSearch(String option) {
        return null;
    }
}
