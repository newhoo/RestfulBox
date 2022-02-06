package io.github.newhoo.restkit.config;

import com.intellij.lang.Language;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AppUIUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
public class ScriptEditDialog extends JDialog {

    private JPanel contentPane;
    private JPanel editPane;
    private JButton okButton;
    private JButton cancelButton;

    private FileEditor scriptEditor;

    private Project project;

    private Consumer<String> newScriptConsumer;

    public ScriptEditDialog(Project project, String scriptContent, Consumer<String> newScriptConsumer) {
        this.project = project;
        this.newScriptConsumer = newScriptConsumer;

        setSize(800, 800);
        setLocationRelativeTo(null);

        setContentPane(contentPane);
        setModal(true);
        setAlwaysOnTop(true);
        setTitle("Edit RESTKit Script");
        getRootPane().setDefaultButton(okButton);
        initScriptEditor(scriptContent);
        initListener();
    }

    private void initListener() {
        okButton.addActionListener(e -> {
            newScriptConsumer.accept(getEditorText(scriptEditor));
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void initScriptEditor(String scriptContent) {
        AppUIUtil.invokeOnEdt(() -> {
            String scriptText = StringUtils.isNotEmpty(scriptContent) ? scriptContent : DEFAULT_SCRIPT_CONTENT;

            Language language = project.isDefault()
                    ? PlainTextLanguage.INSTANCE
                    : ObjectUtils.defaultIfNull(Language.findLanguageByID("JAVA"), PlainTextLanguage.INSTANCE);

            scriptEditor = createEditor("RestKitScript.java", language, scriptText, project);
            editPane.add(scriptEditor.getComponent(), BorderLayout.CENTER);
            scriptEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });
    }
}
