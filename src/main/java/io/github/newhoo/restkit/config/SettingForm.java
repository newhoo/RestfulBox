package io.github.newhoo.restkit.config;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.util.FileUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.newhoo.restkit.common.RestConstant.POST_REQUEST_SCRIPT;
import static io.github.newhoo.restkit.common.RestConstant.PRE_REQUEST_SCRIPT;

/**
 * SettingForm
 *
 * @author huzunrong
 * @since 1.0
 */
public class SettingForm {

    @Getter
    private JPanel mainPanel;

    private JPanel webFrameworkPanel;

    private JPanel httpPanel;
    private JTextField requestTimeoutField;
    private JCheckBox saveRequestLogCheckBox;
    private JCheckBox enableParameterLibraryCheckBox;
    private JCheckBox showModuleCheckBox;

    private JPanel scriptPanel;
    private JPanel preRequestScriptPanel;
    private JLabel preRequestScriptLabel;
    private TextFieldWithBrowseButton preRequestScriptPathTextField;
    private JPanel postRequestScriptPanel;
    private JLabel postRequestScriptLabel;
    private TextFieldWithBrowseButton postRequestScriptPathTextField;

    private final Project project;

    public SettingForm(Project project) {
        this.project = project;

        // 要单独写一个方法，直接用获取到的组件是null
        initView();
        initEvent();
    }

    private void initView() {
        webFrameworkPanel.setBorder(IdeBorderFactory.createTitledBorder("Support Framework", false));
        httpPanel.setBorder(IdeBorderFactory.createTitledBorder("Request Config", false));
        scriptPanel.setBorder(IdeBorderFactory.createTitledBorder("Request Script", false));

        List<RequestResolver> requestResolvers = RequestHelper.getRequestResolvers(project);
        for (RequestResolver requestResolver : requestResolvers) {
            webFrameworkPanel.add(new JCheckBox(requestResolver.getFrameworkName(), true));
        }

        preRequestScriptPathTextField = createScriptPathTextField(preRequestScriptPanel);
        postRequestScriptPathTextField = createScriptPathTextField(postRequestScriptPanel);
    }

    /**
     * com.intellij.compiler.options.ProcessorProfilePanel
     * preRequestScriptPathTextField.addActionListener()
     */
    private TextFieldWithBrowseButton createScriptPathTextField(JPanel panel) {
        TextFieldWithBrowseButton scriptPathTextField = new TextFieldWithBrowseButton() {
            @Override
            public void dispose() {
                if (project.isDisposed()) {
                    super.dispose();
                }
            }
        };
        scriptPathTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("js");
                String scriptPath = FileUtils.getRestDirectory(project);
                VirtualFile toSelect = LocalFileSystem.getInstance().findFileByIoFile(new File(scriptPath));
                final VirtualFile[] files = FileChooser.chooseFiles(descriptor, scriptPathTextField, project, toSelect);
                if (files.length > 0) {
                    String filePath = FileUtil.toSystemDependentName(files[0].getPath());
                    scriptPathTextField.setText(filePath);
                }
            }
        });
        scriptPathTextField.getTextField().setToolTipText("Select javascript file.");
        panel.add(scriptPathTextField, BorderLayout.CENTER);
        return scriptPathTextField;
    }

    private void initEvent() {
        enableParameterLibraryCheckBox.addItemListener(e -> {
            enableParameterLibraryCheckBox.setText("Enable parameter library (need reopen project)");
        });
        addScriptLabelListener(preRequestScriptLabel, preRequestScriptPathTextField, "Pre-request Script.js", PRE_REQUEST_SCRIPT);
        addScriptLabelListener(postRequestScriptLabel, postRequestScriptPathTextField, "Post-request Script.js", POST_REQUEST_SCRIPT);
    }

    private void addScriptLabelListener(JLabel scriptLabel, TextFieldWithBrowseButton scriptPathTextField, String scriptName, String defaultContent) {
        scriptLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && StringUtils.isEmpty(scriptPathTextField.getText())) {
                    String scriptPath = FileUtils.getRestDirectory(project) + scriptName;
                    scriptPathTextField.setText(scriptPath);
                    if (!Files.exists(Paths.get(scriptPath))) {
                        FileUtils.createScript(scriptPath, defaultContent, project);
                    }
                }
            }
        });
    }

    public CommonSetting getModifiedSetting() {
        CommonSetting commonSetting = new CommonSetting();
        saveTo(commonSetting);
        return commonSetting;
    }

    public void saveTo(CommonSetting commonSetting) {
        commonSetting.setEnabledWebFrameworks(getEnabledWebFrameworks());
        commonSetting.setRequestTimeout(parseIntOrZero(requestTimeoutField.getText()));
        commonSetting.setSaveRequestLog(saveRequestLogCheckBox.isSelected());
        commonSetting.setShowModuleInSearchEvery(showModuleCheckBox.isSelected());
        commonSetting.setEnableParameterLibrary(enableParameterLibraryCheckBox.isSelected());
        commonSetting.setPreRequestScriptPath(preRequestScriptPathTextField.getText().trim());
        commonSetting.setPostRequestScriptPath(postRequestScriptPathTextField.getText().trim());
    }

    public void reset(CommonSetting commonSetting) {
        Set<String> supportedWebFrameworks = commonSetting.getEnabledWebFrameworks();
        for (Component component : webFrameworkPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox jCheckBox = (JCheckBox) component;
                jCheckBox.setSelected(supportedWebFrameworks.contains(jCheckBox.getText()));
            }
        }

        requestTimeoutField.setText(String.valueOf(commonSetting.getRequestTimeout()));
        saveRequestLogCheckBox.setSelected(commonSetting.isSaveRequestLog());
        showModuleCheckBox.setSelected(commonSetting.isShowModuleInSearchEvery());
        enableParameterLibraryCheckBox.setSelected(commonSetting.isEnableParameterLibrary());
        enableParameterLibraryCheckBox.setText("Enable parameter library");

        preRequestScriptPathTextField.setText(FileUtil.toSystemDependentName(commonSetting.getPreRequestScriptPath()));
        postRequestScriptPathTextField.setText(FileUtil.toSystemDependentName(commonSetting.getPostRequestScriptPath()));
    }

    private Set<String> getEnabledWebFrameworks() {
        Set<String> enabledWebFrameworks = new HashSet<>(8);
        for (Component component : webFrameworkPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox jCheckBox = (JCheckBox) component;
                if (jCheckBox.isSelected()) {
                    enabledWebFrameworks.add(jCheckBox.getText());
                }
            }
        }
        return enabledWebFrameworks;
    }

    private int parseIntOrZero(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
