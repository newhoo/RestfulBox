package io.github.newhoo.restkit.toolwindow;

import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.AppUIUtil;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.tree.TreeUtil;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.RestRegistry;
import io.github.newhoo.restkit.common.ToolkitIcons;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.config.global.GlobalSetting;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.CompactHelper;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RestItemDetail;
import io.github.newhoo.restkit.util.CommonUtils;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * RestServiceTree
 */
public class RestServiceTree extends JPanel implements DataProvider {

    protected final Project myProject;
    protected final GlobalSetting globalSetting;

    protected final StructureTreeModel<AbstractTreeStructure> myTreeModel;
    protected final SimpleTree myTree;
    protected final RootNode myRoot = buildRoot();

    protected final List<RestItem> restItems = new ArrayList<>();

    public RestServiceTree(Project project) {
        myProject = project;
        globalSetting = ConfigHelper.getGlobalSetting();

        myTreeModel = new StructureTreeModel<>(new SimpleTreeStructure() {
            @Override
            public Object getRootElement() {
                return myRoot;
            }
        }, null, myProject);
        myTree = new SimpleTree(new AsyncTreeModel(myTreeModel, myProject));
        myTree.setRootVisible(globalSetting.isShowTreeRoot());
        myTree.setShowsRootHandles(true);
        myTree.getEmptyText().clear();
        myTree.setBorder(BorderFactory.createEmptyBorder());
        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        setLayout(new BorderLayout());
        add(myTree, BorderLayout.CENTER);

        initEvent();
    }

    void cleanup() {
        restItems.clear();
        innerUpdateNode();
    }

    RootNode buildRoot() {
        return new RootNode();
    }

