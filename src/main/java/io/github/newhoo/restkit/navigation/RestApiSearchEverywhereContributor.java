package io.github.newhoo.restkit.navigation;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.actions.SearchEverywhereBaseAction;
import com.intellij.ide.actions.SearchEverywherePsiRenderer;
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor;
import com.intellij.ide.actions.searcheverywhere.PersistentSearchEverywhereContributorFilter;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereFiltersAction;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereUI;
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor;
import com.intellij.openapi.actionSystem.AnAction;
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
import com.intellij.util.Processor;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.TextWithIcon;
import com.intellij.util.text.Matcher;
import com.intellij.util.ui.UIUtil;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.ToolkitIcons;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.config.global.GlobalSetting;
import io.github.newhoo.restkit.config.ide.CommonSetting;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseCheckboxAction;
import io.github.newhoo.restkit.intellij.BaseChooseByNameFilterConfiguration;
import io.github.newhoo.restkit.intellij.BaseSearchEverywhereContributorFactory;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RestItemDetail;
import io.github.newhoo.restkit.toolwindow.ToolWindowHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * RestSearchEverywhereContributor
 *
 * @author huzunrong
 * @since 1.0.8
 */
public class RestApiSearchEverywhereContributor implements WeightedSearchEverywhereContributor<RestItem> {

    private final AnActionEvent actionEvent;
    private final Project myProject;
    private final boolean showModule;
    private final CommonSetting commonSetting;
    private final GlobalSetting globalSetting;
    private List<RestItem> navItemList;

    public RestApiSearchEverywhereContributor(@NotNull AnActionEvent event) {
        this.actionEvent = event;
        myProject = event.getRequiredData(CommonDataKeys.PROJECT);
        showModule = ConfigHelper.getGlobalSetting().isShowModuleInSearchEvery();
        commonSetting = ConfigHelper.getCommonSetting(myProject);
        globalSetting = ConfigHelper.getGlobalSetting();
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
        return RestBundle.messageWithChineseLangCheck("toolkit.search.everywhere.url.tabName", "APIs");
//        return "URLs";
    }

    @Override
    public int getSortWeight() {
        return 801;
    }

    @Nullable
    @Override
    public String getAdvertisement() {
        return DumbService.isDumb(myProject) ? IdeBundle.message("dumb.mode.results.might.be.incomplete") : null;
    }

