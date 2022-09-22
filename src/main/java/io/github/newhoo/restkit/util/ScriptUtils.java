package io.github.newhoo.restkit.util;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.Request;
import io.github.newhoo.restkit.common.Response;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.Environment;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@UtilityClass
public class ScriptUtils {

    public static void handlePreRequestScript(Request request, Project project) throws Exception {
        String scriptPath = CommonSettingComponent.getInstance(project).getState().getPreRequestScriptPath();
        if (StringUtils.isNotEmpty(scriptPath) && Files.exists(Paths.get(scriptPath))) {
            Map<String, String> environmentMap = Environment.getInstance(project).getCurrentEnabledEnvMap();

            ScriptEngine se = getScriptEngine();
            Bindings bindings = se.createBindings();
            bindings.put("request", request);
            bindings.put("environment", environmentMap);
            se.eval(new FileReader(scriptPath), bindings);
        }
    }

    public static void handlePostRequestScript(Request request, Response response, Project project) throws Exception {
        String scriptPath = CommonSettingComponent.getInstance(project).getState().getPostRequestScriptPath();
        if (StringUtils.isNotEmpty(scriptPath) && Files.exists(Paths.get(scriptPath))) {
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
}
