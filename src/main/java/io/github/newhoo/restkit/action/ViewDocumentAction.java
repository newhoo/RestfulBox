package io.github.newhoo.restkit.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * ViewDocumentAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class ViewDocumentAction extends AnAction {

    private static final String GITHUB_URL = "https://github.com/newhoo/RESTKit";
    private static final String GITEE_URL = "https://gitee.com/newhoo/RESTKit";
    private static final String YUQUE_URL = "https://www.yuque.com/newhoo/restkit";

    @Override
    public void update(@NotNull AnActionEvent e) {
        if ("ActionPlaces.TOOLWINDOW_TOOLBAR_BAR".equals(e.getPlace())) {
            e.getPresentation().setIcon(AllIcons.Actions.Help);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!"ActionPlaces.TOOLWINDOW_TOOLBAR_BAR".equals(e.getPlace())) {
            BrowserUtil.browse(GITHUB_URL);
            return;
        }

        DefaultActionGroup generateActionGroup = new DefaultActionGroup(
                new AnAction("Yuque") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        BrowserUtil.browse(YUQUE_URL);
                    }
                },
                new AnAction("Github") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        BrowserUtil.browse(GITHUB_URL);
                    }
                },
                new AnAction("Gitee") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        BrowserUtil.browse(GITEE_URL);
                    }
                }
        );

        final ListPopup popup = JBPopupFactory.getInstance()
                                              .createActionGroupPopup(
                                                      null,
                                                      generateActionGroup,
                                                      e.getDataContext(),
                                                      JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                                      true);
        Component anchor = e.getInputEvent().getComponent();
        if (anchor.isValid()) {
            popup.showUnderneathOf(anchor);
        } else {
            Component component = e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
            if (component != null) {
                popup.showUnderneathOf(component);
            } else {
                popup.showInFocusCenter();
            }
        }
    }
}