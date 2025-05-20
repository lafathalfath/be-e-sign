package org.bh_foundation.e_sign.services.data;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bh_foundation.e_sign.dto.VerificationResult;
import org.bh_foundation.e_sign.models.DocumentApproval;
import org.bh_foundation.e_sign.repository.DocumentApprovalRepository;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignatureUtil;

@Service
public class PDFVerifierService {

    private final DocumentApprovalRepository approvalRepository;

    public PDFVerifierService(DocumentApprovalRepository approvalRepository) {
        this.approvalRepository = approvalRepository;
    }

    public List<?> verifySignature(InputStream signedPdfStream) throws Exception {
        PdfReader reader = new PdfReader(signedPdfStream);
        reader.setUnethicalReading(true);
        PdfDocument pdfDoc = new PdfDocument(reader);

        SignatureUtil signUtil = new SignatureUtil(pdfDoc);
        List<String> names = signUtil.getSignatureNames();
        if (names.isEmpty())
            return List.of("Tidak ada tanda tangan dalam dokumen");
        int index = 1;
        List<VerificationResult> endResult = new ArrayList<>();
        for (String name : names) {
            VerificationResult result = new VerificationResult();
            result.setNumber(index);
            PdfPKCS7 pkcs7 = signUtil.readSignatureData(name);
            boolean isVerified = pkcs7.verifySignatureIntegrityAndAuthenticity();
            @SuppressWarnings("deprecation")
            Locale locale = new Locale("id", "ID");
            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd MM yyyy - HH.mm.ss", locale);
            if (isVerified) {
                String serial = pkcs7.getSigningCertificate().getSerialNumber().toString(16).toUpperCase();
                List<DocumentApproval> approval = approvalRepository.findBySerialNumber(serial);
                if (!approval.isEmpty()) {
                    result.setValidity(true);
                    result.setSignedAt(formatter.format(pkcs7.getSignDate().getTime()));
                    result.setSerialNumber(serial);
                    String[] subjectDnList = pkcs7.getSigningCertificate().getSubjectX500Principal().getName().split(",");
                    String[] issuerDnList = pkcs7.getSigningCertificate().getIssuerX500Principal().getName().split(",");
                    for (String item : subjectDnList)
                        if (item.startsWith("CN=")) {
                            result.setSigner(item.substring(3));
                            break;
                        }
                    for (String item : issuerDnList)
                        if (item.startsWith("CN=")) {
                            result.setCertifiedBy(item.substring(3));
                            break;
                        }
                }
            }
            endResult.add(result);
            index++;
        }
        pdfDoc.close();
        return endResult;
    }

}
