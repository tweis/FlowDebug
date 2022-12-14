package de.tweis.flowdebug;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import com.jetbrains.php.util.PhpConfigurationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "FlowDebugConfiguration", storages = {@Storage("php.xml")})
public class StateService implements PersistentStateComponent<StateService.State> {
    private State state = new State();

    public static StateService getInstance(@NotNull Project project) {
        return project.getService(StateService.class);
    }

    public boolean isWellConfigured() {
        return ValidationUtility.isValidFlowContext(this.state.getFlowContext())
                && ValidationUtility.isValidFlowDataTemporaryPath(this.state.getFlowDataTemporaryPath());
    }

    public @Nullable StateService.State getState() {
        return this.state;
    }

    public void loadState(@NotNull StateService.State state) {
        this.state = state;
    }

    @NotNull
    public static String calculateContextSpecificDataTemporaryPath(
            @NotNull String flowContext,
            @NotNull String flowDataTemporaryPath
    ) {
        return String.format("%s/%s", flowDataTemporaryPath, flowContext.replace("/", "/SubContext"));
    }

    @Tag("FlowDebugState")
    static class State {
        private String flowContext;
        private String flowDataTemporaryPath;

        public State() {
            this(null, null);
        }

        public State(String flowContext, String flowDataTemporaryPath) {
            this.flowContext = flowContext;
            this.flowDataTemporaryPath = flowDataTemporaryPath;
        }

        @Tag("flowContext")
        public @Nullable String getFlowContext() {
            return this.flowContext;
        }

        public void setFlowContext(String flowContext) {
            this.flowContext = flowContext;
        }

        @Transient
        public @Nullable String getFlowDataTemporaryPath() {
            return this.flowDataTemporaryPath;
        }

        public void setFlowDataTemporaryPath(String flowDataTemporaryPath) {
            this.flowDataTemporaryPath = flowDataTemporaryPath;
        }

        @Tag("flowDataTemporaryPath")
        public @Nullable String getSerializedFlowDataTemporaryPath() {
            return PhpConfigurationUtil.serializePath(this.flowDataTemporaryPath);
        }

        public void setSerializedFlowDataTemporaryPath(@Nullable String flowDataTemporaryPath) {
            this.flowDataTemporaryPath = PhpConfigurationUtil.deserializePath(flowDataTemporaryPath);
        }
    }
}