    private void initEvent() {
        // popup
        myTree.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(final Component comp, final int x, final int y) {
                final String id = getMenuId(getSelectedNodes());
                if (id != null) {
                    final ActionManager actionManager = ActionManager.getInstance();
                    final ActionGroup actionGroup = (ActionGroup) actionManager.getAction(id);
                    if (actionGroup != null) {
                        JPopupMenu component = actionManager.createActionPopupMenu(ActionPlaces.TOOLWINDOW_CONTENT, actionGroup).getComponent();
                        component.show(comp, x, y);
                    }
                }
            }

            /**
             * 同时选择不同类型节点时不展示菜单
             */
            @Nullable
            private String getMenuId(Collection<? extends BaseSimpleNode> nodes) {
                String id = null;
                for (BaseSimpleNode node : nodes) {
                    String menuId = node.getMenuId();
                    if (menuId == null) {
                        return null;
                    }
                    if (id == null) {
                        id = menuId;
                    } else if (!id.equals(menuId)) {
                        return null;
                    }
                }
                return id;
            }
        });
    }

    /**
     * 跳转到节点
     */
    void navigateToTree(PsiElement psiElement, Supplier<RestItem> geneWhenNotExistNode) {
        Optional<RequestNode> psiMethodNode = matchNode(psiElement);
        if (psiMethodNode.isPresent()) {
            myTreeModel.select(psiMethodNode.get(), myTree, treePath -> {
            });
            return;
        }
        if (geneWhenNotExistNode != null) {
            // 生成请求
            RestItem restItem = geneWhenNotExistNode.get();
            if (restItem != null) {
                NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.tree.notexistedgenerate.msg1"), new NotificationAction(RestBundle.message("toolkit.common.btn.refresh")) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                        ToolWindowHelper.scheduleUpdateTree(RestServiceTree.this.myProject);
                        notification.expire();
                    }
                }, myProject);
                MessageBus messageBus = RestServiceTree.this.myProject.getMessageBus();
                messageBus.syncPublisher(RestServiceListener.REST_SERVICE_SELECT).select(restItem);
            } else {
                NotifierUtils.warnBalloon("", RestBundle.message("toolkit.toolwindow.tree.notexistedgenerate.msg2"), myProject);
            }
        } else {
            // 刷新树
            updateTree(aVoid -> reNavigateToTree(psiElement), false);
        }
    }

    void reNavigateToTree(PsiElement psiElement) {
        IdeaUtils.runWhenProjectIsReady(myProject, () -> {
            myRoot.projectNodes.stream()
                               .flatMap(o -> o.moduleNodes.stream())
                               .flatMap(o -> o.packageNodes.stream())
                               .flatMap(o -> o.requestNodes.stream())
                               .filter(o -> o.myRestItem instanceof PsiRestItem)
                               .filter(o -> ((PsiRestItem) o.myRestItem).getPsiElement() == psiElement)
                               .findFirst()
                               .ifPresent(node -> myTreeModel.select(node, myTree, treePath -> {
                               }));
        });
    }

    /**
     * 跳转到节点
     */
    void navigateToTree(String url, String method, String moduleName) {
        Optional<RequestNode> psiMethodNode = matchNode(url, method, moduleName);
        if (psiMethodNode.isPresent()) {
            myTreeModel.select(psiMethodNode.get(), myTree, treePath -> {
            });
        } else {
            NotifierUtils.infoBalloon("", String.format(RestBundle.message("toolkit.toolwindow.tree.notfoundnode.msg") + " [%s, %s, %s].", url, method, moduleName), new NotificationAction(RestBundle.message("toolkit.common.btn.refresh")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    ToolWindowHelper.scheduleUpdateTree(RestServiceTree.this.myProject);
                    notification.expire();
                }
            }, myProject);
        }
    }

    Optional<RequestNode> matchNode(PsiElement psiElement) {
        return myRoot.projectNodes.stream()
                                  .flatMap(o -> o.moduleNodes.stream())
                                  .flatMap(o -> o.packageNodes.stream())
                                  .flatMap(o -> o.requestNodes.stream())
                                  .filter(o -> o.myRestItem instanceof PsiRestItem)
                                  .filter(o -> ((PsiRestItem) o.myRestItem).getPsiElement() == psiElement)
                                  .findFirst();
    }

    Optional<RequestNode> matchNode(String url, String method, String moduleName) {
        HttpMethod httpMethod = StringUtils.isNotEmpty(method) ? HttpMethod.getByRequestMethod(method) : null;
        return myRoot.projectNodes.stream()
                                  .flatMap(o -> o.moduleNodes.stream())
                                  .filter(moduleNode -> moduleName == null || moduleNode.getName().equals(moduleName))
                                  .flatMap(o -> o.packageNodes.stream())
                                  .flatMap(o -> o.requestNodes.stream())
                                  .filter(getNavigateFilter(url, httpMethod))
                                  .findFirst();
    }

    protected Predicate<RequestNode> getNavigateFilter(String url, HttpMethod httpMethod) {
        return requestNode -> {
            RestItem item = requestNode.myRestItem;
            if (item.getMethod() != httpMethod) {
                return false;
            }
            String itemUrl = item.getUrl();
            if (StringUtils.contains(url, "*") && StringUtils.contains(itemUrl, "{")) {
                if (!itemUrl.startsWith(url.substring(0, url.indexOf("*")))) {
                    return false;
                }

                itemUrl = itemUrl.replaceAll("\\{.+?}", "*");
            }
            return itemUrl.equals(url) || itemUrl.equals(url + "/");
        };
    }

    /**
     * @param expand false 折叠
     */
    public void expandAll(boolean expand) {
        if (expand) {
            TreeUtil.expandAll(myTree);
        } else {
            TreeUtil.collapseAll(myTree, false, 1);
        }
    }

    void updateTree(Consumer<Void> callAfterUpdate, boolean trySyncToDataSource) {
        myTree.setRootVisible(globalSetting.isShowTreeRoot());
        ProgressManager.getInstance().run(new Task.Backgroundable(myProject, "[RestfulBox] Search restful api") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                List<RestItem> serviceProjects = getServiceProjects();
                restItems.clear();
                restItems.addAll(serviceProjects);

                innerUpdateNode();

                if (trySyncToDataSource) {
                    DataSourceHelper.trySyncApiToDataSource(serviceProjects, myProject);
                }
                if (callAfterUpdate != null) {
                    callAfterUpdate.accept(null);
                }
            }
        });
    }

    void innerUpdateNode() {
        List<RestProject> restProjects = restItems.stream()
                                                  .collect(Collectors.groupingBy(RestItem::getProject, Collectors.toList()))
                                                  .entrySet()
                                                  .stream()
                                                  .map(entry -> {
                                                      List<RestModule> projectList = entry.getValue().stream()
                                                                                          .collect(Collectors.groupingBy(RestItem::getModuleName, Collectors.toList()))
                                                                                          .entrySet()
                                                                                          .stream()
                                                                                          .map(e -> {
                                                                                              List<RestPackage> packageList = e.getValue().stream()
                                                                                                                               .collect(Collectors.groupingBy(RestItem::getPackageName, Collectors.toList()))
                                                                                                                               .entrySet()
                                                                                                                               .stream()
                                                                                                                               .map(o -> new RestPackage(o.getKey(), o.getValue()))
                                                                                                                               .sorted(new Comparator<RestPackage>() {
                                                                                                                                   @Override
                                                                                                                                   public int compare(RestPackage o1, RestPackage o2) {
                                                                                                                                       return Integer.compare(
                                                                                                                                               RequestHelper.getProtocolOrder(o1.getRestItems().get(0).getProtocol()),
                                                                                                                                               RequestHelper.getProtocolOrder(o2.getRestItems().get(0).getProtocol())
                                                                                                                                       );
                                                                                                                                   }
                                                                                                                               }.thenComparing(new Comparator<RestPackage>() {
                                                                                                                                   @Override
                                                                                                                                   public int compare(RestPackage o1, RestPackage o2) {
                                                                                                                                       return o1.getPackageName().compareToIgnoreCase(o2.getPackageName());
                                                                                                                                   }
                                                                                                                               }))
                                                                                                                               .collect(Collectors.toList());
                                                                                              return new RestModule(e.getKey(), packageList);
                                                                                          })
                                                                                          .sorted((o1, o2) -> o1.getModuleName().compareToIgnoreCase(o2.getModuleName()))
                                                                                          .collect(Collectors.toList());
                                                      return new RestProject(entry.getKey(), projectList);
                                                  })
                                                  .sorted((o1, o2) -> (myProject.getName().equals(o1.getProjectName())) ? -1 : o1.getProjectName().compareToIgnoreCase(o2.getProjectName()))
                                                  .collect(Collectors.toList());
        AppUIUtil.invokeOnEdt(() -> {
            myRoot.updateProjectNodes(restProjects);
        });
    }

    private List<RestItem> getServiceProjects() {
        Set<HttpMethod> filterMethods = ConfigHelper.getCommonSetting(myProject).getFilterMethods();
        boolean selectAllMethod = filterMethods.isEmpty();
        return DumbService.getInstance(myProject)
                          .runReadActionInSmartMode(() -> RequestHelper.buildRequestItemList(myProject)
                                                                       .stream()
                                                                       .filter(item -> selectAllMethod || (item.getMethod() != null && !filterMethods.contains(item.getMethod())))
                                                                       .collect(Collectors.toList())
                          );
    }

    @Nullable
    @Override
    public Object getData(@NotNull @NonNls String dataId) {
        if (RestDataKey.ALL_SERVICE.is(dataId)) {
            return restItems;
        }
        if (RestDataKey.ALL_PROJECT.is(dataId)) {
            return myRoot.projectNodes.stream()
                                      .map(ProjectNode::getName)
                                      .distinct()
                                      .collect(Collectors.toList());
        }
        if (RestDataKey.ALL_MODULE.is(dataId)) {
            return restItems.stream()
                            .map(RestItem::getModuleName)
                            .distinct()
                            .collect(Collectors.toList());
        }
        if (RestDataKey.SELECTED_PROJECT.is(dataId)) {
            BaseSimpleNode node = getSelectedNodes().stream().findFirst().orElse(null);
            if (node == null) {
                return myProject.getName();
            }
            if (node instanceof ProjectNode) {
                return ((ProjectNode) node).moduleNodes.stream()
                                                       .flatMap(e -> e.packageNodes.stream())
                                                       .flatMap(e -> e.requestNodes.stream())
                                                       .map(e -> e.myRestItem)
                                                       .findFirst()
                                                       .map(RestItem::getProject)
                                                       .orElse(myProject.getName());
            } else if (node instanceof ModuleNode) {
                return ((ModuleNode) node).restModule.getRestPackages()
                                                     .stream()
                                                     .flatMap(e -> e.getRestItems().stream())
                                                     .findFirst()
                                                     .map(RestItem::getProject)
                                                     .orElse(myProject.getName());
            } else if (node instanceof PackageNode) {
                return ((PackageNode) node).restPackage.getRestItems()
                                                       .stream()
                                                       .findFirst()
                                                       .map(RestItem::getProject)
                                                       .orElse(myProject.getName());
            } else if (node instanceof RequestNode) {
                return ((RequestNode) node).myRestItem.getProject();
            }
            return myProject.getName();
        }
        // getMenu保证了不能选择不同类型节点
        if (RestDataKey.SELECTED_SERVICE.is(dataId)) {
            List<RestItem> list = new ArrayList<>();
            for (BaseSimpleNode node : getSelectedNodes()) {
                if (node instanceof RootNode) {
                    return restItems;
                } else if (node instanceof ProjectNode) {
                    list.addAll(((ProjectNode) node).moduleNodes.stream()
                                                                .flatMap(e -> e.packageNodes.stream())
                                                                .flatMap(e -> e.requestNodes.stream())
                                                                .map(e -> e.myRestItem)
                                                                .collect(Collectors.toList())
                    );
                } else if (node instanceof ModuleNode) {
                    list.addAll(((ModuleNode) node).restModule.getRestPackages()
                                                              .stream()
                                                              .flatMap(e -> e.getRestItems().stream())
                                                              .collect(Collectors.toList())
                    );
                } else if (node instanceof PackageNode) {
                    list.addAll(((PackageNode) node).restPackage.getRestItems());
                } else if (node instanceof RequestNode) {
                    list.add(((RequestNode) node).myRestItem);
                }
            }
            return list;
        }
        if (RestDataKey.SELECTED_PROJECT_SERVICE.is(dataId)) {
            Set<String> set = getSelectedNodes().stream()
                                                .filter(node -> node instanceof ProjectNode)
                                                .map(node -> ((ProjectNode) node).restProject.getProjectName())
                                                .collect(Collectors.toSet());
            return restItems.stream().filter(e -> set.contains(e.getProject())).collect(Collectors.toList());
        }
        if (RestDataKey.SELECTED_MODULE_SERVICE.is(dataId)) {
            return getSelectedNodes().stream()
                                     .filter(node -> node instanceof ModuleNode)
                                     .flatMap(node -> ((ModuleNode) node).restModule.getRestPackages().stream().flatMap(e -> e.getRestItems().stream()))
                                     .collect(Collectors.toList());
        }
        if (RestDataKey.SELECTED_PACKAGE_SERVICE.is(dataId)) {
            return getSelectedNodes().stream()
                                     .filter(node -> node instanceof PackageNode)
                                     .flatMap(node -> ((PackageNode) node).requestNodes.stream().map(e -> e.myRestItem))
                                     .collect(Collectors.toList());
        }
        return null;
    }

    protected List<BaseSimpleNode> getSelectedNodes() {
        final List<BaseSimpleNode> filtered = new ArrayList<>();

        TreePath[] treePaths = myTree.getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                SimpleNode nodeFor = myTree.getNodeFor(treePath);
                if (!(nodeFor instanceof BaseSimpleNode)) {
                    filtered.clear();
                    break;
                }
                filtered.add((BaseSimpleNode) nodeFor);
            }
        }
        return filtered;
    }

    abstract class BaseSimpleNode extends CachingSimpleNode {

        protected BaseSimpleNode(SimpleNode aParent) {
            super(aParent);
        }

        @Nullable
        @NonNls
        String getMenuId() {
            return null;
        }

        protected void childrenChanged() {
            BaseSimpleNode each = this;
            while (each != null) {
                each.cleanUpCache();
                each = (BaseSimpleNode) each.getParent();
            }
            updateUpTo(this);
        }

        private void updateUpTo(SimpleNode node) {
            SimpleNode each = node;
            while (each != null) {
                updateFrom(each);
                each = each.getParent();
            }
        }
    }

    protected class RootNode extends BaseSimpleNode {
        private final List<ProjectNode> projectNodes = new ArrayList<>();
        protected int serviceCount = 0;

        protected RootNode() {
            super(null);
            getTemplatePresentation().setIcon(ToolkitIcons.TREE_ROOT);
            getTemplatePresentation().setPresentableText(getName());
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return projectNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            if (serviceCount > 0) {
                return serviceCount > 1 ? RestBundle.message("toolkit.toolwindow.tree.foundapi.msg2", serviceCount) : RestBundle.message("toolkit.toolwindow.tree.foundapi.msg1", serviceCount);
            }
            return RestBundle.message("toolkit.toolwindow.tree.foundapi.msg3");
        }

        private void updateProjectNodes(List<RestProject> restProjects) {
            cleanUpCache();
            projectNodes.clear();
            for (RestProject projectItem : restProjects) {
                ProjectNode projectNode = new ProjectNode(this, projectItem);
                projectNodes.add(projectNode);
            }
            serviceCount = restItems.size();
//            serviceCount = restModules.stream()
//                                      .map(restModule -> restModule.getRestPackages().stream().map(e -> e.getRestItems().size()).mapToInt(i -> i).sum())
//                                      .mapToInt(i -> i).sum();

//            updateFrom(getParent());
            childrenChanged();
        }

        @Override
        String getMenuId() {
            return "RESTKit.NavigatorRootMenu";
        }
    }

    class ProjectNode extends BaseSimpleNode {
        private final List<ModuleNode> moduleNodes = new ArrayList<>();
        private final RestProject restProject;

        public ProjectNode(SimpleNode parent, RestProject restProject) {
            super(parent);
            this.restProject = restProject;

            Icon icon = ToolkitIcons.TREE_PROJECT;
            getTemplatePresentation().setIcon(RestServiceTree.this.myProject.getName().equals(restProject.getProjectName()) ? ExecutionUtil.getLiveIndicator(icon) : icon);
            int sum = restProject.getRestModules().stream().mapToInt(e -> e.getRestPackages().stream().mapToInt(e1 -> e1.getRestItems().size()).sum()).sum();
            String tooltip = RestBundle.isChineseLang()
                    ? String.format("%d个模块, %d个接口", restProject.getRestModules().size(), sum)
                    : String.format("%d module%s, %d api%s", restProject.getRestModules().size(), restProject.getRestModules().size() > 1 ? "s" : "", sum, sum > 1 ? "s" : "");
            getTemplatePresentation().setTooltip("<html><body><pre>" + tooltip + "</pre></body></html>");
            updateModuleNode(restProject.getRestModules());
        }

        private void updateModuleNode(List<RestModule> moduleList) {
            moduleNodes.clear();
            for (RestModule moduleItem : moduleList) {
                moduleNodes.add(new ModuleNode(this, moduleItem));
            }

            childrenChanged();
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return moduleNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            return restProject.getProjectName();
        }

        @Override
        protected String getMenuId() {
            return "RESTKit.NavigatorProjectMenu";
        }
    }

    class ModuleNode extends BaseSimpleNode {
        private final List<PackageNode> packageNodes = new ArrayList<>();
        private final RestModule restModule;

        public ModuleNode(SimpleNode parent, RestModule restModule) {
            super(parent);
            this.restModule = restModule;

            getTemplatePresentation().setIcon(ToolkitIcons.TREE_MODULE);

            int packageSize = restModule.getRestPackages().size();
            int apiSize = restModule.getRestPackages().stream().mapToInt(e1 -> e1.getRestItems().size()).sum();
            String tooltip = RestBundle.isChineseLang()
                    ? String.format("%d个包, %d个接口", packageSize, apiSize)
                    : String.format("%d package%s, %d api%s", packageSize, packageSize > 1 ? "s" : "", apiSize, apiSize > 1 ? "s" : "");
            getTemplatePresentation().setTooltip("<html><body><pre>" + tooltip + "</pre></body></html>");
            updatePackageNode(restModule.getRestPackages());
        }

        private void updatePackageNode(List<RestPackage> packageList) {
            packageNodes.clear();
            for (RestPackage packageItem : packageList) {
                packageNodes.add(new PackageNode(this, packageItem));
            }

//            SimpleNode parent = getParent();
//            if (parent != null) {
//                ((BaseSimpleNode) parent).cleanUpCache();
//            }
//            updateFrom(parent);
            childrenChanged();
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return packageNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            return restModule.getModuleName();
        }

        @Override
        protected String getMenuId() {
            return "RESTKit.NavigatorModuleMenu";
        }
    }

    class PackageNode extends BaseSimpleNode {
        private final List<RequestNode> requestNodes = new ArrayList<>();
        private final RestPackage restPackage;

        public PackageNode(SimpleNode parent, RestPackage restPackage) {
            super(parent);
            this.restPackage = restPackage;

            getTemplatePresentation().setIcon(ToolkitIcons.TREE_PACKAGE);
            String tooltip = RestBundle.isChineseLang()
                    ? String.format("%d个接口", restPackage.getRestItems().size())
                    : String.format("%d api%s", restPackage.getRestItems().size(), restPackage.getRestItems().size() > 1 ? "s" : "");
            getTemplatePresentation().setTooltip("<html><body><pre>" + tooltip + "</pre></body></html>");

            updateRequestNode(restPackage.getRestItems());
        }

        private void updateRequestNode(List<RestItem> serviceItems) {
            requestNodes.clear();
            for (RestItem serviceItem : serviceItems) {
                requestNodes.add(new RequestNode(this, serviceItem));
            }

//            SimpleNode parent = getParent();
//            if (parent != null) {
//                ((BaseSimpleNode) parent).cleanUpCache();
//            }
//            updateFrom(parent);
            childrenChanged();
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return requestNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            if (globalSetting.isShowCompletePackageName()) {
                return restPackage.getPackageName();
            }
            return restPackage.getPackageName().contains(".")
                    ? restPackage.getPackageName().substring(restPackage.getPackageName().lastIndexOf(".") + 1)
                    : restPackage.getPackageName();
        }

        @Override
        protected String getMenuId() {
            return "RESTKit.NavigatorPackageMenu";
        }
    }

    class RequestNode extends BaseSimpleNode {
        public final RestItem myRestItem;

        public RequestNode(SimpleNode parent, RestItem restItem) {
            super(parent);
            myRestItem = restItem;

            RestItemDetail restItemDetail = RequestHelper.getRestItemDetail(restItem.getProtocol());
            getTemplatePresentation().setIcon(restItemDetail != null ? restItemDetail.getIcon(restItem) : ToolkitIcons.UNDEFINED);

            String description = restItem.getDescription();
            String[] split = StringUtils.split(description, "#", 3);
            if (split.length >= 3) {
                description = split[2];
            }
            String tooltip;
            if (RestBundle.isChineseLang()) {
                tooltip = "编号 : " + StringUtils.defaultIfEmpty(myRestItem.getId(), "-")
                        + "<br/>协议 : " + myRestItem.getProtocol()
                        + "<br/>路径 : " + myRestItem.getUrl()
                        + "<br/>名称 : " + StringUtils.defaultIfEmpty(description, "-")
                        + "<br/>时间 : " + CommonUtils.formatLocalDateTime(myRestItem.getTs())
                        + "<br/>框架 : " + StringUtils.defaultIfEmpty(myRestItem.getFramework(), "-")
                        + "<br/>来源 : " + StringUtils.defaultIfEmpty(myRestItem.getDataSource(), "-");
                if (RestRegistry.debugMode()) {
                    tooltip += "<br/>同步 : " + myRestItem.isSyncFlag();
                }
            } else {
                tooltip = "id         : " + StringUtils.defaultIfEmpty(myRestItem.getId(), "-")
                        + "<br/>protocol   : " + myRestItem.getProtocol()
                        + "<br/>path       : " + myRestItem.getUrl()
                        + "<br/>name       : " + StringUtils.defaultIfEmpty(description, "-")
                        + "<br/>createAt   : " + CommonUtils.formatLocalDateTime(myRestItem.getTs())
                        + "<br/>framework  : " + StringUtils.defaultIfEmpty(myRestItem.getFramework(), "-")
                        + "<br/>datasource : " + StringUtils.defaultIfEmpty(myRestItem.getDataSource(), "-");
                if (RestRegistry.debugMode()) {
                    tooltip += "<br/>syncFlag   : " + myRestItem.isSyncFlag();
                }
            }
            getTemplatePresentation().setTooltip("<html><body><pre>" + tooltip + "</pre></body></html>");
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new SimpleNode[0];
        }

        @Override
        public String getName() {
            RestItemDetail restItemDetail = RequestHelper.getRestItemDetail(myRestItem.getProtocol());
            if (restItemDetail != null) {
                String name = restItemDetail.getName(myRestItem, globalSetting.isDisplayTreeListUsingApiDesc());
                if (StringUtils.isNotEmpty(name)) {
                    return name;
                }
            }
            String name = myRestItem.getUrl();
            if (globalSetting.isDisplayTreeListUsingApiDesc() && StringUtils.isNotEmpty(myRestItem.getDescription())) {
                name = myRestItem.getDescription();
                String[] split = StringUtils.split(name, "#", 3);
                if (split.length >= 3 && StringUtils.isNotEmpty(split[2])) {
                    name = split[2];
                }
            }
            return name;
        }

        @Override
        public void handleSelection(SimpleTree tree) {
            RequestNode selectedNode = (RequestNode) tree.getSelectedNode();
            if (selectedNode != null) {
                MessageBus messageBus = RestServiceTree.this.myProject.getMessageBus();
                messageBus.syncPublisher(RestServiceListener.REST_SERVICE_SELECT).select(selectedNode.myRestItem);
            }
        }

        @Override
        public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
            SimpleNode selectedNode = tree.getSelectedNode();
            if (!(selectedNode instanceof RequestNode)) {
                return;
            }
            RequestNode requestNode = (RequestNode) selectedNode;
            if (requestNode.myRestItem == null) {
                return;
            }
            RestItemDetail restItemDetail = RequestHelper.getRestItemDetail(requestNode.myRestItem.getProtocol());
            if (restItemDetail != null && restItemDetail.handleDoubleClickOrEnter(requestNode.myRestItem, RestServiceTree.this.myProject)) {
                return;
            }
            if (requestNode.myRestItem instanceof PsiRestItem) {
                PsiRestItem myRestItem = (PsiRestItem) requestNode.myRestItem;
                PsiElement psiElement = myRestItem.getPsiElement();
                if (!psiElement.isValid()) {
                    // try refresh service
                    NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.tree.iteminvalid"), new NotificationAction(RestBundle.message("toolkit.common.btn.refresh")) {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                            ToolWindowHelper.scheduleUpdateTree(RestServiceTree.this.myProject);
                            notification.expire();
                        }
                    }, RestServiceTree.this.myProject);
                }
                PsiNavigateUtil.navigate(psiElement);
//                if (OpenSourceUtil.canNavigate(psiElement)) {
//                    ApplicationManager.getApplication().invokeLater(() -> OpenSourceUtil.navigate((Navigatable) psiElement));
//                }
            }
        }

        @Override
        protected String getMenuId() {
            return "RESTKit.NavigatorServiceMenu";
        }
    }

    private void updateFrom(SimpleNode node) {
        if (node != null) {
            node.update();

            CompactHelper.invalidate(myTreeModel);
        }
    }

    @Getter
    @AllArgsConstructor
    static class RestProject {
        private String projectName;
        private List<RestModule> restModules;
    }

    @Getter
    @AllArgsConstructor
    static class RestModule {
        private String moduleName;
        private List<RestPackage> restPackages;
    }

    @Getter
    @AllArgsConstructor
    static class RestPackage {
        private String packageName;
        private List<RestItem> restItems;
    }
}
