package io.github.newhoo.restkit.config.certificate;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnActionButton;
import io.github.newhoo.restkit.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huzunrong
 * @since 2.1.2
 */
public class CertificateForm extends JPanel {

    private final CertificateTableModel certificateModel = new CertificateTableModel();

    public CertificateForm() {
        initUI();
    }

    private void initUI() {
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new GridLayoutManager(1, 1));

        this.add(createPanel(RestBundle.message("toolkit.config.certificate.panel.title")),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
    }

    private JPanel createPanel(String title) {
        JBTable jbTable = new JBTable(certificateModel) {
            @Override
            public String getToolTipText(@NotNull MouseEvent e) {
                String tip = "";
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);
                int realRowIndex = convertRowIndexToModel(rowIndex);

                if (realColumnIndex == 2 && realRowIndex != -1) {
                    String path = String.valueOf(getValueAt(rowIndex, colIndex));
                    if (path.startsWith("~/") || path.startsWith("~\\")) {
                        tip = RestBundle.message("toolkit.config.certificate.toolTip", FileUtils.expandUserHome(path));
                    }
                    return tip;
                }
                return super.getToolTipText(e);
            }
        };
        jbTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jbTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        jbTable.getColumnModel().getColumn(0).setMaxWidth(220);
        jbTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        jbTable.getColumnModel().getColumn(2).setMaxWidth(200);
        jbTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        jbTable.getColumnModel().getColumn(3).setMaxWidth(90);
        jbTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (column == 3) {
                    Component cmp = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (cmp instanceof JLabel) {
                        ((JLabel) cmp).setHorizontalAlignment(SwingConstants.CENTER);
                        return cmp;
                    }
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jbTable);
        decorator.disableUpDownActions();
        decorator.setAddAction(anActionButton -> {
            final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
            final VirtualFile file = FileChooser.chooseFile(descriptor, null, null);
            if (file != null && !file.isDirectory()) {
                String filepath = FileUtils.getLocationRelativeToUserHome(file.getPath());
                Certificate certificate = new Certificate();
                certificate.setHost(file.getNameWithoutExtension());
                certificate.setPfxFile(filepath);
                certificateModel.addRow(certificate);
            } else {
                certificateModel.addRow();
            }
            int rowCount = certificateModel.getRowCount();
            jbTable.setRowSelectionInterval(rowCount - 1, rowCount - 1);
        });
        decorator.addExtraAction(new BaseAnActionButton(() -> RestBundle.message("toolkit.config.certificate.selectpfx.title"), AllIcons.Actions.Upload) {

            @Override
            public void updateButton(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(jbTable.getSelectedRow() >= 0);
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
                final VirtualFile file = FileChooser.chooseFile(descriptor, null, null);
                if (file != null && !file.isDirectory()) {
                    String filepath = FileUtils.getLocationRelativeToUserHome(file.getPath());
                    certificateModel.setValueAt(filepath, jbTable.getSelectedRow(), 2);
                }
            }
        });
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
        return certificateModel.getItems()
                               .stream()
                               .map(Certificate::copy)
                               .collect(Collectors.toList());
    }
}
