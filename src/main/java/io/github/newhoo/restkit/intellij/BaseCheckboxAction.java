package io.github.newhoo.restkit.intellij;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCheckboxAction extends CheckboxAction {

    public BaseCheckboxAction(String text) {
        super(text);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
