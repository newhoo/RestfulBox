package io.github.newhoo.restkit.toolwindow.client;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.toolwindow.ToolWindowHelper;
import io.github.newhoo.restkit.util.ToolkitUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SaveApiDialog extends DialogWrapper {
    private JPanel mainPanel;
    private JLabel protocolLabel;
    private JComboBox<String> protocolComboBox;
    private JLabel storageLabel;
    private JComboBox<String> saveTypeComboBox;
    private JLabel projectLabel;
    private JComboBox<String> projectComboBox;
    private JLabel moduleLabel;
    private JComboBox<String> moduleComboBox;
    private JLabel packageLabel;
    private JComboBox<String> packageComboBox;
    private JLabel descLabel;
    private JTextField descTextField;
    private JPanel tipPanel;
    private JLabel tipLabel;
    private JRadioButton updateAllRadioButton;
    private JRadioButton saveAsNewOneRadioButton;

    private final Project project;
    private final RestClientApiInfo apiInfo;
    private final List<String> protocols;
    private List<RestItem> existedItemList;
    private final DataSource dataSource;
    private final Map<String, RequestResolver> requestResolverMap;

    private final boolean showMultiLevelServiceNode;

    public SaveApiDialog(Project project, RestClientApiInfo apiInfo, DataSource dataSource, List<String> protocols, Map<String, RequestResolver> requestResolverMap) {
        super(project, true);

        this.project = project;
        this.apiInfo = apiInfo;
        this.dataSource = dataSource;
        this.protocols = protocols;
        this.requestResolverMap = requestResolverMap;

        showMultiLevelServiceNode = ConfigHelper.getGlobalSetting().isShowMultiLevelServiceNode();

        String title = RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.title");
        setTitle(title);
//        setSize(500, 440);

        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        protocolLabel.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.protocolLabel"));
        storageLabel.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.storageLabel"));
        projectLabel.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.projectLabel"));
        moduleLabel.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.moduleLabel"));
        packageLabel.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.packageLabel"));
        descLabel.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.descLabel"));
        updateAllRadioButton.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.updateAllRadioButton"));
        saveAsNewOneRadioButton.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.saveAsNewOneRadioButton"));

        saveTypeComboBox.addItemListener(e -> {
            if (ItemEvent.SELECTED == e.getStateChange()) {
                selectResolver(e.getItem().toString());
            }
        });
        projectComboBox.addItemListener(e -> {
            if (ItemEvent.SELECTED == e.getStateChange()) {
                selectProject(e.getItem().toString());
            }
        });
        requestResolverMap.keySet().forEach(s -> saveTypeComboBox.addItem(s + "(Deprecated)"));

        protocols.forEach(s -> protocolComboBox.addItem(s));

        Object selectedItem = saveTypeComboBox.getSelectedItem();
        if (selectedItem != null) {
            selectResolver(selectedItem.toString());
        }

        packageLabel.setVisible(showMultiLevelServiceNode);
        packageComboBox.setVisible(showMultiLevelServiceNode);
        return mainPanel;
    }

    private void selectResolver(String selectedItem) {
        RequestResolver useRequestResolver = null;
        if (StringUtils.contains(selectedItem, "(Deprecated)")) {
            useRequestResolver = requestResolverMap.get(selectedItem.replace("(Deprecated)", ""));
        }

        projectComboBox.removeAllItems();
        moduleComboBox.removeAllItems();
        packageComboBox.removeAllItems();
        tipPanel.setVisible(false);
        existedItemList = Collections.emptyList();
    }

    private void selectProject(String selectedProject) {
        RequestResolver useRequestResolver = null;
        String saveType = saveTypeComboBox.getSelectedItem().toString();
        if (StringUtils.contains(saveType, "(Deprecated)")) {
            useRequestResolver = requestResolverMap.get(saveType.replace("(Deprecated)", ""));
        }

        moduleComboBox.removeAllItems();
        packageComboBox.removeAllItems();
        tipPanel.setVisible(false);
        existedItemList = Collections.emptyList();

        if (useRequestResolver == null) {

        } else {
            List<RestItem> restItemInProject = useRequestResolver.findRestItemInProject(project)
                                                                 .stream()
                                                                 .filter(item -> selectedProject.equals(item.getProject()))
                                                                 .collect(Collectors.toList());

            restItemInProject.stream().map(RestItem::getModuleName).distinct().forEach(s -> moduleComboBox.addItem(s));
            if (showMultiLevelServiceNode) {
                restItemInProject.stream().map(RestItem::getPackageName).distinct().forEach(s -> packageComboBox.addItem(s));
            }

            existedItemList = restItemInProject.stream()
                                               .filter(item -> apiInfo.getUrl().equals(item.getUrl()) && item.getMethod() == apiInfo.getMethod())
                                               .collect(Collectors.toList());
            if (!existedItemList.isEmpty()) {
                tipPanel.setVisible(true);
                if (existedItemList.size() > 1) {
                    tipLabel.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.tipLabel2", existedItemList.size()));
                } else {
                    tipLabel.setText(RestBundle.message("toolkit.toolwindow.client.saveapi.dialog.tipLabel1", existedItemList.size()));
                }
                String description = existedItemList.get(0).getDescription();
                if (StringUtils.isNotEmpty(description)) {
                    descTextField.setText(description);
                }
            }
        }
    }

    @Override
    protected void doOKAction() {
        RequestResolver useRequestResolver = null;
        String s = saveTypeComboBox.getSelectedItem().toString();
        if (StringUtils.contains(s, "(Deprecated)")) {
            useRequestResolver = requestResolverMap.get(s.replace("(Deprecated)", ""));
        }
        String projectName = (String) projectComboBox.getSelectedItem();
        if (StringUtils.isEmpty(projectName)) {
            projectComboBox.requestFocus();
            return;
        }
        String module = (String) moduleComboBox.getSelectedItem();
        if (StringUtils.isEmpty(module)) {
            moduleComboBox.requestFocus();
            return;
        }

        String packageText;
        if (showMultiLevelServiceNode) {
            packageText = (String) packageComboBox.getSelectedItem();
            if (StringUtils.isEmpty(packageText)) {
                packageComboBox.requestFocus();
                return;
            }
        } else {
            packageText = module;
        }

        String desc = descTextField.getText();
        if (StringUtils.isEmpty(desc)) {
            descTextField.requestFocus();
            return;
        }

        List<KV> headers = ToolkitUtil.textToKVList(apiInfo.getHeaders());
        List<KV> params = ToolkitUtil.textToKVList(apiInfo.getParams());
        String bodyJson = apiInfo.getBodyJson();

        if (tipPanel.isVisible() && updateAllRadioButton.isSelected()) {
            existedItemList.forEach(restItem -> {
                restItem.setHeaders(headers);
                restItem.setParams(params);
                restItem.setBodyJson(bodyJson);
                restItem.setDescription(desc);
                restItem.setProject(projectName);
                restItem.setModuleName(module);
                restItem.setPackageName(packageText);
            });
            if (useRequestResolver == null) {
                DataSourceHelper.updateRestItemToDataSource(existedItemList);
            } else {
                useRequestResolver.update(existedItemList);
                DataSourceHelper.syncCrossProjects(existedItemList);
            }
        } else {
            RestItem restItem = new RestItem(apiInfo.getUrl(), apiInfo.getMethod().name(), headers, params, bodyJson, desc, module, "", protocolComboBox.getSelectedItem().toString());
            restItem.setProject(projectName);
            restItem.setPackageName(packageText);
            if (useRequestResolver == null) {
                // restItem.setFramework(dataSource.name());
                DataSourceHelper.addRestItemToDataSource(Collections.singletonList(restItem));
            } else {
                restItem.setFramework(useRequestResolver.getFrameworkName());
                useRequestResolver.add(Collections.singletonList(restItem));
                DataSourceHelper.syncCrossProjects(existedItemList);
            }
        }
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Save api") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException ignored) {
                }
                ToolWindowHelper.scheduleUpdateTree(v -> {
                    ToolWindowHelper.navigateToTree(apiInfo.getUrl(), apiInfo.getMethod().name(), module, project);
                }, project);
            }
        });

        close(OK_EXIT_CODE);
    }
}
