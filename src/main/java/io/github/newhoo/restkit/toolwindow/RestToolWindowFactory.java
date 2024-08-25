package io.github.newhoo.restkit.toolwindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * RestToolWindowFactory
 */
public class RestToolWindowFactory implements ToolWindowFactory, DumbAware {
    private static final String TOOL_WINDOW_ID = "RESTKit";

    private static final Map<String, RestServiceToolWindow> WINDOW_MAP = new ConcurrentHashMap<>(4);

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        for (Map.Entry<String, RestServiceToolWindow> entry : WINDOW_MAP.entrySet()) {
            if (entry.getValue().tryDispose()) {
                WINDOW_MAP.remove(entry.getKey());
            }
        }
        RestServiceToolWindow restServiceToolWindow = new RestServiceToolWindow(project, toolWindow);
        WINDOW_MAP.put(getKey(project), restServiceToolWindow);
    }

    public static void getRestServiceToolWindow(Project project, Consumer<RestServiceToolWindow> consumer) {
        if (project.isDefault()) {
            return;
        }
        String key = getKey(project);
        RestServiceToolWindow window = WINDOW_MAP.get(key);
        if (window != null && !window.isToolWindowDisposed()) {
            consumer.accept(window);
            return;
        }
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow == null) {
            NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.open.tip"), null, project);
            return;
        }
        toolWindow.activate(() -> {
            consumer.accept(WINDOW_MAP.get(key));
        });
    }

    private static String getKey(Project project) {
        return project.getLocationHash()/* + "_" + Integer.toHexString(project.hashCode())*/;
    }
}
