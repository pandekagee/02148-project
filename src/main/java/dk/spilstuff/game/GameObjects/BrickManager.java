package dk.spilstuff.game.GameObjects;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Mathf;

public class BrickManager extends GameObject {

    public int brickNumber = 0;
    Map<Integer, Brick> brickMap = new HashMap<>();

    int brickWidth = 4*2;
    int brickHeight = 16*2;
    Camera camera;

    Player player;
    public int gameMode;

    public void destroyBrick(int Id){
        Brick brick = brickMap.get(Id);
        
        if (brick != null){
            Game.destroy(brick);
            brickMap.remove(Id);
        }
    }

    private void createAllBricks() {
        int w = 6;
        int h = 6;

        int bw = 10*2+2;
        int bh = 29*2+2;

        int cw = Game.getCamera().getWidth()/2;
        int ch = Game.getCamera().getHeight()/2;

        for(int i = 0; i < w; i++) {
            for(int j = 0; j < h; j++) {
                createBrick(cw + bw*i - (w-1)/2*bw - bw/2, ch + bh*j - (h-1)/2*bh - bh/2, Color.HSBtoRGB((i+j)/11f, 1f, 1f));
            }
        }
    }

    private void createBrick(int x, int y, int color){
        Brick brick = (Brick) Game.instantiate( x, y, "Brick");
        
        brickMap.put(brickNumber, brick);

        brick.brickId = brickNumber;
        brick.color = new Color(color);
        brickNumber++;
    }

    @Override
    public void createEvent() {
        super.createEvent();
        camera = Game.getCamera();

        player = (Player) Game.getInstancesOfType(Player.class)[0];

        gameMode = Game.getActiveScene().getName().equals("rm_game1") ? 1 : 0;

        createAllBricks();
    }

    @Override
    public void updateEvent(){
        super.updateEvent();

        Integer Id = Game.receiveValue(player.playerId, "destroyBrick", Integer.class);
        if (Id != null){
            destroyBrick(Id);
        }
    }

    @Override
    public void drawEvent(){
        
    }
}