    @NotNull
    @Override
    public List<AnAction> getActions(@NotNull Runnable onChanged) {
        BaseCheckboxAction filterDescriptionAction = new BaseCheckboxAction(RestBundle.message("toolkit.search.everywhere.url.filterText")) {

            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return globalSetting.isEnableFilterDescriptionInSearchEvery();
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                globalSetting.setEnableFilterDescriptionInSearchEvery(state);
                onChanged.run();
            }
        };
        PersistentSearchEverywhereContributorFilter<HttpMethod> methodFilter =
                new PersistentSearchEverywhereContributorFilter<>(Arrays.asList(HttpMethod.values()), new BaseChooseByNameFilterConfiguration<>() {
                    @Override
                    protected String nameForElement(HttpMethod type) {
                        return type.name();
                    }

                    @Override
                    public boolean isVisible(HttpMethod type) {
                        return !commonSetting.getFilterMethods().contains(type);
                    }

                    @Override
                    public void setVisible(HttpMethod type, boolean value) {
                        if (value) {
                            commonSetting.getFilterMethods().remove(type);
                        } else {
                            commonSetting.getFilterMethods().add(type);
                        }
                        navItemList = null;
                    }
                },
                        Enum::name, httpMethod -> null
                );
        SearchEverywhereFiltersAction<HttpMethod> methodFiltersAction = new SearchEverywhereFiltersAction<>(methodFilter, onChanged);
        methodFiltersAction.getTemplatePresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.methodfilter.action.text"));
        return Arrays.asList(filterDescriptionAction, methodFiltersAction);
    }

    @Override
    public boolean processSelectedItem(@NotNull RestItem selected, int modifiers, @NotNull String searchText) {
        RestItemDetail restItemDetail = RequestHelper.getRestItemDetail(selected.getProtocol());
        if (restItemDetail != null && restItemDetail.handleDoubleClickOrEnter(selected, myProject)) {
            return true;
        }
        if (selected instanceof PsiRestItem) {
            PsiNavigateUtil.navigate(((PsiRestItem) selected).getPsiElement());
        } else {
            ToolWindowHelper.navigateToTree(selected.getUrl(), ObjectUtils.defaultIfNull(selected.getMethod(), HttpMethod.GET).name(), selected.getModuleName(), myProject);
        }
        return true;
    }

    @NotNull
    @Override
    public ListCellRenderer<Object> getElementsRenderer() {
        return new SearchEverywherePsiRenderer(this) {

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
                        ToolWindowHelper.navigateToTree(selected.getUrl(), selected.getMethod() != null ? selected.getMethod().name() : null, selected.getModuleName(), myProject);
                        IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
                    }
                }
            };

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component retComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index == 0 && this.list == null) {
                    this.list = list;
                    Container parent = list.getParent();
                    try {
                        for (int i = 0; i < 5; i++) {
                            if (parent == null) {
                                break;
                            }
                            if (parent instanceof SearchEverywhereUI) {
                                ((SearchEverywhereUI) parent).getSearchField().addKeyListener(keyAdapter);
                                break;
                            } else {
                                if (parent.getComponentCount() > 0 && parent.getComponents()[0] instanceof SearchEverywhereUI) {
                                    SearchEverywhereUI component = ((SearchEverywhereUI) parent.getComponents()[0]);
                                    component.getSearchField().addKeyListener(keyAdapter);
                                    break;
                                }
                            }
                            parent = parent.getParent();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

                RestItem item = (RestItem) value;
                String name = item.getUrl() + " ";
                String locationString = item.getDescription();

//                renderer.append(name, nameAttributes);
                ItemMatchers itemMatchers = getItemMatchers(list, value);
                SpeedSearchUtil.appendColoredFragmentForMatcher(name, renderer, nameAttributes, itemMatchers.nameMatcher, bgColor, selected);

                Matcher locationMatcher = itemMatchers.locationMatcher;
                if (globalSetting.isEnableFilterDescriptionInSearchEvery()) {
                    if (!Optional.ofNullable(itemMatchers.nameMatcher).map(m -> m.matches(name)).orElse(false)) {
                        locationMatcher = itemMatchers.nameMatcher;
                    }
                }

                RestItemDetail restItemDetail = RequestHelper.getRestItemDetail(item.getProtocol());
                renderer.setIcon(restItemDetail != null ? restItemDetail.getIcon(item) : ToolkitIcons.UNDEFINED);

                if (StringUtils.isNotEmpty(locationString)) {
                    locationString = "(" + locationString + ")";
                    FontMetrics fm = list.getFontMetrics(list.getFont());
                    int maxWidth = list.getWidth() - fm.stringWidth(name) - myRightComponentWidth - 36;
                    int fullWidth = fm.stringWidth(locationString);
                    if (fullWidth < maxWidth) {
//                        renderer.append(locationString, new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY));
                        SpeedSearchUtil.appendColoredFragmentForMatcher(locationString, renderer, SimpleTextAttributes.GRAYED_ATTRIBUTES, locationMatcher, bgColor, selected);
                    } else {
                        int adjustedWidth = Math.max(locationString.length() * maxWidth / fullWidth - 1, 3);
                        locationString = StringUtil.trimMiddle(locationString, adjustedWidth);
                        SpeedSearchUtil.appendColoredFragmentForMatcher(locationString, renderer, SimpleTextAttributes.GRAYED_ATTRIBUTES, locationMatcher, bgColor, selected);
                    }
                }
                return true;
            }

            @Nullable
            @Override
            protected TextWithIcon getItemLocation(Object value) {
                if (showModule && value instanceof RestItem) {
                    return new TextWithIcon(((RestItem) value).getModuleName(), AllIcons.Nodes.Module);
                }
                return super.getItemLocation(value);
            }
        };
    }

    @Nullable
    @Override
    public Object getDataForItem(@NotNull RestItem element, @NotNull String dataId) {
        return null;
    }

    @Override
    public boolean isEmptyPatternSupported() {
        return true;
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
    public boolean isDumbAware() {
        return DumbService.isDumb(myProject);
    }

    @Override
    public void fetchWeightedElements(@NotNull String pattern, @NotNull ProgressIndicator progressIndicator, @NotNull Processor<? super FoundItemDescriptor<RestItem>> consumer) {
        if (isDumbAware() || !shouldProvideElements(pattern)) {
            return;
        }

        Set<HttpMethod> filterMethods = commonSetting.getFilterMethods();
        boolean selectAllMethod = filterMethods.isEmpty();

        // 从ALL -> URL Tab或快捷键进入时列表为空
        if (navItemList == null) {
            // 必须从read线程访问，耗时不能过长，还是慢了
            ApplicationManager.getApplication().runReadAction(() -> {
                navItemList = RequestHelper.buildRequestItemList(myProject);
            });
        }
        if (navItemList != null) {
            MinusculeMatcher matcher = null;
            if (!pattern.isEmpty()) {
                matcher = NameUtil.buildMatcher("*" + pattern, NameUtil.MatchingCaseSensitivity.NONE);
            }
            for (RestItem restItem : navItemList) {
                if (selectAllMethod || (restItem.getMethod() != null && !filterMethods.contains(restItem.getMethod()))) {
                    if (pattern.isEmpty()) {
                        if (!consumer.process(new FoundItemDescriptor<>(restItem, 0))) {
                            return;
                        }
                    } else {
                        if (matcher.matches(restItem.getUrl())) {
                            if (!consumer.process(new FoundItemDescriptor<>(restItem, matcher.matchingDegree(restItem.getUrl())))) {
                                return;
                            }
                        } else if (globalSetting.isEnableFilterDescriptionInSearchEvery() && matcher.matches(restItem.getDescription())) {
                            if (!consumer.process(new FoundItemDescriptor<>(restItem, matcher.matchingDegree(restItem.getDescription())))) {
                                return;
                            }
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
        SearchEverywhereManager seManager = SearchEverywhereManager.getInstance(myProject);
        if (seManager.isShown()) {
            return getSearchProviderId().equals(seManager.getSelectedTabID());
        }
        return false;
    }

    @Override
    public void dispose() {
        WeightedSearchEverywhereContributor.super.dispose();
        this.navItemList = null;
    }

    public static class Factory implements BaseSearchEverywhereContributorFactory<RestItem> {
        @Override
        public boolean isAvailable(Project project) {
            return true;
        }

        @NotNull
        @Override
        public SearchEverywhereContributor<RestItem> createContributor(@NotNull AnActionEvent initEvent) {
            return new RestApiSearchEverywhereContributor(initEvent);
        }
    }

    public static class RestApiSearchEverywhereAction extends SearchEverywhereBaseAction {

        @Override
        public void update(@NotNull AnActionEvent event) {
            event.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.search.action.text"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String tabID = RestApiSearchEverywhereContributor.class.getSimpleName();
            showInSearchEverywherePopup(tabID, e, true, false);
        }
    }
}
