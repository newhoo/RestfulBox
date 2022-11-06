package io.github.newhoo.restkit.config.certificate;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * CertificateConfigurable
 *
 * @author huzunrong
 * @since 2.1.2
 */
public class CertificateConfigurable implements Configurable {

    private final CertificateComponent certificateComponent;
    private final CertificateForm certificateForm;

    private CertificateConfigurable(Project project) {
        this.certificateComponent = CertificateComponent.getInstance();
        this.certificateForm = new CertificateForm(project);
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Certificate";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return certificateForm;
    }

    @Override
    public boolean isModified() {
        return certificateComponent.isModified(certificateForm.getCertificates());
    }

    @Override
    public void apply() {
        certificateComponent.setCertificates(certificateForm.getCertificates());
    }

    @Override
    public void reset() {
        certificateForm.reset(certificateComponent.getCertificates());
    }
}