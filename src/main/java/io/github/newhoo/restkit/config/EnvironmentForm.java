package io.github.newhoo.restkit.config;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import io.github.newhoo.restkit.common.BKV;
import io.github.newhoo.restkit.common.EnvList;
import io.github.newhoo.restkit.common.EnvModel;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.util.EnvironmentUtils;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

/**
 * EnvironmentForm
 *
 * @author huzunrong
 * @since 1.0
 */
public class EnvironmentForm {

    public JPanel mainPanel;
    private JPanel environmentPanel;
    private JPanel globalHeaderPanel;

    private JButton addEnvironmentBtn;
    private JButton copyEnvironmentBtn;
    private JButton deleteEnvironmentBtn;
    private JButton scriptBtn;
    private JButton exportEnvBtn;
    private JButton importEnvBtn;

    private JTabbedPane envTabbedPanel;

    private final List<EnvModel> envModelList = new ArrayList<>();
    private final KeyValueTableModel headerModel = new KeyValueTableModel();
    private String script;

    private final Project project;

    private EnvironmentForm(Project project) {
        this.project = project;

        initView();
        initEvent();
    }

    private void initView() {
        environmentPanel.setBorder(IdeBorderFactory.createTitledBorder("Environment", false));
        globalHeaderPanel.setBorder(IdeBorderFactory.createTitledBorder("Global Request Header", false));

        Environment setting = Environment.getInstance(project);
        initHeaderModel(setting.getGlobalHeaderList());
        initEnvModel(setting.getEnvList());
        initScript(setting.getScript());

        // header
        globalHeaderPanel.add(createToolbarPanel(headerModel), BorderLayout.CENTER);

        if (project.isDefault()) {
            scriptBtn.setEnabled(false);
            scriptBtn.setVisible(false);
        }
    }

    private void initEvent() {
        addEnvironmentBtn.addActionListener(e -> {
            String envName = Messages.showInputDialog(project, "Input new environment name such as DEV", "Add Environment", null, "", getEnvInputValidator());
            if (StringUtils.isEmpty(envName)) {
                return;
            }
            KeyValueTableModel defaultTableModel = new KeyValueTableModel();
            defaultTableModel.addRow(true, "baseUrl", "http://localhost:8080");

            addEnvTab(envName, defaultTableModel);
        });
        copyEnvironmentBtn.addActionListener(e -> {
            if (envTabbedPanel.getSelectedIndex() > -1) {
                final String titleAt = envTabbedPanel.getTitleAt(envTabbedPanel.getSelectedIndex());
                String envName = Messages.showInputDialog(project, "Input new environment name such as FAT", "Copy Current Environment", null, titleAt + "_COPY", getEnvInputValidator());
                if (StringUtils.isEmpty(envName)) {
                    return;
                }
                envModelList.stream().filter(envModel -> titleAt.equals(envModel.getEnv())).findFirst()
                            .ifPresent(envModel -> {
                                final KeyValueTableModel keyValueTableModel = new KeyValueTableModel();
                                for (Object[] objects : envModel.getModel().list) {
                                    keyValueTableModel.addRow((Boolean) objects[0], (String) objects[1], (String) objects[2]);
                                }
                                addEnvTab(envName, keyValueTableModel);
                            });
            }
        });
        deleteEnvironmentBtn.addActionListener(e -> {
            if (envTabbedPanel.getTabCount() > 0) {
                envModelList.removeIf(envModel -> envModel.getEnv().equals(envTabbedPanel.getTitleAt(envTabbedPanel.getSelectedIndex())));
                envTabbedPanel.remove(envTabbedPanel.getSelectedComponent());
            }
        });
        scriptBtn.addActionListener(e -> {
            new ScriptEditDialog(project, script, s -> {
                this.script = s;
            }).setVisible(true);
        });
        exportEnvBtn.addActionListener((e -> {
            Environment setting = Environment.getInstance(project);
            CopyPasteManager.getInstance().setContents(new StringSelection(JsonUtils.toJson(setting)));
            NotifierUtils.infoBalloon(RestBundle.message("toolkit.environment.export.title"), RestBundle.message("toolkit.environment.export.content"), null, project);
        }));
        importEnvBtn.addActionListener((e -> {
            Transferable contents = CopyPasteManager.getInstance().getContents();
            if (contents == null) {
                return;
            }
            try {
                String data = contents.getTransferData(DataFlavor.stringFlavor).toString();
                Environment setting = JsonUtils.fromJson(data, Environment.class);
                if (setting != null) {
                    int yesNoDialog = Messages.showYesNoDialog(project, RestBundle.message("toolkit.environment.import.tip.content"), RestBundle.message("toolkit.environment.import.tip.title"), null);
                    if (Messages.YES != yesNoDialog) {
                        return;
                    }
                    initEnvModel(setting.getEnvList());
                    initHeaderModel(setting.getGlobalHeaderList());
                    initScript(setting.getScript());
                } else {
                    NotifierUtils.errorBalloon(RestBundle.message("toolkit.environment.import.title"), RestBundle.message("toolkit.environment.import.content", "format error"), project);
                }
            } catch (Exception ex) {
                NotifierUtils.errorBalloon(RestBundle.message("toolkit.environment.import.title"), RestBundle.message("toolkit.environment.import.content", ex.toString()), project);
            }
        }));
    }

