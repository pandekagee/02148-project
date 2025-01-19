package dk.spilstuff.engine;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.lwjgl.opengl.ARBTextureRectangle;
import org.lwjgl.opengl.GL30;

import dk.spilstuff.engine.Sprite.SpriteImage;

import org.json.JSONArray;
import org.json.JSONException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static org.lwjgl.opengl.GL11.*;

public class Scene {
    private final String name;
    private final ObjectInfo[] objects;
    private final int width;
    private final int height;
    private final int fps;
    private final int cameraWidth;
    private final int cameraHeight;
    private final Sprite background;
    private final int backgroundType;

    private final int id;
    private ArrayList<DrawCall> drawCalls;
    
    public class ObjectInfo {
        public final String objectName;
        public final double x;
        public final double y;
        public final double xScale;
        public final double yScale;
        public final double rotation;
        public final Sprite sprite;
        public final int depth;

        public ObjectInfo(String objectName, double x, double y, double xScale, double yScale, double rotation, String spriteName, int xoffset, int yoffset, int depth) {
            this.objectName = objectName;
            this.x = x;
            this.y = y;
            this.xScale = xScale;
            this.yScale = yScale;
            this.rotation = rotation;
            this.sprite = spriteName.equals("") ? null : new Sprite(spriteName, xoffset, yoffset);
            this.depth = depth;
        }

        public void instantiate() {
            Game.instantiate(x, y, xScale, yScale, rotation, depth, sprite, objectName);
        }
    }

    public Scene(int id, String jsonFilePath) throws RuntimeException{
        try {
            this.id = id;
            this.drawCalls = new ArrayList<DrawCall>();
            
            this.name = jsonFilePath.substring(0, jsonFilePath.length() - 5).substring(jsonFilePath.lastIndexOf("\\") + 1);
            
            FileReader fileReader = new FileReader(jsonFilePath);

            // Create a JSONTokener to parse the file
            JSONTokener tokener = new JSONTokener(fileReader);

            // Create a JSONObject from the tokener
            JSONObject jsonObject = new JSONObject(tokener);

            // Get values from the JSON
            JSONArray objectArray = jsonObject.getJSONArray("objects");
            
            int objectCount = objectArray.length();

            objects = new ObjectInfo[objectCount];

            for(int i = 0; i < objectCount; i++) {
                JSONObject j = objectArray.getJSONObject(i);

                objects[i] = new ObjectInfo(
                    j.getString("name"),
                    j.getDouble("x"),
                    j.getDouble("y"),
                    j.getDouble("xscale"),
                    j.getDouble("yscale"),
                    j.getDouble("rotation"),
                    j.getString("sprite"),
                    j.getInt("xoffset"),
                    j.getInt("yoffset"),
                    j.getInt("depth")
                );
            }

            width = jsonObject.getInt("width");
            height = jsonObject.getInt("height");
            fps = jsonObject.getInt("fps");
            cameraWidth = jsonObject.getInt("camwidth");
            cameraHeight = jsonObject.getInt("camheight");
            
            String backgroundFileName = jsonObject.getString("bg");
            
            background = !backgroundFileName.equals("") ? new Sprite(backgroundFileName, false) : null;

            backgroundType = jsonObject.getInt("bgtype");

            fileReader.close();
        }
        catch (IOException e) {
            Logger.addError("Scene asset \"" + jsonFilePath + "\" not found.");

            throw new RuntimeException();
        }
        catch(JSONException e) {
            Logger.addError("Scene asset \"" + jsonFilePath + "\" failed to parse.");

            throw new RuntimeException();
        }
        catch(RuntimeException e) {
            Logger.addError("Failed to create sprite asset from background.");

            throw new RuntimeException();
        }
    }

    public void instantiateAll() {
        for(ObjectInfo object : objects) {
            object.instantiate();
        }
    }

    public void addDrawCall(DrawCall drawCall) {
        drawCalls.add(drawCall);
    }

    public void sortDrawCallsByDepth() {
        if(this.drawCalls.size() > 1) {
            Collections.sort(this.drawCalls, new Comparator<DrawCall>() {
                public int compare (DrawCall o1, DrawCall o2) {
                    int comp = o2.depth - o1.depth;
                    return comp;
                }
            });
        }
    }

