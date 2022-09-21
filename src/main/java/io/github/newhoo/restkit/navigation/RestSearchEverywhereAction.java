package io.github.newhoo.restkit.navigation;

import com.intellij.ide.actions.GotoActionBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * RestSearchEverywhereAction
 *
 * @author huzunrong
 * @since 1.0.8
 */
public class RestSearchEverywhereAction extends GotoActionBase {

    @Override
    public void update(@NotNull AnActionEvent event) {
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showInSearchEverywherePopup(RestSearchEverywhereContributor.class.getSimpleName(), e, true);
//        if (Registry.is("new.search.everywhere")) {
//        } else {
//            super.actionPerformed(e);
//        }
    }

    @Override
    protected void gotoActionPerformed(@NotNull AnActionEvent e) {
//        showInSearchEverywherePopup(RestSearchEverywhereContributor.class.getSimpleName(), e, true);
    }
}
