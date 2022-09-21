package io.github.newhoo.restkit.navigation;

import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.actions.SearchEverywherePsiRenderer;
import com.intellij.ide.actions.searcheverywhere.PersistentSearchEverywhereContributorFilter;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFilter;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereUI;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.ui.UIUtil;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.ToolkitIcons;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.HttpMethodFilterConfiguration;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * RestSearchEverywhereContributor
 *
 * @author huzunrong
 * @since 1.0.8
 */
public class RestSearchEverywhereContributor implements SearchEverywhereContributor<RestItem> {

    private final AnActionEvent actionEvent;
    private final Project myProject;
    private final PersistentSearchEverywhereContributorFilter<HttpMethod> myFilter;
    private final boolean showModule;
    private List<RestItem> navItemList;

    public RestSearchEverywhereContributor(@NotNull AnActionEvent event) {
        this.actionEvent = event;
        myProject = event.getRequiredData(CommonDataKeys.PROJECT);
        myFilter = new PersistentSearchEverywhereContributorFilter<>(
                Arrays.asList(HttpMethod.values()), HttpMethodFilterConfiguration.getInstance(myProject),
                Enum::name, httpMethod -> null
        );
        showModule = CommonSettingComponent.getInstance(myProject).getState().isShowModuleInSearchEvery();
        // RestServiceToolWindow:54
        navItemList = RestDataKey.ALL_SERVICE.getData(actionEvent.getDataContext());
    }

    @NotNull
    @Override
    public String getSearchProviderId() {
        return getClass().getSimpleName();
    }

    @NotNull
    @Override
    public String getGroupName() {
        return "URL";
    }

    @Nullable
    @Override
    public String includeNonProjectItemsText() {
        return null;
    }

    @Nullable
    @Override
    public Object getDataForItem(@NotNull Object element, @NotNull String dataId) {
        return null;
    }

    @Override
    public int getSortWeight() {
        return 800;
    }

    @Override
    public boolean processSelectedItem(@NotNull Object selected, int modifiers, @NotNull String searchText) {
        if (selected instanceof PsiRestItem) {
            PsiNavigateUtil.navigate(((PsiRestItem) selected).getPsiElement());
        } else if (selected instanceof RestItem) {
            RestToolWindowFactory.getRestServiceToolWindow(myProject, restServiceToolWindow -> {
                restServiceToolWindow.navigateToTree(((RestItem) selected).getUrl(), ObjectUtils.defaultIfNull(((RestItem) selected).getMethod(), HttpMethod.GET).name(), ((RestItem) selected).getModuleName());
            });
        }
        return true;
    }

    @NotNull
    @Override
    public ListCellRenderer<Object> getElementsRenderer(@NotNull JList<?> list) {
        return new SearchEverywherePsiRenderer(list) {

            private JList list = null;
            private final KeyAdapter keyAdapter = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ALT && list != null) {
//                        if (selected == null || EXTRA_ELEM.equals(selected)) {
//                            return;
//                        }
                        if (!(list.getSelectedValue() instanceof RestItem)) {
                            return;
                        }
                        final RestItem selected = (RestItem) list.getSelectedValue();
                        RestToolWindowFactory.getRestServiceToolWindow(myProject, restServiceToolWindow -> {
                            restServiceToolWindow.navigateToTree(selected.getUrl(), ObjectUtils.defaultIfNull(selected.getMethod(), HttpMethod.GET).name(), selected.getModuleName());
                        });
                        IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
                    }
                }
            };

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component retComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index == 0 && this.list == null) {
                    this.list = list;
                    try {
                        Component[] components = list.getParent().getParent().getParent().getParent().getParent().getComponents();
                        Component component = components[0];
                        if (component instanceof SearchEverywhereUI) {
                            SearchEverywhereUI component1 = ((SearchEverywhereUI) component);
                            component1.getSearchField().addKeyListener(keyAdapter);
                        }
                    } catch (Exception e) {
                    }
                }
                return retComponent;
            }

            @Override
            protected boolean customizeNonPsiElementLeftRenderer(ColoredListCellRenderer renderer, JList list, Object value, int index, boolean selected, boolean hasFocus) {
                Color fgColor = list.getForeground();
                Color bgColor = UIUtil.getListBackground();
                TextAttributes attributes = getNavigationItemAttributes(value);
                SimpleTextAttributes nameAttributes = attributes != null ? SimpleTextAttributes.fromTextAttributes(attributes) : null;
                if (nameAttributes == null)
                    nameAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, fgColor);

                ItemMatchers itemMatchers = getItemMatchers(list, value);
                RestItem item = (RestItem) value;
                String name = item.getUrl() + " ";
                String locationString = item.getDescription();

