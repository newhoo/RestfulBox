package io.github.newhoo.restkit.toolwindow.parameter.library;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.util.ui.JBUI;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.RestClientEditorInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.intellij.CompactHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * SaveParameterAction
 *
 * @author huzunrong
 * @since 1.0.8
 */
public class SaveParameterAction extends BaseAnAction implements CustomComponentAction {

    private final Editor editor;

    public SaveParameterAction(Editor editor) {
        super(() -> "", () -> RestBundle.message("toolkit.toolwindow.client.parameter.save.text"), AllIcons.Actions.MenuSaveall);
        this.editor = editor;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = editor.getProject();
        if (project == null || project.isDefault()) {
            return;
        }
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(editor.getDocument().getTextLength() > 0
                && RestDataKey.CLIENT_EDITOR_INFO.getData(e.getDataContext()) != null);
    }

    @Override
    public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        // JBUI.size(16)
        // ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
        return new ActionButtonWithText(this, presentation, place, JBUI.size(18)) {
            @Override
            protected int iconTextSpace() {
                return JBUI.scale(2);
            }

            @Override
            public Insets getInsets() {
                return JBUI.insets(2);
            }

            @Override
            protected Insets getMargins() {
//                    if (myPresentation.icon == AllIcons.General.ReaderMode) JBUI.emptyInsets() else JBUI.insetsRight(5)
                return JBUI.emptyInsets();
            }
        };
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        RestClientEditorInfo restClientEditorInfo = RestDataKey.CLIENT_EDITOR_INFO.getData(e.getDataContext());
        if (restClientEditorInfo == null) {
            return;
        }

        List<KV> parameterList = DataSourceHelper.getDataSource().selectParameter(restClientEditorInfo, editor.getProject());
        if (parameterList == null || parameterList.isEmpty()) {
            parameterList = new LinkedList<>();
        }

        List<KV> finalParameterList = parameterList;
        String paramName = CompactHelper.showInputDialog(e.getProject(), RestBundle.message("toolkit.toolwindow.client.parameter.save.input.msg"), RestBundle.message("toolkit.toolwindow.client.parameter.save.input.title", restClientEditorInfo.getEditor()), null, restClientEditorInfo.getEditor() + "1", new InputValidator() {

            @Override
            public boolean checkInput(String inputString) {
                return StringUtils.isNotEmpty(inputString) && finalParameterList.stream().noneMatch(kv -> inputString.equals(kv.getKey()));
            }

            @Override
            public boolean canClose(String inputString) {
                return true;
            }
        });
        if (StringUtils.isEmpty(paramName)) {
            return;
        }

        parameterList.add(new KV(paramName, editor.getDocument().getText()));
        DataSourceHelper.getDataSource().syncParameter(restClientEditorInfo, parameterList, editor.getProject());
    }
}