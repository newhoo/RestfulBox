package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.util.ElementsChooser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.config.ide.CommonSetting;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseToggleAction;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.toolwindow.ToolWindowHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SupportFrameworkFilterAction
 *
 * @author huzunrong
 * @see com.intellij.ide.actions.bigPopup.ShowFilterAction
 */
public class SupportFrameworkFilterAction extends BaseToggleAction {

    private JBPopup myFilterPopup;

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null || project.isDefault()) {
            return;
        }
        Icon icon = getTemplatePresentation().getIcon();
        e.getPresentation().setIcon(isActive(project) ? ExecutionUtil.getLiveIndicator(icon) : icon);
        e.getPresentation().setEnabledAndVisible(true);
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.framework.action.text"));
        Toggleable.setSelected(e.getPresentation(), isSelected(e));
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return myFilterPopup != null && !myFilterPopup.isDisposed();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        if (state) {
            showPopup(e);
        } else {
            if (myFilterPopup != null && !myFilterPopup.isDisposed()) {
                myFilterPopup.cancel();
            }
        }
    }

    private boolean isActive(Project myProject) {
//        Set<String> allScanRequestResolvers = RequestHelper.getAllScanRequestResolvers(myProject)
//                                                           .stream()
//                                                           .map(RequestResolver::getFrameworkName)
//                                                           .collect(Collectors.toSet());
        Set<String> disabledWebFrameworks = ConfigHelper.getCommonSetting(myProject).getDisabledWebFrameworks();
//        return allScanRequestResolvers.size() != disabledWebFrameworks.size();
        return !disabledWebFrameworks.isEmpty();
    }

    private void showPopup(AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        if (myFilterPopup != null) {
            return;
        }
        JBPopupListener popupCloseListener = new JBPopupListener() {
            @Override
            public void onClosed(@NotNull LightweightWindowEvent event) {
                myFilterPopup = null;
            }
        };
        myFilterPopup = JBPopupFactory.getInstance()
                                      .createComponentPopupBuilder(createFilterPanel(project), null)
                                      .setModalContext(false)
                                      .setFocusable(true)
                                      .setRequestFocus(true)
                                      .setResizable(true)
//                                      .setCancelOnClickOutside(true)
                                      .setMinSize(new Dimension(200, 150))
//                                      .setDimensionServiceKey(project, getDimensionServiceKey(), false)
                                      .addListener(popupCloseListener)
                                      .createPopup();
        Component anchor = e.getInputEvent().getComponent();
        if (anchor.isValid()) {
            myFilterPopup.showUnderneathOf(anchor);
        } else {
            Component component = e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
            if (component != null) {
                myFilterPopup.showUnderneathOf(component);
            } else {
                myFilterPopup.showInFocusCenter();
            }
        }
    }

    private JComponent createFilterPanel(Project project) {
        ElementsChooser<?> chooser = createChooser(project);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(chooser);
        return panel;
    }

    private ElementsChooser<String> createChooser(@NotNull Project project) {
        CommonSetting commonSetting = ConfigHelper.getCommonSetting(project);
        Set<String> disabledWebFrameworks = commonSetting.getDisabledWebFrameworks();
        List<String> resolvers = RequestHelper.getAllScanRequestResolvers(project).stream()
                                              .map(RequestResolver::getFrameworkName)
                                              .distinct()
                                              .collect(Collectors.toList());
        ElementsChooser<String> res = new ElementsChooser<>(resolvers, false);
        res.markElements(ContainerUtil.filter(resolvers, new Condition<String>() {
            @Override
            public boolean value(String s) {
                return !disabledWebFrameworks.contains(s);
            }
        }));
        ElementsChooser.ElementsMarkListener<String> listener = (element, isMarked) -> {
            if (isMarked) {
                commonSetting.getDisabledWebFrameworks().remove(element);
            } else {
                commonSetting.getDisabledWebFrameworks().add(element);
            }
            ToolWindowHelper.scheduleUpdateTree(project);
        };
        res.addElementsMarkListener(listener);
        return res;
    }
}