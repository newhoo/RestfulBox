package io.github.newhoo.restkit.config.certificate;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import io.github.newhoo.restkit.i18n.RestBundle;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * CertificateTableModel
 *
 * @author huzunrong
 * @since 2.1.2
 */
@SuppressWarnings("all")
public class CertificateTableModel extends ListTableModel<Certificate> {

    public CertificateTableModel() {
        super(
                new CertificateColumnInfo<String>("toolkit.config.certificate.table.header.host"),
                new CertificateColumnInfo<String>("toolkit.config.certificate.table.header.pfxfile"),
                new CertificateColumnInfo<String>("toolkit.config.certificate.table.header.passphrase"),
                new CertificateColumnInfo<Boolean>("toolkit.config.certificate.table.header.enable")
        );
    }

    @Override
    public void addRow() {
        addRow(new Certificate());
    }

    public static class CertificateColumnInfo<Aspect> extends ColumnInfo<Certificate, Aspect> {
        public CertificateColumnInfo(String name) {
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
        public Aspect valueOf(Certificate certificate) {
            String name = super.getName();
            switch (name) {
                case "toolkit.config.certificate.table.header.enable":
                    return (Aspect) certificate.getEnable();
                case "toolkit.config.certificate.table.header.host":
                    return (Aspect) certificate.getHost();
                case "toolkit.config.certificate.table.header.pfxfile":
                    return (Aspect) certificate.getPfxFile();
                case "toolkit.config.certificate.table.header.passphrase":
                    return (Aspect) certificate.getPassphrase();
                default:
            }
            return null;
        }

        @Override
        public void setValue(Certificate certificate, Aspect value) {
            String name = super.getName();
            switch (name) {
                case "toolkit.config.certificate.table.header.enable":
                    certificate.setEnable((Boolean) value);
                    break;
                case "toolkit.config.certificate.table.header.host":
                    certificate.setHost((String) value);
                    break;
                case "toolkit.config.certificate.table.header.pfxfile":
                    certificate.setPfxFile((String) value);
                    break;
                case "toolkit.config.certificate.table.header.passphrase":
                    certificate.setPassphrase((String) value);
                    break;
                default:
            }
        }

        @Override
        public Class<?> getColumnClass() {
            String name = super.getName();
            if ("toolkit.config.certificate.table.header.enable".equals(name)) {
                return Boolean.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(Certificate certificate) {
            return true;
        }
    }
}