package com.example.blog.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private final int width = 120;
    private final int height = 40;
    private final int codeLength = 4;

    // Store captcha codes temporarily (in production, use Redis)
    private final ConcurrentHashMap<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();

    private static class CaptchaEntry {
        final String code;
        final long createdAt;

        CaptchaEntry(String code) {
            this.code = code;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 300_000; // 5 minutes
        }
    }

    // Use simple block characters that can be drawn without font files
    private static final char[] CAPTCHA_CHARS = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
        'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '2', '3', '4', '5', '6', '7', '8', '9'
    };

    // Simple pixel-based rendering without using Font
    private static final int[][][] CHAR_PATTERNS = {
        // 0 - A
        {{0,1,1,0}, {1,0,0,1}, {1,1,1,1}, {1,0,0,1}, {1,0,0,1}},
        // 1 - B
        {{1,1,1,0}, {1,0,0,1}, {1,1,1,0}, {1,0,0,1}, {1,1,1,0}},
        // 2 - C
        {{0,1,1,1}, {1,0,0,0}, {1,0,0,0}, {1,0,0,0}, {0,1,1,1}},
        // 3 - D
        {{1,1,1,0}, {1,0,0,1}, {1,0,0,1}, {1,0,0,1}, {1,1,1,0}},
        // 4 - E
        {{1,1,1,1}, {1,0,0,0}, {1,1,1,0}, {1,0,0,0}, {1,1,1,1}},
        // 5 - F
        {{1,1,1,1}, {1,0,0,0}, {1,1,1,0}, {1,0,0,0}, {1,0,0,0}},
        // 6 - G
        {{0,1,1,1}, {1,0,0,0}, {1,0,1,1}, {1,0,0,1}, {0,1,1,1}},
        // 7 - H
        {{1,0,0,1}, {1,0,0,1}, {1,1,1,1}, {1,0,0,1}, {1,0,0,1}},
        // 8 - J
        {{0,0,1,1}, {0,0,0,1}, {0,0,0,1}, {1,0,0,1}, {0,1,1,0}},
        // 9 - K
        {{1,0,0,1}, {1,0,1,0}, {1,1,0,0}, {1,0,1,0}, {1,0,0,1}},
        // 10 - L
        {{1,0,0,0}, {1,0,0,0}, {1,0,0,0}, {1,0,0,0}, {1,1,1,1}},
        // 11 - M
        {{1,0,0,1}, {1,1,1,1}, {1,0,0,1}, {1,0,0,1}, {1,0,0,1}},
        // 12 - N
        {{1,0,0,1}, {1,1,0,1}, {1,0,1,1}, {1,0,0,1}, {1,0,0,1}},
        // 13 - P
        {{1,1,1,0}, {1,0,0,1}, {1,1,1,0}, {1,0,0,0}, {1,0,0,0}},
        // 14 - Q
        {{0,1,1,0}, {1,0,0,1}, {1,0,0,1}, {1,0,1,0}, {0,1,0,1}},
        // 15 - R
        {{1,1,1,0}, {1,0,0,1}, {1,1,1,0}, {1,0,1,0}, {1,0,0,1}},
        // 16 - S
        {{0,1,1,1}, {1,0,0,0}, {0,1,1,0}, {0,0,0,1}, {1,1,1,0}},
        // 17 - T
        {{1,1,1,1}, {0,1,0,0}, {0,1,0,0}, {0,1,0,0}, {0,1,0,0}},
        // 18 - U
        {{1,0,0,1}, {1,0,0,1}, {1,0,0,1}, {1,0,0,1}, {0,1,1,0}},
        // 19 - V
        {{1,0,0,1}, {1,0,0,1}, {1,0,0,1}, {0,1,1,0}, {0,1,0,0}},
        // 20 - W
        {{1,0,1,0}, {1,0,1,0}, {1,0,1,0}, {1,1,1,1}, {1,0,1,0}},
        // 21 - X
        {{1,0,0,1}, {0,1,1,0}, {0,1,1,0}, {0,1,1,0}, {1,0,0,1}},
        // 22 - Y
        {{1,0,0,1}, {1,0,0,1}, {0,1,1,0}, {0,1,0,0}, {0,1,0,0}},
        // 23 - Z
        {{1,1,1,1}, {0,0,1,0}, {0,1,0,0}, {1,0,0,0}, {1,1,1,1}},
        // 24 - 2
        {{0,1,1,0}, {1,0,0,1}, {0,0,1,0}, {0,1,0,0}, {1,1,1,1}},
        // 25 - 3
        {{1,1,1,0}, {0,0,0,1}, {0,1,1,0}, {0,0,0,1}, {1,1,1,0}},
        // 26 - 4
        {{1,0,0,1}, {1,0,0,1}, {1,1,1,1}, {0,0,0,1}, {0,0,0,1}},
        // 27 - 5
        {{1,1,1,1}, {1,0,0,0}, {1,1,1,0}, {0,0,0,1}, {1,1,1,0}},
        // 28 - 6
        {{0,1,1,0}, {1,0,0,0}, {1,1,1,0}, {1,0,0,1}, {0,1,1,0}},
        // 29 - 7
        {{1,1,1,1}, {0,0,0,1}, {0,0,1,0}, {0,1,0,0}, {0,1,0,0}},
        // 30 - 8
        {{0,1,1,0}, {1,0,0,1}, {0,1,1,0}, {1,0,0,1}, {0,1,1,0}},
        // 31 - 9
        {{0,1,1,0}, {1,0,0,1}, {0,1,1,1}, {0,0,0,1}, {0,1,1,0}}
    };

    /**
     * Generate captcha image using pixel-based rendering (no fonts required)
     * @return Map containing captchaId and image base64
     */
    public Map<String, String> generateCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String code = generateCode();

        // Store code with captchaId
        captchaStore.put(captchaId, new CaptchaEntry(code));

        // Generate image using pixel-based rendering
        BufferedImage image = drawCaptchaPixel(code);

        // Convert to base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64 = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes);
            baos.close();

            Map<String, String> result = new HashMap<>();
            result.put("captchaId", captchaId);
            result.put("image", base64);

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate captcha", e);
        }
    }

    /**
     * Draw captcha using pixel patterns - no fonts needed
     */
    private BufferedImage drawCaptchaPixel(String code) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // White background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        Random random = new Random();

        // Draw interference dots
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.fillRect(x, y, 2, 2);
        }

        // Draw each character using pixel patterns
        int charWidth = 20;
        int startX = 10;
        int charIndex = 0;

        for (char c : code.toCharArray()) {
            int charCode = getCharCodeIndex(c);
            int[][] pattern = CHAR_PATTERNS[charCode];

            // Random color for each character
            Color charColor = new Color(
                20 + random.nextInt(100),
                20 + random.nextInt(100),
                20 + random.nextInt(100)
            );
            g.setColor(charColor);

            int pixelSize = 4;
            int offsetX = startX + charIndex * charWidth;
            int offsetY = 5;

            // Draw character pattern
            for (int row = 0; row < pattern.length; row++) {
                for (int col = 0; col < pattern[row].length; col++) {
                    if (pattern[row][col] == 1) {
                        int x = offsetX + col * pixelSize;
                        int y = offsetY + row * pixelSize;
                        g.fillRect(x, y, pixelSize - 1, pixelSize - 1);
                    }
                }
            }

            charIndex++;
        }

        // Draw interference lines
        g.setStroke(new BasicStroke(1));
        for (int i = 0; i < 3; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();
        return image;
    }

    private int getCharCodeIndex(char c) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        int index = chars.indexOf(c);
        return index >= 0 ? index : 0;
    }

    /**
     * Verify captcha code
     */
    public boolean verifyCaptcha(String captchaId, String code) {
        if (captchaId == null || code == null) {
            return false;
        }

        CaptchaEntry entry = captchaStore.get(captchaId);
        if (entry == null) {
            return false;
        }

        if (entry.isExpired()) {
            captchaStore.remove(captchaId);
            return false;
        }

        boolean valid = entry.code.equalsIgnoreCase(code.trim());

        if (valid) {
            captchaStore.remove(captchaId);
        }

        return valid;
    }

    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void cleanExpiredCaptchas() {
        captchaStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        String chars = new String(CAPTCHA_CHARS);
        for (int i = 0; i < codeLength; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}
