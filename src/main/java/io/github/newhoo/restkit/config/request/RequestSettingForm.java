package io.github.newhoo.restkit.config.request;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import io.github.newhoo.restkit.common.BKV;
import io.github.newhoo.restkit.common.EnvList;
import io.github.newhoo.restkit.common.EnvModel;
import io.github.newhoo.restkit.common.RestRegistry;
import io.github.newhoo.restkit.config.CommonHeader;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.config.ExportWrapper;
import io.github.newhoo.restkit.config.KeyValueModel;
import io.github.newhoo.restkit.config.RequestSetting;
import io.github.newhoo.restkit.config.ScriptEditDialog;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.CompactHelper;
import io.github.newhoo.restkit.util.CommonUtils;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.newhoo.restkit.common.RestConstant.HTTP_BASE_URL;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_BASE_URL_DEFAULT;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * MultiSettingForm
 *
 * @author huzunrong
 * @since 1.0
 */
public class RequestSettingForm {

    @Getter
    private JPanel mainPanel;

    private JLabel projectLabel;
    private JComboBox<String> projectComboBox;
    private JButton refreshBtn;
    private JButton exportBtn;
    private JButton importBtn;

    private JTabbedPane tabbedPanel;

    // ======================= Request =======================
    private JPanel requestTabPanel;
    private JPanel requestPanel;
    private JCheckBox saveRequestLogCheckBox;
    private JCheckBox preRequestScriptCheckbox;
    private JPanel preRequestScriptPanel;
    private TextFieldWithBrowseButton preRequestScriptPathTextField;
    private JCheckBox postRequestScriptCheckbox;
    private JPanel postRequestScriptPanel;
    private TextFieldWithBrowseButton postRequestScriptPathTextField;

    private JPanel httpPanel;
    private JTextField requestTimeoutField;
    private JCheckBox generateMultilineCurlSnippetCheckBox;
    private JCheckBox supportForWslPathCheckBox;
    private JCheckBox supportMinifyBodyJsonCheckBox;
    private JPanel downloadDirectoryPanel;
    private TextFieldWithBrowseButton downloadDirectoryTextField;
    private JComboBox<String> defaultDateFormatComboBox;
    private JLabel requestTimeoutLabel;
    private JLabel downloadDirectoryLabel;
    private JLabel defaultDateFormatLabel;

    // ======================= Environment =======================
    private JPanel envTabPanel;
    private JPanel environmentPanel;
    private JButton addEnvironmentBtn;
    private JButton renameEnvironmentBtn;
    private JButton deleteEnvironmentBtn;
    private JButton scriptBtn;
    private JButton copyEnvironmentBtn;
    private JTabbedPane envTabbedPanel;

    // ======================= Header =======================
    private JPanel headerTabPanel;
    private JPanel commonHeaderPanel;

    private final Project project;
    private final Consumer<String> projectCallback;

    private final List<EnvModel> envModelList = new ArrayList<>();
    private final KeyValueModel headerModel = new KeyValueModel();
    private String script;

    public RequestSettingForm(Project project, Consumer<String> projectCallback) {
        this.project = project;
        this.projectCallback = projectCallback;

        // 要单独写一个方法，直接用获取到的组件是null
        initView();
        initEvent();
    }

