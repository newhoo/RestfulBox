package io.github.newhoo.restkit;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import io.github.newhoo.restkit.common.BKV;
import io.github.newhoo.restkit.common.EnvList;
import io.github.newhoo.restkit.config.CommonSetting;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.config.HttpSetting;
import io.github.newhoo.restkit.config.HttpSettingComponent;
import io.github.newhoo.restkit.config.certificate.Certificate;
import io.github.newhoo.restkit.config.certificate.CertificateComponent;
import io.github.newhoo.restkit.util.EnvironmentUtils;
import io.github.newhoo.restkit.util.FileUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.HTTP_BASE_URL;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_PASSWD;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_PATH;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_URL_HTTPS;

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
    private static final String KEY_P12_SYNC = "RESTKit.p12.sync";

    @Override
    public void runActivity(@NotNull Project project) {
        if (project.isDefault()) {
            return;
        }
        // 默认创建项目级别
        CommonSetting setting = CommonSettingComponent.getInstance(project).getState();
        String apiFile = setting.getApiFilePath();
        if (StringUtils.isEmpty(apiFile)) {
            apiFile = FileUtils.getApiFilePath(project);
            setting.setApiFilePath(apiFile);
        }
        HttpSetting httpSetting = HttpSettingComponent.getInstance(project).getState();
        if (StringUtils.isEmpty(httpSetting.getDownloadDirectory())) {
            httpSetting.setDownloadDirectory(FileUtils.getDownloadDirectory(project));
        }

        // 同步p12证书
        PropertiesComponent oldSetting = PropertiesComponent.getInstance(project);
        if (!oldSetting.getBoolean(KEY_P12_SYNC)) {
            List<Certificate> certificates = CertificateComponent.getInstance().getCertificates();
            List<EnvList> envLists = Environment.getInstance(project).getEnvList();
            for (EnvList env : envLists) {
                List<BKV> items = env.getItems();
                Map<String, BKV> map = items.stream().collect(Collectors.toMap(BKV::getKey, o -> o, (o1, o2) -> o1));
                BKV baseUrlKV = map.get(HTTP_BASE_URL);
                BKV p12KV = map.get(HTTP_P12_PATH);
                if (baseUrlKV != null && p12KV != null && StringUtils.startsWith(baseUrlKV.getValue(), HTTP_URL_HTTPS)) {
                    BKV p12PassKV = map.get(HTTP_P12_PASSWD);
                    String url = baseUrlKV.getValue().substring(8);
                    String host = url.contains("/") ? url.substring(0, url.indexOf("/")) : url;
                    if (certificates.stream().noneMatch(e -> host.equals(e.getHost()))) {
                        Certificate cert = new Certificate();
                        cert.setEnable(p12KV.getEnabled());
                        cert.setHost(host);
                        cert.setPfxFile(p12KV.getValue());
                        cert.setPassphrase(p12PassKV != null ? p12PassKV.getValue() : "");
                        certificates.add(cert);
                    }
                }
            }
            oldSetting.setValue(KEY_P12_SYNC, true);
        }

        // 兼容旧版本版本
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