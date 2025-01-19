package dk.spilstuff.game.GameObjects;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.Server.BrickDestructInfo;
import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;

public class BrickManager extends GameObject {

    public int brickNumber = 0;
    Map<Integer, Brick> brickMap = new HashMap<>();

    int brickWidth = 4*2;
    int brickHeight = 16*2;
    Camera camera;

    Player player;
    public int gameMode;

    public void destroyBrick(BallInfo ballInfo, int Id, boolean performEffect){
        Brick brick = brickMap.get(Id);
        
        if (brick != null){
            if(performEffect) brick.applyEffect(ballInfo);
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

        // Extra balls
        brickMap.get(7).brickType = 1;
        brickMap.get(10).brickType = 1;
        brickMap.get(14).brickType = 1;
        brickMap.get(15).brickType = 1;
        brickMap.get(20).brickType = 1;
        brickMap.get(21).brickType = 1;
        brickMap.get(25).brickType = 1;
        brickMap.get(28).brickType = 1;
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

        BrickDestructInfo destructInfo = Game.receiveValue(player.playerId, "destroyBrick", BrickDestructInfo.class);


        if (destructInfo != null){
            if (destructInfo.ballInfo.team == 0){
                
            } else if (destructInfo.ballInfo.team == player.playerId+1){
                player.opponentScore += 1;
            } else{
                player.playerScore += 1;
            }

            destroyBrick(destructInfo.ballInfo, destructInfo.id, false);
        }
    }

    @Override
    public void drawEvent(){
        
    }
}