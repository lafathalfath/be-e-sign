package org.bh_foundation.e_sign.utils;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.bh_foundation.e_sign.models.User;

public class ImageUtility {

    public static BufferedImage addBorderImageSign(BufferedImage inputImage, int padding, int borderWidth, String text,
            User user, int fontSize) {
        String hexDate = Integer.toHexString(Math.abs(LocalDateTime.now().hashCode()));
        String text2 = hexDate + "-" + Integer.toHexString(Math.abs(user.getEmail().hashCode())) + "-"
                + Integer.toHexString(Math.abs(user.getUsername().hashCode()));
        text2 = text2.toUpperCase();
        int textPadding = 5; // Jarak antara border dan teks
        int textHeight = fontSize + textPadding;
        int newWidth = inputImage.getWidth() + padding * 2;
        int newHeight = inputImage.getHeight() + padding * 2 + textHeight;
        // int newHeight = 2*newWidth/3;
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, newWidth, newHeight);
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Draw image
        g2d.drawImage(inputImage, padding, padding, null);
        // Set font and measure text
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textWidth2 = fm.stringWidth(text2);
        int textX = (newWidth - textWidth) / 2;
        int textY = textPadding * 4;
        // Draw text
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, textX, textY + (newWidth / 5)); // text1
        g2d.drawString(text2, textX, newHeight - (newWidth / 5)); // text2
        // Draw border with gap at bottom center (based on text width)
        g2d.setStroke(new BasicStroke(borderWidth));
        g2d.drawLine(0, (newWidth / 5) + (textHeight + padding) / 2, 0, (newHeight - textHeight / 2) - (newWidth / 5)); // Left
        g2d.drawLine(newWidth - 1, (newWidth / 5) + (textHeight + padding) / 2, newWidth - 1,
                (newHeight - textHeight / 2) - (newWidth / 5)); // Right
        int gapMargin = 10;
        int gapStart = textX - gapMargin;
        int gapEnd = textX + textWidth + gapMargin;
        int gapEnd2 = textX + textWidth2 + gapMargin;
        g2d.setStroke(new BasicStroke(borderWidth / 2));
        if (gapStart > 0)
            g2d.drawLine(0, (newWidth / 5) + (textHeight / 2), gapStart, (newWidth / 5) + (textHeight / 2));
        if (gapEnd < newWidth)
            g2d.drawLine(gapEnd, (newWidth / 5) + (textHeight / 2), newWidth - 1, (newWidth / 5) + (textHeight / 2));

        if (gapStart > 0)
            g2d.drawLine(0, (newHeight - textHeight / 4) - (newWidth / 5), gapStart,
                    (newHeight - textHeight / 4) - (newWidth / 5)); // Bottom
        if (gapEnd2 < newWidth)
            g2d.drawLine(gapEnd2, (newHeight - textHeight / 4) - (newWidth / 5), newWidth - 1,
                    (newHeight - textHeight / 4) - (newWidth / 5)); // Bottom
        g2d.dispose();
        return outputImage;
    }

    public static BufferedImage addTextQr(BufferedImage inputImage, String username, String CLIENT_URL) {
        if (CLIENT_URL.startsWith("http://"))
            CLIENT_URL = CLIENT_URL.replace("http://", "");
        else if (CLIENT_URL.startsWith("https://"))
            CLIENT_URL = CLIENT_URL.replace("https://", "");
        String signedBy = "Digitally signed by:";
        String title = username;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
        String dateTime = LocalDateTime.now().format(dateTimeFormatter);
        String date = "Date: " + dateTime;
        String verifyAt = "Verifiy at " + CLIENT_URL;
        int width = inputImage.getWidth() * 3;
        int height = inputImage.getHeight();
        int titleSize = height / 6;
        int fontSize = height / 10;
        int textPadding = height / 20;
        int paddingTextToImage = height / 20;
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(inputImage, 0, 0, null);
        int textHeight = fontSize + textPadding * 2;
        int textX = inputImage.getWidth() + paddingTextToImage;
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        g2d.setColor(Color.BLACK);
        g2d.drawString(signedBy, textX, textPadding * 2);
        g2d.setFont(new Font("Arial", Font.BOLD, titleSize));
        g2d.drawString(title, textX, textHeight + textPadding * 2);
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        g2d.drawString(date, textX, height - textHeight);
        g2d.drawString(verifyAt, textX, height - textPadding);
        g2d.dispose();
        return outputImage;
    }

    public static BufferedImage addBorderTextQrSign(BufferedImage inputImage, String CLIENT_URL) {
        if (CLIENT_URL.startsWith("http://"))
            CLIENT_URL = CLIENT_URL.replace("http://", "");
        else if (CLIENT_URL.startsWith("https://"))
            CLIENT_URL = CLIENT_URL.replace("https://", "");
        String text = "digitally signed";
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int fontSize = inputImage.getHeight()/20;
        int textPadding = inputImage.getHeight()/40;
        BufferedImage outputImage = new BufferedImage(width + textPadding, height + textPadding, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(inputImage, 0, 0, null);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth= fm.stringWidth(text) + textPadding * 2;
        int urlWidth= fm.stringWidth(CLIENT_URL) + textPadding * 2;
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        g2d.drawString(text, textPadding + width/2, textPadding*2);
        g2d.drawString(CLIENT_URL, textPadding + width/2, height);
        g2d.setStroke(new BasicStroke(height/80));
        g2d.drawLine(textWidth*2 - textPadding + width/2, textPadding, width - textPadding, textPadding);
        g2d.drawLine(width - textPadding, textPadding, width - textPadding, height - textPadding);
        g2d.drawLine(urlWidth*2 - textPadding + width/2, height - textPadding, width - textPadding, height - textPadding);
        g2d.dispose();
        return outputImage;
    }

}
