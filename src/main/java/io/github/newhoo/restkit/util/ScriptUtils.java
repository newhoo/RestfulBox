package io.github.newhoo.restkit.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import groovy.lang.GroovyClassLoader;
import io.github.newhoo.restkit.common.Request;
import io.github.newhoo.restkit.common.Response;
import io.github.newhoo.restkit.config.CommonSetting;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.Environment;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class ScriptUtils {
    public static final Logger LOG = Logger.getInstance(ScriptUtils.class);

    public static void handlePreRequestScript(Request request, Project project) throws Exception {
        CommonSetting setting = CommonSettingComponent.getInstance(project).getState();
        String scriptPath = setting.getPreRequestScriptPath();
        if (setting.isEnablePreRequestScript() && StringUtils.isNotEmpty(scriptPath) && Files.exists(Paths.get(scriptPath))) {
            Map<String, String> environmentMap = Environment.getInstance(project).getCurrentEnabledEnvMap();

            ScriptEngine se = getScriptEngine();
            Bindings bindings = se.createBindings();
            bindings.put("request", request);
            bindings.put("environment", environmentMap);
            se.eval(new FileReader(scriptPath), bindings);
        }
    }

    public static void handlePostRequestScript(Request request, Response response, Project project) throws Exception {
        CommonSetting setting = CommonSettingComponent.getInstance(project).getState();
        String scriptPath = setting.getPostRequestScriptPath();
        if (setting.isEnablePostRequestScript() && StringUtils.isNotEmpty(scriptPath) && Files.exists(Paths.get(scriptPath))) {
            Map<String, String> environmentMap = Environment.getInstance(project).getCurrentEnabledEnvMap();

            ScriptEngine se = getScriptEngine();
            Bindings bindings = se.createBindings();
            bindings.put("request", request);
            bindings.put("response", response);
            bindings.put("environment", environmentMap);
            se.eval(new FileReader(scriptPath), bindings);
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
            LOG.error("script variable error: " + t);
        }
        return Collections.emptyMap();
    }
}
