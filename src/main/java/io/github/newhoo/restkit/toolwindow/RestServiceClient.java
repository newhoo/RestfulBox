package io.github.newhoo.restkit.toolwindow;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.config.SettingListener;
import io.github.newhoo.restkit.config.global.GlobalSetting;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.intellij.CompactHelper;
import io.github.newhoo.restkit.common.LicenseDialog;
import io.github.newhoo.restkit.toolwindow.parameter.library.RestParameterListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import static io.github.newhoo.restkit.config.SettingListener.ENV_UPDATE;
import static io.github.newhoo.restkit.toolwindow.RestServiceListener.REST_SERVICE_SELECT;
import static io.github.newhoo.restkit.toolwindow.parameter.library.RestParameterListener.REST_PARAMETER_UPDATE;

/**
 * RestServiceClient
 *
 * @since 1.0
 */
public class RestServiceClient extends JPanel implements DataProvider {

    private final Project project;
    private final GlobalSetting globalSetting;

    private JTabbedPane multiTabbedClientPane;
    private RestServiceClientPanel singleClientPanel;

    public RestServiceClient(Project project) {
        this.project = project;
        this.globalSetting = ConfigHelper.getGlobalSetting();

        initUI();
        initEvent();
    }

    private void initUI() {
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new GridLayoutManager(1, 1));

        if (globalSetting.isEnableMultiRequestClient()) {
            multiTabbedClientPane = new JBTabbedPane();
            ((JBTabbedPane) multiTabbedClientPane).setTabComponentInsets(JBUI.emptyInsets());
            multiTabbedClientPane.addTab("Default", new RestServiceClientPanel(project, true));
            this.add(multiTabbedClientPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        } else {
            singleClientPanel = new RestServiceClientPanel(project, true);
            this.add(singleClientPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        }
    }

    private void initEvent() {
        if (globalSetting.isEnableMultiRequestClient() && multiTabbedClientPane != null) {
            multiTabbedClientPane.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        DefaultActionGroup generateActionGroup = new DefaultActionGroup(
                                new BaseAnAction(() -> RestBundle.message("toolkit.common.btn.new")) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        if (multiTabbedClientPane.getTabCount() > 1) {
                                            new LicenseDialog(null).show();
                                            return;
                                        }
                                        multiTabbedClientPane.addTab("New", new RestServiceClientPanel(project, true));
                                        multiTabbedClientPane.setSelectedIndex(multiTabbedClientPane.getTabCount() - 1);
                                    }
                                },
                                new BaseAnAction(() -> RestBundle.message("toolkit.common.btn.rename")) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        String title = CompactHelper.showInputDialog(e.getProject(), "", RestBundle.message("toolkit.toolwindow.client.editor.renametab.title"), null, null, null);
                                        if (StringUtils.isNotEmpty(title)) {
                                            multiTabbedClientPane.setTitleAt(multiTabbedClientPane.getSelectedIndex(), title);
                                        }
                                    }
                                },
                                new BaseAnAction(() -> RestBundle.message("toolkit.common.btn.close")) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        RestServiceClientPanel componentAt = (RestServiceClientPanel) multiTabbedClientPane.getComponentAt(multiTabbedClientPane.getSelectedIndex());
                                        componentAt.destroy();
                                        multiTabbedClientPane.remove(multiTabbedClientPane.getSelectedIndex());
                                    }
                                },
                                Separator.create(),
                                new BaseAnAction(RestBundle.message("toolkit.toolwindow.client.editor.switchproject.action.text")) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        RestServiceClientPanel componentAt = (RestServiceClientPanel) multiTabbedClientPane.getComponentAt(multiTabbedClientPane.getSelectedIndex());
                                        List<String> selectedProjects = Collections.singletonList(project.getName());
                                        int i = CompactHelper.showChooseDialog(project, RestBundle.message("toolkit.toolwindow.client.editor.switchproject.msg"), RestBundle.message("toolkit.toolwindow.client.editor.switchproject.title"), null, selectedProjects.toArray(new String[0]), componentAt.getCurrentProject());
                                        if (i > -1) {
                                            componentAt.setEnv(selectedProjects.get(i));
                                        }
                                    }
                                });

                        DataContext dataContext = DataManager.getInstance().getDataContext(multiTabbedClientPane.getTabComponentAt(multiTabbedClientPane.getSelectedIndex()));
                        final ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(null, generateActionGroup, dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);
                        popup.showInBestPositionFor(dataContext);
                    }
                }
            });
        }

        project.getMessageBus().connect(project).subscribe(ENV_UPDATE, new SettingListener() {
            @Override
            public void changeEnv(String project) {
                if (multiTabbedClientPane != null) {
                    for (int i = 0; i < multiTabbedClientPane.getTabCount(); i++) {
                        Component componentAt = multiTabbedClientPane.getComponentAt(i);
                        if (componentAt instanceof RestServiceClientPanel) {
                            RestServiceClientPanel selectedComponent = (RestServiceClientPanel) componentAt;
                            if (project.equals(selectedComponent.getCurrentProject())) {
                                selectedComponent.setEnv(project);
                            }
                        }
                    }
                } else if (singleClientPanel != null) {
                    singleClientPanel.setEnv(project);
                }
            }
        });
        project.getMessageBus().connect(project).subscribe(REST_SERVICE_SELECT, new RestServiceListener() {
            @Override
            public void select(RestItem serviceItem) {
                if (multiTabbedClientPane != null) {
                    RestServiceClientPanel selectedComponent = (RestServiceClientPanel) multiTabbedClientPane.getSelectedComponent();
                    if (selectedComponent != null) {
                        selectedComponent.showServiceDetail(serviceItem);
                    } else {
                        RestServiceClientPanel restServiceClientPanel = new RestServiceClientPanel(project, false);
                        multiTabbedClientPane.addTab("Default", restServiceClientPanel);
                        multiTabbedClientPane.setSelectedIndex(multiTabbedClientPane.getTabCount() - 1);
                        restServiceClientPanel.showServiceDetail(serviceItem);
                    }
                } else if (singleClientPanel != null) {
                    singleClientPanel.showServiceDetail(serviceItem);
                }
            }
        });
        project.getMessageBus().connect(project).subscribe(REST_PARAMETER_UPDATE, new RestParameterListener() {
            @Override
            public void update(String editor, String content) {
                if (multiTabbedClientPane != null) {
                    RestServiceClientPanel selectedComponent = (RestServiceClientPanel) multiTabbedClientPane.getSelectedComponent();
                    selectedComponent.handleParameterUpdate(editor, content);
                } else if (singleClientPanel != null) {
                    singleClientPanel.handleParameterUpdate(editor, content);
                }
            }
        });
    }

    void cleanup() {
        if (multiTabbedClientPane != null) {
            multiTabbedClientPane.removeAll();
            multiTabbedClientPane = null;
        } else if (singleClientPanel != null) {
            singleClientPanel.removeAll();
            singleClientPanel = null;
        }
    }

    @Nullable
    @Override
    public Object getData(@NotNull @NonNls String dataId) {
        if (RestDataKey.CLIENT_EDITOR_INFO.is(dataId) || RestDataKey.CLIENT_API_INFO.is(dataId)) {
            if (multiTabbedClientPane != null) {
                Component selectedComponent = multiTabbedClientPane.getSelectedComponent();
                if (selectedComponent instanceof RestServiceClientPanel) {
                    return ((RestServiceClientPanel) selectedComponent).getData(dataId);
                }
            } else if (singleClientPanel != null) {
                return singleClientPanel.getData(dataId);
            }
        }
        return null;
    }
}