    private void addEnvTab(String envName, KeyValueTableModel keyValueTableModel) {
        envModelList.add(new EnvModel(envName, keyValueTableModel));
        envTabbedPanel.addTab(envName, createToolbarPanel(keyValueTableModel));
        envTabbedPanel.setSelectedIndex(envTabbedPanel.getTabCount() - 1);
    }

    private JPanel createToolbarPanel(KeyValueTableModel model) {
        JBTable jbTable = new JBTable(model);
        jbTable.getColumnModel().getColumn(0).setMaxWidth(40);
        ToolbarDecorator decorationToolbar = ToolbarDecorator.createDecorator(jbTable);

        decorationToolbar.setAddAction(button -> {
            EventQueue.invokeLater(model::addRow);
        });
        decorationToolbar.setRemoveAction(button -> {
            EventQueue.invokeLater(() -> {
                TableUtil.removeSelectedItems(jbTable);
            });
        });
        return decorationToolbar.createPanel();
    }

    private void initEnvModel(List<EnvList> envList) {
        if (envList == null) {
            return;
        }
        this.envModelList.clear();
        this.envTabbedPanel.removeAll();

        for (EnvList env : envList) {
            KeyValueTableModel model = new KeyValueTableModel();
            if (env.getItems() != null) {
                for (BKV item : env.getItems()) {
                    model.addRow(item.getEnabled(), item.getKey(), item.getValue());
                }
            }
            this.envModelList.add(new EnvModel(env.getEnv(), model));

            envTabbedPanel.addTab(env.getEnv(), createToolbarPanel(model));
        }
    }

    private void initHeaderModel(List<BKV> headerList) {
        if (headerList == null) {
            return;
        }
        this.headerModel.clear();
        for (BKV bkv : headerList) {
            this.headerModel.addRow(bkv.getEnabled(), bkv.getKey(), bkv.getValue());
        }
    }

    private void initScript(String script) {
        if (script == null) {
            return;
        }
        this.script = script;
    }

    public boolean isModified(Environment environment) {
        String env1 = EnvironmentUtils.formatEnvModelList(envModelList);
        String header1 = EnvironmentUtils.formatHeaderModel(headerModel);

        List<EnvList> envList = environment.getEnvList();
        List<BKV> globalHeaderList = environment.getGlobalHeaderList();
        String env2 = EnvironmentUtils.formatEnvList(envList);
        String header2 = EnvironmentUtils.formatHeaderList(globalHeaderList);

        return envModelList.size() != envList.size()
                || headerModel.list.size() != globalHeaderList.size()
                || !StringUtils.equals(env1, env2)
                || !StringUtils.equals(header1, header2)
                || !StringUtils.equals(StringUtils.defaultString(script), StringUtils.defaultString(environment.getScript()));
    }

    public void saveTo(Environment environment) {
        environment.setEnvList(EnvironmentUtils.buildEnvList(envModelList));
        environment.setGlobalHeaderList(EnvironmentUtils.buildHeaderList(headerModel));
        environment.setScript(script);
    }

    public void reset(Environment environment) {
        initHeaderModel(environment.getGlobalHeaderList());
        initEnvModel(environment.getEnvList());
        initScript(environment.getScript());
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
}