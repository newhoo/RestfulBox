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
import java.awt.event.ItemEvent;
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
import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;

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

    private JPanel uiConfigPanel;
    private JCheckBox showModuleCheckBox;
    private JCheckBox displayApiGroupUsingFileNameCheckBox;
    private JCheckBox displayTreeListUsingApiDescCheckBox;
    private JCheckBox enableParameterLibraryCheckBox;
    private JCheckBox enableMethodLineMarkerCheckBox;

    private JPanel requestPanel;
    private JCheckBox saveRequestLogCheckBox;
    private JCheckBox preRequestScriptCheckbox;
    private JPanel preRequestScriptPanel;
    private TextFieldWithBrowseButton preRequestScriptPathTextField;
    private JCheckBox postRequestScriptCheckbox;
    private JPanel postRequestScriptPanel;
    private TextFieldWithBrowseButton postRequestScriptPathTextField;

    private JPanel otherPanel;
    private JLabel apiFilePathLabel;
    private JPanel apiFilePathPanel;
    private TextFieldWithBrowseButton apiFilePathTextField;

    private final Project project;

    public SettingForm(Project project) {
        this.project = project;

        // 要单独写一个方法，直接用获取到的组件是null
        initView();
        initEvent();
    }

    private void initView() {
        webFrameworkPanel.setBorder(IdeBorderFactory.createTitledBorder("Support Framework", false));
        uiConfigPanel.setBorder(IdeBorderFactory.createTitledBorder("UI Config", false));
        requestPanel.setBorder(IdeBorderFactory.createTitledBorder("Request Config", false));
        otherPanel.setBorder(IdeBorderFactory.createTitledBorder("Other", false));

        List<RequestResolver> requestResolvers = RequestHelper.getAllRequestResolvers(project);
        for (RequestResolver requestResolver : requestResolvers) {
            JCheckBox checkBox = new JCheckBox(requestResolver.getFrameworkName(), true);
            webFrameworkPanel.add(checkBox);
            if (WEB_FRAMEWORK_LOCAL.equals(requestResolver.getFrameworkName())) {
                checkBox.addItemListener(e -> apiFilePathTextField.setEnabled(e.getStateChange() == ItemEvent.SELECTED));
            }
        }

        preRequestScriptPathTextField = createScriptPathTextField(preRequestScriptPanel, "js", "Select javascript file.");
        postRequestScriptPathTextField = createScriptPathTextField(postRequestScriptPanel, "js", "Select javascript file.");

        apiFilePathTextField = createScriptPathTextField(apiFilePathPanel, "json", "Select json file for File Store.");
    }

    /**
     * com.intellij.compiler.options.ProcessorProfilePanel
     * preRequestScriptPathTextField.addActionListener()
     */
    private TextFieldWithBrowseButton createScriptPathTextField(JPanel panel, String extension, String toolTip) {
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
                final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(extension);
                String scriptPath = FileUtils.getRestDirectory(project);
                VirtualFile toSelect = LocalFileSystem.getInstance().findFileByIoFile(new File(scriptPath));
                final VirtualFile[] files = FileChooser.chooseFiles(descriptor, scriptPathTextField, project, toSelect);
                if (files.length > 0) {
                    String filePath = FileUtil.toSystemDependentName(files[0].getPath());
                    scriptPathTextField.setText(filePath);
                }
            }
        });
        scriptPathTextField.getTextField().setToolTipText(toolTip);
        panel.add(scriptPathTextField, BorderLayout.CENTER);
        return scriptPathTextField;
    }

    private void initEvent() {
        enableParameterLibraryCheckBox.addItemListener(e -> {
            enableParameterLibraryCheckBox.setText("Enable parameter library (need reopen project)");
        });
        addScriptLabelListener(preRequestScriptCheckbox, preRequestScriptPathTextField, "Pre-request Script.js", PRE_REQUEST_SCRIPT);
        addScriptLabelListener(postRequestScriptCheckbox, postRequestScriptPathTextField, "Post-request Script.js", POST_REQUEST_SCRIPT);
        apiFilePathLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (StringUtils.isNotEmpty(apiFilePathTextField.getText()) || project.isDefault()) {
                    return;
                }
                if (e.getClickCount() == 2) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        String path = FileUtils.getApiFilePath();
                        if (path != null) {
                            apiFilePathTextField.setText(path);
                        }
                    } else {
                        apiFilePathTextField.setText(FileUtils.getApiFilePath(project));
                    }
                }
            }
        });
    }

    private void addScriptLabelListener(JCheckBox scriptCheckBox, TextFieldWithBrowseButton scriptPathTextField, String scriptName, String defaultContent) {
        scriptCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && StringUtils.isEmpty(scriptPathTextField.getText()) && !project.isDefault()) {
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

        commonSetting.setDisplayTreeListUsingApiDesc(displayTreeListUsingApiDescCheckBox.isSelected());
        commonSetting.setShowModuleInSearchEvery(showModuleCheckBox.isSelected());
        commonSetting.setDisplayApiGroupUsingFileName(displayApiGroupUsingFileNameCheckBox.isSelected());
        commonSetting.setEnableParameterLibrary(enableParameterLibraryCheckBox.isSelected());
        commonSetting.setEnableMethodLineMarker(enableMethodLineMarkerCheckBox.isSelected());

        commonSetting.setSaveRequestLog(saveRequestLogCheckBox.isSelected());
        commonSetting.setEnablePreRequestScript(preRequestScriptCheckbox.isSelected());
        commonSetting.setEnablePostRequestScript(postRequestScriptCheckbox.isSelected());
        commonSetting.setPreRequestScriptPath(preRequestScriptPathTextField.getText().trim());
        commonSetting.setPostRequestScriptPath(postRequestScriptPathTextField.getText().trim());

        commonSetting.setApiFilePath(apiFilePathTextField.getText().trim());
    }

    public void reset(CommonSetting commonSetting) {
        Set<String> supportedWebFrameworks = commonSetting.getEnabledWebFrameworks();
        for (Component component : webFrameworkPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox jCheckBox = (JCheckBox) component;
                jCheckBox.setSelected(supportedWebFrameworks.contains(jCheckBox.getText()));
            }
        }

        displayTreeListUsingApiDescCheckBox.setSelected(commonSetting.isDisplayTreeListUsingApiDesc());
        showModuleCheckBox.setSelected(commonSetting.isShowModuleInSearchEvery());
        displayApiGroupUsingFileNameCheckBox.setSelected(commonSetting.isDisplayApiGroupUsingFileName());
        enableParameterLibraryCheckBox.setSelected(commonSetting.isEnableParameterLibrary());
        enableParameterLibraryCheckBox.setText("Enable parameter library");
        enableMethodLineMarkerCheckBox.setSelected(commonSetting.isEnableMethodLineMarker());

        saveRequestLogCheckBox.setSelected(commonSetting.isSaveRequestLog());
        preRequestScriptCheckbox.setSelected(commonSetting.isEnablePreRequestScript());
        postRequestScriptCheckbox.setSelected(commonSetting.isEnablePostRequestScript());
        preRequestScriptPathTextField.setText(FileUtil.toSystemDependentName(commonSetting.getPreRequestScriptPath()));
        postRequestScriptPathTextField.setText(FileUtil.toSystemDependentName(commonSetting.getPostRequestScriptPath()));

        apiFilePathTextField.setText(FileUtil.toSystemDependentName(commonSetting.getApiFilePath()));
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
}
