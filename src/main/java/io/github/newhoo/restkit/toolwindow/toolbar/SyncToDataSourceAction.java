package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.ToolkitIcons;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.common.LicenseDialog;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SyncToDataSourceAction extends BaseAnAction {

    public SyncToDataSourceAction() {
        super(ToolkitIcons.SYNC_DATA_SOURCE);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.syncdatasource.action.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        if (true) {
            new LicenseDialog(null).show();
            return;
        }
        final Project project = actionEvent.getProject();
        if (project == null) {
            return;
        }
        List<RestItem> itemList = RestDataKey.ALL_SERVICE.getData(actionEvent.getDataContext());
        DataSourceHelper.trySyncApiToDataSource(itemList, project);
    }
}
