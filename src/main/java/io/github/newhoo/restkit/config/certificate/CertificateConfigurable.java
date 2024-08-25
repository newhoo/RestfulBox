package io.github.newhoo.restkit.config.certificate;

import com.intellij.openapi.options.Configurable;
import io.github.newhoo.restkit.common.RestConstant;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.util.FileUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;

import javax.swing.*;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CertificateConfigurable
 *
 * @author huzunrong
 * @since 2.1.2
 */
public class CertificateConfigurable implements Configurable {

    private final CertificateForm certificateForm;

    private final DataSource repository = DataSourceHelper.getDataSource();
    private final List<Certificate> cacheList = new LinkedList<>();

    public CertificateConfigurable() {
        this.certificateForm = new CertificateForm();
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Certificate";
    }

    @Override
    public JComponent createComponent() {
        return certificateForm;
    }

    @Override
    public boolean isModified() {
        return isModified(cacheList, certificateForm.getCertificates());
    }

    @Override
    public void apply() {
        List<Certificate> certificates = certificateForm.getCertificates();
        cacheList.clear();
        cacheList.addAll(certificates);

        if (!Objects.equals(repository.name(), RestConstant.DATA_SOURCE_IDE)) {
            cacheList.forEach(certificate -> {
                String filepath = FileUtils.expandUserHome(certificate.getPfxFile());
                try (FileInputStream fis = new FileInputStream(filepath);) {
                    byte[] bytes = fis.readAllBytes();
                    String s = Base64.getEncoder().encodeToString(bytes);
                    certificate.setPfxFileContent(s);
                } catch (Exception e) {
                    System.err.println("获取证书文件失败: " + e.toString());
                }
            });
        }

        repository.syncCertificate(cacheList, null);
    }

    @Override
    public void reset() {
        if (cacheList.isEmpty()) {
            List<Certificate> certificates = repository.selectCertificate(null);
            cacheList.addAll(certificates);
        }
        certificateForm.reset(cacheList);
    }

    private boolean isModified(List<Certificate> first, List<Certificate> second) {
        String s1 = first.stream().map(Certificate::toString).collect(Collectors.joining());
        String s2 = second.stream().map(Certificate::toString).collect(Collectors.joining());
        return !s1.equals(s2);
    }
}