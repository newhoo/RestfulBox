package io.github.newhoo.restkit.action;

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
}
