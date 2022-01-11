package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.config.LocalApiLibrary;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;

/**
 * DeleteApiAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class DeleteApiAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        List<RestItem> items = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        boolean match = items != null && items.stream()
                                              .allMatch(restItem -> WEB_FRAMEWORK_LOCAL.equals(restItem.getFramework()));
        e.getPresentation().setVisible(match);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        List<RestItem> items = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        if (items != null) {
            List<RestItem> delItems = items.stream()
                                           .filter(restItem -> WEB_FRAMEWORK_LOCAL.equals(restItem.getFramework()))
                                           .collect(Collectors.toList());
            LocalApiLibrary.getInstance(project).getItemList().removeAll(delItems);
            RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);
        }
    }
}
