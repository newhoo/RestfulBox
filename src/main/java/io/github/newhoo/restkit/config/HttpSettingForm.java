package io.github.newhoo.restkit.config;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * @author newhoo
 * @date 2022/8/15 22:10
 * @since 1.0.0
 */
public class HttpSettingForm {

    @Getter
    private JPanel mainPanel;
    private JTextField requestTimeoutField;
    private JCheckBox generateMultilineCurlSnippetCheckBox;
    private JCheckBox supportForWslPathCheckBox;
    private JPanel downloadDirectoryPanel;
    private TextFieldWithBrowseButton downloadDirectoryTextField;

    private final Project project;

    public HttpSettingForm(Project project) {
        this.project = project;

        // 要单独写一个方法，直接用获取到的组件是null
        initView();
    }

    private void initView() {
        supportForWslPathCheckBox.setVisible(SystemInfo.isWindows);
        initDownloadDirectoryTextField();
    }

    private void initDownloadDirectoryTextField() {
        downloadDirectoryTextField = new TextFieldWithBrowseButton() {
            @Override
            public void dispose() {
                if (project.isDisposed()) {
                    super.dispose();
                }
            }
        };
        downloadDirectoryTextField.addActionListener(e -> {
            VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), downloadDirectoryTextField, project, null);
            if (file != null) {
                String filePath = FileUtil.toSystemDependentName(file.getPath());
                downloadDirectoryTextField.setText(filePath);
            }
        });
        downloadDirectoryTextField.getTextField().setToolTipText("Select download dest directory.");
        downloadDirectoryPanel.add(downloadDirectoryTextField, BorderLayout.CENTER);
    }

    public HttpSetting getModifiedSetting() {
        HttpSetting httpSetting = new HttpSetting();
        saveTo(httpSetting);
        return httpSetting;
    }

    public void saveTo(HttpSetting httpSetting) {
        httpSetting.setRequestTimeout(parseIntOrZero(requestTimeoutField.getText()));
        httpSetting.setGenerateMultilineCurlSnippet(generateMultilineCurlSnippetCheckBox.isSelected());
        httpSetting.setSupportForWslPath(supportForWslPathCheckBox.isSelected());
        httpSetting.setDownloadDirectory(downloadDirectoryTextField.getText().trim());
    }

    public void reset(HttpSetting httpSetting) {
        requestTimeoutField.setText(String.valueOf(httpSetting.getRequestTimeout()));
        generateMultilineCurlSnippetCheckBox.setSelected(httpSetting.isGenerateMultilineCurlSnippet());
        supportForWslPathCheckBox.setSelected(httpSetting.isSupportForWslPath());
        downloadDirectoryTextField.setText(httpSetting.getDownloadDirectory());
    }

    private int parseIntOrZero(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
