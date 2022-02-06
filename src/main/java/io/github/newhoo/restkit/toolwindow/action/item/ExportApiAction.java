package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ExportApiAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class ExportApiAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        List<RestItem> serviceItems = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        if (CollectionUtils.isEmpty(serviceItems)) {
            return;
        }

        // check api item is valid. PsiElement may be invalid
        if (serviceItems.stream().anyMatch(restItem -> !restItem.isValid())) {
            NotifierUtils.errorBalloon(RestBundle.message("toolkit.local.api.export.error.title"), RestBundle.message("toolkit.local.api.export.error.content"), project);
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "[RESTKit] Export api") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                List<RestItem> exportItems = DumbService.getInstance(project).runReadActionInSmartMode(() -> {
                    return serviceItems.stream().map(o -> {
                        RestItem restItem = new RestItem();
                        restItem.setUrl(o.getUrl());
                        restItem.setMethod(o.getMethod());
                        restItem.setHeaders(o.getHeaders());
                        restItem.setParams(o.getParams());
                        restItem.setBodyJson(o.getBodyJson());
                        restItem.setDescription(o.getDescription());
                        restItem.setModuleName(o.getModuleName());
                        restItem.setFramework(o.getFramework());
                        return restItem;
                    }).collect(Collectors.toList());
                });

                IdeaUtils.copyToClipboard(JsonUtils.toJson(exportItems));
                NotifierUtils.infoBalloon(RestBundle.message("toolkit.local.api.export.title"), RestBundle.message("toolkit.local.api.export.content"), null, project);
            }
        });
    }
}
