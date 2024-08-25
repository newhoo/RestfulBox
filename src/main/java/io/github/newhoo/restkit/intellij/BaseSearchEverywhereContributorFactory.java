package io.github.newhoo.restkit.intellij;

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.openapi.project.Project;

public interface BaseSearchEverywhereContributorFactory<Item> extends SearchEverywhereContributorFactory<Item> {
    boolean isAvailable(Project project);
}
