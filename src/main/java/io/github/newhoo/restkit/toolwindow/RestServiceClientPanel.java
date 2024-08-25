package io.github.newhoo.restkit.toolwindow;

import com.intellij.ide.DataManager;
import com.intellij.json.JsonLanguage;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.AppUIUtil;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.PopupMenuListenerAdapter;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.Request;
import io.github.newhoo.restkit.common.RequestInfo;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestClientData;
import io.github.newhoo.restkit.common.RestClientEditorInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.RestRegistry;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.config.RequestSetting;
import io.github.newhoo.restkit.config.request.RequestConfigurable;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RestClient;
import io.github.newhoo.restkit.restful.http.HttpClient;
import io.github.newhoo.restkit.util.EnvironmentUtils;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import io.github.newhoo.restkit.util.ScriptUtils;
import io.github.newhoo.restkit.util.ToolkitUtil;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.EDITOR_FILENAME_PREFIX;
import static io.github.newhoo.restkit.common.RestConstant.PLACEHOLDER_URL;
import static io.github.newhoo.restkit.common.RestConstant.PROTOCOL;
import static io.github.newhoo.restkit.common.RestConstant.PROTOCOL_HTTP;
import static io.github.newhoo.restkit.util.IdeaUtils.createEditor;
import static io.github.newhoo.restkit.util.IdeaUtils.getEditorText;
import static io.github.newhoo.restkit.util.IdeaUtils.setEditorText;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * RestServiceClient
 *
 * @since 1.0
 */
