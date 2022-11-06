package io.github.newhoo.restkit.config.certificate;

import lombok.Data;

/**
 * Certificate
 *
 * @author huzunrong
 * @since 2.1.2
 */
@Data
public class Certificate {

    private Boolean enable;
    private String host;
    // private String crtFile;
    // private String keyFile;
    private String pfxFile;
    private String passphrase;

    public Certificate copy() {
        Certificate cert = new Certificate();
        cert.setEnable(enable);
        cert.setHost(host);
        cert.setPfxFile(pfxFile);
        cert.setPassphrase(passphrase);
        return cert;
    }
}