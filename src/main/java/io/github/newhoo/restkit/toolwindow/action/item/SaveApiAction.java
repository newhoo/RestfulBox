package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.CommonSetting;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.SettingConfigurable;
import io.github.newhoo.restkit.restful.local.LocalStoreHelper;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import io.github.newhoo.restkit.util.HtmlUtil;
import io.github.newhoo.restkit.util.NotifierUtils;
import io.github.newhoo.restkit.util.ToolkitUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;

/**
 * SaveApiAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class SaveApiAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        CommonSetting commonSetting = CommonSettingComponent.getInstance(project).getState();
        e.getPresentation().setEnabledAndVisible(commonSetting.getEnabledWebFrameworks().contains(WEB_FRAMEWORK_LOCAL));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        RestClientApiInfo apiInfo = RestDataKey.CLIENT_API_INFO.getData(e.getDataContext());
        if (apiInfo == null) {
            return;
        }
        String url = apiInfo.getUrl();
        if (StringUtils.isEmpty(url)) {
            NotifierUtils.errorBalloon("Save Api Error", "Url can't be empty.", project);
            return;
        }
        CommonSetting commonSetting = CommonSettingComponent.getInstance(project).getState();
        if (StringUtils.isEmpty(commonSetting.getApiFilePath())) {
            NotifierUtils.infoBalloon("", "Local api store path is empty. " + HtmlUtil.link("Edit", "Edit"), new NotificationListener.Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingConfigurable.class);
                }
            }, project);
            return;
        }

        HttpMethod method = apiInfo.getMethod();

        List<RestItem> restItemInProject = new LocalStoreHelper(project).queryAll();
        List<RestItem> existed = restItemInProject.stream()
                                                  .filter(item -> url.equals(item.getUrl()) && item.getMethod() == method)
                                                  .collect(Collectors.toList());
        if (!existed.isEmpty()) {
            String msg = String.format("[%s %s] existed %d, how? %s, %s", method, url, existed.size(), HtmlUtil.link("Update", "Update"), HtmlUtil.link("Save As", "Save As"));
            NotifierUtils.infoBalloon("Save Api", msg, new NotificationListener.Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent) {
                    notification.expire();
                    switch (hyperlinkEvent.getDescription()) {
                        case "Update": {
                            saveOrUpdateApi(apiInfo, project, existed, restItemInProject);
                            break;
                        }
                        case "Save As": {
                            saveOrUpdateApi(apiInfo, project, Collections.emptyList(), restItemInProject);
                            break;
                        }
                        default:
                    }
                }
            }, project);
        } else {
            saveOrUpdateApi(apiInfo, project, Collections.emptyList(), restItemInProject);
        }
    }

    private void saveOrUpdateApi(RestClientApiInfo apiInfo, Project project, List<RestItem> existedList, List<RestItem> allRestItemInProject) {
        List<KV> headers = ToolkitUtil.textToKVList(apiInfo.getHeaders());
        List<KV> params = ToolkitUtil.textToKVList(apiInfo.getParams());
        String bodyJson = apiInfo.getBodyJson();

        String initialDesc = existedList.isEmpty() ? "" : existedList.get(0).getDescription();
        String desc = Messages.showInputDialog(project, "Input api description", "Api Description", null, initialDesc, new InputValidator() {
            @Override
            public boolean checkInput(String inputString) {
                return StringUtils.isNotBlank(inputString);
            }

            @Override
            public boolean canClose(String inputString) {
                return true;
            }
        });
        if (StringUtils.isEmpty(desc)) {
            return;
        }

        LocalStoreHelper localStoreHelper = new LocalStoreHelper(project);
        if (existedList.isEmpty()) {
            String[] moduleNames = allRestItemInProject.stream().map(RestItem::getModuleName).distinct().toArray(String[]::new);
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
            localStoreHelper.asyncAdd(Collections.singletonList(new RestItem(apiInfo.getUrl(), apiInfo.getMethod().name(), headers, params, bodyJson, desc, moduleName, WEB_FRAMEWORK_LOCAL)));
        } else {
            existedList.forEach(restItem -> {
                restItem.setHeaders(headers);
                restItem.setParams(params);
                restItem.setBodyJson(bodyJson);
                restItem.setDescription(desc);
            });
            localStoreHelper.asyncReplaceAll(allRestItemInProject);
        }
        RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);
    }
}
