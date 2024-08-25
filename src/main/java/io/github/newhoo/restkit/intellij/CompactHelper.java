package io.github.newhoo.restkit.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.tree.StructureTreeModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CompactHelper {

    public static void invalidate(StructureTreeModel<?> myTreeModel) {
//        myTreeModel.invalidate();
        myTreeModel.invalidateAsync();
    }

    public static int showYesNoDialog(@Nullable Project project, @NlsContexts.DialogMessage String message, @NotNull @NlsContexts.DialogTitle String title, @Nullable Icon icon) {
        return Messages.showYesNoDialog(project, message, title, icon);
    }

    public static int showYesNoDialog(@NotNull Component parent, @NlsContexts.DialogMessage String message, @NotNull @NlsContexts.DialogTitle String title, @Nullable Icon icon) {
        return Messages.showYesNoDialog(parent, message, title, icon);
    }

    @Nullable
    public static String showInputDialog(@Nullable Project project, @NlsContexts.DialogMessage String message, @NlsContexts.DialogTitle String title, @Nullable Icon icon, @Nullable @NonNls String initialValue, @Nullable InputValidator validator) {
        return Messages.showInputDialog(project, message, title, icon, initialValue, validator);
    }

    @Nullable
    public static String showEditableChooseDialog(@NlsContexts.DialogMessage String message, @NlsContexts.DialogTitle String title, @Nullable Icon icon, String[] values, String initialValue, @Nullable InputValidator validator) {
        return Messages.showEditableChooseDialog(message, title, icon, values, initialValue, validator);
    }

    public static int showChooseDialog(@NlsContexts.DialogMessage String message, @NlsContexts.DialogTitle String title, String[] values, String initialValue, @Nullable Icon icon) {
        return Messages.showChooseDialog(message, title, values, initialValue, icon);
    }

    public static int showChooseDialog(Project project, @NlsContexts.DialogMessage String message, @NlsContexts.DialogTitle String title, Icon icon, String[] values, String initialValue) {
        return Messages.showChooseDialog(project, message, title, icon, values, initialValue);
    }
}
