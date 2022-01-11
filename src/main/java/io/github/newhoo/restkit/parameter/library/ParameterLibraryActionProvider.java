package io.github.newhoo.restkit.parameter.library;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.InspectionWidgetActionProvider;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.ParameterLibrary;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 利用监视动作提供参数库操作
 *
 * @author huzunrong
 * @since 1.0.8
 */
public class ParameterLibraryActionProvider implements InspectionWidgetActionProvider {

    @Nullable
    @Override
    public AnAction createAction(@NotNull Editor editor) {
        Project project = editor.getProject();
        if (project == null
                || project.isDefault()
                || !CommonSettingComponent.getInstance(project).getState().isEnableParameterLibrary()) {
            return null;
        }
        String editorDoc = editor.getDocument().toString().replace("\\", "/");
        if (StringUtils.containsAny(editorDoc, "/Headers", "/Params", "/Body")) {
            ParameterLibrary parameterLibrary = ParameterLibrary.getInstance(project);
            DefaultActionGroup defaultActionGroup = new DefaultActionGroup(
                    new SaveParameterAction(editor, parameterLibrary),
                    new ShowParameterAction(editor, parameterLibrary)
            );
            if (StringUtils.contains(editorDoc, "/Body")) {
                defaultActionGroup.add(Separator.create());
            }
            return defaultActionGroup;
        }
        return null;
    }
}
