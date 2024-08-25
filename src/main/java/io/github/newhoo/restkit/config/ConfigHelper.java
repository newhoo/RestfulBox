package io.github.newhoo.restkit.config;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.RestConstant;
import io.github.newhoo.restkit.common.ToolkitIcons;
import io.github.newhoo.restkit.config.global.GlobalSetting;
import io.github.newhoo.restkit.config.global.GlobalSettingComponent;
import io.github.newhoo.restkit.config.ide.CommonSetting;
import io.github.newhoo.restkit.config.ide.CommonSettingComponent;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@NotProguard
public class ConfigHelper {

    public static final String NAVIGATE_TEXT = RestBundle.message("toolkit.navigate.text");
    public static final Icon NAVIGATE_ICON = ToolkitIcons.REQUEST;
    public static final String HTTP_FILE_DEFAULT_DESCRIPTOR = RestConstant.HTTP_FILE_DEFAULT_DESCRIPTOR;

    public static GlobalSetting getGlobalSetting() {
        return GlobalSettingComponent.getInstance().getState();
    }

    public static CommonSetting getCommonSetting(@NotNull Project project) {
        return CommonSettingComponent.getInstance(project).getState();
    }

    public static String getCurrentDataSourceName() {
        return DataSourceHelper.getDataSource().name();
    }
}
