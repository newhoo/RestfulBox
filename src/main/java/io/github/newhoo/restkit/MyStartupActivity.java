package io.github.newhoo.restkit;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import io.github.newhoo.restkit.common.BKV;
import io.github.newhoo.restkit.common.EnvList;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.util.EnvironmentUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目打开后的后台任务：合并旧版本的配置
 *
 * @author huzunrong
 */
public class MyStartupActivity implements StartupActivity {

    private static final String KEY_REQUEST_HEADER_LIST = "RESTKit.requestHeaderList";
    private static final String KEY_REQUEST_ENV_LIST = "RESTKit.envList";
    private static final String KEY_REQUEST_SCRIPT = "RESTKit.script";
    private static final String KEY_REQUEST_SYNC = "RESTKit.sync";

    @Override
    public void runActivity(@NotNull Project project) {
        if (project.isDefault()) {
            return;
        }

        PropertiesComponent fromSetting = PropertiesComponent.getInstance(project);
        if (!fromSetting.getBoolean(KEY_REQUEST_SYNC)) {
            Environment toSetting = Environment.getInstance(project);
            List<BKV> headerList = EnvironmentUtils.parseHeaderList(fromSetting.getValue(KEY_REQUEST_HEADER_LIST));
            if (!headerList.isEmpty()) {
                if (toSetting.getGlobalHeaderList().isEmpty()) {
                    toSetting.setGlobalHeaderList(headerList);
                } else {
                    toSetting.getGlobalHeaderList().addAll(headerList);
                }
            }
            List<EnvList> envList = EnvironmentUtils.parseEnvList(fromSetting.getValue(KEY_REQUEST_ENV_LIST));
            if (!envList.isEmpty()) {
                if (CollectionUtils.isEmpty(toSetting.getEnvList())) {
                    toSetting.setEnvList(envList);
                } else {
                    Set<String> envSet = toSetting.getEnvList().stream().map(EnvList::getEnv).collect(Collectors.toSet());
                    for (EnvList list : envList) {
                        if (envSet.contains(list.getEnv())) {
                            list.setEnv(list.getEnv() + "_" + new Random().nextInt(10));
                        }
                        toSetting.getEnvList().add(list);
                    }
                }
                toSetting.setCurrentEnv(toSetting.getEnvList().get(0).getEnv());
            }
            String script = fromSetting.getValue(KEY_REQUEST_SCRIPT);
            if (StringUtils.isNotEmpty(script) && StringUtils.isEmpty(toSetting.getScript())) {
                toSetting.setScript(script);
            }

            EnvironmentUtils.notifyEnvUpdate(project);
            fromSetting.setValue(KEY_REQUEST_SYNC, true);
        }
    }
}