package dk.spilstuff.engine;

import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Sprite{
    final private SpriteImage[] sprites;
    final private String spriteName;
    final private int imgCount;
    final private int xoffset;
    final private int yoffset;
    final private int width;
    final private int height;

    public class SpriteImage {
        private final int pos;
        private final int x;
        private final int y;
        private final int w;
        private final int h;
        private final int sheet;

        public SpriteImage(int pos, int x, int y, int w, int h, int sheet) {
            this.pos = pos;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.sheet = sheet;
        }

        public int getPosition() {
            return pos;
        }
    
        public int getX() {
            return x;
        }
    
        public int getY() {
            return y;
        }
    
        public int getWidth() {
            return w;
        }
    
        public int getHeight() {
            return h;
        }

        public int getSheet() {
            return sheet;
        }
    }
    
    public Sprite(String spriteName, int xoffset, int yoffset) throws RuntimeException {
        this.spriteName = spriteName;

        try {
            JSONObject jsonObject = new JSONObject(new JSONTokener(new FileReader(Game.gameName + "\\runtime\\sprites\\spriteSheetData.json")));
            JSONObject subImageData = jsonObject.getJSONObject(this.spriteName);

            this.imgCount = subImageData.length();
            this.sprites = new SpriteImage[subImageData.length()];

            for(int i = 0; i < subImageData.length(); i++) {
                try {
                    JSONObject o = subImageData.getJSONObject(""+i);
                    this.sprites[i] = new SpriteImage(i, o.getInt("x"), o.getInt("y"), o.getInt("w"), o.getInt("h"), o.getInt("sheet"));
                }
                catch(JSONException e) {
                    Logger.addError("spriteSheetData failed to parse!");
                }
            }

            this.width = this.sprites[0].getWidth();
            this.height = this.sprites[0].getHeight();

            this.xoffset = xoffset;
            this.yoffset = yoffset;
        }
        catch(IOException e) {
            Logger.addError("Sprite asset \"" + spriteName + "\" not found.");
            
            throw new RuntimeException();
        }
    }

    public Sprite(String spriteName, boolean centered) throws RuntimeException {
        this.spriteName = spriteName;

        try {
            JSONObject jsonObject = new JSONObject(new JSONTokener(new FileReader(Game.gameName + "\\runtime\\sprites\\spriteSheetData.json")));
            JSONObject subImageData = jsonObject.getJSONObject(this.spriteName);

            this.imgCount = subImageData.length();
            this.sprites = new SpriteImage[subImageData.length()];

            for(int i = 0; i < subImageData.length(); i++) {
                try {
                    JSONObject o = subImageData.getJSONObject(""+i);
                    this.sprites[i] = new SpriteImage(i, o.getInt("x"), o.getInt("y"), o.getInt("w"), o.getInt("h"), o.getInt("sheet"));
                }
                catch(JSONException e) {
                    Logger.addError("spriteSheetData failed to parse!");
                }
            }

            this.width = this.sprites[0].getWidth();
            this.height = this.sprites[0].getHeight();

            if(centered) {
                this.xoffset = this.width / 2;
                this.yoffset = this.height / 2;
            }
            else {
                this.xoffset = 0;
                this.yoffset = 0;
            }
        }
        catch(IOException e) {
            Logger.addError("Sprite asset \"" + spriteName + "\" not found.");
            
            throw new RuntimeException();
        }
    }

    public SpriteImage getSubImg(int subimg) {
        return this.sprites[subimg % imgCount];
    }

    public String getSpriteName() {
        return this.spriteName;
    }

    public int getXOffset() {
        return this.xoffset;
    }

    public int getYOffset() {
        return this.yoffset;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSubImgCount() {
        return imgCount;
    }

    public String toString() {
        return "Sprite(" + this.spriteName + ", " + this.imgCount + ")";
    }
}
