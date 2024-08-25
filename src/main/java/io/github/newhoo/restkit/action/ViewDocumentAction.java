package io.github.newhoo.restkit.action;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.util.Optional;

/**
 * ViewDocumentAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class ViewDocumentAction extends BaseAnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.action.viewdoc.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DefaultActionGroup generateActionGroup = new DefaultActionGroup(
                new BaseAnAction("Github") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        BrowserUtil.browse("https://github.com/newhoo/RestfulBox");
                    }
                },
                new BaseAnAction("Gitee") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        BrowserUtil.browse("https://gitee.com/newhoo/RestfulBox");
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
        Optional.ofNullable(e.getInputEvent())
                .map(ComponentEvent::getComponent)
                .filter(Component::isValid)
                .ifPresentOrElse(popup::showUnderneathOf, () -> Optional.ofNullable(e.getData(PlatformDataKeys.CONTEXT_COMPONENT))
                                                                        .ifPresentOrElse(popup::showUnderneathOf, popup::showInFocusCenter)
                );
    }
}