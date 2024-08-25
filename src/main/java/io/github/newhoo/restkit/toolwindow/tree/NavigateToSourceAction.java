package io.github.newhoo.restkit.toolwindow.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.util.PsiNavigateUtil;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NavigateToSourceAction extends BaseAnAction implements DumbAware {

    @Override
    public void update(@NotNull AnActionEvent e) {
        List<RestItem> items = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        boolean match = items != null && items.stream()
                                              .allMatch(restItem -> restItem instanceof PsiRestItem);
        e.getPresentation().setVisible(match);
        if (match) {
            e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.tree.navigatetosource.action.text"));
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        List<RestItem> items = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        if (items != null) {
            items.stream()
                 .filter(restItem -> restItem instanceof PsiRestItem)
                 .forEach(restItem -> {
                     PsiNavigateUtil.navigate(((PsiRestItem) restItem).getPsiElement());
                 });
        }
    }
}