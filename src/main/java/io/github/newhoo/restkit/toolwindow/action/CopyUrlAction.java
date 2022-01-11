package io.github.newhoo.restkit.toolwindow.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAware;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class CopyUrlAction extends AnAction implements DumbAware {

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
        NotifierUtils.infoBalloon("", "Copy url to clipboard successfully.", null, e.getRequiredData(CommonDataKeys.PROJECT));
    }
}