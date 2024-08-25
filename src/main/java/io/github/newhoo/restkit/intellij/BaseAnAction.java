package io.github.newhoo.restkit.intellij;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Supplier;

public abstract class BaseAnAction extends AnAction {
    public BaseAnAction() {
    }

    public BaseAnAction(Icon icon) {
        super(icon);
    }

    public BaseAnAction(@Nullable @NlsActions.ActionText String text) {
        super(text);
    }

    public BaseAnAction(@NotNull Supplier<@NlsActions.ActionText String> dynamicText) {
        super(dynamicText);
    }

    public BaseAnAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    public BaseAnAction(@NotNull Supplier<@NlsActions.ActionText String> dynamicText, @Nullable Icon icon) {
        super(dynamicText, icon);
    }

    public BaseAnAction(@NotNull Supplier<@NlsActions.ActionText String> dynamicText, @NotNull Supplier<@NlsActions.ActionDescription String> dynamicDescription, @Nullable Icon icon) {
        super(dynamicText, dynamicDescription, icon);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
