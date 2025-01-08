package dk.spilstuff.engine.gen;

import java.io.FileInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.lwjgl.opengl.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL11.*;

public class GenUtil {
    public static int[] generateTexturesFromByteBufferList(ArrayList<ByteBuffer> bufferList, int[] w, int[] h) {
        int[] textures = new int[bufferList.size()];

        for(int i = 0; i < textures.length; i++) {
            if(bufferList.get(i) == null) {
                return null;
            }

            textures[i] = glGenTextures();
            glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, textures[i]);

            glTexParameteri(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexImage2D(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, 0, GL_RGBA, w[i], h[i], 0, GL_RGBA, GL_UNSIGNED_BYTE, bufferList.get(i));
        }

        glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, 0);

        return textures;
    }

    public static int generateTextureFromByteBuffer(ByteBuffer buffer, int w, int h) {
        int texture = glGenTextures();
        glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, texture);

        glTexParameteri(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexImage2D(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, 0);

        return texture;
    }

    public static ByteBuffer byteBufferFromBufferedImage(BufferedImage image, int width, int height) {
        if(image == null) {
            return null;
        }
        
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                buffer.put((byte) (pixel & 0xFF));         // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
        }

        buffer.flip();

        return buffer;
    }

    public static String generateChecksum(String folderPath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        Files.walk(Paths.get(folderPath))
             .filter(Files::isRegularFile)
             .forEach(file -> {
                 try (InputStream is = new FileInputStream(file.toFile())) {
                     byte[] buffer = new byte[1024];
                     int bytesRead;

                     // Include file name in the checksum
                     md.update(file.getFileName().toString().getBytes());

                     while ((bytesRead = is.read(buffer)) != -1) {
                         md.update(buffer, 0, bytesRead);
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             });

        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void writeChecksumToFile(String checksum, String outputPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(outputPath)) {
            writer.println(checksum);
        }
    }
}
