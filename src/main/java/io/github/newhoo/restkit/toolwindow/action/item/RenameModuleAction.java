package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;

/**
 * RenameModuleAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class RenameModuleAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        List<RestItem> serviceItems = RestDataKey.SELECTED_MODULE.getData(e.getDataContext());
        e.getPresentation().setEnabledAndVisible(serviceItems != null
                && serviceItems.stream().allMatch(o -> WEB_FRAMEWORK_LOCAL.equals(o.getFramework())));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        List<RestItem> serviceItems = RestDataKey.SELECTED_MODULE.getData(e.getDataContext());
        if (CollectionUtils.isEmpty(serviceItems)) {
            return;
        }

        List<String> moduleNames = serviceItems.stream().map(RestItem::getModuleName).distinct().collect(Collectors.toList());
        String moduleName = Messages.showInputDialog(project, "Input module name", "Edit Module Name", null, moduleNames.isEmpty() ? null : moduleNames.get(0), new InputValidator() {
            @Override
            public boolean checkInput(String inputString) {
                return StringUtils.isNotBlank(inputString);
            }

            @Override
            public boolean canClose(String inputString) {
                return true;
            }
        });
        if (StringUtils.isEmpty(moduleName)) {
            return;
        }

        serviceItems.forEach(o -> o.setModuleName(moduleName));
        RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);
    }
}
