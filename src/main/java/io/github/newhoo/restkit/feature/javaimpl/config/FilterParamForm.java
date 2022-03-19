package io.github.newhoo.restkit.feature.javaimpl.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ListUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

/**
 * @author huzunrong
 * @since 2.0.3
 */
public class FilterParamForm extends JPanel {

    @Getter
    private final DefaultListModel<String> typeModel = new DefaultListModel<>();
    private final Project project;

    public FilterParamForm(Project project) {
        this.project = project;

        initUI();
    }

    private void initUI() {
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new GridLayoutManager(1, 1));

        this.add(createPanel("Filter By Class or Annotation", typeModel),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
    }

    private JPanel createPanel(String title, DefaultListModel<String> model) {
        JBList<String> jbList = new JBList<>(model);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jbList);
        decorator.disableUpDownActions();
        decorator.setAddAction(button -> {
            String name = Messages.showInputDialog(project, "Input qualified class or annotation name:", title, null, null, new InputValidator() {
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
