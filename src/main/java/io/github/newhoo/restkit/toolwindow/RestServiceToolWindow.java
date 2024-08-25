package io.github.newhoo.restkit.toolwindow;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.util.IdeaUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * RestServiceToolWindow
 */
public class RestServiceToolWindow extends SimpleToolWindowPanel {

    private final ToolWindow myToolWindow;
    private final Project myProject;

    private RestServiceTree restServiceTree;
    private RestServiceClient restServiceClient;

    public RestServiceToolWindow(Project myProject, ToolWindow toolWindow) {
        super(true, true);

        this.myProject = myProject;
        this.myToolWindow = toolWindow;

        initToolWindow();
    }

    private void initToolWindow() {
        // 设置ToolWindow的顶部工具条
        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR,
                                                                        (DefaultActionGroup) actionManager.getAction("RESTKit.NavigatorActionsToolbar"),
                                                                        true);
        setToolbar(actionToolbar.getComponent());

        // 上下两部分：tree和RestServiceClient
        Splitter servicesContentPaneSplitter = new Splitter(true, .5f);
        servicesContentPaneSplitter.setShowDividerControls(true);
        servicesContentPaneSplitter.setDividerWidth(10);

        restServiceTree = getRestServiceTree();
        restServiceClient = new RestServiceClient(myProject);
        // 关系到获取dataContext
        actionToolbar.setTargetComponent(restServiceTree);

        servicesContentPaneSplitter.setFirstComponent(ScrollPaneFactory.createScrollPane(restServiceTree));
        servicesContentPaneSplitter.setSecondComponent(restServiceClient);
        setContent(servicesContentPaneSplitter);

        Content content = ApplicationManager.getApplication().getService(ContentFactory.class).createContent(this, "", false);
        ContentManager contentManager = myToolWindow.getContentManager();
        contentManager.addContent(content);
        contentManager.setSelectedContent(content, false);

        // update tree
        IdeaUtils.runWhenProjectIsReady(myProject, () -> restServiceTree.updateTree(null, false));
    }

    private RestServiceTree getRestServiceTree() {
        boolean showMultiLevelServiceNode = ConfigHelper.getGlobalSetting().isShowMultiLevelServiceNode();
        if (!showMultiLevelServiceNode) {
            return new SimpleRestServiceTree(myProject);
        }
        // 普通版允许3个项目为多层级tree
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length <= 3) {
            return new RestServiceTree(myProject);
        }
        return new SimpleRestServiceTree(myProject);
    }

    /**
     * 跳转到节点，节点不存在时可生成请求
     */
    void navigateToTree(PsiElement psiElement, Supplier<RestItem> geneWhenNotExistNode) {
        if (myToolWindow.isDisposed() || !myToolWindow.isVisible()) {
            myToolWindow.show(() -> {
                restServiceTree.navigateToTree(psiElement, geneWhenNotExistNode);
            });
        } else {
            restServiceTree.navigateToTree(psiElement, geneWhenNotExistNode);
        }
    }

    /**
     * 跳转到节点
     */
    void navigateToTree(String url, String method, String moduleName) {
        if (myToolWindow.isDisposed() || !myToolWindow.isVisible()) {
            myToolWindow.show(() -> {
                restServiceTree.navigateToTree(url, method, moduleName);
            });
        } else {
            restServiceTree.navigateToTree(url, method, moduleName);
        }
    }

    public void scheduleUpdateTree(Consumer<Void> callAfterUpdate, boolean trySyncToDataSource) {
        IdeaUtils.runWhenProjectIsReady(myProject, () -> {
            if (myToolWindow.isDisposed() || !myToolWindow.isVisible()) {
                myToolWindow.show(() -> {
                    restServiceTree.updateTree(callAfterUpdate, trySyncToDataSource);
                });
            } else {
                restServiceTree.updateTree(callAfterUpdate, trySyncToDataSource);
            }
        });
    }

    public void expandAll(boolean expand) {
        restServiceTree.expandAll(expand);
    }

    boolean isToolWindowDisposed() {
        return myToolWindow.isDisposed();
    }

    boolean tryDispose() {
        if (myProject.isDisposed()) {
            this.restServiceTree.cleanup();
            this.restServiceClient.cleanup();
            this.restServiceTree = null;
            this.restServiceClient = null;
            return true;
        }
        return false;
    }
}