    private void initView() {
        projectLabel.setText(RestBundle.message("toolkit.config.request.project.projectLabel"));

        projectComboBox.setModel(new CollectionComboBoxModel<>(Collections.singletonList(project.getName())));

        refreshBtn.setText(RestBundle.message("toolkit.common.btn.refresh"));
        exportBtn.setText(RestBundle.message("toolkit.common.btn.export"));
        importBtn.setText(RestBundle.message("toolkit.common.btn.import"));

        tabbedPanel.setTitleAt(0, RestBundle.message("toolkit.config.request.request.tabName"));
        tabbedPanel.setTitleAt(1, RestBundle.message("toolkit.config.request.environment.tabName"));
        tabbedPanel.setTitleAt(2, RestBundle.message("toolkit.config.request.header.tabName"));

        requestPanel.setBorder(IdeBorderFactory.createTitledBorder(RestBundle.message("toolkit.config.request.request.common.title"), false));
        saveRequestLogCheckBox.setText(RestBundle.message("toolkit.config.request.request.common.saveRequestLogCheckBox"));
        preRequestScriptCheckbox.setText(RestBundle.message("toolkit.config.request.request.common.preRequestScriptCheckbox"));
        postRequestScriptCheckbox.setText(RestBundle.message("toolkit.config.request.request.common.postRequestScriptCheckbox"));
        preRequestScriptPathTextField = createScriptPathTextField(preRequestScriptPanel, "js", RestBundle.message("toolkit.config.request.request.common.tooltip.selectjsfile"));
        postRequestScriptPathTextField = createScriptPathTextField(postRequestScriptPanel, "js", RestBundle.message("toolkit.config.request.request.common.tooltip.selectjsfile"));

        httpPanel.setBorder(IdeBorderFactory.createTitledBorder(RestBundle.message("toolkit.config.request.request.http.title"), false));
        requestTimeoutLabel.setText(RestBundle.message("toolkit.config.request.request.http.requestTimeoutLabel"));
        downloadDirectoryLabel.setText(RestBundle.message("toolkit.config.request.request.http.downloadDirectoryLabel"));
        defaultDateFormatLabel.setText(RestBundle.message("toolkit.config.request.request.http.defaultDateFormatLabel"));
        generateMultilineCurlSnippetCheckBox.setText(RestBundle.message("toolkit.config.request.request.http.generateMultilineCurlSnippetCheckBox"));
        supportMinifyBodyJsonCheckBox.setText(RestBundle.message("toolkit.config.request.request.http.supportMinifyBodyJsonCheckBox"));
        supportForWslPathCheckBox.setText(RestBundle.message("toolkit.config.request.request.http.supportForWslPathCheckBox"));
        supportForWslPathCheckBox.setVisible(SystemInfo.isWindows);
        initDownloadDirectoryTextField();

        environmentPanel.setBorder(IdeBorderFactory.createTitledBorder(RestBundle.message("toolkit.config.request.environment.title"), false));
        addEnvironmentBtn.setText(RestBundle.message("toolkit.common.btn.add"));
        copyEnvironmentBtn.setText(RestBundle.message("toolkit.common.btn.copy"));
        renameEnvironmentBtn.setText(RestBundle.message("toolkit.common.btn.rename"));
        deleteEnvironmentBtn.setText(RestBundle.message("toolkit.common.btn.delete"));
        scriptBtn.setText(RestBundle.message("toolkit.config.request.environment.scriptBtn"));

        commonHeaderPanel.setBorder(IdeBorderFactory.createTitledBorder(RestBundle.message("toolkit.config.request.header.title"), false));
        commonHeaderPanel.add(createToolbarPanel(headerModel),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
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
                String filePath = FileUtils.getLocationRelativeToUserHome(file.getPath());
                downloadDirectoryTextField.setText(filePath);
                setTextFieldToolTip(downloadDirectoryTextField, RestBundle.message("toolkit.config.request.request.http.tooltip.selectdowndir"), filePath);
            }
        });
//        setTextFieldToolTip(downloadDirectoryTextField, "Select download directory.", null);
        downloadDirectoryPanel.add(downloadDirectoryTextField, BorderLayout.CENTER);
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
                    String filePath = FileUtils.getLocationRelativeToUserHome(files[0].getPath());
                    scriptPathTextField.setText(filePath);
                    setTextFieldToolTip(scriptPathTextField, toolTip, filePath);
                }
            }
        });
