package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MoveApiAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class MoveApiAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        List<RestItem> items = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        if (project == null || items == null) {
            return;
        }
        e.getPresentation().setVisible(items.stream().allMatch(RestItem::canUpdate));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        List<RestItem> serviceItems = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        if (CollectionUtils.isEmpty(serviceItems)) {
            return;
        }

        List<String> moduleData = RestDataKey.ALL_MODULE.getData(e.getDataContext());
        String[] moduleNames = CollectionUtils.isNotEmpty(moduleData) ? moduleData.toArray(new String[0]) : new String[0];
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
        Map<String, RequestResolver> resolverMap = RequestHelper.getAllRequestResolvers(project)
                                                                .stream()
                                                                .collect(Collectors.toMap(RequestResolver::getFrameworkName, o -> o));
        serviceItems.stream()
                    .collect(Collectors.groupingBy(RestItem::getFramework))
                    .entrySet()
                    .stream()
                    .filter(entry -> resolverMap.containsKey(entry.getKey()))
                    .forEach(entry -> {
                        resolverMap.get(entry.getKey()).update(entry.getValue());
                    });
        RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);
    }
}
