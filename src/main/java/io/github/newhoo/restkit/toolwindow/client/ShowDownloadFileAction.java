package io.github.newhoo.restkit.toolwindow.client;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static io.github.newhoo.restkit.common.RestConstant.EDITOR_FILENAME_PREFIX;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_DOWNLOAD_FILEPATH_PREFIX;

/**
 * ShowDownloadFileAction
 *
 * @author huzunrong
 * @since 2.0.11
 */
public class ShowDownloadFileAction extends BaseAnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            e.getPresentation().setVisible(false);
            return;
        }
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (!(file instanceof LightVirtualFile)) {
            e.getPresentation().setVisible(false);
            return;
        }
        if (!StringUtils.equals(file.getName(), EDITOR_FILENAME_PREFIX + "Response")) {
            e.getPresentation().setVisible(false);
            return;
        }
        if (!StringUtils.startsWith(editor.getDocument().getText(), HTTP_DOWNLOAD_FILEPATH_PREFIX)) {
            e.getPresentation().setVisible(false);
            return;
        }
        e.getPresentation().setVisible(true);
        e.getPresentation().setText(RestBundle.message("toolkit.toolwindow.client.showsownloadfile.action.text"));
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
