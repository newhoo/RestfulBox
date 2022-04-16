package io.github.newhoo.restkit.navigation;

import com.intellij.ide.actions.SearchEverywhereBaseAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

/**
 * RestSearchEverywhereAction
 *
 * @author huzunrong
 * @since 1.0.8
 */
public class RestSearchEverywhereAction extends SearchEverywhereBaseAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String tabID = Registry.is("search.everywhere.group.contributors.by.type")
                ? "SearchEverywhere.Project"
                : RestSearchEverywhereContributor.class.getSimpleName();
        showInSearchEverywherePopup(tabID, e, true, true);
    }
}
