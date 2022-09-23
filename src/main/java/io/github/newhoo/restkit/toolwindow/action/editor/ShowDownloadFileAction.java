package io.github.newhoo.restkit.toolwindow.action.editor;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static io.github.newhoo.restkit.common.RestConstant.HTTP_DOWNLOAD_FILEPATH_PREFIX;

/**
 * ShowDownloadFileAction
 *
 * @author huzunrong
 * @since 2.0.11
 */
public class ShowDownloadFileAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null || !editor.toString().replace("\\", "/").contains("/Response")) {
            e.getPresentation().setVisible(false);
            return;
        }
        if (!StringUtils.startsWith(editor.getDocument().getText(), HTTP_DOWNLOAD_FILEPATH_PREFIX)) {
            e.getPresentation().setVisible(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        String[] lines = editor.getDocument().getText().split("\n");
        if (lines.length > 0) {
            String filepath = lines[0].replace(HTTP_DOWNLOAD_FILEPATH_PREFIX, "");
            filepath = StringUtils.trim(filepath);
            if (StringUtils.isNotEmpty(filepath)) {
                // com.intellij.ide.actions.ShowFilePathAction.openFile
                RevealFileAction.openFile(new File(filepath));
            }
        }
    }
}
