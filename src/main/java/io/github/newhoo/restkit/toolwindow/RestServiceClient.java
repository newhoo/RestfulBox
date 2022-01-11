package io.github.newhoo.restkit.toolwindow;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
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
import io.github.newhoo.restkit.common.HttpInfo;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestClientEditorInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.config.EnvironmentConfigurable;
import io.github.newhoo.restkit.parameter.library.RestParameterListener;
import io.github.newhoo.restkit.util.EnvironmentUtils;
import io.github.newhoo.restkit.util.HttpUtils;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.ToolkitUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.PLACEHOLDER_BASE_URL;
import static io.github.newhoo.restkit.common.RestConstant.PLACEHOLDER_URL;
import static io.github.newhoo.restkit.config.SettingListener.ENV_UPDATE;
import static io.github.newhoo.restkit.parameter.library.RestParameterListener.REST_PARAMETER_UPDATE;
import static io.github.newhoo.restkit.toolwindow.RestServiceListener.REST_SERVICE_SELECT;
import static io.github.newhoo.restkit.util.IdeaUtils.createEditor;
import static io.github.newhoo.restkit.util.IdeaUtils.getEditorText;
import static io.github.newhoo.restkit.util.IdeaUtils.setEditorText;

/**
 * RestServiceClient
 *
 * @since 1.0
 */
public class RestServiceClient extends JPanel implements DataProvider {

    public static final Logger LOG = Logger.getInstance(RestServiceClient.class);
    private final Project project;
    private final Environment environment;

    private JComboBox<String> requestEnv;
    private JComboBox<HttpMethod> requestMethod;
    private JTextField requestUrl;
    private JButton sendButton;

    private JTabbedPane tabbedPane;
    private FileEditor requestHeaderEditor;
    private FileEditor requestParamEditor;
    private FileEditor requestBodyEditor;
    private FileEditor responseEditor;
    private FileEditor infoEditor;

    public RestServiceClient(Project project) {
        this.project = project;
        this.environment = Environment.getInstance(project);

        initUI();
        initEvent();

        project.getMessageBus().connect(project).subscribe(ENV_UPDATE, () -> {
            requestEnv.setModel(new CollectionComboBoxModel<>(environment.getEnvKeys()));
            requestEnv.setSelectedItem(environment.getCurrentEnv());
            setEnvToolTip();
        });

        project.getMessageBus().connect(project).subscribe(REST_SERVICE_SELECT, this::showServiceDetail);
        project.getMessageBus().connect(project).subscribe(REST_PARAMETER_UPDATE, new RestParameterListener() {
            @Override
            public void update(String editor, String content) {
                String titleAt = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                switch (titleAt) {
                    case "Headers":
                        setEditorText(requestHeaderEditor, content, project);
                        break;
                    case "Params":
                        setEditorText(requestParamEditor, content, project);
                        break;
                    case "Body":
                        setEditorText(requestBodyEditor, content, project);
                        break;
                    default:
                }
            }
        });
    }

    private void initEvent() {
        bindSendButtonActionListener();
        bindEnvActionListener();
    }

    private void initUI() {
        JPanel urlPanel = new JBPanel<>();
        urlPanel.setLayout(new GridLayoutManager(1, 4, JBUI.insets(0, 4, 0, 4), 0, 0));

        requestEnv = new ComboBox<>(new CollectionComboBoxModel<>(environment.getEnvKeys()), 100);
        requestEnv.setSelectedItem(environment.getCurrentEnv());
        setEnvToolTip();
        urlPanel.add(requestEnv,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        requestMethod = new ComboBox<>(new EnumComboBoxModel<>(HttpMethod.class), 86);
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
            requestHeaderEditor = createEditor("Headers", PlainTextLanguage.INSTANCE, null, project);
            tabbedPane.addTab("Headers", requestHeaderEditor.getComponent());
            requestHeaderEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });
        AppUIUtil.invokeOnEdt(() -> {
            requestParamEditor = createEditor("Params", PlainTextLanguage.INSTANCE, null, project);
            tabbedPane.addTab("Params", requestParamEditor.getComponent());
            requestParamEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });
        AppUIUtil.invokeOnEdt(() -> {
            requestBodyEditor = createEditor("Body", JsonLanguage.INSTANCE, null, project);
            tabbedPane.addTab("Body", requestBodyEditor.getComponent());
            requestBodyEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });
        AppUIUtil.invokeOnEdt(() -> {
            responseEditor = createEditor("Response", JsonLanguage.INSTANCE, null, project);
            tabbedPane.addTab("Response", responseEditor.getComponent());
            responseEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });
        AppUIUtil.invokeOnEdt(() -> {
            infoEditor = createEditor("Info", PlainTextLanguage.INSTANCE, null, project);
            tabbedPane.addTab("Info", infoEditor.getComponent());
            infoEditor.getComponent().setBorder(BorderFactory.createEmptyBorder());
        });

