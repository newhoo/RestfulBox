package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.LocalApiLibrary;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;

/**
 * MoveApiAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class MoveApiAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        List<RestItem> serviceItems = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        e.getPresentation().setEnabledAndVisible(serviceItems != null
                && serviceItems.stream().allMatch(o -> WEB_FRAMEWORK_LOCAL.equals(o.getFramework())));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        List<RestItem> serviceItems = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        if (CollectionUtils.isEmpty(serviceItems)) {
            return;
        }

        List<RestItem> library = LocalApiLibrary.getInstance(project).getItemList();
        String[] moduleNames = library.stream().map(RestItem::getModuleName).distinct().toArray(String[]::new);
        String moduleName = Messages.showEditableChooseDialog("Select or input module name", "Edit Module Name", null, moduleNames, moduleNames.length > 0 ? moduleNames[0] : "local", new InputValidator() {
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
