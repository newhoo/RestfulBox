package io.github.newhoo.restkit.toolwindow.client;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class MinifyJSONAction extends BaseAnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(false);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        String text = editor.getDocument().getText();
        text = StringUtils.trim(text);
        if ((StringUtils.startsWith(text, "{") && StringUtils.endsWith(text, "}"))
                || StringUtils.startsWith(text, "[") && StringUtils.endsWith(text, "]")) {
            e.getPresentation().setVisible(true);
            e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.client.minifyjson.action.text"));
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileEditor editor = e.getData(com.intellij.openapi.actionSystem.PlatformCoreDataKeys.FILE_EDITOR);
        if (editor != null) {
            String editorText = IdeaUtils.getEditorText(editor);
            String minify = JsonUtils.minify(editorText);
            IdeaUtils.setEditorText(editor, minify, e.getProject());
        }
    }
}
