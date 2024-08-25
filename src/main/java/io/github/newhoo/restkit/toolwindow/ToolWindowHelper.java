package io.github.newhoo.restkit.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.RestItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

@NotProguard
public class ToolWindowHelper {

    /**
     * 刷新树
     */
    public static void scheduleUpdateTree(@NotNull Project project) {
        scheduleUpdateTree(null, project);
    }

    /**
     * 刷新树
     *
     * @param callAfterUpdate 刷新后回调
     */
    public static void scheduleUpdateTree(Consumer<Void> callAfterUpdate, @NotNull Project project) {
        RestToolWindowFactory.getRestServiceToolWindow(project, restServiceToolWindow -> {
            restServiceToolWindow.scheduleUpdateTree(callAfterUpdate, false);
        });
    }

    /**
     * 跳转到节点，节点不存在时可生成请求
     */
    public static void navigateToTree(PsiElement psiElement, Supplier<RestItem> geneWhenNotExistNode) {
        RestToolWindowFactory.getRestServiceToolWindow(psiElement.getProject(), restServiceToolWindow -> {
            restServiceToolWindow.navigateToTree(psiElement, geneWhenNotExistNode);
        });
    }

    /**
     * 跳转到节点
     */
    public static void navigateToTree(String url, String method, String moduleName, @NotNull Project project) {
        RestToolWindowFactory.getRestServiceToolWindow(project, restServiceToolWindow -> {
            restServiceToolWindow.navigateToTree(url, method, moduleName);
        });
    }
}
