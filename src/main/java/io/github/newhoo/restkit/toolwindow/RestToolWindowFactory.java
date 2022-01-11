package io.github.newhoo.restkit.toolwindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
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
        RestServiceToolWindow restServiceToolWindow = new RestServiceToolWindow(project, toolWindow);
        WINDOW_MAP.put(getKey(project), restServiceToolWindow);
    }

    public static void getRestServiceToolWindow(Project project, Consumer<RestServiceToolWindow> consumer) {
        String key = getKey(project);
        RestServiceToolWindow window = WINDOW_MAP.get(key);
        if (window != null && !window.getMyToolWindow().isDisposed()) {
            consumer.accept(window);
            return;
        }
        ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID).activate(() -> {
            consumer.accept(WINDOW_MAP.get(key));
        });
    }

    private static String getKey(Project project) {
        return project.getLocationHash()/* + "_" + Integer.toHexString(project.hashCode())*/;
    }
}
