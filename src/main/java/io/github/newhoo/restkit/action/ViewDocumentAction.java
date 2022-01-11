package io.github.newhoo.restkit.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import static io.github.newhoo.restkit.common.RestConstant.DOC_URL;

/**
 * ViewDocumentAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class ViewDocumentAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (ActionPlaces.TOOLWINDOW_TOOLBAR_BAR.equals(e.getPlace())) {
            e.getPresentation().setIcon(AllIcons.Actions.Help);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.browse(DOC_URL);
    }
}