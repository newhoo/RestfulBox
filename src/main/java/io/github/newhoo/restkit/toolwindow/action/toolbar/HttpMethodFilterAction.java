package io.github.newhoo.restkit.toolwindow.action.toolbar;

import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.ElementsChooser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.util.containers.ContainerUtil;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.config.HttpMethodFilterConfiguration;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * HttpMethodFilterAction
 *
 * @author huzunrong
 * @see com.intellij.ide.actions.bigPopup.ShowFilterAction
 */
public class HttpMethodFilterAction extends ToggleAction {
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
        HttpMethodFilterConfiguration instance = HttpMethodFilterConfiguration.getInstance(myProject);
        HttpMethod[] httpMethods = HttpMethod.values();
        return httpMethods.length != ContainerUtil.filter(httpMethods, instance::isFileTypeVisible).size();
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
                                      .setMinSize(new Dimension(200, 200))
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
        JPanel buttons = new JPanel();
        JButton all = new JButton(IdeBundle.message("big.popup.filter.button.all"));
        all.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                chooser.setAllElementsMarked(true);
            }
        });
        buttons.add(all);
        JButton none = new JButton(IdeBundle.message("big.popup.filter.button.none"));
        none.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                chooser.setAllElementsMarked(false);
            }
        });
        buttons.add(none);
        JButton invert = new JButton(IdeBundle.message("big.popup.filter.button.invert"));
        invert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                chooser.invertSelection();
            }
        });
        buttons.add(invert);
        panel.add(buttons);
        return panel;
    }

    private ElementsChooser<HttpMethod> createChooser(@NotNull Project project) {
        HttpMethodFilterConfiguration instance = HttpMethodFilterConfiguration.getInstance(project);
        ElementsChooser<HttpMethod> res = new ElementsChooser<HttpMethod>(Arrays.asList(HttpMethod.values()), false) {
            @Override
            protected String getItemText(@NotNull HttpMethod value) {
                return value.name();
            }
        };
        res.markElements(ContainerUtil.filter(HttpMethod.values(), instance::isFileTypeVisible));
        ElementsChooser.ElementsMarkListener<HttpMethod> listener = (element, isMarked) -> {
            instance.setVisible(element, isMarked);
            RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);
        };
        res.addElementsMarkListener(listener);
        return res;
    }
}
