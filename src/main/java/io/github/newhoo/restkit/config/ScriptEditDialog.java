package io.github.newhoo.restkit.config;

import com.intellij.lang.Language;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import io.github.newhoo.restkit.i18n.RestBundle;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.function.Consumer;

import static io.github.newhoo.restkit.util.IdeaUtils.createEditor;
import static io.github.newhoo.restkit.util.IdeaUtils.getEditorText;

/**
 * ScriptEditDialog
 *
 * @author huzunrong
 * @since 1.0
 */
public class ScriptEditDialog extends DialogWrapper {

    private final Project project;
    private final Consumer<String> newScriptConsumer;
    private final String scriptContent;

    private FileEditor scriptEditor;

    public ScriptEditDialog(Project project, String scriptContent, Consumer<String> newScriptConsumer) {
        super(project, true);

        this.project = project;
        this.newScriptConsumer = newScriptConsumer;
        this.scriptContent = scriptContent;

        setTitle("[RestfulBox] " + RestBundle.message("toolkit.config.request.environment.script.dialog.title"));
        setSize(800, 800);
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel contentPanel = new JBPanel<>();
        contentPanel.setLayout(new GridLayoutManager(2, 1, JBUI.insets(0, 0, 0, 0), 4, 4));

        contentPanel.add(new JBLabel(RestBundle.message("toolkit.config.request.environment.script.dialog.desc")),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        final String DEFAULT_SCRIPT_CONTENT =
                "public class RestKitScript {\n" +
                        "\n" +
                        "    /**\n" +
                        "     * Your script method, no param, method name must from {{$methodName$}}.\n" +
                        "     * Use pre-request script can be more powerful.\n" +
                        "     *\n" +
                        "     * @return return value should be String\n" +
                        "     */\n" +
                        "    public static String methodName() {\n" +
                        "        return \"\";\n" +
                        "    }\n" +
                        "\n" +
                        "}";
        String scriptText = StringUtils.defaultIfEmpty(scriptContent, DEFAULT_SCRIPT_CONTENT);
        Language language = project.isDefault()
                ? PlainTextLanguage.INSTANCE
                : ObjectUtils.defaultIfNull(Language.findLanguageByID("JAVA"), PlainTextLanguage.INSTANCE);
        scriptEditor = createEditor("RestKitScript.java", language, scriptText, "BasicEditorPopupMenu", project);
        contentPanel.add(scriptEditor.getComponent(),
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                        GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                        null, null, null));
        return contentPanel;
    }

    @Override
    protected void doOKAction() {
        newScriptConsumer.accept(getEditorText(scriptEditor));
        close(OK_EXIT_CODE);
    }
}
