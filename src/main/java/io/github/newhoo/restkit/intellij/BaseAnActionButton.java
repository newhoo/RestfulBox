package io.github.newhoo.restkit.intellij;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.Supplier;

public abstract class BaseAnActionButton extends AnAction /*AnActionButton*/ {

    public BaseAnActionButton(@NotNull Supplier<String> dynamicText, Icon icon) {
        super(dynamicText, icon);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    public void update(@NotNull AnActionEvent e) {
        updateButton(e);
    }

    public void updateButton(@NotNull AnActionEvent e) {
        update(e);
    }
}
