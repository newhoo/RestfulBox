package io.github.newhoo.restkit.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import groovy.lang.GroovyClassLoader;
import io.github.newhoo.restkit.common.Request;
import io.github.newhoo.restkit.common.Response;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.config.RequestSetting;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.config.SettingListener.ENV_UPDATE;

@UtilityClass
public class ScriptUtils {
    private static final Logger LOG = Logger.getInstance(ScriptUtils.class);

    public static void handlePreRequestScript(Request request, RequestSetting setting, String currentEnv, Environment environment, Project project) throws Exception {
        if (!setting.isEnablePreRequestScript()) {
            return;
        }
        String scriptPath = FileUtils.expandUserHome(setting.getPreRequestScriptPath());
        if (StringUtils.isNotEmpty(scriptPath) && Files.exists(Paths.get(scriptPath))) {
            Map<String, String> currentEnabledEnvMap = environment.getEnabledEnvMap(currentEnv);
            Map<String, String> scriptEnvMap = new HashMap<>(currentEnabledEnvMap);
            ScriptEngine se = getScriptEngine();
            Bindings bindings = se.createBindings();
            bindings.put("request", request);
            bindings.put("environment", scriptEnvMap);
            se.eval(new FileReader(scriptPath), bindings);

            updateCurrentEnabledEnvMap(currentEnv, currentEnabledEnvMap, scriptEnvMap, environment, project);
        }
    }

    public static void handlePostRequestScript(Request request, Response response, RequestSetting setting, String currentEnv, Environment environment, Project project) throws Exception {
        if (!setting.isEnablePostRequestScript()) {
            return;
        }
        String scriptPath = FileUtils.expandUserHome(setting.getPostRequestScriptPath());
        if (StringUtils.isNotEmpty(scriptPath) && Files.exists(Paths.get(scriptPath))) {
            Map<String, String> currentEnabledEnvMap = environment.getEnabledEnvMap(currentEnv);
            Map<String, String> scriptEnvMap = new HashMap<>(currentEnabledEnvMap);
            ScriptEngine se = getScriptEngine();
            Bindings bindings = se.createBindings();
            bindings.put("request", request);
            bindings.put("response", response);
            bindings.put("environment", scriptEnvMap);
            se.eval(new FileReader(scriptPath), bindings);

            updateCurrentEnabledEnvMap(currentEnv, currentEnabledEnvMap, scriptEnvMap, environment, project);
        }
    }

    private static void updateCurrentEnabledEnvMap(String currentEnv, Map<String, String> currentEnabledEnvMap, Map<String, String> scriptEnvMap, Environment environment, Project project) {
        if (StringUtils.isEmpty(currentEnv)) {
            return;
        }
        try {
            if (!currentEnabledEnvMap.equals(scriptEnvMap)) {
                environment.updateEnabledEnvMap(currentEnv, scriptEnvMap);
                DataSourceHelper.getDataSource().syncEnvironment(environment, project);
                project.getMessageBus().syncPublisher(ENV_UPDATE).changeEnv(environment.getProject());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ScriptEngine getScriptEngine() {
        ScriptEngine javascriptEngine = new ScriptEngineManager().getEngineByName("javascript");
        if (javascriptEngine != null) {
            return javascriptEngine;
        }
        return new NashornScriptEngineFactory().getScriptEngine();
    }

    private static final List<String> ignoreMethods = Arrays.asList("wait", "wait", "wait", "equals", "toString", "hashCode", "getClass", "notify", "notifyAll");

    public Map<String, Method> getScriptMethodMapFromJava(String script) {
        if (StringUtils.isEmpty(script)) {
            return Collections.emptyMap();
        }
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader()) {
            Class<?> clazz = groovyClassLoader.parseClass(script);
            return Arrays.stream(clazz.getMethods())
                         .filter(m -> !ignoreMethods.contains(m.getName()) && m.getModifiers() == (Modifier.STATIC | Modifier.PUBLIC))
                         .collect(Collectors.toMap(Method::getName, m -> m));
        } catch (Throwable t) {
            LOG.error("script variable error: " + t.toString(), t);
        }
        return Collections.emptyMap();
    }
}
