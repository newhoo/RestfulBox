package io.github.newhoo.restkit.config;

import com.intellij.util.ui.EditableModel;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * KeyValueTableModel
 *
 * @author huzunrong
 * @since 1.0
 */
public class KeyValueTableModel extends AbstractTableModel implements EditableModel {

    public List<Object[]> list = new ArrayList<>();

    private String[] head = {"", "KEY", "VALUE"};

    private Class<?>[] typeArray = {Boolean.class, Object.class, Object.class};

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return head.length;
    }

    @Override
    public String getColumnName(int column) {
        return head[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return list.get(rowIndex)[columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        list.get(rowIndex)[columnIndex] = aValue;
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return typeArray[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void addRow(boolean enable, String name, String value) {
        list.add(new Object[]{enable, name, value});
        fireTableRowsInserted(getRowCount() - 1, getRowCount());
    }

    public void clear() {
        list.clear();
        fireTableDataChanged();
    }

    @Override
    public void addRow() {
        list.add(new Object[]{true, "", ""});
        fireTableRowsInserted(getRowCount() - 1, getRowCount());
    }

    @Override
    public void exchangeRows(int oldIndex, int newIndex) {
        Object[] objects = list.get(oldIndex);
        list.set(oldIndex, list.get(newIndex));
        list.set(newIndex, objects);
        fireTableDataChanged();
    }

    @Override
    public boolean canExchangeRows(int oldIndex, int newIndex) {
        return true;
    }

    @Override
    public void removeRow(int idx) {
        list.remove(idx);
        fireTableRowsDeleted(0, getRowCount());
    }
}