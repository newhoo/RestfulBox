package io.github.newhoo.restkit.toolwindow;

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.ui.AppUIUtil;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.Consumer;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.tree.TreeUtil;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.RestModule;
import io.github.newhoo.restkit.common.ToolkitIcons;
import io.github.newhoo.restkit.config.CommonSetting;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.HttpMethodFilterConfiguration;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.util.IdeaUtils;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * RestServiceTree
 */
public class RestServiceTree extends JPanel implements DataProvider {

    public static final Logger LOG = Logger.getInstance(RestServiceTree.class);
    private final Project myProject;
    private final CommonSetting setting;

    private final StructureTreeModel<AbstractTreeStructure> myTreeModel;
    public final SimpleTree myTree;
    private final RootNode myRoot = new RootNode();

    public RestServiceTree(Project project) {
        myProject = project;
        setting = CommonSettingComponent.getInstance(project).getState();

        myTreeModel = new StructureTreeModel<>(new SimpleTreeStructure() {
            @Override
            public Object getRootElement() {
                return myRoot;
            }
        }, null, myProject);
        myTree = new SimpleTree(new AsyncTreeModel(myTreeModel, myProject));
        myTree.setRootVisible(true);
        myTree.setShowsRootHandles(true);
        myTree.getEmptyText().clear();
        myTree.setBorder(BorderFactory.createEmptyBorder());
        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        setLayout(new BorderLayout());
        add(myTree, BorderLayout.CENTER);

        initEvent();
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
            private String getMenuId(Collection<? extends RestServiceTree.BaseSimpleNode> nodes) {
                String id = null;
                for (RestServiceTree.BaseSimpleNode node : nodes) {
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
    public void navigateToTree(PsiElement psiElement) {
        Optional<RestServiceTree.RequestNode> psiMethodNode = myRoot.moduleNodes.stream()
                                                                                .flatMap(o -> o.requestNodes.stream())
                                                                                .filter(o -> o.myRestItem instanceof PsiRestItem)
                                                                                .filter(o -> ((PsiRestItem) o.myRestItem).getPsiElement() == psiElement)
                                                                                .findFirst();
        if (psiMethodNode.isPresent()) {
            myTreeModel.select(psiMethodNode.get(), myTree, treePath -> {
            });
            return;
        }
        updateTree(aVoid -> {
            IdeaUtils.runWhenProjectIsReady(myProject, () -> {
                myRoot.moduleNodes.stream()
                                  .flatMap(o -> o.requestNodes.stream())
                                  .filter(o -> o.myRestItem instanceof PsiRestItem)
                                  .filter(o -> ((PsiRestItem) o.myRestItem).getPsiElement() == psiElement)
                                  .findFirst()
                                  .ifPresent(node -> myTreeModel.select(node, myTree, treePath -> {
                                  }));
            });
        });
    }

    /**
     * 跳转到节点
     */
    public void navigateToTree(String url, String method, String moduleName) {
        HttpMethod httpMethod = ObjectUtils.defaultIfNull(HttpMethod.getByRequestMethod(method), HttpMethod.GET);
        Optional<RestServiceTree.RequestNode> psiMethodNode = myRoot.moduleNodes.stream()
                                                                                .filter(moduleNode -> moduleName == null || moduleNode.getName().equals(moduleName))
                                                                                .flatMap(o -> o.requestNodes.stream())
                                                                                .filter(getNavigateFilter(url, httpMethod))
                                                                                .findFirst();
        if (psiMethodNode.isPresent()) {
            myTreeModel.select(psiMethodNode.get(), myTree, treePath -> {
            });
            return;
        }
        updateTree(aVoid -> {
            IdeaUtils.runWhenProjectIsReady(myProject, () -> {
                myRoot.moduleNodes.stream()
                                  .filter(moduleNode -> moduleName == null || moduleNode.getName().equals(moduleName))
                                  .flatMap(o -> o.requestNodes.stream())
                                  .filter(getNavigateFilter(url, httpMethod))
                                  .findFirst()
                                  .ifPresent(node -> myTreeModel.select(node, myTree, treePath -> {
                                  }));
            });
        });
    }

    private Predicate<RequestNode> getNavigateFilter(String url, HttpMethod httpMethod) {
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
//                System.out.println(url + " --- " + item.getUrl() + " --- " + itemUrl);
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

    public void updateTree(Consumer<Void> consumer) {
        ProgressManager.getInstance().run(new Task.Backgroundable(myProject, "[RESTKit] Search restful api") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                List<RestModule> restModules = getServiceProjects();
                AppUIUtil.invokeOnEdt(() -> {
                    myRoot.updateModuleNodes(restModules);
                    if (consumer != null) {
                        consumer.consume(null);
                    }
                    // 自动展开指定的路径
                    /*else if (currentModulePath != null) {
                        System.out.println("updateTree: " + currentModulePath);
                        TreeUtil.expand(myTree, new TreeVisitor() {
                            @Override
                            @NotNull
                            public Action visit(@NotNull TreePath path) {
                                if (path.getParentPath() == null) {
                                    return Action.CONTINUE;
                                }
                                if (path.getLastPathComponent().toString().equals(currentModulePath.getLastPathComponent().toString())) {
                                    return Action.INTERRUPT;
                                }
                                if (path.getPathCount() == 2) {
                                    return Action.SKIP_CHILDREN;
                                }
                                return Action.SKIP_SIBLINGS;
                            }
                        }, path -> {
                            System.out.println("expand path: " + path);
                            myTree.setSelectionPath(path);
                        });
                    }*/
                });
            }
        });
    }

    private List<RestModule> getServiceProjects() {
        Set<String> filteredOutHttpMethods = HttpMethodFilterConfiguration.getInstance(myProject).getState().getFilteredOutFileTypeNames();
        boolean selectAll = filteredOutHttpMethods.isEmpty();
        return DumbService.getInstance(myProject).runReadActionInSmartMode(() ->
                RequestHelper.buildRequestItemList(myProject).stream()
                             .filter(item -> selectAll || (item.getMethod() != null && !filteredOutHttpMethods.contains(item.getMethod().name())))
                             .collect(Collectors.groupingBy(RestItem::getModuleName, Collectors.toList()))
                             .entrySet()
                             .stream()
                             .map(entry -> new RestModule(entry.getKey(), entry.getValue()))
                             .collect(Collectors.toList())
        );
    }

    @Nullable
    @Override
    public Object getData(@NotNull @NonNls String dataId) {
        if (RestDataKey.ALL_SERVICE.is(dataId)) {
            return myRoot.moduleNodes.stream()
                                     .flatMap(moduleNode -> moduleNode.requestNodes.stream())
                                     .map(requestNode -> requestNode.myRestItem)
                                     .collect(Collectors.toList());
        }
        if (RestDataKey.ALL_MODULE.is(dataId)) {
            return myRoot.moduleNodes.stream()
                                     .map(moduleNode -> moduleNode.restModule.getModuleName())
                                     .distinct()
                                     .collect(Collectors.toList());
        }
        // getMenu保证了不能选择不同类型节点
        if (RestDataKey.SELECTED_SERVICE.is(dataId)) {
            List<RestItem> list = new ArrayList<>();
            for (BaseSimpleNode node : getSelectedNodes()) {
                if (node instanceof RootNode) {
                    return myRoot.moduleNodes.stream()
                                             .flatMap(moduleNode -> moduleNode.requestNodes.stream())
                                             .map(requestNode -> requestNode.myRestItem)
                                             .collect(Collectors.toList());
                } else if (node instanceof ModuleNode) {
                    list.addAll(((ModuleNode) node).restModule.getRestItems());
                } else if (node instanceof RequestNode) {
                    list.add(((RequestNode) node).myRestItem);
                }
            }
            return list;
        }
        if (RestDataKey.SELECTED_MODULE_SERVICE.is(dataId)) {
            return getSelectedNodes().stream()
                                     .filter(node -> node instanceof ModuleNode)
                                     .flatMap(node -> ((ModuleNode) node).restModule.getRestItems().stream())
                                     .collect(Collectors.toList());
        }
        return null;
    }

    private List<BaseSimpleNode> getSelectedNodes() {
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

    public abstract class BaseSimpleNode extends CachingSimpleNode {

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
    }

    public class RootNode extends BaseSimpleNode {
        List<ModuleNode> moduleNodes = new ArrayList<>();

        protected RootNode() {
            super(null);
            getTemplatePresentation().setIcon(ToolkitIcons.ROOT_MODULE);
            getTemplatePresentation().setPresentableText(getName());
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return moduleNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            List<Integer> list = moduleNodes.stream().map(moduleNode -> moduleNode.requestNodes.size()).collect(Collectors.toList());
            int serviceCount = list.stream().mapToInt(i -> i).sum();
            return serviceCount > 0 ? String.format("Found %d services", serviceCount) : "No service found";
        }

        public void updateModuleNodes(List<RestModule> restModules) {
            cleanUpCache();
            moduleNodes.clear();
            for (RestModule restModule : restModules) {
                ModuleNode moduleNode = new ModuleNode(this, restModule);
                moduleNodes.add(moduleNode);
            }

            updateFrom(getParent());
            childrenChanged();
        }

        @Override
        String getMenuId() {
            return "RESTKit.NavigatorRootMenu";
        }
    }

    public class ModuleNode extends BaseSimpleNode {
        private final List<RequestNode> requestNodes = new ArrayList<>();
        private final RestModule restModule;

        public ModuleNode(SimpleNode parent, RestModule restModule) {
            super(parent);
            this.restModule = restModule;

            getTemplatePresentation().setIcon(ToolkitIcons.PROJECT_MODULE);
            getTemplatePresentation().setTooltip(String.valueOf(restModule.getRestItems().size()));

            updateRequestNode(restModule.getRestItems());
        }

        private void updateRequestNode(List<RestItem> serviceItems) {
            requestNodes.clear();
            for (RestItem serviceItem : serviceItems) {
                requestNodes.add(new RequestNode(this, serviceItem));
            }

            SimpleNode parent = getParent();
            if (parent != null) {
                ((BaseSimpleNode) parent).cleanUpCache();
            }
            updateFrom(parent);
            childrenChanged();
            updateUpTo(this);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return requestNodes.toArray(new SimpleNode[0]);
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

    public class RequestNode extends BaseSimpleNode {
        public final RestItem myRestItem;

        public RequestNode(SimpleNode parent, RestItem restItem) {
            super(parent);
            myRestItem = restItem;

            Icon icon = ToolkitIcons.METHOD.get(restItem.getMethod());
            getTemplatePresentation().setIcon(icon);

            String description = restItem.getDescription();
            if (restItem instanceof PsiRestItem) {
                String[] split = StringUtils.split(description, "#", 3);
                if (split.length >= 3) {
                    description = split[0] + "#" + split[1] + "<br/>" + split[2];
                }
            }
            String tooltip = "source: " + myRestItem.getFramework() + "<br/>"
                    + "url: " + myRestItem.getUrl() + "<br/>"
                    + "desc: " + description;
            getTemplatePresentation().setTooltip(tooltip);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new SimpleNode[0];
        }

        @Override
        public String getName() {
            if (setting.isDisplayTreeListUsingApiDesc() && StringUtils.isNotEmpty(myRestItem.getDescription())) {
                return myRestItem.getDescription();
            }
            return myRestItem.getUrl();
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
            RequestNode selectedNode = (RequestNode) tree.getSelectedNode();
            if (selectedNode != null && selectedNode.myRestItem instanceof PsiRestItem) {
                PsiRestItem myRestItem = (PsiRestItem) selectedNode.myRestItem;
                PsiElement psiElement = myRestItem.getPsiElement();
                if (!psiElement.isValid()) {
                    // try refresh service
                    LOG.info("psiMethod is invalid: " + psiElement);
                    RestToolWindowFactory.getRestServiceToolWindow(RestServiceTree.this.myProject, RestServiceToolWindow::scheduleUpdateTree);
                    return;
                }
                if (OpenSourceUtil.canNavigate(psiElement)) {
                    ApplicationManager.getApplication().invokeLater(() -> OpenSourceUtil.navigate((Navigatable) psiElement));
                }
            }
        }

        @Override
        protected String getMenuId() {
            return "RESTKit.NavigatorServiceMenu";
        }
    }

    private void updateUpTo(SimpleNode node) {
        SimpleNode each = node;
        while (each != null) {
            updateFrom(each);
            each = each.getParent();
        }
    }

    private void updateFrom(SimpleNode node) {
        if (node != null) {
            node.update();
            myTreeModel.invalidate();
        }
    }
}
