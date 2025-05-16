package org.bh_foundation.e_sign.utils;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.source.ByteArrayOutputStream;

public class QRCodeGenerator {

    public static byte[] generate(String text, float size) throws Exception {
        QRCodeWriter qrwriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrwriter.encode(text, BarcodeFormat.QR_CODE, Math.round(size), Math.round(size));
        BitMatrix croppedMatrix = removeWhiteBorder(bitMatrix);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(croppedMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private static BitMatrix removeWhiteBorder(BitMatrix matrix) {
        int[] enclosingRectangle = matrix.getEnclosingRectangle();
        int width = enclosingRectangle[2];
        int height = enclosingRectangle[3];
        BitMatrix croppedMatrix = new BitMatrix(width, height);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (matrix.get(x + enclosingRectangle[0], y + enclosingRectangle[1]))
                    croppedMatrix.set(x, y);
        return croppedMatrix;
    }

}
