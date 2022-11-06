package io.github.newhoo.restkit.config.certificate;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huzunrong
 * @since 2.1.2
 */
public class CertificateForm extends JPanel {

    private final CertificateTableModel certificateModel = new CertificateTableModel();
    private final Project project;

    public CertificateForm(Project project) {
        this.project = project;

        initUI();
    }

    private void initUI() {
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new GridLayoutManager(1, 1));

        this.add(createPanel("Client Certificates"),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
    }

    private JPanel createPanel(String title) {
        JBTable jbTable = new JBTable(certificateModel);
        jbTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 2 && jbTable.getSelectedColumn() == 2) {
                    final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
                    final VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
                    if (file != null && !file.isDirectory()) {
                        String filepath = FileUtil.toSystemDependentName(file.getPath());
                        certificateModel.setValueAt(filepath, jbTable.getSelectedRow(), 2);
                    }
                }
            }
        });
        jbTable.getColumnModel().getColumn(0).setMaxWidth(60);
        jbTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        jbTable.getColumnModel().getColumn(1).setMaxWidth(160);
        jbTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        jbTable.getColumnModel().getColumn(3).setMaxWidth(160);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jbTable);
        JPanel panel = decorator.createPanel();
        panel.setBorder(IdeBorderFactory.createTitledBorder(title, false));
        return panel;
    }

    public void reset(List<Certificate> certificates) {
        List<Certificate> list = certificates.stream()
                                             .map(Certificate::copy)
                                             .collect(Collectors.toList());
        certificateModel.setItems(list);
    }

    public List<Certificate> getCertificates() {
        return certificateModel.getItems();
    }
}
