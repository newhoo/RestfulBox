package io.github.newhoo.restkit.config.certificate;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CertificateComponent
 *
 * @author huzunrong
 * @since 2.1.2
 */
@Data
@State(name = "RESTKit_Certificate", storages = {@Storage("restkit/RESTKit_Certificate.xml")})
public class CertificateComponent implements PersistentStateComponent<CertificateComponent> {

    private List<Certificate> certificates = new LinkedList<>();

    public static CertificateComponent getInstance() {
        return ApplicationManager.getApplication().getService(CertificateComponent.class);
    }

    @NotNull
    @Override
    public CertificateComponent getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CertificateComponent state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }

    public boolean isModified(List<Certificate> others) {
        String s1 = certificates.stream().map(Certificate::toString).collect(Collectors.joining());
        String s2 = others.stream().map(Certificate::toString).collect(Collectors.joining());
        return !s1.equals(s2);
    }

    public Certificate getEnabledCertificate(String host) {
        return certificates.stream().filter(Certificate::getEnable)
                           .filter(e -> host.equals(e.getHost()))
                           .findFirst()
                           .orElse(null);
    }
}
