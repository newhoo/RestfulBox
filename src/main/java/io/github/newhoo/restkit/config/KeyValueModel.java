package io.github.newhoo.restkit.config;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import io.github.newhoo.restkit.common.BKV;
import io.github.newhoo.restkit.i18n.RestBundle;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * KeyValueTableModel
 *
 * @author huzunrong
 * @since 1.0
 */
public class KeyValueModel extends ListTableModel<BKV> {

    public KeyValueModel() {
        super(
                new KeyValueModelColumnInfo<String>("toolkit.common.table.header.name"),
                new KeyValueModelColumnInfo<String>("toolkit.common.table.header.value"),
                new KeyValueModelColumnInfo<Boolean>("toolkit.common.table.header.enable")
        );
    }

    @Override
    public void addRow() {
        addRow(new BKV(true, "", ""));
    }

    public void addRow(boolean enable, String name, String value) {
        addRow(new BKV(enable, name, value));
    }

    public static class KeyValueModelColumnInfo<Aspect> extends ColumnInfo<BKV, Aspect> {
        public KeyValueModelColumnInfo(String name) {
            super(name);
        }

        @Override
        public @NlsContexts.ColumnName String getName() {
            String name = super.getName();
            if (StringUtils.isNotEmpty(name)) {
                return RestBundle.message(name);
            }
            return name;
        }

        @Nullable
        @Override
        public Aspect valueOf(BKV item) {
            String name = super.getName();
            switch (name) {
                case "toolkit.common.table.header.enable":
                    return (Aspect) item.getEnabled();
                case "toolkit.common.table.header.name":
                    return (Aspect) item.getKey();
                case "toolkit.common.table.header.value":
                    return (Aspect) item.getValue();
                default:
            }
            return null;
        }

        @Override
        public void setValue(BKV item, Aspect value) {
            String name = super.getName();
            switch (name) {
                case "toolkit.common.table.header.enable":
                    item.setEnabled((Boolean) value);
                    break;
                case "toolkit.common.table.header.name":
                    item.setKey((String) value);
                    break;
                case "toolkit.common.table.header.value":
                    item.setValue((String) value);
                    break;
                default:
            }
        }

        @Override
        public Class<?> getColumnClass() {
            String name = super.getName();
            if ("toolkit.common.table.header.enable".equals(name)) {
                return Boolean.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(BKV item) {
            return true;
        }
    }
}