//        setTextFieldToolTip(scriptPathTextField, toolTip, null);
        panel.add(scriptPathTextField, BorderLayout.CENTER);
        return scriptPathTextField;
    }

    private void setTextFieldToolTip(TextFieldWithBrowseButton textField, String tip, String path) {
        if (StringUtils.startsWith(path, "~")) {
            tip = RestBundle.message("toolkit.config.request.request.common.tooltip.absolutepath", FileUtils.expandUserHome(path));
        }
        textField.getTextField().setToolTipText(tip);
    }

    private void initEvent() {
        projectComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Object item = e.getItem();
                projectCallback.accept(item.toString());
            }
        });
        exportBtn.addActionListener((e -> {
            ExportWrapper exportWrapper = new ExportWrapper(getModifiedSetting(), getModifiedEnvironment(), getModifiedHeader());
            CopyPasteManager.getInstance().setContents(new StringSelection(JsonUtils.toJson(exportWrapper)));
            NotifierUtils.infoBalloon(RestBundle.message("toolkit.config.request.export.title"), RestBundle.message("toolkit.config.request.export.content"), null, project);
        }));
        importBtn.addActionListener((e -> {
            Transferable contents = CopyPasteManager.getInstance().getContents();
            if (contents == null) {
                return;
            }
            try {
                String data = contents.getTransferData(DataFlavor.stringFlavor).toString();
                if (StringUtils.isEmpty(data) || !StringUtils.startsWith(data, "{") || !StringUtils.endsWith(data, "}")) {
                    NotifierUtils.errorBalloon(RestBundle.message("toolkit.config.request.import.title"), RestBundle.message("toolkit.config.request.import.content", "format error"), project);
                    return;
                }
                ExportWrapper exportWrapper = JsonUtils.fromJson(data, ExportWrapper.class);
                if (exportWrapper != null) {
                    int yesNoDialog = CompactHelper.showYesNoDialog(project, RestBundle.message("toolkit.config.request.import.tip.content"), RestBundle.message("toolkit.config.request.import.tip.title"), null);
                    if (Messages.YES != yesNoDialog) {
                        return;
                    }
                    reset(exportWrapper.getSetting());
                    reset(exportWrapper.getEnvironment());
                    reset(exportWrapper.getHeader());
                } else {
                    NotifierUtils.errorBalloon(RestBundle.message("toolkit.config.request.import.title"), RestBundle.message("toolkit.config.request.import.content", "format error"), project);
                }
            } catch (Exception ex) {
                NotifierUtils.errorBalloon(RestBundle.message("toolkit.config.request.import.title"), RestBundle.message("toolkit.config.request.import.content", ex.toString()), project);
            }
        }));


        final String PRE_REQUEST_SCRIPT =
                "// You can use custom preset object request and environment, attributes are:\n" +
                        "// \n" +
                        "// request attributes\n" +
                        "// url:         java.lang.String,               request url, can be modified by pre-request script.\n" +
                        "// method:      java.lang.String,               request method, can be modified by pre-request script.\n" +
                        "// config:      java.util.Map<String, String>,  request config, can be modified by pre-request script.\n" +
                        "// headers:     java.util.Map<String, String>,  request headers, can be modified by pre-request script.\n" +
                        "// params:      java.util.Map<String, String>,  request params, can be modified by pre-request script.\n" +
                        "// body:        java.lang.String,               request body, can be modified by pre-request script.\n" +
                        "//\n" +
                        "// environment: java.util.Map<String, String>,  current environment, can be modified by pre-request script.\n" +
                        "//\n" +
                        "// See more usages in nashorn-users-guide: https://docs.oracle.com/en/java/javase/12/nashorn/nashorn-users-guide.pdf\n" +
                        "\n" +
                        "var env = environment;\n" +
                        "var baseUrl = env.baseUrl;\n" +
                        "var username = env['username'];\n" +
                        "\n" +
                        "var req = request;\n" +
                        "var url = req.url;\n" +
                        "var method = req.method;\n" +
                        "var headers = req.headers;\n" +
                        "var params = req.params;\n" +
                        "var body = req.body;\n" +
                        "\n" +
                        "req.url = 'http://httpbin.org/ip';\n" +
                        "req.method = 'GET';\n" +
                        "req.headers = {\n" +
                        "    'x-auth-ts': '' + new Date().valueOf(),\n" +
                        "    'x-auth-traceid': '83b557cc-366a-4274-8912-078e71216c51',\n" +
                        "};\n" +
                        "req.headers['x-auth-token'] = '70309f2cc6a6462497f824e77baa77f9';\n" +
                        "req.params = { code: 'ABCD' };\n" +
                        "req.params.name = 'JavaNashorn';\n" +
                        "req.body = JSON.stringify({ reqBody: 'Hello world!' });\n" +
                        "\n" +
                        "// modify environment\n" +
                        "env['key'] = 'value123'\n";
        final String POST_REQUEST_SCRIPT =
                "// You can use custom preset object request、response and environment, attributes are:\n" +
                        "//\n" +
                        "// request: see pre-request script comment\n" +
                        "//\n" +
                        "// response attributes\n" +
                        "// original:    org.apache.http.HttpResponse,   original http response, from http-client 4.4.\n" +
                        "// body:        java.lang.String,               response body can be modified by post-request script.\n" +
                        "//\n" +
                        "// environment: java.util.Map<String, String>,  current environment, can be modified by post-request script.\n" +
                        "//\n" +
                        "// See more usages in nashorn-users-guide: https://docs.oracle.com/en/java/javase/12/nashorn/nashorn-users-guide.pdf\n" +
                        "\n" +
                        "var env = environment;\n" +
                        "var baseUrl = env.baseUrl;\n" +
                        "var username = env['username'];\n" +
                        "\n" +
                        "var req = request;\n" +
                        "var resp = response;\n" +
                        "var statusCode = resp.original.getStatusLine().getStatusCode();\n" +
                        "\n" +
                        "if (statusCode != 200) {\n" +
                        "    resp.body = JSON.stringify({ error: 'error occurred!' });\n" +
                        "} else {\n" +
                        "    resp.body = JSON.parse(resp.body).origin;\n" +
                        "    env['success'] = true + '';\n" +
                        "}\n";
        addScriptLabelListener(preRequestScriptCheckbox, preRequestScriptPathTextField, "Pre-request Script.js", PRE_REQUEST_SCRIPT);
        addScriptLabelListener(postRequestScriptCheckbox, postRequestScriptPathTextField, "Post-request Script.js", POST_REQUEST_SCRIPT);

        addEnvironmentBtn.addActionListener(e -> {
            String envName = CompactHelper.showInputDialog(project, RestBundle.message("toolkit.config.request.environment.addEnvironmentBtn.msg"), RestBundle.message("toolkit.config.request.environment.addEnvironmentBtn.title"), null, "", getEnvInputValidator());
            if (StringUtils.isEmpty(envName)) {
                return;
            }
            KeyValueModel defaultTableModel = new KeyValueModel();
            if (project.getName().equals(projectComboBox.getSelectedItem().toString())) {
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    ApplicationManager.getApplication().runReadAction(() -> {
                        List<String> springContextPath = CommonUtils.getSpringContextPath(project);
                        SwingUtilities.invokeLater(() -> {
                            if (!springContextPath.isEmpty()) {
                                for (int i = 0; i < springContextPath.size(); i++) {
                                    defaultTableModel.addRow(i == 0, HTTP_BASE_URL, springContextPath.get(i));
                                }
                            } else {
                                defaultTableModel.addRow(true, HTTP_BASE_URL, HTTP_BASE_URL_DEFAULT);
                            }
                        });
                    });
                });
            } else {
                defaultTableModel.addRow(true, HTTP_BASE_URL, HTTP_BASE_URL_DEFAULT);
            }

            addEnvTab(envName, defaultTableModel);
        });
        renameEnvironmentBtn.addActionListener(e -> {
            int selectedIndex = envTabbedPanel.getSelectedIndex();
            if (selectedIndex > -1) {
                final String titleAt = envTabbedPanel.getTitleAt(selectedIndex);
                String newEnvName = CompactHelper.showInputDialog(project, RestBundle.message("toolkit.config.request.environment.renameEnvironmentBtn.msg"), RestBundle.message("toolkit.config.request.environment.renameEnvironmentBtn.title"), null, titleAt, getEnvInputValidator());
                if (StringUtils.isEmpty(newEnvName)) {
                    return;
                }
                envTabbedPanel.setTitleAt(selectedIndex, newEnvName);
                envModelList.stream()
                            .filter(envModel -> titleAt.equals(envModel.getEnv())).findFirst()
                            .ifPresent(envModel -> envModel.setEnv(newEnvName));
            }
        });
        copyEnvironmentBtn.addActionListener(e -> {
            if (envTabbedPanel.getSelectedIndex() > -1) {
                final String titleAt = envTabbedPanel.getTitleAt(envTabbedPanel.getSelectedIndex());
                String envName = CompactHelper.showInputDialog(project, RestBundle.message("toolkit.config.request.environment.copyEnvironmentBtn.msg"), RestBundle.message("toolkit.config.request.environment.copyEnvironmentBtn.title"), null, titleAt + "_COPY", getEnvInputValidator());
                if (StringUtils.isEmpty(envName)) {
                    return;
                }
                envModelList.stream().filter(envModel -> titleAt.equals(envModel.getEnv())).findFirst()
                            .ifPresent(envModel -> {
                                final KeyValueModel keyValueModel = new KeyValueModel();
                                for (BKV objects : envModel.getModel().getItems()) {
                                    keyValueModel.addRow(new BKV(objects.getEnabled(), objects.getKey(), objects.getValue()));
                                }
                                addEnvTab(envName, keyValueModel);
                            });
            }
        });
        deleteEnvironmentBtn.addActionListener(e -> {
            if (envTabbedPanel.getTabCount() > 0) {
                String title = envTabbedPanel.getTitleAt(envTabbedPanel.getSelectedIndex());
                int yesNoDialog = CompactHelper.showYesNoDialog(mainPanel, RestBundle.message("toolkit.config.request.environment.deleteEnvironmentBtn.msg", title), RestBundle.message("toolkit.config.request.environment.deleteEnvironmentBtn.title"), null);
                if (Messages.YES == yesNoDialog) {
                    envModelList.removeIf(envModel -> envModel.getEnv().equals(title));
                    envTabbedPanel.remove(envTabbedPanel.getSelectedComponent());
                }
            }
        });
        scriptBtn.addActionListener(e -> new ScriptEditDialog(project, script, s -> this.script = s).show());
    }

    private void addScriptLabelListener(JCheckBox scriptCheckBox, TextFieldWithBrowseButton scriptPathTextField, String scriptName, String defaultContent) {
        scriptCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedProject = projectComboBox.getSelectedItem().toString();
                if (e.getClickCount() == 2 && StringUtils.isEmpty(scriptPathTextField.getText()) && !"Default (Template) Project".equals(selectedProject)) {
                    String scriptPath = FileUtils.getScriptFilePath(selectedProject, scriptName);
                    String relativeToUserHome = FileUtils.getLocationRelativeToUserHome(scriptPath);
                    scriptPathTextField.setText(relativeToUserHome);
                    setTextFieldToolTip(scriptPathTextField, RestBundle.message("toolkit.config.request.request.common.tooltip.selectjsfile"), relativeToUserHome);
                    if (!Files.exists(Paths.get(scriptPath))) {
                        createScript(scriptPath, defaultContent);
                    }
                }
            }
        });
    }

    private void createScript(String scriptPath, String content) {
        IdeaUtils.invokeLater(() -> {
            try {
                Path path = Paths.get(scriptPath);
                Files.createDirectories(path.getParent());
                // TRUNCATE_EXISTING
                Files.write(path, content.getBytes(StandardCharsets.UTF_8), CREATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void addEnvTab(String envName, KeyValueModel keyValueModel) {
        envModelList.add(new EnvModel(envName, keyValueModel));
        envTabbedPanel.addTab(envName, createToolbarPanel(keyValueModel));
        envTabbedPanel.setSelectedIndex(envTabbedPanel.getTabCount() - 1);
    }

    private JPanel createToolbarPanel(KeyValueModel model) {
        JBTable jbTable = new JBTable(model);
        jbTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        jbTable.getColumnModel().getColumn(2).setMaxWidth(90);
        jbTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (column == 2) {
                    Component cmp = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (cmp instanceof JLabel) {
                        ((JLabel) cmp).setHorizontalAlignment(SwingConstants.CENTER);
                        return cmp;
                    }
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        ToolbarDecorator decorationToolbar = ToolbarDecorator.createDecorator(jbTable);
        return decorationToolbar.createPanel();
    }

    public boolean isModified(RequestSetting modifiedSetting) {
        RequestSetting modifiedSetting1 = getModifiedSetting();
        return modifiedSetting1.isSaveRequestLog() != modifiedSetting.isSaveRequestLog()
                || modifiedSetting1.isEnablePreRequestScript() != modifiedSetting.isEnablePreRequestScript()
                || modifiedSetting1.isEnablePostRequestScript() != modifiedSetting.isEnablePostRequestScript()
                || !StringUtils.equals(modifiedSetting1.getPreRequestScriptPath(), modifiedSetting.getPreRequestScriptPath())
                || !StringUtils.equals(modifiedSetting1.getPostRequestScriptPath(), modifiedSetting.getPostRequestScriptPath())
                || modifiedSetting1.getRequestTimeout() != modifiedSetting.getRequestTimeout()
                || modifiedSetting1.isGenerateMultilineCurlSnippet() != modifiedSetting.isGenerateMultilineCurlSnippet()
                || (SystemInfo.isWindows && modifiedSetting1.isSupportForWslPath() != modifiedSetting.isSupportForWslPath())
                || modifiedSetting1.isSupportMinifyJson() != modifiedSetting.isSupportMinifyJson()
                || !StringUtils.equals(modifiedSetting1.getDownloadDirectory(), modifiedSetting.getDownloadDirectory())
                || !StringUtils.equals(modifiedSetting1.getDefaultDateFormat(), StringUtils.defaultIfEmpty(modifiedSetting.getDefaultDateFormat(), "timestamp(ms)"));
    }

    public boolean isModified(Environment environment) {
        String env1 = formatEnvModelList(envModelList);
        List<EnvList> envList = environment.getEnvList();
        String env2 = formatEnvList(envList);
        return envModelList.size() != envList.size()
                || !StringUtils.equals(env1, env2)
                || !StringUtils.equals(StringUtils.defaultString(script), StringUtils.defaultString(environment.getScript()));
    }

    public boolean isModified(CommonHeader commonHeader) {
        if (headerModel.getItems().size() != commonHeader.getGlobalHeaderList().size()) {
            return true;
        }
        String header1 = JsonUtils.toJson(headerModel.getItems());
        String header2 = JsonUtils.toJson(commonHeader.getGlobalHeaderList());
        return !StringUtils.equals(header1, header2);
    }

    public RequestSetting getModifiedSetting() {
        RequestSetting requestSetting = new RequestSetting();
        requestSetting.setProject(ObjectUtils.defaultIfNull(projectComboBox.getSelectedItem(), "").toString());
        requestSetting.setSaveRequestLog(saveRequestLogCheckBox.isSelected());
        requestSetting.setEnablePreRequestScript(preRequestScriptCheckbox.isSelected());
        requestSetting.setEnablePostRequestScript(postRequestScriptCheckbox.isSelected());
        requestSetting.setPreRequestScriptPath(preRequestScriptPathTextField.getTextField().getText().trim());
        requestSetting.setPostRequestScriptPath(postRequestScriptPathTextField.getTextField().getText().trim());

        requestSetting.setRequestTimeout(parseIntOrZero(requestTimeoutField.getText()));
        requestSetting.setGenerateMultilineCurlSnippet(generateMultilineCurlSnippetCheckBox.isSelected());
        requestSetting.setSupportForWslPath(supportForWslPathCheckBox.isSelected());
        requestSetting.setSupportMinifyJson(supportMinifyBodyJsonCheckBox.isSelected());
        requestSetting.setDownloadDirectory(downloadDirectoryTextField.getTextField().getText().trim());
        requestSetting.setDefaultDateFormat((String) defaultDateFormatComboBox.getSelectedItem());
        return requestSetting;
    }

    public Environment getModifiedEnvironment() {
        Environment environment = new Environment();
        environment.setProject(ObjectUtils.defaultIfNull(projectComboBox.getSelectedItem(), "").toString());
        environment.setEnvList(buildEnvList(envModelList));
        environment.setScript(script);
        return environment;
    }

    public CommonHeader getModifiedHeader() {
        CommonHeader commonHeader = new CommonHeader();
        commonHeader.setProject(ObjectUtils.defaultIfNull(projectComboBox.getSelectedItem(), "").toString());
        List<BKV> list = headerModel.getItems().stream()
                                    .map(bkv -> new BKV(bkv.getEnabled(), bkv.getKey(), bkv.getValue()))
                                    .collect(Collectors.toList());
        commonHeader.setGlobalHeaderList(list);
        return commonHeader;
    }

    public void reset(String project) {
        projectComboBox.setSelectedItem(project);
    }

    public void reset(RequestSetting requestSetting) {
        if (requestSetting == null) {
            return;
        }

        saveRequestLogCheckBox.setSelected(requestSetting.isSaveRequestLog());
        preRequestScriptCheckbox.setSelected(requestSetting.isEnablePreRequestScript());
        postRequestScriptCheckbox.setSelected(requestSetting.isEnablePostRequestScript());
        preRequestScriptPathTextField.setText(requestSetting.getPreRequestScriptPath());
        postRequestScriptPathTextField.setText(requestSetting.getPostRequestScriptPath());

        requestTimeoutField.setText(String.valueOf(requestSetting.getRequestTimeout()));
        generateMultilineCurlSnippetCheckBox.setSelected(requestSetting.isGenerateMultilineCurlSnippet());
        supportForWslPathCheckBox.setSelected(requestSetting.isSupportForWslPath());
        supportMinifyBodyJsonCheckBox.setSelected(requestSetting.isSupportMinifyJson());
        downloadDirectoryTextField.setText(requestSetting.getDownloadDirectory());

        List<String> dateFormatList = Stream.of("timestamp(ms)", "timestamp(s)", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss").collect(Collectors.toList());
        String df = StringUtils.defaultIfEmpty(requestSetting.getDefaultDateFormat(), "timestamp(ms)");
        if (!dateFormatList.contains(df)) {
            dateFormatList.add(0, df);
        }
        defaultDateFormatComboBox.setModel(new CollectionComboBoxModel<>(dateFormatList));
        defaultDateFormatComboBox.setSelectedItem(df);

        // setTextFieldToolTip
        setTextFieldToolTip(preRequestScriptPathTextField, RestBundle.message("toolkit.config.request.request.common.tooltip.selectjsfile"), StringUtils.defaultString(requestSetting.getPreRequestScriptPath()));
        setTextFieldToolTip(postRequestScriptPathTextField, RestBundle.message("toolkit.config.request.request.common.tooltip.selectjsfile"), StringUtils.defaultString(requestSetting.getPostRequestScriptPath()));
        setTextFieldToolTip(downloadDirectoryTextField, RestBundle.message("toolkit.config.request.request.http.tooltip.selectdowndir"), StringUtils.defaultString(requestSetting.getDownloadDirectory()));
    }

    public void checkInput() throws ConfigurationException {
        if (StringUtils.isNotEmpty(requestTimeoutField.getText())) {
            try {
                Integer.parseInt(requestTimeoutField.getText());
            } catch (Exception e) {
                throw new ConfigurationException(e.getMessage(), "Check Number Format: " + requestTimeoutField.getText());
            }
        }
        String dateFormatV = (String) defaultDateFormatComboBox.getSelectedItem();
        if (StringUtils.isNotEmpty(dateFormatV) && !"timestamp(ms)".equals(dateFormatV) && !"timestamp(s)".equals(dateFormatV)) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatV);
                dateFormat.format(new Date());
            } catch (Exception e) {
                throw new ConfigurationException(e.getMessage(), "Check Date Format: " + dateFormatV);
            }
        }
    }

    public void reset(Environment environment) {
        if (environment == null) {
            return;
        }
        this.script = environment.getScript();

        if (environment.getEnvList() == null) {
            return;
        }
        this.envModelList.clear();
        this.envTabbedPanel.removeAll();

        for (EnvList env : environment.getEnvList()) {
            KeyValueModel model = new KeyValueModel();
            if (env.getItems() != null) {
                for (BKV item : env.getItems()) {
                    model.addRow(item.getEnabled(), item.getKey(), item.getValue());
                }
            }
            this.envModelList.add(new EnvModel(env.getEnv(), model));
            envTabbedPanel.addTab(env.getEnv(), createToolbarPanel(model));
        }
    }

    public void reset(CommonHeader commonHeader) {
        if (commonHeader == null || commonHeader.getGlobalHeaderList() == null) {
            return;
        }
        List<BKV> list = commonHeader.getGlobalHeaderList().stream()
                                     .map(bkv -> new BKV(bkv.getEnabled(), bkv.getKey(), bkv.getValue()))
                                     .collect(Collectors.toList());
        this.headerModel.setItems(list);
    }

    public void selectProjectTab(String selectProject, int tabIndex) {
        tabbedPanel.setSelectedIndex(tabIndex);

        if (((CollectionComboBoxModel<String>) projectComboBox.getModel()).getItems().contains(selectProject)) {
            if (RestRegistry.debugMode()) {
                NotifierUtils.infoBalloon("", "selectProject: " + selectProject, null, project);
            }
            if (selectProject.equals(projectComboBox.getSelectedItem())) {
                projectCallback.accept(projectComboBox.getSelectedItem().toString());
            } else {
                projectComboBox.setSelectedItem(selectProject);
            }
        }
    }

    private InputValidator getEnvInputValidator() {
        return new InputValidator() {
            @Override
            public boolean checkInput(String inputString) {
                return StringUtils.isNotBlank(inputString) && envModelList.stream().noneMatch(e -> inputString.equalsIgnoreCase(e.getEnv()));
            }

            @Override
            public boolean canClose(String inputString) {
                return true;
            }
        };
    }

    private String formatEnvModelList(List<EnvModel> envModelList) {
        StringBuilder sb = new StringBuilder();
        for (EnvModel envModel : envModelList) {
            sb.append(envModel.getEnv());
            for (BKV objects : envModel.getModel().getItems()) {
                sb.append(objects.getEnabled()).append(objects.getKey()).append(objects.getValue());
            }
        }
        return sb.toString();
    }

    private String formatEnvList(List<EnvList> envList) {
        StringBuilder sb = new StringBuilder();
        for (EnvList list : envList) {
            sb.append(list.getEnv());
            for (BKV item : list.getItems()) {
                sb.append(item.getEnabled()).append(item.getKey()).append(item.getValue());
            }
        }
        return sb.toString();
    }

    private List<EnvList> buildEnvList(List<EnvModel> envModelList) {
        List<EnvList> list = new ArrayList<>();
        for (EnvModel envModel : envModelList) {
            List<BKV> items = new ArrayList<>();
            for (BKV objects : envModel.getModel().getItems()) {
                items.add(new BKV(objects.getEnabled(), objects.getKey(), objects.getValue()));
            }
            list.add(new EnvList(envModel.getEnv(), items));
        }
        return list;
    }

    private int parseIntOrZero(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
