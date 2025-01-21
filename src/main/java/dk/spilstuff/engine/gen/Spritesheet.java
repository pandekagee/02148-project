package dk.spilstuff.engine.gen;

import dk.spilstuff.engine.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.FileWriter;

public class Spritesheet {
    private static int SHEET_SIZE;
    private static int spriteProgress = 0;
    private static int sheetNumber = 0;
    private static int spriteAmount;
    private static JSONObject json = new JSONObject();
    private static JSONObject currentSubimageObject = null;
    private static String prevName = "";

    public static int[] generate(String gameName, boolean forceRemake) {
        //create runtime folder if missing
        File folder = new File(gameName + "\\runtime");

        if(!folder.exists()) {
            folder.mkdir();

            folder = new File(gameName + "\\runtime\\sprites");
            folder.mkdir();
            folder = new File(gameName + "\\runtime\\fonts");
            folder.mkdir();
        }

        if(!forceRemake) {
            try {
                String c = GenUtil.generateChecksum(gameName + "\\Sprites");

                try (BufferedReader reader = new BufferedReader(new FileReader(gameName + "\\runtime\\checksum.txt"))) {
                    String fileChecksum = reader.readLine();

                    if(c.equals(fileChecksum)) {
                        return useExistingSpriteSheets(gameName);
                    }
                } catch (IOException e) {
                    // no problem, generate the spritesheets!
                }

                GenUtil.writeChecksumToFile(c, gameName + "\\runtime\\checksum.txt");
            }
            catch(Exception e) {
                Logger.addError("Checksum failed to generate.");
            }
        }
        else {
            spriteProgress = 0;
        }

        // Delete existing spritesheets - we're making new ones.
        File[] runtimeFiles = new File(gameName + "\\runtime\\sprites").listFiles();
        for(File file : runtimeFiles) {
            file.delete();
        }
        
        File[] files = new File(gameName + "\\Sprites").listFiles();

        spriteAmount = files.length;

        if (files == null || spriteAmount == 0) {
            Logger.addError("No sprite files found in the Sprites folder!");
            return null;
        }

        ArrayList<ByteBuffer> bufferList = new ArrayList<ByteBuffer>();
        
        while(spriteProgress < spriteAmount) {
            bufferList.add(GenUtil.byteBufferFromBufferedImage(combineImagesIntoSpritesheet(files, spriteProgress), SHEET_SIZE, SHEET_SIZE));
            sheetNumber++;
        }

        try (FileWriter fileWriter = new FileWriter(gameName + "\\runtime\\sprites\\spriteSheetData.json")) {
            fileWriter.write(json.toString(3));
            fileWriter.close();
        } catch (IOException e) {
            Logger.addError("Failed to write spritemap json!");
        }

        int[] dim = new int[bufferList.size()];
        for(int i = 0; i < dim.length; i++) {dim[i] = SHEET_SIZE;}

        int[] textures = GenUtil.generateTexturesFromByteBufferList(bufferList, dim, dim);

        if(textures == null) {
            new File(gameName + "runtime\\checksum.txt").delete();
        }

        return textures;
    }

    private static int[] useExistingSpriteSheets(String gameName) {
        File[] runtimeFiles = new File(gameName + "\\runtime\\sprites").listFiles();

        try {
            JSONObject jsonObject = new JSONObject(new JSONTokener(new FileReader(gameName + "\\runtime\\sprites\\spriteSheetData.json")));
            SHEET_SIZE = jsonObject.getInt("width");
        }
        catch(IOException e) {
            Logger.addError("Checksum matched, but no spriteSheetData.json found!");
            return null;
        }

        ArrayList<ByteBuffer> bufferList = new ArrayList<ByteBuffer>();

        for(File file : runtimeFiles) {
            if(file.getName().endsWith(".png")) {
                try {
                    BufferedImage spritesheet = ImageIO.read(file);

                    bufferList.add(GenUtil.byteBufferFromBufferedImage(spritesheet, SHEET_SIZE, SHEET_SIZE));
                }
                catch(IOException e) {
                    Logger.addError("Couldn't load spritesheet \"" + file.getName() + "\".");
                }
            }
        }

        int[] dim = new int[bufferList.size()];
        for(int i = 0; i < dim.length; i++) {dim[i] = SHEET_SIZE;}

        return GenUtil.generateTexturesFromByteBufferList(bufferList, dim, dim);
    }