        this.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new GridLayoutManager(2, 1));
        this.add(urlPanel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
        this.add(tabbedPane,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
    }

    private void bindEnvActionListener() {
        requestEnv.addItemListener(e -> {
            environment.setCurrentEnv(e.getItem().toString());
            setEnvToolTip();
        });
        requestEnv.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (requestEnv.getItemCount() <= 0) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, EnvironmentConfigurable.class);
                }
            }
        });
    }

    private void setEnvToolTip() {
        StringBuilder sb = new StringBuilder();
        environment.getCurrentEnabledEnvMap().forEach((k, v) -> {
            if (StringUtils.isNotBlank(k)) {
                sb.append(k).append(" : ").append(v).append("<br/>");
            }
        });
        requestEnv.setToolTipText(StringUtils.defaultIfEmpty(sb.toString(), "No Environments."));
    }

    private void bindSendButtonActionListener() {
        sendButton.addActionListener(e -> {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Sending request") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    sendButton.setEnabled(false);
                    setResponse("Loading...");
                    try {
                        String url = EnvironmentUtils.handlePlaceholderVariable(requestUrl.getText(), project);
                        // 环境变量未设置【baseUrl】时强行替换为localhost:8080
                        url = url.replaceFirst("\\{\\{baseUrl}}", "http://localhost:8080");
                        // http method
                        HttpMethod method = (HttpMethod) Objects.requireNonNull(requestMethod.getSelectedItem());
                        // header
                        Map<String, String> headerMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(getEditorText(requestHeaderEditor), project));
                        // param
                        Map<String, String> paramMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(getEditorText(requestParamEditor), project));

                        // send request
                        HttpInfo response = HttpUtils.request(url, method, paramMap, getEditorText(requestBodyEditor), headerMap, project);

                        // response
                        setResponse(response.getResponseBody());

                        // info
                        setHttpInfo(response.formatResponseInfo());

                        AppUIUtil.invokeOnEdt(() -> tabbedPane.setSelectedIndex(3));
                    } finally {
                        sendButton.setEnabled(true);
                    }
                }
            });
        });
    }

    /**
     * 显示服务详情，url
     */
    private void showServiceDetail(RestItem restItem) {
        if (!restItem.isValid()) {
            // try refresh service
            LOG.warn("psiMethod is invalid: " + restItem);
            RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String url = StringUtils.defaultString(restItem.getUrl());
            String method = ObjectUtils.defaultIfNull(restItem.getMethod(), HttpMethod.GET).name();
            ApplicationManager.getApplication().runReadAction(() -> {
                List<KV> requestHeaders = restItem.getHeaders();
                List<KV> requestParams = restItem.getParams();
                String requestBodyJson = restItem.getBodyJson();

                // 在UI展示
                AppUIUtil.invokeOnEdt(() -> {
                    showRequest(url, method, requestHeaders, requestParams, requestBodyJson);
                });
            });
        });
    }

    private void showRequest(String url, String method, List<KV> headers, List<KV> params, String body) {
        if (CollectionUtils.isEmpty(params) && StringUtils.isNotEmpty(body)) {
            tabbedPane.setSelectedIndex(2);
        } else {
            tabbedPane.setSelectedIndex(1);
        }

        if (!url.contains("://")) {
            url = PLACEHOLDER_BASE_URL + url;
        }
        requestUrl.setText(url);
        requestUrl.setToolTipText(url);
        requestMethod.setSelectedItem(HttpMethod.valueOf(method));
        setHeader(headers);
        setParams(params);
        setReqBody(body);
        setResponse(StringUtils.EMPTY);
        setHttpInfo(StringUtils.EMPTY);
    }

    private void setHeader(List<KV> reqHeader) {
        List<KV> globalHeaderList = environment.getEnabledGlobalHeader();
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
        respText = JsonUtils.format(respText);
        if (StringUtils.isNotBlank(respText)) {
            respText = respText.replace("\r", "");
            // gson会将字符转成unicode，此处需转回来
            respText = org.apache.commons.lang.StringEscapeUtils.unescapeJava(respText);
        }
        setEditorText(responseEditor, respText, project);
    }

    private void setHttpInfo(String httpInfo) {
        setEditorText(infoEditor, httpInfo, project);
    }

    @Nullable
    @Override
    public Object getData(@NotNull @NonNls String dataId) {
        if (RestDataKey.CLIENT_EDITOR_INFO.is(dataId)) {
            String requestUrl = this.requestUrl.getText();
            if (StringUtils.isEmpty(requestUrl) || PLACEHOLDER_URL.equals(requestUrl)) {
                return null;
            }
            RestClientEditorInfo restClientEditorInfo = new RestClientEditorInfo();
            restClientEditorInfo.setMethod(String.valueOf(requestMethod.getSelectedItem()));
            restClientEditorInfo.setUrl(requestUrl.replace(PLACEHOLDER_BASE_URL, ""));
            restClientEditorInfo.setEditor(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
            return restClientEditorInfo;
        }
        if (RestDataKey.CLIENT_API_INFO.is(dataId)) {
            RestClientApiInfo restClientApiInfo = new RestClientApiInfo();
            restClientApiInfo.setUrl(StringUtils.replace(requestUrl.getText(), PLACEHOLDER_BASE_URL, ""));
            restClientApiInfo.setMethod((HttpMethod) Objects.requireNonNull(requestMethod.getSelectedItem()));
            restClientApiInfo.setHeaders(getEditorText(requestHeaderEditor));
            restClientApiInfo.setParams(getEditorText(requestParamEditor));
            restClientApiInfo.setBodyJson(getEditorText(requestBodyEditor));
            return restClientApiInfo;
        }
        return null;
    }
}