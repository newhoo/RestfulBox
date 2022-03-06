package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.openapi.project.Project;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SaveApiDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField urlTextField;
    private JComboBox<String> saveTypeComboBox;
    private JComboBox<String> moduleComboBox;
    private JTextArea descTextArea;
    private JLabel existedLabel;
    private JRadioButton updateRadioButton;
    private JRadioButton saveAsRadioButton;

    private final Project project;
    private final Map<String, RequestResolver> resolverMap;
    private final RestClientApiInfo apiInfo;
    private List<RestItem> existedItemList;

    public SaveApiDialog(Project project, Map<String, RequestResolver> resolverMap, RestClientApiInfo apiInfo) {
        setSize(380, 300);
        setLocationRelativeTo(null);

        setContentPane(contentPane);
        setModal(true);
        setAlwaysOnTop(true);
        setTitle("Save Api");
        getRootPane().setDefaultButton(buttonOK);

        saveTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ItemEvent.SELECTED == e.getStateChange()) {
                    String selectedItem = e.getItem().toString();
                    selectResolver(selectedItem);
                }
            }
        });

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> dispose());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.project = project;
        this.apiInfo = apiInfo;
        this.resolverMap = resolverMap;
        urlTextField.setText(apiInfo.getMethod() + " " + apiInfo.getUrl());
        for (String s : resolverMap.keySet().toArray(new String[0])) {
            saveTypeComboBox.addItem(s);
        }
        selectResolver(saveTypeComboBox.getSelectedItem().toString());
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

    private void onOK() {
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
                    new RestItem(apiInfo.getUrl(), apiInfo.getMethod().name(), headers, params, bodyJson, desc, module, requestResolver.getFrameworkName())
            ));
        }
        RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);

        dispose();
    }
}
