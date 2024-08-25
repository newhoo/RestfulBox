package io.github.newhoo.restkit.feature.javaimpl.config;

import com.intellij.openapi.ui.InputValidator;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ListUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.CompactHelper;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

/**
 * @author huzunrong
 * @since 2.0.3
 */
public class JavaFilterForm extends JPanel {

    @Getter
    private final DefaultListModel<String> filterClassTypeModel = new DefaultListModel<>();
    @Getter
    private final DefaultListModel<String> filterParamTypeModel = new DefaultListModel<>();
    @Getter
    private final DefaultListModel<String> fileParameterTypeModel = new DefaultListModel<>();

    public JavaFilterForm() {
        initUI();
    }

    private void initUI() {
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new GridLayoutManager(1, 1));

        JTabbedPane tabbedPane = new JBTabbedPane();
        tabbedPane.addTab(RestBundle.message("toolkit.java-filter.filterApi.tabName"), createPanel(RestBundle.message("toolkit.java-filter.filterApi.panel.title"), filterClassTypeModel));
        tabbedPane.addTab(RestBundle.message("toolkit.java-filter.filterParameter.tabName"), createPanel(RestBundle.message("toolkit.java-filter.filterParameter.panel.title"), filterParamTypeModel));
        tabbedPane.addTab(RestBundle.message("toolkit.java-filter.fileParameter.tabName"), createPanel(RestBundle.message("toolkit.java-filter.fileParameter.panel.title"), fileParameterTypeModel));

        this.add(tabbedPane,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
    }

    private JPanel createPanel(String title, DefaultListModel<String> model) {
        JBList<String> jbList = new JBList<>(model);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jbList);
        decorator.disableUpDownActions();
        decorator.setAddAction(button -> {
            String name = CompactHelper.showInputDialog(null, RestBundle.message("toolkit.java-filter.input.title"), title, null, null, new InputValidator() {
                @Override
                public boolean checkInput(String inputString) {
                    return StringUtils.isNotBlank(inputString);
                }

                @Override
                public boolean canClose(String inputString) {
                    return true;
                }
            });
            if (StringUtils.isNotEmpty(name)) {
                model.addElement(name);
            }
        });
        decorator.setRemoveAction(button -> ListUtil.removeSelectedItems(jbList));
        JPanel panel = decorator.createPanel();
        panel.setBorder(IdeBorderFactory.createTitledBorder(title, false));
        return panel;
    }
}