public class RestServiceClientPanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(RestServiceClientPanel.class);
    private final Project project;
    @Getter
    private String currentProject;
    private Environment environment = new Environment();

    private JComboBox<String> requestEnv;
    private JComboBox<HttpMethod> requestMethod;
    private JTextField requestUrl;
    private JButton sendButton;

    private JTabbedPane tabbedPane;
    private FileEditor requestConfigEditor;
    private FileEditor requestHeaderEditor;
    private FileEditor requestParamEditor;
    private FileEditor requestBodyEditor;
    private FileEditor responseEditor;
    private FileEditor infoEditor;

    public RestServiceClientPanel(Project project, boolean loadDefaultEnv) {
        this.project = project;

        initUI();
        initEvent();

        if (loadDefaultEnv) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> setEnv(project.getName()));
        }
    }

    private void initEvent() {
        bindSendButtonActionListener();
        bindEnvActionListener();
    }

    public void handleParameterUpdate(String editor, String content) {
        String titleAt = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        switch (titleAt) {
            case "Config":
                setEditorText(requestConfigEditor, content, project);
                break;
            case "Headers":
                setEditorText(requestHeaderEditor, content, project);
                break;
            case "Params":
                setEditorText(requestParamEditor, content, project);
                break;
            case "Body":
                setEditorText(requestBodyEditor, content, project);
                break;
            case "Response":
                setEditorText(responseEditor, content, project);
                break;
            case "Info":
                setEditorText(infoEditor, content, project);
                break;
            default:
        }
    }

    private void initUI() {
        JPanel urlPanel = new JBPanel<>();
        urlPanel.setLayout(new GridLayoutManager(1, 4, JBUI.insets(UIUtil.PANEL_SMALL_INSETS.top, 4, 0, 4), 0, 0));

        requestEnv = new ComboBox<>(new CollectionComboBoxModel<>(environment.getEnvKeys()));
        requestEnv.setPrototypeDisplayValue(HttpMethod.GET.name());
        requestEnv.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value != null) {
                    list.setToolTipText(getEnvToolTip(value.toString(), value.toString()));
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        requestEnv.setToolTipText(getEnvToolTip(this.currentProject, (String) requestEnv.getSelectedItem()));
        urlPanel.add(requestEnv,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        requestMethod = new ComboBox<>(new EnumComboBoxModel<>(HttpMethod.class));
        requestMethod.setPrototypeDisplayValue(HttpMethod.GET);
        urlPanel.add(requestMethod,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        requestUrl = new JBTextField(PLACEHOLDER_URL);
        urlPanel.add(requestUrl,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));

        sendButton = new JButton("Send");
        urlPanel.add(sendButton,
                new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        tabbedPane = new JBTabbedPane();

        AppUIUtil.invokeOnEdt(() -> {
            requestConfigEditor = createEditor(EDITOR_FILENAME_PREFIX + "Config", PlainTextLanguage.INSTANCE, null, "RESTKit.EditorContextMenu.ConfigTab", project);
            tabbedPane.addTab("Config", requestConfigEditor.getComponent());
            requestConfigEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
            // 默认显示
            setConfig(Collections.singletonList(new KV(PROTOCOL, PROTOCOL_HTTP)));
        });
        AppUIUtil.invokeOnEdt(() -> {
            requestHeaderEditor = createEditor(EDITOR_FILENAME_PREFIX + "Headers", PlainTextLanguage.INSTANCE, null, "RESTKit.EditorContextMenu.HeadersTab", project);
            tabbedPane.addTab("Headers", requestHeaderEditor.getComponent());
            requestHeaderEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });
        AppUIUtil.invokeOnEdt(() -> {
            requestParamEditor = createEditor(EDITOR_FILENAME_PREFIX + "Params", PlainTextLanguage.INSTANCE, null, "RESTKit.EditorContextMenu.ParamsTab", project);
            tabbedPane.addTab("Params", requestParamEditor.getComponent());
            requestParamEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });
        AppUIUtil.invokeOnEdt(() -> {
            requestBodyEditor = createEditor(EDITOR_FILENAME_PREFIX + "Body", JsonLanguage.INSTANCE, null, "RESTKit.EditorContextMenu.BodyTab", project);
            tabbedPane.addTab("Body", requestBodyEditor.getComponent());
            requestBodyEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });
        AppUIUtil.invokeOnEdt(() -> {
            responseEditor = createEditor(EDITOR_FILENAME_PREFIX + "Response", JsonLanguage.INSTANCE, null, "RESTKit.EditorContextMenu.ResponseTab", project);
            tabbedPane.addTab("Response", responseEditor.getComponent());
            responseEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });
        AppUIUtil.invokeOnEdt(() -> {
            infoEditor = createEditor(EDITOR_FILENAME_PREFIX + "Info", PlainTextLanguage.INSTANCE, null, "RESTKit.EditorContextMenu", project);
            tabbedPane.addTab("Info", infoEditor.getComponent());
            infoEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });

        this.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new GridLayoutManager(2, 1));
        this.add(urlPanel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
        this.add(tabbedPane,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                        null, null, null));
    }

    public void destroy() {
        this.currentProject = null;
        this.environment = null;
        this.requestEnv = null;
        this.requestMethod = null;
        this.requestUrl = null;
        this.tabbedPane = null;
        this.sendButton = null;
        this.requestConfigEditor = null;
        this.requestHeaderEditor = null;
        this.requestParamEditor = null;
        this.requestBodyEditor = null;
        this.responseEditor = null;
        this.infoEditor = null;
    }

    private void bindEnvActionListener() {
        requestEnv.addItemListener(e -> {
            environment.setCurrentEnv(e.getItem().toString());
            requestEnv.setToolTipText(getEnvToolTip(this.currentProject, (String) requestEnv.getSelectedItem()));
        });
        requestEnv.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (requestEnv.getItemCount() <= 0) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, RequestConfigurable.class, c -> c.selectProjectTab(environment.getProject(), 1));
                }
            }
        });
        requestEnv.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2 && e.getClickCount() == 2) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, RequestConfigurable.class, c -> c.selectProjectTab(environment.getProject(), 1));
                }
                if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 2) {
                    setEnv(currentProject);
                }
            }
        });
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                    RestClientEditorInfo restClientEditorInfo = (RestClientEditorInfo) getData(RestDataKey.CLIENT_EDITOR_INFO.getName());
                    DefaultActionGroup generateActionGroup = new DefaultActionGroup(
                            new BaseAnAction(() -> RestBundle.message("toolkit.toolwindow.client.parameter.export.action.text")) {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e) {
                                    List<KV> parameters = DataSourceHelper.getDataSource().selectParameter(restClientEditorInfo, project);
                                    if (CollectionUtils.isEmpty(parameters)) {
                                        NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.client.parameter.export.empty.msg", title), null, project);
                                        return;
                                    }
                                    IdeaUtils.copyToClipboard(JsonUtils.toJson(parameters));
                                    NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.client.parameter.export.success.msg", title), null, project);
                                }
                            },
                            new BaseAnAction(() -> RestBundle.message("toolkit.toolwindow.client.parameter.import.action.text")) {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e) {
                                    Transferable contents = CopyPasteManager.getInstance().getContents();
                                    if (contents == null) {
                                        return;
                                    }
                                    try {
                                        String data = contents.getTransferData(DataFlavor.stringFlavor).toString();
                                        if (StringUtils.isEmpty(data) || !StringUtils.startsWith(data, "[") || !StringUtils.endsWith(data, "]")) {
                                            NotifierUtils.errorBalloon(RestBundle.message("toolkit.toolwindow.client.parameter.import.empty.title"), RestBundle.message("toolkit.toolwindow.client.parameter.import.empty.msg"), project);
                                            return;
                                        }
                                        List<KV> kvs = JsonUtils.fromJsonArr(data, KV.class);
                                        if (CollectionUtils.isEmpty(kvs) || kvs.stream().anyMatch(o -> StringUtils.isAnyEmpty(o.getKey(), o.getValue()))) {
                                            NotifierUtils.errorBalloon(RestBundle.message("toolkit.toolwindow.client.parameter.import.empty.title"), RestBundle.message("toolkit.toolwindow.client.parameter.import.empty.msg"), project);
                                            return;
                                        }
                                        kvs = new ArrayList<>(kvs);
                                        List<KV> parameters = DataSourceHelper.getDataSource().selectParameter(restClientEditorInfo, project);
                                        kvs.addAll(parameters);
                                        DataSourceHelper.getDataSource().syncParameter(restClientEditorInfo, kvs, project);
                                        NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.client.parameter.import.success.msg", title), null, project);
                                    } catch (Exception ex) {
                                        NotifierUtils.errorBalloon(RestBundle.message("toolkit.toolwindow.client.parameter.import.empty.title"), RestBundle.message("toolkit.toolwindow.client.parameter.import.error.msg") + ex.toString(), project);
                                    }
                                }
                            });

                    DataContext dataContext = DataManager.getInstance().getDataContext(tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex()));
                    final ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(null, generateActionGroup, dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);
                    popup.showInBestPositionFor(dataContext);
                }
            }
        });
    }

    public void setEnv(String currentProject) {
        this.currentProject = currentProject;
        this.environment = DataSourceHelper.getDataSource().selectEnvironment(currentProject, project);
        if (RestRegistry.debugMode()) {
            NotifierUtils.infoBalloon("Env: " + currentProject, JsonUtils.toJson(environment), null, project);
        }

//        String lastSelectedEnv = this.environment.getCurrentEnv();
        String lastSelectedEnv = StringUtils.defaultString((String) requestEnv.getSelectedItem());
        List<String> envKeys = environment.getEnvKeys();
        requestEnv.setModel(new CollectionComboBoxModel<>(envKeys));
        if (envKeys.contains(lastSelectedEnv)) {
            requestEnv.setSelectedItem(lastSelectedEnv);
        } else if (!envKeys.isEmpty()) {
            requestEnv.setSelectedItem(environment.getCurrentEnv());
        }
        requestEnv.setToolTipText(getEnvToolTip(this.currentProject, (String) requestEnv.getSelectedItem()));
    }

    private String getEnvToolTip(String title, String envName) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(title)) {
            sb.append("[").append(title).append("]").append("<br/>");
        }
        Map<String, String> currentEnabledEnvMap = environment.getEnabledEnvMap(envName);
        for (Map.Entry<String, String> entry : currentEnabledEnvMap.entrySet()) {
            String k = entry.getKey();
            if (StringUtils.isNotBlank(k)) {
                String v = entry.getValue();
                String kk = k + ": ";
                if (StringUtils.length(kk + v) > 100) {
                    sb.append(k).append(": ").append(kk + v, kk.length(), 96).append("...").append("<br/>");
                } else {
                    sb.append(k).append(": ").append(v).append("<br/>");
                }
            }
        }
        if (currentEnabledEnvMap.isEmpty()) {
            sb.append("No Environment");
        }
        return "<html><body><pre>" + sb + "</pre></body></html>";
    }

    private void bindSendButtonActionListener() {
        sendButton.addActionListener(e -> {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, RestBundle.message("toolkit.toolwindow.client.editor.send.bgmsg")) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    sendButton.setEnabled(false);
                    setResponse("Loading...");
                    try {
                        if (PLACEHOLDER_URL.equals(requestUrl.getText())) {
                            setResponse(RestBundle.message("toolkit.toolwindow.client.editor.send.urlnotspecific"));
                            AppUIUtil.invokeOnEdt(() -> tabbedPane.setSelectedIndex(4));
                            requestUrl.requestFocus();
                            return;
                        }
                        String currentEnv = (String) requestEnv.getSelectedItem();
                        Map<String, String> currentEnvMap = environment.getEnabledEnvMap(currentEnv);
                        Map<String, Method> scriptMethodMap = environment.getScriptMethodMap();

                        // config
                        Map<String, String> configMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(getEditorText(requestConfigEditor), currentEnvMap, scriptMethodMap));
                        // protocol
                        String protocol = StringUtils.defaultIfEmpty(configMap.get(PROTOCOL), PROTOCOL_HTTP);
                        if (PROTOCOL_HTTP.equals(protocol) && HttpMethod.UNDEFINED == requestMethod.getSelectedItem()) {
                            setResponse(RestBundle.message("toolkit.toolwindow.client.editor.send.methodnotspecific"));
                            AppUIUtil.invokeOnEdt(() -> tabbedPane.setSelectedIndex(4));
                            requestMethod.requestFocus();
                            return;
                        }

                        // url
                        String url = EnvironmentUtils.handlePlaceholderVariable(requestUrl.getText(), currentEnvMap, scriptMethodMap);
                        // http method
                        HttpMethod method = (HttpMethod) Objects.requireNonNull(requestMethod.getSelectedItem());
                        // header
                        Map<String, String> headerMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(getEditorText(requestHeaderEditor), currentEnvMap, scriptMethodMap));
                        // param
                        Map<String, String> paramMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(getEditorText(requestParamEditor), currentEnvMap, scriptMethodMap));
                        // body
                        String reqBody = EnvironmentUtils.handlePlaceholderVariable(getEditorText(requestBodyEditor), currentEnvMap, scriptMethodMap);

                        RestClient restClient = RequestHelper.getRestClient(protocol, () -> {
                            List<String> collect = RequestHelper.getRestClient().stream().map(RestClient::getProtocol).collect(Collectors.toList());
                            NotifierUtils.warnBalloon(RestBundle.message("toolkit.toolwindow.client.editor.send.notsupportedprotocol.title", protocol), RestBundle.message("toolkit.toolwindow.client.editor.send.notsupportedprotocol.msg", collect), project);
                            return new HttpClient();
                        });

                        RestClientData clientData = new RestClientData();
                        clientData.setProject(currentProject);
                        clientData.setUrl(url);
                        clientData.setMethod(method.name());
                        clientData.setConfig(configMap);
                        clientData.setHeaders(headerMap);
                        clientData.setParams(paramMap);
                        clientData.setBody(reqBody);

                        // send request
                        RequestInfo response = sendRequest(clientData, restClient);

                        // response
                        setResponse(response.getResponseBody());

                        // info
                        setHttpInfo(restClient.formatResponseInfo(response));

                        AppUIUtil.invokeOnEdt(() -> tabbedPane.setSelectedIndex(4));
                    } catch (Throwable t) {
                        LOG.error(RestBundle.message("toolkit.toolwindow.client.editor.send.error.log") + t);
                    } finally {
                        sendButton.setEnabled(true);
                    }
                }

                @Override
                public void onCancel() {
                    sendButton.setEnabled(true);
                }
            });
        });
    }

    private RequestInfo sendRequest(RestClientData restClientData, RestClient restClient) {
        // 不同的请求端参数是不一样的
        Request request = restClient.createRequest(restClientData, project);
        request.setClient(restClient);

        RequestSetting setting = DataSourceHelper.getDataSource().selectRequestSetting(restClientData.getProject(), project);

        // Pre-request Script
        try {
            ScriptUtils.handlePreRequestScript(request, setting, (String) requestEnv.getSelectedItem(), environment, project);
        } catch (Exception e) {
            e.printStackTrace();
            return new RequestInfo(request, RestBundle.message("toolkit.toolwindow.client.editor.send.error.prescript") + ": \n\n" + e.toString());
        }

        RequestInfo requestInfo = restClient.sendRequest(request, project);

        // log request
        logRequestInfo(requestInfo, setting);

        // Post-request Script
        if (requestInfo.getResponse() != null) {
            try {
                ScriptUtils.handlePostRequestScript(request, requestInfo.getResponse(), setting, (String) requestEnv.getSelectedItem(), environment, project);
            } catch (Exception e) {
                e.printStackTrace();
                requestInfo.setErrMsg(RestBundle.message("toolkit.toolwindow.client.editor.send.error.postscript") + ": \n\n" + e.toString());
            }
        }
        return requestInfo;
    }

    /**
     * 显示服务详情，url
     */
    public void showServiceDetail(RestItem restItem) {
        if (!restItem.isValid()) {
            // try refresh service
            LOG.warn("PsiElement is invalid: " + restItem);
            NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.tree.iteminvalid"), new NotificationAction(() -> RestBundle.message("toolkit.common.btn.refresh")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    ToolWindowHelper.scheduleUpdateTree(project);
                    notification.expire();
                }
            }, project);
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            // 切换环境变量
            if (!StringUtils.equals(currentProject, restItem.getProject())) {
                setEnv(restItem.getProject());
            }
        });
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String url = StringUtils.defaultString(restItem.getUrl());
            HttpMethod method = ObjectUtils.defaultIfNull(restItem.getMethod(), HttpMethod.UNDEFINED);
            ApplicationManager.getApplication().runReadAction(() -> {
                List<KV> requestConfig = new ArrayList<>();
                requestConfig.add(new KV(PROTOCOL, restItem.getProtocol()));
                RestClient restClient = RequestHelper.getRestClient(restItem.getProtocol(), () -> null);
                if (restClient != null) {
                    requestConfig.addAll(restClient.getConfig(restItem, project));
                }
                List<KV> requestHeaders = restItem.getHeaders();
                List<KV> requestParams = restItem.getParams();
                String requestBodyJson = restItem.getBodyJson();

                // 在UI展示
                AppUIUtil.invokeOnEdt(() -> {
                    showRequest(url, method, requestConfig, requestHeaders, requestParams, requestBodyJson);
                });
            });
        });
    }

    private void showRequest(String url, HttpMethod method, List<KV> config, List<KV> headers, List<KV> params, String body) {
        if ((CollectionUtils.isEmpty(params) && StringUtils.isNotEmpty(body))) {
            tabbedPane.setSelectedIndex(3);
        } else {
            tabbedPane.setSelectedIndex(2);
        }

        requestUrl.setText(url);
        requestUrl.setToolTipText(url);
        requestMethod.setSelectedItem(method);
        setConfig(config);
        setHeader(headers);
        setParams(params);
        setReqBody(body);
        setResponse(StringUtils.EMPTY);
        setHttpInfo(StringUtils.EMPTY);
    }

    private void setHeader(List<KV> reqHeader) {
        List<KV> globalHeaderList = DataSourceHelper.getDataSource().selectHeader(currentProject, project).getEnabledGlobalHeader();
        Set<String> global = globalHeaderList.stream().map(KV::getUniqueKey).collect(Collectors.toSet());
        List<KV> headers = new ArrayList<>(globalHeaderList);

        for (KV header : reqHeader) {
            if (!global.contains(header.getUniqueKey())) {
                headers.add(header);
            }
        }

        String headerStr = headers.stream()
                                  .map(kv -> kv.getKey() + ": " + kv.getValue())
                                  .collect(Collectors.joining("\n"));

        setEditorText(requestHeaderEditor, headerStr, project);
    }

    private void setConfig(List<KV> requestConfig) {
        String paramStr = requestConfig.stream()
                                       .map(kv -> kv.getKey() + ": " + kv.getValue())
                                       .collect(Collectors.joining("\n"));
        setEditorText(requestConfigEditor, paramStr, project);
    }

    private void setParams(List<KV> requestParams) {
        String paramStr = requestParams.stream()
                                       .map(kv -> kv.getKey() + ": " + kv.getValue())
                                       .collect(Collectors.joining("\n"));
        setEditorText(requestParamEditor, paramStr, project);
    }

    private void setReqBody(String reqBody) {
        setEditorText(requestBodyEditor, reqBody, project);
    }

    private void setResponse(String respText) {
        setEditorText(responseEditor, JsonUtils.format(respText), project);
    }

    private void setHttpInfo(String httpInfo) {
        setEditorText(infoEditor, httpInfo, project);
    }

    private void logRequestInfo(RequestInfo requestInfo, RequestSetting setting) {
        if (setting.isSaveRequestLog() && requestInfo.getRequest().getClient() != null) {
            String log = requestInfo.getRequest().getClient().formatLogInfo(requestInfo);

            IdeaUtils.invokeLater(() -> {
                try {
                    Files.createDirectories(Paths.get(FileUtils.getLogDirectory()));
                    Files.write(Paths.get(FileUtils.getTodayLog()), log.getBytes(StandardCharsets.UTF_8), CREATE, APPEND);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Nullable
    public Object getData(@NotNull @NonNls String dataId) {
        if (RestDataKey.CLIENT_EDITOR_INFO.is(dataId)) {
            String requestUrl = this.requestUrl.getText();
            if (StringUtils.isEmpty(requestUrl)) {
                return null;
            }
            RestClientEditorInfo restClientEditorInfo = new RestClientEditorInfo();
            restClientEditorInfo.setProject(currentProject);
            restClientEditorInfo.setMethod(String.valueOf(requestMethod.getSelectedItem()));
            restClientEditorInfo.setUrl(requestUrl);
            restClientEditorInfo.setEditor(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
            return restClientEditorInfo;
        }
        if (RestDataKey.CLIENT_API_INFO.is(dataId)) {
            RestClientApiInfo restClientApiInfo = new RestClientApiInfo();
            restClientApiInfo.setProject(currentProject);
            restClientApiInfo.setCurrentEnv((String) requestEnv.getSelectedItem());
            restClientApiInfo.setUrl(StringUtils.defaultString(requestUrl.getText()));
            restClientApiInfo.setMethod((HttpMethod) Objects.requireNonNull(requestMethod.getSelectedItem()));
            restClientApiInfo.setHeaders(getEditorText(requestHeaderEditor));
            restClientApiInfo.setParams(getEditorText(requestParamEditor));
            restClientApiInfo.setBodyJson(getEditorText(requestBodyEditor));
            restClientApiInfo.setConfig(getEditorText(requestConfigEditor));
            return restClientApiInfo;
        }
        return null;
    }
}