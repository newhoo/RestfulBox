package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import io.github.newhoo.restkit.util.ToolkitUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SaveApiDialog
 *
 * @author huzunrong
 * @since 2.0.3
 */
public class SaveApiDialog extends DialogWrapper {

    private JComboBox<String> protocolComboBox;
    private JComboBox<String> saveTypeComboBox;
    private JComboBox<String> moduleComboBox;
    private JTextArea descTextArea;
    private JLabel existedLabel;
    private JRadioButton updateRadioButton;
    private JRadioButton saveAsRadioButton;

    private final Project project;
    private final RestClientApiInfo apiInfo;
    private final Map<String, RequestResolver> resolverMap;
    private final List<String> protocols;
    private List<RestItem> existedItemList;

    public SaveApiDialog(Project project, RestClientApiInfo apiInfo, Map<String, RequestResolver> resolverMap, List<String> protocols) {
        super(project, true);

        this.project = project;
        this.apiInfo = apiInfo;
        this.resolverMap = resolverMap;
        this.protocols = protocols;

        setTitle("Save Api");
        setSize(380, 300);

        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel contentPanel = new JBPanel<>();
        contentPanel.setLayout(new GridLayoutManager(6, 2, JBUI.insets(0, 0, 0, 0), 4, 4));

        contentPanel.add(new JBLabel("Key: "),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
        JBTextField keyTextField = new JBTextField(apiInfo.getMethod() + " " + apiInfo.getUrl());
        keyTextField.setEnabled(false);
        contentPanel.add(keyTextField,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        contentPanel.add(new JBLabel("Protocol: "),
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
        protocolComboBox = new JComboBox<>(new DefaultComboBoxModel<>(protocols.toArray(new String[0])));
        contentPanel.add(protocolComboBox,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        contentPanel.add(new JBLabel("SaveType: "),
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
        saveTypeComboBox = new JComboBox<>(new DefaultComboBoxModel<>(resolverMap.keySet().toArray(new String[0])));
        saveTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ItemEvent.SELECTED == e.getStateChange()) {
                    String selectedItem = e.getItem().toString();
                    selectResolver(selectedItem);
                }
            }
        });
        contentPanel.add(saveTypeComboBox,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        contentPanel.add(new JBLabel("Module: "),
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
        moduleComboBox = new JComboBox<>();
        moduleComboBox.setEditable(true);
        contentPanel.add(moduleComboBox,
                new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

        contentPanel.add(new JBLabel("Description: "),
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
        descTextArea = new JTextArea();
        descTextArea.setRows(0);
        descTextArea.setMargin(new Insets(4, 4, 4, 4));
        contentPanel.add(descTextArea,
                new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                        null, null, null));

        existedLabel = new JLabel("Existed: ");
        contentPanel.add(existedLabel,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
        updateRadioButton = new JRadioButton("update");
        saveAsRadioButton = new JRadioButton("save as");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(updateRadioButton);
        buttonGroup.add(saveAsRadioButton);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(updateRadioButton);
        radioPanel.add(saveAsRadioButton);
        contentPanel.add(radioPanel,
                new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.ALIGN_LEFT, GridConstraints.ALIGN_CENTER,
                        null, null, null));

        selectResolver(saveTypeComboBox.getSelectedItem().toString());

        return contentPanel;
    }

    @Override
    protected void doOKAction() {
        String module = (String) moduleComboBox.getSelectedItem();
        if (StringUtils.isEmpty(module)) {
            moduleComboBox.requestFocus();
            return;
        }
        String desc = descTextArea.getText();
        if (StringUtils.isEmpty(desc)) {
            descTextArea.requestFocus();
            return;
        }
        RequestResolver requestResolver = resolverMap.get(saveTypeComboBox.getSelectedItem());

        List<KV> headers = ToolkitUtil.textToKVList(apiInfo.getHeaders());
        List<KV> params = ToolkitUtil.textToKVList(apiInfo.getParams());
        String bodyJson = apiInfo.getBodyJson();

        if (updateRadioButton.isSelected()) {
            existedItemList.forEach(restItem -> {
                restItem.setHeaders(headers);
                restItem.setParams(params);
                restItem.setBodyJson(bodyJson);
                restItem.setDescription(desc);
                restItem.setModuleName(module);
            });
            requestResolver.update(existedItemList);
        } else {
            requestResolver.add(Collections.singletonList(
                    new RestItem(apiInfo.getUrl(), apiInfo.getMethod().name(), headers, params, bodyJson, desc, module, requestResolver.getFrameworkName(), protocolComboBox.getSelectedItem().toString())
            ));
        }
        RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);

        close(OK_EXIT_CODE);
    }

    private void selectResolver(String selectedItem) {
        RequestResolver requestResolver = resolverMap.get(selectedItem);
        String url = apiInfo.getUrl();
        HttpMethod method = apiInfo.getMethod();

        List<RestItem> restItemInProject = requestResolver.findRestItemInProject(project);
        moduleComboBox.removeAllItems();
        restItemInProject.stream().map(RestItem::getModuleName).distinct().forEach(s -> moduleComboBox.addItem(s));

        existedItemList = restItemInProject.stream()
                                           .filter(item -> url.equals(item.getUrl()) && item.getMethod() == method)
                                           .collect(Collectors.toList());
        existedLabel.setText("Existed " + existedItemList.size() + ": ");
        if (!existedItemList.isEmpty()) {
            updateRadioButton.setSelected(true);
            updateRadioButton.setEnabled(true);
            saveAsRadioButton.setEnabled(true);
            if (StringUtils.isEmpty(descTextArea.getText())) {
                descTextArea.setText(existedItemList.get(0).getDescription());
            }
        } else {
            saveAsRadioButton.setSelected(true);
            saveAsRadioButton.setEnabled(false);
            updateRadioButton.setEnabled(false);
        }
    }
}
