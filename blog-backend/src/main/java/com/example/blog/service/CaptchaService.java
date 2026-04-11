package com.example.blog.service;

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
    private final ConcurrentHashMap<String, String> captchaStore = new ConcurrentHashMap<>();

    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    /**
     * Generate captcha image
     * @return Map containing captchaId and image base64
     */
    public Map<String, String> generateCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String code = generateCode();

        // Store code with captchaId
        captchaStore.put(captchaId, code);

        // Generate image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        //干扰点
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.fillOval(x, y, 2, 2);
        }

        // Draw code
        g.setFont(new Font("Arial", Font.BOLD, 24));
        int charWidth = width / (codeLength + 1);
        int startX = charWidth / 2;

        for (int i = 0; i < codeLength; i++) {
            String ch = String.valueOf(code.charAt(i));

            // Random color
            g.setColor(new Color(
                    20 + random.nextInt(100),
                    20 + random.nextInt(100),
                    20 + random.nextInt(100)
            ));

            // Slight rotation
            double angle = (random.nextDouble() - 0.5) * 0.5;
            g.rotate(angle, startX + i * charWidth + 10, height / 2);

            g.drawString(ch, startX + i * charWidth, height / 2 + 10);
            g.rotate(-angle, startX + i * charWidth + 10, height / 2);
        }

        //干扰线
        for (int i = 0; i < 3; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();

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
     * Verify captcha code
     * @param captchaId Captcha ID
     * @param code User input code
     * @return true if valid
     */
    public boolean verifyCaptcha(String captchaId, String code) {
        if (captchaId == null || code == null) {
            return false;
        }

        String storedCode = captchaStore.get(captchaId);
        if (storedCode == null) {
            return false;
        }

        // Case insensitive
        boolean valid = storedCode.equalsIgnoreCase(code.trim());

        // Remove used captcha
        if (valid) {
            captchaStore.remove(captchaId);
        }

        return valid;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return code.toString();
    }
}
