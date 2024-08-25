package io.github.newhoo.restkit.toolwindow.tree;

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
import io.github.newhoo.restkit.intellij.BaseAnAction;
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
public class ExportApiAction extends BaseAnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.common.btn.export"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        List<RestItem> serviceItems = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        if (CollectionUtils.isEmpty(serviceItems)) {
            e.getPresentation().setVisible(false);
            return;
        }

        // check api item is valid. PsiElement may be invalid
        if (serviceItems.stream().anyMatch(restItem -> !restItem.isValid())) {
            NotifierUtils.errorBalloon(RestBundle.message("toolkit.toolwindow.tree.exportapi.error.title"), RestBundle.message("toolkit.toolwindow.tree.exportapi.error.content"), project);
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "[RestfulBox] Export api") {
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
                        restItem.setProject(o.getProject());
                        restItem.setModuleName(o.getModuleName());
                        restItem.setPackageName(o.getPackageName());
                        restItem.setFramework(o.getFramework());
                        restItem.setId(o.getId());
                        restItem.setProtocol(o.getProtocol());
                        return restItem;
                    }).collect(Collectors.toList());
                });

                IdeaUtils.copyToClipboard(JsonUtils.toJson(exportItems));
                NotifierUtils.infoBalloon(RestBundle.message("toolkit.toolwindow.tree.exportapi.title"), RestBundle.message("toolkit.toolwindow.tree.exportapi.content"), null, project);
            }
        });
    }
}
