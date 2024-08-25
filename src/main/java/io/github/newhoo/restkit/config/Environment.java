package io.github.newhoo.restkit.config;

import io.github.newhoo.restkit.common.BKV;
import io.github.newhoo.restkit.common.EnvList;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.util.ScriptUtils;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Environment
 *
 * @author huzunrong
 * @since 1.0
 */
@NotProguard
@Data
public class Environment {

    private String project;
    private List<EnvList> envList = Collections.emptyList();
    private List<BKV> globalHeaderList = Collections.emptyList();
    private String script;

    public String getCurrentEnv() {
        if (CollectionUtils.isEmpty(envList)) {
            return "";
        }
        String currentEnv = ConfigHelper.getGlobalSetting().getProjectCurrentEnvMap().get(project);
        Optional<EnvList> first = envList.stream()
                                         .filter(envList -> envList.getEnv().equals(currentEnv))
                                         .findFirst();
        if (first.isPresent()) {
            return currentEnv;
        }
        String env = envList.get(0).getEnv();
        ConfigHelper.getGlobalSetting().getProjectCurrentEnvMap().put(project, env);
        return env;
    }

    public void setCurrentEnv(String currentEnv) {
        ConfigHelper.getGlobalSetting().getProjectCurrentEnvMap().put(project, currentEnv);
    }

    public List<String> getEnvKeys() {
        return CollectionUtils.isEmpty(envList)
                ? Collections.emptyList()
                : envList.stream().map(EnvList::getEnv).distinct().collect(Collectors.toList());
    }

    public Map<String, String> getEnabledEnvMap(String currentEnv) {
        if (StringUtils.isEmpty(currentEnv) || CollectionUtils.isEmpty(envList)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new LinkedHashMap<>(8);
        Optional<EnvList> first = envList.stream()
                                         .filter(envList -> envList.getEnv().equals(currentEnv))
                                         .findFirst();
        first.orElse(envList.get(0)).getItems().forEach(item -> {
            if (item.getEnabled()) {
                map.put(item.getKey(), item.getValue());
            }
        });
        return map;
    }

    public void updateEnabledEnvMap(String currentEnv, Map<String, String> newEnvMap) {
//        String currentEnv = GlobalSettingComponent.getInstance().getState().getProjectCurrentEnvMap().get(project);
        Optional<EnvList> first = envList.stream()
                                         .filter(envList -> envList.getEnv().equals(currentEnv))
                                         .findFirst();
        if (first.isEmpty()) {
            return;
        }
        EnvList list = first.get();
        if (ObjectUtils.isEmpty(newEnvMap)) {
            list.getItems().removeIf(BKV::getEnabled);
            return;
        }
        for (Map.Entry<String, String> entry : newEnvMap.entrySet()) {
            AtomicReference<Boolean> flag = new AtomicReference<>(true);
            list.getItems().stream().filter(BKV::getEnabled).filter(item -> entry.getKey().equals(item.getKey())).forEach(item -> {
                item.setValue(StringUtils.defaultString(entry.getValue()));
                flag.set(false);
            });
            if (flag.get()) {
                BKV bkv = new BKV(true, entry.getKey(), StringUtils.defaultString(entry.getValue()));
                list.getItems().add(bkv);
            }
        }
    }

    public Map<String, Method> getScriptMethodMap() {
        return ScriptUtils.getScriptMethodMapFromJava(script);
    }
}