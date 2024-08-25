package io.github.newhoo.restkit.toolwindow.client;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.util.ToolkitUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.github.newhoo.restkit.common.RestConstant.EDITOR_FILENAME_PREFIX;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_FILE_PREFIX;

/**
 * SelectUploadFileAction
 *
 * @author huzunrong
 * @since 2.0.8
 */
public class SelectUploadFileAction extends BaseAnAction {

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
        if (!StringUtils.equals(file.getName(), EDITOR_FILENAME_PREFIX + "Params")) {
            e.getPresentation().setVisible(false);
            return;
        }
        String text = editor.getDocument().getText();
        if (StringUtils.isEmpty(text) || !text.contains(HTTP_FILE_PREFIX)) {
            e.getPresentation().setVisible(false);
            return;
        }
        e.getPresentation().setVisible(true);
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.client.selectuploadfile.action.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);

        List<AnAction> actions = new ArrayList<>(4);
        String[] lines = editor.getDocument().getText().split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(":") && lines[i].contains(HTTP_FILE_PREFIX)) {
                String actionText = (i + 1) + "-" + lines[i];
                int finalI = i;
                actions.add(new BaseAnAction(() -> actionText) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e1) {
                        innerActionPerformed(editor, project, lines, finalI);
                    }
                });
            }
        }

        if (actions.size() == 1) {
            actions.get(0).actionPerformed(e);
            return;
        }

        final ListPopup popup = JBPopupFactory.getInstance()
                                              .createActionGroupPopup(
                                                      "Select File Parameter",
                                                      new DefaultActionGroup(actions),
                                                      e.getDataContext(),
                                                      JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                                      true);
        popup.showInBestPositionFor(e.getDataContext());
    }

    private void innerActionPerformed(Editor editor, Project project, String[] lines, int lineIndex) {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        final VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
        if (file != null && !file.isDirectory()) {
            String filepath = FileUtil.toSystemDependentName(file.getPath());

            String line = lines[lineIndex];
            lines[lineIndex] = line.substring(0, line.indexOf(":")) + ": " + ToolkitUtil.getUploadFileDescriptor(filepath);
            String newParamText = Strings.join(lines, s -> s, "\n");

            Document doc = editor.getDocument();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                doc.setText(newParamText);
            });
        }
    }
}
