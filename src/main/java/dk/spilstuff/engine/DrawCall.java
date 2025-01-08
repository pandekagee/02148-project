package dk.spilstuff.engine;

import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class DrawCall {
    final public int callType;

    public Sprite sprite = null;
    public TextFont textFont = null;
    public CharSequence text = null;
    public Color color = null;
    public int subimg = 0;
    public int depth;
    public double x;
    public double y;
    public double xScale = 1.0;
    public double yScale = 1.0;
    public double rotation = 0;
    public double alpha = 1.0;
    public int textureID = 0;

    public DrawCall(Sprite sprite, double subimg, int depth, double x, double y) {
        this.callType = 0;

        this.sprite = sprite;
        this.subimg = (int)subimg;
        this.depth = depth;
        this.x = x;
        this.y = y;
    }

    public DrawCall(Sprite sprite, double subimg, int depth, double x, double y, double xScale, double yScale, double rotation, Color color, double alpha) {
        this.callType = 1;

        this.sprite = sprite;
        this.subimg = (int)subimg;
        this.depth = depth;
        this.x = x;
        this.y = y;
        this.xScale = xScale;
        this.yScale = yScale;
        this.rotation = negMod(rotation, 360);
        this.color = color;
        this.alpha = alpha;
    }

    public DrawCall(TextFont textFont, CharSequence string, int depth, double x, double y) {
        this.callType = 2;

        this.textFont = textFont;
        this.text = string;
        this.depth = depth;
        this.x = x;
        this.y = y;
    }

    public DrawCall(TextFont textFont, CharSequence string, int depth, double x, double y, double xScale, double yScale, double rotation, Color color, double alpha) {
        this.callType = 3;

        this.textFont = textFont;
        this.text = string;
        this.depth = depth;
        this.x = x;
        this.y = y;
        this.xScale = xScale;
        this.yScale = yScale;
        this.rotation = negMod(rotation, 360);
        this.color = color;
        this.alpha = alpha;
    }

    public DrawCall(Sprite sprite, int backgroundType) {
        this.callType = -1 - backgroundType;

        this.sprite = sprite;
        this.subimg = 0;
        this.depth = 1000000;
    }

    public DrawCall(Scene surface, int depth, double x, double y) {
        this.callType = 4;
        
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, surface.getWidth(), surface.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        this.x = x;
        this.y = y;
        this.depth = depth;
        this.textureID = texture;
    }

    public DrawCall(Scene surface, int depth, double x, double y, double xScale, double yScale, double rotation, Color color, double alpha) {
        this.callType = 5;
        
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, surface.getWidth(), surface.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        this.x = x;
        this.y = y;
        this.depth = depth;
        this.textureID = texture;
        this.xScale = xScale;
        this.yScale = yScale;
        this.rotation = negMod(rotation, 360);
        this.color = color;
        this.alpha = alpha;
    }

    public String toString() {
        return "DrawCall(" + (sprite != null ? sprite.toString() : "") + (text != null ? text.toString() : "") + ", " + subimg + ", " + depth + ", " + x + ", " + y + ")";
    }

    private double negMod(double n1, double n2) {
        double res = n1 % n2;

        if(res < 0) {
            res += n2;
        }

        return res;
    }
}
