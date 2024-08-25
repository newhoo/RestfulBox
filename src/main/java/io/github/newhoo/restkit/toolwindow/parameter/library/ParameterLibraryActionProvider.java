package io.github.newhoo.restkit.toolwindow.parameter.library;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.InspectionWidgetActionProvider;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.config.global.GlobalSetting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static io.github.newhoo.restkit.common.RestConstant.EDITOR_FILENAME_PREFIX;

/**
 * 利用监视动作提供参数库操作
 *
 * @author huzunrong
 * @since 1.0.8
 */
public class ParameterLibraryActionProvider implements InspectionWidgetActionProvider {

    private final KeyboardShortcut saveParameterShortcut = KeyboardShortcut.fromString(SystemInfo.isMac ? "meta S" : "control S");
    private final KeyboardShortcut showParameterShortcut = KeyboardShortcut.fromString(SystemInfo.isMac ? "meta shift S" : "control shift S");

    @Nullable
    @Override
    public AnAction createAction(@NotNull Editor editor) {
        Project project = editor.getProject();
        if (project == null || project.isDefault()) {
            return null;
        }
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (!(file instanceof LightVirtualFile)) {
            return null;
        }
        String fileName = file.getName();
        if (!StringUtils.startsWith(fileName, EDITOR_FILENAME_PREFIX)) {
            return null;
        }
        GlobalSetting globalSetting = ConfigHelper.getGlobalSetting();
        if (!globalSetting.isEnableParameterLibrary()) {
            return null;
        }
        AnAction saveParameterAction = new SaveParameterAction(editor);
        AnAction showParameterAction = new ShowParameterAction(editor);
        DefaultActionGroup defaultActionGroup = new DefaultActionGroup(saveParameterAction, showParameterAction);

        if (globalSetting.isEnableParameterLibraryShortcut() && editor instanceof EditorImpl) {
            JComponent editorComponent = ((EditorImpl) editor).getScrollPane();
            saveParameterAction.registerCustomShortcutSet(new CustomShortcutSet(saveParameterShortcut), editorComponent);
            showParameterAction.registerCustomShortcutSet(new CustomShortcutSet(showParameterShortcut), editorComponent);
        }
        if (StringUtils.containsAny(fileName, "Body", "Response")) {
            defaultActionGroup.add(Separator.create());
        }
        return defaultActionGroup;
    }
}