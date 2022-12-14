package de.tweis.flowdebug;

import org.jetbrains.annotations.Nullable;

public class ValidationUtility {
    public static boolean isValidFlowContext(@Nullable String flowContext) {
        return flowContext != null && flowContext.matches("^(?!.*(SubContext)).+(?<!/)$");
    }

    public static boolean isValidFlowDataTemporaryPath(@Nullable String flowDataTemporaryPath) {
        return flowDataTemporaryPath != null && flowDataTemporaryPath.matches("^.+/Data/Temporary$");
    }
}
