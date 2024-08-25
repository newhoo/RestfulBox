package io.github.newhoo.restkit.config.certificate;

import io.github.newhoo.restkit.common.NotProguard;
import lombok.Data;

/**
 * Certificate
 *
 * @author huzunrong
 * @since 2.1.2
 */
@NotProguard
@Data
public class Certificate {

    private Integer id;
    private Boolean enable = false;
    private String host = "";
    // private String crtFile="";
    // private String keyFile="";
    private String pfxFile = "";
    private String pfxFileContent = "";
    private String passphrase = "";

    public Certificate copy() {
        Certificate cert = new Certificate();
        cert.setEnable(enable);
        cert.setHost(host);
        cert.setPfxFile(pfxFile);
        cert.setPfxFileContent(pfxFileContent);
        cert.setPassphrase(passphrase);
        return cert;
    }
}