    private static BufferedImage combineImagesIntoSpritesheet(File[] files, int progress) {
        ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
        ArrayList<String> imageNames = new ArrayList<String>();

        for (int i = progress; i < files.length; i++) {
            try {
                BufferedImage image = ImageIO.read(files[i]);

                if(progress == 0) {
                    SHEET_SIZE = Math.max(SHEET_SIZE, image.getWidth());
                    SHEET_SIZE = Math.max(SHEET_SIZE, image.getHeight());
                }

                images.add(image);
                imageNames.add(files[i].getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(progress == 0) {
            SHEET_SIZE = Math.max(1024, 1 << (32 - Integer.numberOfLeadingZeros(SHEET_SIZE - 1))); //scale up to nearest power of 2, or 1024
            json.put("width", SHEET_SIZE);
            json.put("height", SHEET_SIZE);
        }

        BufferedImage spritesheet = new BufferedImage(SHEET_SIZE, SHEET_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        boolean[][] occupied = new boolean[SHEET_SIZE][SHEET_SIZE];

        int yOffset = 0;
        String name = "";

        boolean nextSheet = false;

        for (int k = 0; k < images.size(); k++) {
            BufferedImage image = images.get(k);
            if(!name.equals("")) prevName = new String(name);
            name = imageNames.get(k);
            
            int xOffset = 0;
            
            outerLoop:
            for(; yOffset < SHEET_SIZE; yOffset++) {
                xOffset = 0;

                if(yOffset + image.getHeight() > SHEET_SIZE) {
                    nextSheet = true;
                    break outerLoop;
                }
                
                for(; xOffset < SHEET_SIZE; xOffset++) {
                    if(xOffset + image.getWidth() > SHEET_SIZE) {
                        break;   
                    }

                    if(xOffset + image.getWidth() <= SHEET_SIZE && yOffset + image.getHeight() <= SHEET_SIZE) {
                        boolean occ = false;

                        for(int i = 0; i < image.getWidth(); i++) {
                            if(occupied[xOffset + i][yOffset]) {
                                occ = true;
                            }
                        }

                        for(int i = 0; i < image.getHeight(); i++) {
                            if(occupied[xOffset][yOffset + i]) {
                                occ = true;
                            }
                        }
                        
                        if(!occ) {
                            break outerLoop;
                        }
                    }
                }
            }

            if(nextSheet) {
                break;
            }

            System.out.print("\rWriting spritesheet(s)... " + (int)((spriteProgress / (double)spriteAmount) * 100d) + "%");

            spritesheet.getGraphics().drawImage(image, xOffset, yOffset, null);

            spriteProgress++;

            // Save in JSON
            int order;

            try {
                order = Integer.parseInt(name.split("_")[name.split("_").length - 1].split("\\.")[0]);
            }
            catch(NumberFormatException e) {
                Logger.addLog("\n");
                Logger.addError("Malformed sprite should include sprite position: \"" + name + "\".");
                return null;
            }

            if(order == 0) {
                if(currentSubimageObject != null) {
                    json.put(prevName.substring(0, prevName.lastIndexOf("_")), currentSubimageObject);
                }

                currentSubimageObject = new JSONObject();
            }

            JSONObject s = new JSONObject();
            s.put("x", xOffset);
            s.put("y", yOffset);
            s.put("w", image.getWidth());
            s.put("h", image.getHeight());
            s.put("sheet", sheetNumber);

            currentSubimageObject.put(""+order, s);

            for(int i = 0; i < image.getWidth(); i++) {
                for(int j = 0; j < image.getHeight(); j++) {
                    occupied[xOffset + i][yOffset + j] = true;
                }
            }

            xOffset += image.getWidth();
        }

        if(!nextSheet && currentSubimageObject != null && !name.equals("")) {
            json.put(name.substring(0, name.lastIndexOf("_")), currentSubimageObject);
        }

        //white square for drawSquare
        if(!nextSheet) {
            spritesheet.setRGB(SHEET_SIZE - 1, SHEET_SIZE - 1, 0xFFFFFFFF);
            spritesheet.setRGB(SHEET_SIZE - 2, SHEET_SIZE - 1, 0xFFFFFFFF);
            spritesheet.setRGB(SHEET_SIZE - 1, SHEET_SIZE - 2, 0xFFFFFFFF);
            spritesheet.setRGB(SHEET_SIZE - 2, SHEET_SIZE - 2, 0xFFFFFFFF);
            currentSubimageObject = new JSONObject();
            JSONObject s = new JSONObject();
                s.put("x", SHEET_SIZE - 2);
                s.put("y", SHEET_SIZE - 2);
                s.put("w", 2);
                s.put("h", 2);
                s.put("sheet", sheetNumber);
            currentSubimageObject.put("0", s);
            json.put("whiteSquareBUILTIN", currentSubimageObject);
        }

        // Saving the data
        try {
            ImageIO.write(spritesheet, "png", new File(Game.gameName + "\\runtime\\sprites\\spriteSheet"+sheetNumber+".png"));
        }
        catch(IOException e) {
            Logger.addError("Failed to write spritesheet image!");
        }

        return spritesheet;
    }
}
