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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.function.Consumer;

import static io.github.newhoo.restkit.common.RestConstant.DEFAULT_SCRIPT_CONTENT;
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

        setTitle("Edit RESTKit Script");
        setSize(800, 800);
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel contentPanel = new JBPanel<>();
        contentPanel.setLayout(new GridLayoutManager(2, 1, JBUI.insets(0, 0, 0, 0), 4, 4));

        contentPanel.add(new JBLabel("Add script method using java (method signature should be like \"public static String xxx()\"):"),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        String scriptText = StringUtils.isNotEmpty(scriptContent) ? scriptContent : DEFAULT_SCRIPT_CONTENT;
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