    public void redraw() {
        if(!drawCalls.isEmpty()) {

            int activeSheet = 0;

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
            glViewport(0,0,width,height);

            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, Game.getSpriteSheetTexture(0));

            double height = Game.getActiveScene().getHeight();

            for(DrawCall call : drawCalls.toArray(new DrawCall[0])) {
                SpriteImage s = call.sprite == null ? null : call.sprite.getSubImg(call.subimg);
                int x1=0, x2=0, y1=0, y2=0;
                double l=0, r=0, t=0, b=0;

                if(s != null) {
                    if(activeSheet != s.getSheet()) {
                        glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, Game.getSpriteSheetTexture(s.getSheet()));
                        activeSheet = s.getSheet();
                    }

                    // Texcoords
                    x1 = s.getX();
                    y1 = s.getY();
                    x2 = s.getX() + s.getWidth();
                    y2 = s.getY() + s.getHeight();

                    // Vertexcoords
                    l = -s.getWidth()/(double)2;
                    r = s.getWidth()/(double)2;
                    t = s.getHeight()/(double)2;
                    b = -s.getHeight()/(double)2;
                }

                glReset();

                glTranslated(call.x, height - call.y, 0d);

                if(call.callType == 1 || call.callType == 3 || call.callType == 5) {
                    if(call.color != null) {
                        glColor4d(call.color.getRed() / 255d, call.color.getGreen() / 255d, call.color.getBlue() / 255d, call.alpha);
                    }
                    else {
                        glColor4d(1d, 1d, 1d, call.alpha);
                    }

                    glRotated(call.rotation, 0d, 0d, 1d);
                    glScaled(call.xScale, call.yScale, 0d);
                }

                if(call.callType <= 1 && call.callType != -2) glTranslated(-call.sprite.getXOffset() + s.getWidth()/2, call.sprite.getYOffset() - s.getHeight()/2, 0d);

                if(call.callType == -3) {
                    for(int i = 0; i < Game.getActiveScene().getWidth(); i += s.getWidth()) {
                        glTranslated(s.getWidth(), 0d, 0d);
                        
                        for(int j = 0; j < Game.getActiveScene().getHeight(); j += s.getHeight()) {
                            glTranslated(0d, -s.getHeight(), 0d);

                            glBegin(GL_QUADS);
                            glTexCoord2i(x1, y1);
                            glVertex2d(l, t); //top-left
                            glTexCoord2i(x1, y2);
                            glVertex2d(l, b); //bottom-left
                            glTexCoord2i(x2, y2);
                            glVertex2d(r, b); //bottom-right
                            glTexCoord2i(x2, y1);
                            glVertex2d(r, t); //top-right
                            glEnd();
                        }
                    }
                }
                else if(call.callType <= 1) { //sprite
                    if(call.callType == -2) {
                        glScaled((double)Game.getActiveScene().getWidth() / s.getWidth(), (double)Game.getActiveScene().getHeight() / s.getHeight(), 0d);
                        glTranslated(s.getWidth()/2,-s.getHeight()/2, 0d);
                    }

                    glBegin(GL_QUADS);
                    glTexCoord2i(x1, y1);
                    glVertex2d(l, t); //top-left
                    glTexCoord2i(x1, y2);
                    glVertex2d(l, b); //bottom-left
                    glTexCoord2i(x2, y2);
                    glVertex2d(r, b); //bottom-right
                    glTexCoord2i(x2, y1);
                    glVertex2d(r, t); //top-right
                    glEnd();
                }
                else if(call.callType <= 3){ //text
                    TextFont font = call.textFont;

                    glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, font.texture);
                    activeSheet = -1;

                    double _x = 0;
                    double _y = 0;
                    int textHeight = font.getHeight(call.text);

                    if(textHeight > font.fontHeight) {
                        _y += textHeight - font.fontHeight;
                    }

                    for(int i = 0; i < call.text.length(); i++) {
                        char ch = call.text.charAt(i);
                        if (ch == '\n') {
                            _y -= font.fontHeight;
                            _x = 0;
                            continue;
                        }
                        if (ch == '\r') {
                            continue;
                        }
                        Glyph g = font.glyphs.get(ch);

                        double _w = ch == ' ' ? 5d : g.width;
                        double _h = g.height;

                        glBegin(GL_QUADS);
                        glTexCoord2i(g.x, g.y);
                        glVertex2d(_x, _y); //top-left
                        glTexCoord2i(g.x, g.y + g.height);
                        glVertex2d(_x, -_h + _y); //bottom-left
                        glTexCoord2i(g.x + g.width, g.y + g.height);
                        glVertex2d(_w + _x, -_h + _y); //bottom-right
                        glTexCoord2i(g.x + g.width, g.y);
                        glVertex2d(_w + _x, _y); //top-right
                        glEnd();

                        _x += _w;
                    }
                }
            }

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, 0);
            
            drawCalls.clear();
        }
    }

    private void glReset() {
        glLoadIdentity();
        glColor4f(1f,1f,1f,1f);
    }

    public int getID() {
        return id;
    }

    public ObjectInfo[] getObjects() {
        return objects;
    }

    public int getFPS() {
        return fps;
    }

    public Sprite getBackground() {
        return background;
    }

    public int getBackgroundType() {
        return backgroundType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    public int getCameraWidth() {
        return cameraWidth;
    }

    public int getCameraHeight() {
        return cameraHeight;
    }
}
