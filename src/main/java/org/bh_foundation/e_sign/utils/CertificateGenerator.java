package org.bh_foundation.e_sign.utils;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.UUID;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;


public class CertificateGenerator {

    public static X509v3CertificateBuilder generate(X500Name issuer, BigInteger serialNumber, Date notBefore,
            Date notAfter, X500Name subject, PublicKey publicKey) {
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serialNumber,
                notBefore,
                notAfter,
                subject,
                publicKey);
        return certBuilder;
    }

    public static byte[] generateP12(String email, String commonName, String location, String state, String country, String passphrase, Integer maxAgeDays) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // generate KeyPair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        // generate serial number
        // UUID uuid = UUID.randomUUID();
        UUID uuid = UUID.nameUUIDFromBytes((UUID.randomUUID().toString() + System.nanoTime()).getBytes());
        BigInteger serial = new BigInteger(uuid.toString().replace("-", ""), 16);
        // set subject and issuer
        String subDn = "CN=" + commonName + ", E=" + email;
        X500Name subject = new X500Name(subDn);
        X500NameBuilder issuerBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        issuerBuilder.addRDN(BCStyle.C, country);
        issuerBuilder.addRDN(BCStyle.ST, state);
        issuerBuilder.addRDN(BCStyle.L, location);
        issuerBuilder.addRDN(BCStyle.O, "BHF");
        issuerBuilder.addRDN(BCStyle.OU, "BHF");
        issuerBuilder.addRDN(BCStyle.CN, "Bogor Heritage Foundation");
        X500Name issuer = issuerBuilder.build();
        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + (Long.parseLong(""+maxAgeDays)*24*60*60*1000));
        // build certificate
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
            issuer, serial, notBefore, notAfter, subject, keyPair.getPublic()
        );
        X509Certificate cert= new JcaX509CertificateConverter()
            .setProvider("BC")
            .getCertificate(certBuilder.build(signer));
        // store to PKCS12
        KeyStore pkcs12 = KeyStore.getInstance("PKCS12");
        pkcs12.load(null, null);
        pkcs12.setKeyEntry("key", keyPair.getPrivate(), passphrase.toCharArray(), new X509Certificate[]{cert});
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pkcs12.store(baos, passphrase.toCharArray());
        return baos.toByteArray();
    }

}
