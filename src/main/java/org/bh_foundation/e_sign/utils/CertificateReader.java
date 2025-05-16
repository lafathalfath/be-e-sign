package org.bh_foundation.e_sign.utils;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class CertificateReader {
    
    public static String getSubject() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("src/main/resources/certificate.p12"), "password".toCharArray());
        String alias = keyStore.aliases().nextElement();
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
        return cert.getSubjectX500Principal().getName();
    }

}
