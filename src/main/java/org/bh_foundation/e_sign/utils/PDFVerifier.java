package org.bh_foundation.e_sign.utils;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignatureUtil;

public class PDFVerifier {

    public static String verifySignature(InputStream signedPdfStream) throws Exception {
        PdfReader reader = new PdfReader(signedPdfStream);
        reader.setUnethicalReading(true);
        PdfDocument pdfDoc = new PdfDocument(reader);

        SignatureUtil signUtil = new SignatureUtil(pdfDoc);
        List<String> names = signUtil.getSignatureNames();
        if (names.isEmpty())
            return "Tidak ada tanda tangan dalam dokumen";
        int index = 1;
        String endResult = "";
        for (String name : names) {
            String result = "";
            if (index == 1)
                result = "---------------------------------------------------\n";
            result += "[" + index + "]\n";
            PdfPKCS7 pkcs7 = signUtil.readSignatureData(name);
            boolean isVerified = pkcs7.verifySignatureIntegrityAndAuthenticity();
            result += "Valid? " + (isVerified ? "Ya" : "Tidak") + "\n";
            X509Certificate cert = (X509Certificate) pkcs7.getSigningCertificate();
            result += "Ditanda tangani oleh: " + cert.getSubjectX500Principal() + "\n";
            result += "Tanggal penanda tanganan: " + pkcs7.getSignDate().getTime() + "\n";
            result += "---------------------------------------------------\n";
            endResult += result;
            index++;
        }
        pdfDoc.close();
        return endResult;
    }

}
