package dk.spilstuff.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.reflections.Reflections;

import dk.spilstuff.engine.gen.Spritesheet;
import dk.spilstuff.engine.gen.Fontsheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.Color;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.awt.Font;

public class Game {
    private static boolean[] heldKeys;
    private static boolean[] keyDifferences;
    private static int currentKey = -1;
    private static int FRAME_RATE;
    public static String gameName = "src\\main\\java\\dk\\spilstuff\\game";
    public static String rootDirectory;
    private static HashMap<String, Class<? extends GameObject>> gameObjects;
    private static HashMap<String, Scene> scenes;
    private static Scene activeScene;
    private static ArrayList<GameObject> instantiatedObjects;
    private static ArrayList<GameObject> instantiateQueue;
    private static long TARGET_DELTA_TIME; 
    public static double DELTA_TIME = 0;
    private static long previousFrameTime = 1000000;
    private static long tickTime = 0;
    private static double mouseX;
    private static double mouseY;
    private static boolean[] heldMouseButtons;
    private static boolean[] mouseButtonDifferences;
    private static Sprite whiteSquareSprite;
    private static long fps;
    private static long realFPS;
    private static Camera camera = null;

    private static long window;
    private static int[] spriteSheets;
    private static HashMap<String, TextFont> textFontMap;

    // multiplayer handling variables
    public static RemoteSpace lobby;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    public static int lobbyUpdateAlarm = 0;

    public static void main(String[] args) {
        instantiatedObjects = new ArrayList<GameObject>();
        instantiateQueue = new ArrayList<GameObject>();
        heldKeys = new boolean[Keys.VK_LAST];
        keyDifferences = new boolean[Keys.VK_LAST];
        heldMouseButtons = new boolean[4];
        mouseButtonDifferences = new boolean[4];
        gameObjects = new HashMap<String, Class<? extends GameObject>>();
        scenes = new HashMap<String, Scene>();

        // multiplayer
        // Connect to the lobby
        try {
            lobby = new RemoteSpace("tcp://localhost:9001/lobby?keep");
        } catch (Exception e) {
            System.err.println("An error occurred while connecting to the lobby: " + e.getMessage());
            e.printStackTrace();
        }

        // compilation
        rootDirectory = System.getProperty("user.dir");

        buildWindow();
        
        glEnable(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if(compile(gameName)) {
            Logger.addError("Compile failed. Check stacktrace for information.");

            Logger.writeAll();
            Logger.clear();
        }
        else {
            Logger.addSuccess("Compilation finished.");

            Logger.writeAll();
            Logger.clear();
            
            // running
            try {
                Scene s = activeScene;
                activeScene = null;
                setActiveScene(s.getName());

                tickMaintenance();
            }
            catch(NullPointerException e) {
                Logger.addError("No scenes exist. Can't launch game.");
            }

            previousFrameTime = System.nanoTime();

            // Set the clear color
            glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

            while(!glfwWindowShouldClose(window)) {
                if(Math.abs(previousFrameTime - System.nanoTime()) >= TARGET_DELTA_TIME) {
                    DELTA_TIME = (double)Math.abs(System.nanoTime() - previousFrameTime) / 1000000;

                    previousFrameTime = System.nanoTime();

                    fps = DELTA_TIME == 0 ? 0 : (long)(1000d / DELTA_TIME + 0.5d);

                    if(camera != null) {
                        updateCamera();
                    }

                    glfwSwapBuffers(window); // swap the color buffers

                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

                    glfwPollEvents();

                    updateCursorPosition();

                    tick();
                }
            }
        }
    }

    private static void buildWindow() {
        // Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(300, 300, gameName, NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (_, key, _, action, _) -> {
            if(action == GLFW_PRESS) {
                try {
                    keyDifferences[key] = !heldKeys[key];
                    heldKeys[key] = true;
                    currentKey = key;
                }
                catch(ArrayIndexOutOfBoundsException ex) {
                    //do nothing
                }
            }
            else if(action == GLFW_RELEASE) {
                try {
                    heldKeys[key] = false;
                    keyDifferences[key] = true;
                }
                catch(ArrayIndexOutOfBoundsException ex) {
                    //do nothing
                }
            }
		});

