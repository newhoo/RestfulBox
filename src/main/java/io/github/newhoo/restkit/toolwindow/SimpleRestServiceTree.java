package io.github.newhoo.restkit.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.AppUIUtil;
import com.intellij.ui.treeStructure.SimpleNode;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.ToolkitIcons;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.util.IdeaUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SimpleRestServiceTree
 */
public class SimpleRestServiceTree extends RestServiceTree {

    public SimpleRestServiceTree(Project project) {
        super(project);
    }

    @Override
    RestServiceTree.RootNode buildRoot() {
        return new RootNode();
    }

    @Override
    Optional<RequestNode> matchNode(String url, String method, String moduleName) {
        HttpMethod httpMethod = StringUtils.isNotEmpty(method) ? HttpMethod.getByRequestMethod(method) : null;
        return ((SimpleRestServiceTree.RootNode) myRoot).packageNodes.stream()
                                                                     .filter(moduleNode -> moduleName == null || moduleNode.getName().equals(moduleName))
                                                                     .flatMap(o -> o.requestNodes.stream())
                                                                     .filter(getNavigateFilter(url, httpMethod))
                                                                     .findFirst();
    }

    @Override
    Optional<RequestNode> matchNode(PsiElement psiElement) {
        return ((SimpleRestServiceTree.RootNode) myRoot).packageNodes.stream()
                                                                     .flatMap(o -> o.requestNodes.stream())
                                                                     .filter(o -> o.myRestItem instanceof PsiRestItem)
                                                                     .filter(o -> ((PsiRestItem) o.myRestItem).getPsiElement() == psiElement)
                                                                     .findFirst();
    }

    @Override
    void reNavigateToTree(PsiElement psiElement) {
        IdeaUtils.runWhenProjectIsReady(myProject, () -> {
            ((SimpleRestServiceTree.RootNode) myRoot).packageNodes.stream()
                                                                  .flatMap(o -> o.requestNodes.stream())
                                                                  .filter(o -> o.myRestItem instanceof PsiRestItem)
                                                                  .filter(o -> ((PsiRestItem) o.myRestItem).getPsiElement() == psiElement)
                                                                  .findFirst()
                                                                  .ifPresent(node -> myTreeModel.select(node, myTree, treePath -> {
                                                                  }));
        });
    }

    @Override
    void innerUpdateNode() {
        List<RestPackage> restProjects = restItems.stream()
                                                  .collect(Collectors.groupingBy(RestItem::getModuleName, Collectors.toList()))
                                                  .entrySet()
                                                  .stream()
                                                  .map(entry -> new RestPackage(entry.getKey(), entry.getValue()))
                                                  .collect(Collectors.toList());
        AppUIUtil.invokeOnEdt(() -> {
            ((SimpleRestServiceTree.RootNode) myRoot).updateProjectNodes2(restProjects);
        });
    }

    @Nullable
    @Override
    public Object getData(@NotNull @NonNls String dataId) {
        if (RestDataKey.ALL_SERVICE.is(dataId)) {
            return restItems;
        }
        if (RestDataKey.ALL_PROJECT.is(dataId)) {
            return restItems.stream()
                            .map(RestItem::getProject)
                            .distinct()
                            .collect(Collectors.toList());
        }
        if (RestDataKey.ALL_MODULE.is(dataId)) {
            return restItems.stream()
                            .map(e -> e.getModuleName())
                            .distinct()
                            .collect(Collectors.toList());
        }
        if (RestDataKey.SELECTED_PROJECT.is(dataId)) {
            BaseSimpleNode node = getSelectedNodes().stream().findFirst().orElse(null);
            if (node == null) {
                return myProject.getName();
            }
            if (node instanceof PackageNode) {
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
                } else if (node instanceof PackageNode) {
                    list.addAll(((PackageNode) node).restPackage.getRestItems());
                } else if (node instanceof RequestNode) {
                    list.add(((RequestNode) node).myRestItem);
                }
            }
            return list;
        }
        if (RestDataKey.SELECTED_MODULE_SERVICE.is(dataId)) {
            return getSelectedNodes().stream()
                                     .filter(node -> node instanceof PackageNode)
                                     .flatMap(node -> ((PackageNode) node).restPackage.getRestItems().stream())
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

    class RootNode extends RestServiceTree.RootNode {
        private final List<PackageNode> packageNodes = new ArrayList<>();

        @Override
        protected SimpleNode[] buildChildren() {
            return packageNodes.toArray(new SimpleNode[0]);
        }

        public void updateProjectNodes2(List<RestPackage> restProjects) {
            cleanUpCache();
            packageNodes.clear();
            for (RestPackage projectItem : restProjects) {
                PackageNode projectNode = new PackageNode(this, projectItem);
                packageNodes.add(projectNode);
            }
            super.serviceCount = restItems.size();
            childrenChanged();
        }
    }

    class PackageNode extends BaseSimpleNode {
        private final List<RequestNode> requestNodes = new ArrayList<>();
        private final RestPackage restPackage;

        public PackageNode(SimpleNode parent, RestPackage restPackage) {
            super(parent);
            this.restPackage = restPackage;

            getTemplatePresentation().setIcon(ToolkitIcons.TREE_PACKAGE);
            int size = restPackage.getRestItems().size();
            String tooltip = RestBundle.isChineseLang()
                    ? String.format("%d个接口", size)
                    : String.format("%d api%s", size, size > 1 ? "s" : "");
            getTemplatePresentation().setTooltip("<html><body><pre>" + tooltip + "</pre></body></html>");

            updateRequestNode(restPackage.getRestItems());
        }

        private void updateRequestNode(List<RestItem> serviceItems) {
            requestNodes.clear();
            for (RestItem serviceItem : serviceItems) {
                requestNodes.add(new RequestNode(this, serviceItem));
            }
            childrenChanged();
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return requestNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            return restPackage.getPackageName().contains(".")
                    ? restPackage.getPackageName().substring(restPackage.getPackageName().lastIndexOf(".") + 1)
                    : restPackage.getPackageName();
        }

        @Override
        protected String getMenuId() {
            return "RESTKit.NavigatorModuleMenu";
        }
    }
}
