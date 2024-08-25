package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.newhoo.restkit.i18n.RestBundle;
import org.jetbrains.annotations.NotNull;

/**
 * open post-request script in editor
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class OpenPostScriptAction extends OpenPreScriptAction {

    @Override
    protected boolean isPreScript() {
        return false;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(()-> RestBundle.message("toolkit.toolwindow.toolbar.postscript.action.text"));
    }
}
