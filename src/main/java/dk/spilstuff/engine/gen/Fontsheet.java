package dk.spilstuff.engine.gen;

import dk.spilstuff.engine.*;

import java.util.HashMap;

import java.awt.image.BufferedImage;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.*;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

import java.awt.Font;

public class Fontsheet {
    private static int fontAmount;

    public static HashMap<String, TextFont> generate(String gameName) {
        // Delete existing fontsheets - we're making new ones.
        File[] runtimeFiles = new File(gameName + "\\runtime\\fonts").listFiles();
        for(File file : runtimeFiles) {
            file.delete();
        }
        
        File[] files = new File(gameName + "\\Fonts").listFiles();

        fontAmount = files.length + 2;

        Font[] fonts = new Font[fontAmount];

        fonts[0] = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
        fonts[1] = new Font(Font.MONOSPACED, Font.PLAIN, 16);

        HashMap<String, TextFont> textFontMap = new HashMap<String, TextFont>();

        textFontMap.put("Default", createTextFont("Default", fonts[0], false, 0));
        textFontMap.put("Mono", createTextFont("Mono", fonts[1], false, 0));

        for(int i = 0; i < fontAmount - 2; i++) {
            try {
                InputStream in = new FileInputStream(files[i].getAbsolutePath());

                textFontMap.put(files[i].getName(), createTextFont(files[i].getName(), Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(Font.PLAIN, 16), true, 0));
            }catch(Exception e){

            }
        }
        
        return textFontMap;
    }

    public static TextFont createTextFont(String name, Font font, boolean antiAlias, int separation) {
        int imageWidth = 0;
        int imageHeight = 0;

        for (int i = 32; i < 256; i++) {
            if (i == 127) {
                continue;
            }
            char c = (char) i;
            BufferedImage ch = createCharImage(font, c, antiAlias);
            if (ch == null) {
                continue;
            }

            imageWidth += ch.getWidth() + separation;
            imageHeight = Math.max(imageHeight, ch.getHeight());
        }

        int fontHeight = imageHeight;

        HashMap<Character, Glyph> glyphs = new HashMap<Character, Glyph>();

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        int x = 0;

        for (int i = 32; i < 256; i++) {
            if (i == 127) {
                continue;
            }
            char c = (char) i;
            BufferedImage charImage = createCharImage(font, c, antiAlias);
            if (charImage == null) {
                continue;
            }

            int charWidth = charImage.getWidth() + separation;
            int charHeight = charImage.getHeight();

            Glyph ch = new Glyph(charWidth, charHeight, x, image.getHeight() - charHeight, 0f);
            g.drawImage(charImage, x, 0, null);
            x += ch.width;
            glyphs.put(c, ch);
        }

        // Saving the data
        try {
            ImageIO.write(image, "png", new File(Game.gameName + "\\runtime\\fonts\\"+name+".png"));
        }
        catch(IOException e) {
            Logger.addError("Failed to write font image!");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        ByteBuffer buffer = GenUtil.byteBufferFromBufferedImage(image, width, height);

        int fontTexture = GenUtil.generateTextureFromByteBuffer(buffer, width, height);

        return new TextFont(glyphs, fontHeight, fontTexture);
    }

    private static BufferedImage createCharImage(Font font, char c, boolean antiAlias) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.dispose();

        int charWidth = metrics.charWidth(c);
        int charHeight = metrics.getHeight();

        if (charWidth == 0) {
            return null;
        }

        image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        g.setPaint(java.awt.Color.WHITE);
        g.drawString(String.valueOf(c), 0, metrics.getAscent());
        g.dispose();

        return image;
    }
}
