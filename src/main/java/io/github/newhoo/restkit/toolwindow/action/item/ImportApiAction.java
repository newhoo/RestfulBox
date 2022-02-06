package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.CommonSetting;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.SettingConfigurable;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.restful.local.LocalStoreHelper;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import io.github.newhoo.restkit.util.HtmlUtil;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;

/**
 * ImportApiAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class ImportApiAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        CommonSetting commonSetting = CommonSettingComponent.getInstance(project).getState();
        if (!commonSetting.getEnabledWebFrameworks().contains(WEB_FRAMEWORK_LOCAL)) {
            NotifierUtils.infoBalloon("", "Local api store disabled. " + HtmlUtil.link("Edit", "Edit"), new NotificationListener.Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingConfigurable.class);
                }
            }, project);
            return;
        }

        Transferable contents = CopyPasteManager.getInstance().getContents();
        if (contents == null) {
            return;
        }
        try {
            String data = contents.getTransferData(DataFlavor.stringFlavor).toString();
            List<RestItem> restItems = JsonUtils.fromJsonArr(data, RestItem.class);
            if (restItems != null) {
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "[RESTKit] Import api") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        restItems.forEach(o -> {
                            o.setUrl(StringUtils.defaultString(o.getUrl()));
                            o.setBodyJson(StringUtils.defaultString(o.getBodyJson()));
                            o.setDescription(StringUtils.defaultString(o.getDescription()));
                            o.setFramework(WEB_FRAMEWORK_LOCAL);
                            o.setModuleName(o.getModuleName() + "(import)");
                            o.setTs(System.currentTimeMillis());
                        });
                        new LocalStoreHelper(project).asyncAdd(restItems);
                        RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);
                    }
                });
            } else {
                NotifierUtils.errorBalloon(RestBundle.message("toolkit.local.api.import.title"), RestBundle.message("toolkit.local.api.import.content", "format error"), project);
            }
        } catch (Exception ex) {
            NotifierUtils.errorBalloon(RestBundle.message("toolkit.local.api.import.title"), RestBundle.message("toolkit.local.api.import.content", ex.toString()), project);
        }
    }
}