        glfwSetMouseButtonCallback(window, (_, button, action, _) -> {
            if(action == GLFW_PRESS) {
                mouseButtonDifferences[button] = !heldMouseButtons[button];
                heldMouseButtons[button] = true;
            }
            else if(action == GLFW_RELEASE) {
                heldMouseButtons[button] = false;
                mouseButtonDifferences[button] = true;
            }
        });

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
            if(vidmode != null) {
                glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
                );
            }
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
        GL.createCapabilities();

		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
    }

    private static void tick() { // everything that should happen during a frame.
        long t = System.nanoTime();

        Collections.sort(instantiatedObjects, new Comparator<GameObject>() {
            public int compare (GameObject o1, GameObject o2) {
                int comp = o2.depth - o1.depth;
                return comp;
            }
        });

        //create
        for(GameObject object : instantiatedObjects) {
            if(!object.instantiated && !object.destroyed) {
                object.createEvent();
            }
        }

        //update
        for(GameObject object : instantiatedObjects) {
            if(!object.destroyed) {
                object.updateEvent();
            }
        }

        //draw
        drawBackground();

        for(GameObject object : instantiatedObjects) {
            if(!object.destroyed && object.visible) {
                object.drawEvent();
            }
        }

        for(Scene scene : scenes.values()) {
            scene.sortDrawCallsByDepth();
            scene.redraw();
        }

        tickMaintenance();

        Logger.writeAll();
        Logger.clear();

        tickTime = Math.abs(System.nanoTime() - t);
        realFPS = 1000000000L / tickTime;
    }

    private static void tickMaintenance() {
        //destroy all destroyed objects
        instantiatedObjects.removeIf(i -> i.destroyed);

        //instantiate all in queue
        for(GameObject object : instantiateQueue) {
            instantiatedObjects.add(object);
        }

        instantiateQueue.clear();

        //reset key differences
        keyDifferences = new boolean[Keys.VK_LAST];
        mouseButtonDifferences = new boolean[4];
        currentKey = -1;
    }

    private static boolean compile(String gameName) {    
        // Create a Reflections instance to scan the specified package
        Reflections reflections = new Reflections("dk.spilstuff.game.GameObjects");

        // Get all subclasses of GameObject
        Set<Class<? extends GameObject>> objectSet = reflections.getSubTypesOf(GameObject.class);

        for(Class<? extends GameObject> object : objectSet) {
            gameObjects.put(object.getName().substring(object.getName().lastIndexOf('.') + 1), object);
        }

        Logger.addSuccess("GameObjects collected!");

        // Spritesheet creation
        spriteSheets = Spritesheet.generate(gameName, false);

        if(spriteSheets == null) {
            return true;
        }

        whiteSquareSprite = new Sprite("whiteSquareBUILTIN", false);

        System.out.println("\rWriting spritesheet(s)... 100%");
        Logger.addSuccess("Spritesheet(s) generated!");

        // Fontsheet creation
        textFontMap = Fontsheet.generate(gameName);

        if(textFontMap == null) {
            return true;
        }

        Logger.addSuccess("Fonts created!");

        // Instantiate scenes
        try {
            instantiateScenes(gameName + "\\Scenes");
        }
        catch(RuntimeException e) {
            return true;
        }

        // Set active scene to first scene in hashmap
        for(Scene scene : scenes.values()) {
            activeScene = scene;
            break;
        }

        return false;
    }

    private static void instantiateScenes(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    String sceneName = file.getName().substring(0, file.getName().length() - 5);
                    
                    try {
                        scenes.put(sceneName, new Scene(0, file.getAbsolutePath()));
                    }
                    catch(RuntimeException e) {
                        Logger.addError("Scene with name \"" + sceneName + "\" failed to instantiate.");
                    }
                }
            }
        }
        else {
            Logger.addError("No scenes found in \"" + folder.getAbsolutePath() + "\" folder. Make sure it's present and contains valid .json files.");
        }
    }

    private static void updateCursorPosition() {
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        glfwGetCursorPos(window, xPos, yPos);

        if(camera == null) {
            mouseX = xPos[0];
            mouseY = yPos[0];
        }
        else {
            mouseX = (xPos[0] / activeScene.getWidth()) * camera.getWidth() + camera.getX();
            mouseY = (yPos[0] / activeScene.getHeight()) * camera.getHeight() + camera.getY();
        }
    }

    private static void updateCamera() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(camera.getX(), camera.getX() + camera.getWidth(), activeScene.getHeight() - (camera.getY() + camera.getHeight()), activeScene.getHeight() - camera.getY(), -1.0f, 1.0f);
        glMatrixMode(GL_MODELVIEW);
    }

    /**
     * Instantiates a new instance of a GameObject.
     * @param x The x position to create the new instance at.
     * @param y The y position to create the new instance at.
     * @param objectName The name of the GameObject to instantiate. i.e. the part before ".java".
     * @return The instance that has been instantiated.
     */
    public static GameObject instantiate(double x, double y, String objectName) {
        if(gameObjects.containsKey(objectName)) {
            try {
                GameObject instance = gameObjects.get(objectName).getDeclaredConstructor().newInstance();
                instance.x = x;
                instance.y = y;

                instantiateQueue.add(instance);

                return instance;
            }
            catch(Exception e) {
                Logger.addError("GameObject with name \"" + objectName + "\" cannot be instantiated.");
            }
        }
        else {
            Logger.addError("GameObject with name \"" + objectName + "\" not found.");
        }

        return null;
    }

    /**
     * Instantiates a new instance of a GameObject.
     * @param x The x position to create the new instance at.
     * @param y The y position to create the new instance at.
     * @param xScale The xScale to create the new instance with.
     * @param yScale The yScale to create the new instance with.
     * @param rotation The rotation to create the new instance with.
     * @param depth The depth to create the new instance with.
     * @param sprite The sprite to create the new instance with.
     * @param objectName The name of the GameObject to instantiate. i.e. the part before ".java".
     * @return The instance that has been instantiated.
     */
    public static GameObject instantiate(double x, double y, double xScale, double yScale, double rotation, int depth, Sprite sprite, String objectName) {
        GameObject instance = instantiate(x, y, objectName);

        if(instance != null) {
            instance.xScale = xScale;
            instance.yScale = yScale;
            instance.rotation = rotation;
            instance.depth = depth;
            instance.sprite = sprite;
        }
        
        return instance;
    }

    /**
     * Destroys an instance of a GameObject.
     * @param instance The instance to destroy.
     */
    public static void destroy(GameObject instance) {
        instance.destroyed = true;
    }

    public static void destroyAllInstances() {
        for(GameObject instance : instantiatedObjects) {destroy(instance);}
        instantiateQueue.clear();
    }

    public static int getInstanceTotal() {
        return instantiatedObjects.size();
    }

    /**
     * Gets the amount of instances of a specific GameObject.
     * @param object The GameObject to get the count for. Give this value with ".class".
     * @return The amount of instances of the GameObject.
     */
    public static long getInstanceCount(Class<? extends GameObject> object) {
        long count = instantiatedObjects.stream()
            .filter(object::isInstance)
            .count();
        
        return count;
    }

    /**
     * Gets a list of all the instances of a specific GameObject.
     * @param object The GameObject to check for. Give this value with ".class".
     * @return An array of every instance of the GameObject. An empty array is returned if no instances exist.
     */
    public static GameObject[] getInstancesOfType(Class<? extends GameObject> object) {
        ArrayList<GameObject> a = new ArrayList<GameObject>();
        
        instantiatedObjects.stream().filter(object::isInstance).forEach(gameObject -> a.add(gameObject));
            
        return a.toArray(new GameObject[0]);
    }

    /**
     * Gets the nearest instance of a GameObject from a position.
     * @param x The x position to check from.
     * @param y The y position to check from.
     * @param object The GameObject to check for. Give this value with ".class".
     * @return The instance of the GameObject given that is closest to the given position, or null if no such instance exists.
     */
    public static GameObject nearestInstance(double x, double y, Class<? extends GameObject> object) {
        GameObject minO = null;
        double minD = Double.MAX_VALUE;

        for(GameObject o : getInstancesOfType(object)) {
            double d = Mathf.pointDistance(x, y, o.x, o.y);
            
            if(d < minD) {
                minO = o;
                minD = d;
            }
        }

        return minO;
    }

    /**
     * Gets the closest distance from a position to an instance of a GameObject.
     * @param x The x position to check from.
     * @param y The y position to check from.
     * @param object The GameObject to check for. Give this value with ".class".
     * @return The distance to the nearest instance of the GameObject given, or null if no such instance exists.
     */
    public static Double distanceNearestObject(double x, double y, Class<? extends GameObject> object) {
        Double minD = Double.MAX_VALUE;

        for(GameObject o : getInstancesOfType(object)) {
            double d = Mathf.pointDistance(x, y, o.x, o.y);
            
            if(d < minD) {
                minD = d;
            }
        }

        return minD;
    }

    /**
     * Checks if two instances are colliding (using sprite bounding boxes).
     * @param instance1 The first instance.
     * @param instance2 The second instance.
     * @return Whether or not the instances are currently colliding.
     */
    public static boolean instanceColliding(GameObject instance1, GameObject instance2) {
        double x1a = instance1.x - instance1.sprite.getXOffset() * instance1.xScale;
        double y1a = instance1.y - instance1.sprite.getYOffset() * instance1.yScale;
        double x2a = x1a + instance1.sprite.getWidth() * instance1.xScale;
        double y2a = y1a + instance1.sprite.getHeight() * instance1.yScale;
        double t = Math.min(x1a,x2a);
        x2a = Math.max(x1a, x2a);
        x1a = t;

        double x1b = instance2.x - instance2.sprite.getXOffset() * instance2.xScale;
        double y1b = instance2.y - instance2.sprite.getYOffset() * instance2.yScale;
        double x2b = x1b + instance2.sprite.getWidth() * instance2.xScale;
        double y2b = y1b + instance2.sprite.getHeight() * instance2.yScale;
        t = Math.min(x1b,x2b);
        x2b = Math.max(x1b, x2b);
        x1b = t;
        
        return (x1a < x2b) && (x2a > x1b) && (y1a < y2b) && (y2a > y1b);
    }

    public static boolean instanceCollidingOffset(double posX, double posY, GameObject instance1, GameObject instance2) {
        double x1a = posX - instance1.sprite.getXOffset() * instance1.xScale;
        double y1a = posY - instance1.sprite.getYOffset() * instance1.yScale;
        double x2a = x1a + instance1.sprite.getWidth() * instance1.xScale;
        double y2a = y1a + instance1.sprite.getHeight() * instance1.yScale;
        double t = Math.min(x1a,x2a);
        x2a = Math.max(x1a, x2a);
        x1a = t;

        double x1b = instance2.x - instance2.sprite.getXOffset() * instance2.xScale;
        double y1b = instance2.y - instance2.sprite.getYOffset() * instance2.yScale;
        double x2b = x1b + instance2.sprite.getWidth() * instance2.xScale;
        double y2b = y1b + instance2.sprite.getHeight() * instance2.yScale;
        t = Math.min(x1b,x2b);
        x2b = Math.max(x1b, x2b);
        x1b = t;
        
        return (x1a < x2b) && (x2a > x1b) && (y1a < y2b) && (y2a > y1b);
    }

    /**
     * Checks if a point is colliding with an instance (using sprite bounding boxes).
     * @param cx The x coordinate of the point.
     * @param cy The y coordinate of the point.
     * @param instance The instance to check.
     * @return Whether or not the point is colliding with the given instance.
     */
    public static boolean positionColliding(double cx, double cy, GameObject instance) {
        double x = instance.x - instance.sprite.getXOffset() * instance.xScale;
        double y = instance.y - instance.sprite.getYOffset() * instance.yScale;
        double w = instance.sprite.getWidth() * instance.xScale;
        double h = instance.sprite.getHeight() * instance.yScale;

        return cx > Math.min(x, x + w)
            && cx < Math.max(x, x + w)
            && cy > Math.min(y, y + h)
            && cy < Math.max(y, y + h);
    }

    public static Scene createScene(String sceneName) {
        try {
            Scene scene = new Scene(GL30.glGenFramebuffers(), gameName + "\\Scenes\\" + sceneName + ".json");
            scenes.put(sceneName, scene);
            return scene;
        }
        catch(RuntimeException e) {
            Logger.addError("Scene with name \"" + sceneName + "\" failed to instantiate.");
            return null;
        }
    }

    public static void setActiveScene(String sceneName) {
        Scene scene = scenes.get(sceneName);
        
        if(scene != null) {
            if(scene != activeScene) {
                destroyAllInstances();
                activeScene = scene;
                activeScene.instantiateAll();
                setFrameRate(activeScene.getFPS());

                glfwSetWindowSize(window, activeScene.getWidth(), activeScene.getHeight());

                if(activeScene.getCameraWidth() + activeScene.getCameraHeight() != 0) {
                    camera = new Camera(0,0,activeScene.getCameraWidth(),activeScene.getCameraHeight());
                }
                else {
                    glMatrixMode(GL_PROJECTION);
                    glLoadIdentity();
                    glOrtho(0,activeScene.getWidth(),0f,activeScene.getHeight(), -1, 1);
                    glMatrixMode(GL_MODELVIEW);

                    camera = null;
                }

                glViewport(0, 0, activeScene.getWidth(), activeScene.getHeight());

                //center window
                try ( MemoryStack stack = stackPush() ) {
                    IntBuffer pWidth = stack.mallocInt(1); // int*
                    IntBuffer pHeight = stack.mallocInt(1); // int*
        
                    // Get the window size passed to glfwCreateWindow
                    glfwGetWindowSize(window, pWidth, pHeight);
        
                    // Get the resolution of the primary monitor
                    GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        
                    // Center the window
                    if(vidmode != null) {
                        glfwSetWindowPos(
                            window,
                            (vidmode.width() - pWidth.get(0)) / 2,
                            (vidmode.height() - pHeight.get(0)) / 2
                        );
                    }
                } // the stack frame is popped automatically
            }
        }
        else {
            Logger.addError("Scene with name \"" + sceneName + "\" doesn't exist.");
        }
    }

    public static Scene getActiveScene() {
        return activeScene;
    }

    public static void setFrameRate(int frameRate) {
        FRAME_RATE = frameRate;
        TARGET_DELTA_TIME = (long)((1000.0 / FRAME_RATE) * 1000000);
    }

    public static int getFrameRate() {
        return FRAME_RATE;
    }

    public static boolean[] getKeyDifferences() {
        return keyDifferences;
    }

    public static boolean[] getHeldKeys() {
        return heldKeys;
    }

    public static boolean[] getMouseButtonDifferences() {
        return mouseButtonDifferences;
    }

    public static boolean[] getHeldMouseButtons() {
        return heldMouseButtons;
    }

    public static boolean keyIsHeld(int keyCode) {
        try {
            return heldKeys[keyCode];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public static boolean keyIsPressed(int keyCode) {
        try {
            return keyDifferences[keyCode] && heldKeys[keyCode];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public static boolean keyIsReleased(int keyCode) {
        try {
            return keyDifferences[keyCode] && !heldKeys[keyCode];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Returns the currently pressed key.
     * @return The keycode of the currently pressed key. On every frame where no key is pressed, returns -1.
     */
    public static int getCurrentKey() {
        return currentKey;
    }

    public static boolean mouseButtonIsHeld(int mouseButtonCode) {
        try {
            return heldMouseButtons[mouseButtonCode];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public static boolean mouseButtonIsPressed(int mouseButtonCode) {
        try {
            return mouseButtonDifferences[mouseButtonCode] && heldMouseButtons[mouseButtonCode];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public static boolean mouseButtonIsReleased(int mouseButtonCode) {
        try {
            return mouseButtonDifferences[mouseButtonCode] && !heldMouseButtons[mouseButtonCode];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * 
     * @return The current mouse x position in world coordinates.
     */
    public static double getMouseX() {
        return mouseX;
    }

    /**
     * 
     * @return The current mouse y position in world coordinates.
     */
    public static double getMouseY() {
        return mouseY;
    }

    public static long getWindow() {
        return window;
    }

    public static Camera getCamera() {
        return camera;
    }

    public static long getRealFPS() {
        return realFPS;
    }

    public static long getFPS() {
        return fps;
    }

    public static int getSpriteSheetTexture(int index) {
        return spriteSheets[index];
    }

    public static void setSpriteSheetTextures(int[] newTextures) {
        spriteSheets = newTextures;
    }

    /**
     * Returns the TextFont tied to the key given. On compilation, all fonts in the Fonts folder are compiled and added as-is, meaning their direct filenames are used as keys. Additionally, the default fonts "Default" and "Mono" can also be accessed.
     * @param fontName The name to look up.
     * @return A TextFont object to use in text draw calls.
     */
    public static TextFont getTextFont(String fontName) {
        TextFont t = textFontMap.get(fontName);
        
        if(t == null) {
            Logger.addWarning("Font with name \"" + fontName + "\" cannot be resolved. Using default font.");
            return getTextFont("Default");
        }

        return t;
    }

    /**
     * Creates a new font from the given parameters and maps it to the name given, to be obtained with Game.getTextFont(<name>).
     * @param fontName The name to be mapped to the font.
     * @param ttfFontFile A file object directly pointing to the file location of the ttf file to create the font from.
     * @param size The font size to write the font atlas with.
     * @param antiAlias Whether or not to write the font atlas with antialiasing.
     * @param separation The separation between characters.
     */
    public static void createTextFont(String fontName, File ttfFontFile, int size, boolean antiAlias, int separation) {
        try {
            InputStream in = new FileInputStream(ttfFontFile.getAbsolutePath());

            textFontMap.put(fontName, Fontsheet.createTextFont(fontName, Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(Font.PLAIN, size), antiAlias, separation));
        }catch(Exception e){
            Logger.addError("Failed to create font. File \"" + ttfFontFile.getAbsolutePath() + "\" not found");
        }
    }

    public static void drawSprite(Sprite sprite, double subimg, int depth, double x, double y) {
        activeScene.addDrawCall(new DrawCall(sprite, subimg, depth, x, y));
    }

    public static void drawSpriteScaled(Sprite sprite, double subimg, int depth, double x, double y, double xScale, double yScale, double rotation, Color color, double alpha) {
        activeScene.addDrawCall(new DrawCall(sprite, subimg, depth, x, y, xScale, yScale, rotation, color, alpha));
    }

    public static void drawText(TextFont textFont, CharSequence string, int depth, double x, double y) {
        activeScene.addDrawCall(new DrawCall(textFont, string, depth, x, y));
    }

    public static void drawTextScaled(TextFont textFont, CharSequence string, int depth, double x, double y, double xScale, double yScale, double rotation, Color color, double alpha) {
        activeScene.addDrawCall(new DrawCall(textFont, string, depth, x, y, xScale, yScale, rotation, color, alpha));
    }

    public static void drawSquare(int depth, double x, double y, double xScale, double yScale, double rotation, Color color, double alpha) {
        activeScene.addDrawCall(new DrawCall(whiteSquareSprite, 0, depth, x, y, xScale, yScale, rotation, color, alpha));
    }

    private static void drawBackground() {
        if(activeScene.getBackground() != null) {
            activeScene.addDrawCall(new DrawCall(activeScene.getBackground(), activeScene.getBackgroundType()));
        }
    }

    public static <T> void sendValue(int playerId, String variable, T value) {
        executor.submit(() -> {
            try {
                lobby.put(playerId, variable, value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Failed to send data: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    public static Integer receiveInteger(int playerId, String variable) {
        return receiveValue(playerId, variable, Integer.class);
    }

    public static String receiveString(int playerId, String variable) {
        return receiveValue(playerId, variable, String.class);
    }

    public static Double receiveDouble(int playerId, String variable) {
        return receiveValue(playerId, variable, Double.class);
    }

    public static <T> T receiveValue(int playerId, String variable, Class<T> type) {
        try {
            
            if (lobbyUpdateAlarm == 0){
                Future<Object[]> future = executor.submit(() -> 
                    lobby.getp(new ActualField(playerId), new ActualField(variable), new FormalField(type))
                );

                Object[] message = future.get();
                if (message != null) {
                    return type.cast(message[2]);
                }
            } else{
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to receive data: " + e.getMessage());
        } catch (ExecutionException e) {
            System.err.println("Task execution failed: " + e.getCause().getMessage());
        }
    
        return null;
    }

    public static boolean removeValue(int playerId, String variable, int actualValue) {
        try {
            Future<Object[]> future = executor.submit(() -> 
                lobby.getp(new ActualField(playerId), new ActualField(variable), new ActualField(actualValue))
            );
    
            Object[] message = future.get();
            
            return (message != null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to receive data: " + e.getMessage());
        } catch (ExecutionException e) {
            System.err.println("Task execution failed: " + e.getCause().getMessage());
        }
    
        return false;
    }

    public static boolean queryValue(int playerId, String variable, int actualValue) {
        try {
            Future<Object[]> future = executor.submit(() -> 
                lobby.queryp(new ActualField(playerId), new ActualField(variable), new ActualField(actualValue))
            );
    
            Object[] message = future.get();

            return (message != null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to receive data: " + e.getMessage());
        } catch (ExecutionException e) {
            System.err.println("Task execution failed: " + e.getCause().getMessage());
        }
    
        return false;
    }    
}