//                renderer.append(name, nameAttributes);
                SpeedSearchUtil.appendColoredFragmentForMatcher(name, renderer, nameAttributes, itemMatchers.nameMatcher, bgColor, selected);
                renderer.setIcon(ToolkitIcons.METHOD.get(item.getMethod()));

                if (StringUtils.isNotEmpty(locationString)) {
                    locationString = "(" + locationString + ")";
                    FontMetrics fm = list.getFontMetrics(list.getFont());
                    int maxWidth = list.getWidth() - fm.stringWidth(name) - myRightComponentWidth - 36;
                    int fullWidth = fm.stringWidth(locationString);
                    if (fullWidth < maxWidth) {
//                        renderer.append(locationString, new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY));
                        SpeedSearchUtil.appendColoredFragmentForMatcher(locationString, renderer, SimpleTextAttributes.GRAYED_ATTRIBUTES, itemMatchers.nameMatcher, bgColor, selected);
                    } else {
                        int adjustedWidth = Math.max(locationString.length() * maxWidth / fullWidth - 1, 3);
                        locationString = StringUtil.trimMiddle(locationString, adjustedWidth);
                        SpeedSearchUtil.appendColoredFragmentForMatcher(locationString, renderer, SimpleTextAttributes.GRAYED_ATTRIBUTES, itemMatchers.nameMatcher, bgColor, selected);
                    }
                }
                return true;
            }

//            @Nullable
//            @Override
//            protected TextWithIcon getItemLocation(Object value) {
//                if (showModule && value instanceof RestItem) {
//                    return new TextWithIcon(((RestItem) value).getModuleName(), AllIcons.Nodes.Module);
//                }
//                return super.getItemLocation(value);
//            }
        };
    }

    @Override
    public boolean isShownInSeparateTab() {
        return true;
    }

    @Override
    public boolean showInFindResults() {
        return false;
    }

    @Override
    public void fetchElements(@NotNull String pattern, boolean everywhere, @Nullable SearchEverywhereContributorFilter<RestItem> filter, @NotNull ProgressIndicator progressIndicator, @NotNull Function<Object, Boolean> consumer) {
        if (DumbService.isDumb(myProject) || !shouldProvideElements(pattern)) {
            return;
        }

        MinusculeMatcher matcher = NameUtil.buildMatcher("*" + pattern + "*", NameUtil.MatchingCaseSensitivity.NONE);
        Set<HttpMethod> httpMethodSet = new HashSet<>(myFilter.getSelectedElements());
        boolean selectAll = httpMethodSet.size() == HttpMethod.values().length;

        // 从ALL -> URL Tab或快捷键进入时列表为空
        if (navItemList == null) {
            // 必须从read线程访问，耗时不能过长
            ApplicationManager.getApplication().runReadAction(() -> {
                navItemList = RequestHelper.buildRequestItemList(myProject);
            });
        }
        if (navItemList != null) {
            for (RestItem restItem : navItemList) {
                if (selectAll || httpMethodSet.contains(restItem.getMethod())) {
                    if (matcher.matches(restItem.getUrl()) || matcher.matches(restItem.getDescription())) {
                        if (!consumer.apply(restItem)) {
                            return;
                        }
                    }
                }
            }
        }
    }


    /**
     * 判断是否应该返回列表元素
     *
     * @param pattern 搜索词
     */
    private boolean shouldProvideElements(String pattern) {
        boolean shouldProvideElements = true;
        SearchEverywhereManager seManager = SearchEverywhereManager.getInstance(myProject);
        if (seManager.isShown()) {
            // 非URL Tab, 也只有ALL Tab
            if (!getSearchProviderId().equals(seManager.getShownContributorID())) {
                if (StringUtils.isEmpty(StringUtils.trimToNull(pattern))) {
                    shouldProvideElements = false;
                }
            }
        } else {
            // ALL Tab
            if (ActionsBundle.message("action.SearchEverywhere.text").equals(actionEvent.getPresentation().getText())) {
                if (StringUtils.isEmpty(StringUtils.trimToNull(pattern))) {
                    shouldProvideElements = false;
                }
            }
        }
        return shouldProvideElements;
    }

    public static class Factory implements SearchEverywhereContributorFactory<RestItem> {
        @NotNull
        @Override
        public SearchEverywhereContributor<RestItem> createContributor(@NotNull AnActionEvent initEvent) {
            return new RestSearchEverywhereContributor(initEvent);
        }

        @Nullable
        @Override
        public SearchEverywhereContributorFilter<RestItem> createFilter(AnActionEvent initEvent) {
//            PersistentSearchEverywhereContributorFilter<HttpMethod> myFilter = new PersistentSearchEverywhereContributorFilter<>(
//                    Arrays.asList(HttpMethod.values()), HttpMethodFilterConfiguration.getInstance(initEvent.getRequiredData(CommonDataKeys.PROJECT)),
//                    Enum::name, httpMethod -> null
//            );
//            return myFilter;
            return null;
        }
    }
}
