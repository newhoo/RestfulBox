package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.LocalApiLibrary;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;

/**
 * DeleteModuleAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class DeleteModuleAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        List<RestItem> serviceItems = RestDataKey.SELECTED_MODULE.getData(e.getDataContext());
        e.getPresentation().setEnabledAndVisible(serviceItems != null
                && serviceItems.stream().allMatch(o -> WEB_FRAMEWORK_LOCAL.equals(o.getFramework())));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        List<RestItem> serviceItems = RestDataKey.SELECTED_MODULE.getData(e.getDataContext());
        if (CollectionUtils.isNotEmpty(serviceItems)) {
            LocalApiLibrary.getInstance(project).getItemList().removeAll(serviceItems);
            RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);
        }
    }
}
