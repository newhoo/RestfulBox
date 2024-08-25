package io.github.newhoo.restkit.toolwindow.client;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAware;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CopyUrlAction extends BaseAnAction implements DumbAware {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(RestBundle.message("toolkit.toolwindow.client.copyurl.action.text"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        List<RestItem> serviceItems = RestDataKey.SELECTED_SERVICE.getData(e.getDataContext());
        if (CollectionUtils.isEmpty(serviceItems)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (RestItem serviceItem : serviceItems) {
            if (sb.length() > 1) {
                sb.append("\n\n");
            }
            sb.append(serviceItem.getUrl());
        }

        IdeaUtils.copyToClipboard(sb.toString());
        NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.client.copyurl.msg"), null, e.getRequiredData(CommonDataKeys.PROJECT));
    }
}