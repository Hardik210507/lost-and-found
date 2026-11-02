package com.campus.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Locale;
import javax.imageio.ImageIO;

public final class ImageHasher {

    private ImageHasher() {
    }

    public static Long averageHash(String imageData) {
        if (imageData == null || imageData.isBlank()) {
            return null;
        }

        try {
            String base64Data = imageData.trim();
            int commaIdx = base64Data.indexOf(',');
            if (commaIdx != -1 && base64Data.substring(0, commaIdx).toLowerCase(Locale.ROOT).contains("base64")) {
                base64Data = base64Data.substring(commaIdx + 1);
            }

            byte[] decoded = Base64.getMimeDecoder().decode(base64Data);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(decoded));
            if (img == null) {
                return null;
            }

            Image scaled = img.getScaledInstance(8, 8, Image.SCALE_AREA_AVERAGING);
            BufferedImage canvas = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = canvas.createGraphics();
            try {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, 8, 8);
                // In headless environments, drawImage with null observer on getScaledInstance is synchronous for BufferedImage
                g.drawImage(scaled, 0, 0, null);
            } finally {
                g.dispose();
            }

            double[] lum = new double[64];
            double sum = 0.0;
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    int rgb = canvas.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int gVal = (rgb >> 8) & 0xFF;
                    int bVal = rgb & 0xFF;
                    double l = 0.299 * r + 0.587 * gVal + 0.114 * bVal;
                    lum[y * 8 + x] = l;
                    sum += l;
                }
            }

            double mean = sum / 64.0;
            long hash = 0L;
            for (int i = 0; i < 64; i++) {
                if (lum[i] > mean) {
                    hash |= (1L << (63 - i));
                }
            }
            return hash;
        } catch (Throwable t) {
            return null;
        }
    }

    public static int distance(long a, long b) {
        return Long.bitCount(a ^ b);
    }
}
