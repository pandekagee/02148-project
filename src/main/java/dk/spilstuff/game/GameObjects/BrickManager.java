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

    public void destroyBrick(BallInfo ballInfo, int Id){
        Brick brick = brickMap.get(Id);
        
        if (brick != null){
            if(brick.brickType != 1 || Game.removeValue(0, "brick", Id)) brick.applyEffect(ballInfo);
            Game.destroy(brick);
            brickMap.remove(Id);

            InactiveBrick inactiveBrick = (InactiveBrick)Game.instantiate(brick.x, brick.y, brick.xScale, brick.yScale, brick.rotation, brick.depth, brick.sprite, "InactiveBrick");
            inactiveBrick.brickId = brick.brickId;
            inactiveBrick.brickType = brick.brickType;
            inactiveBrick.color = brick.color;
        }
    }

    private void createAllBricks() {
        int w = 14;
        int h = 8;

        int bw = 6*2+2;
        int bh = 22*2+2;

        int cw = Game.getCamera().getWidth()/2;
        int ch = Game.getCamera().getHeight()/2;

        for(int i = 0; i < w; i++) {
            for(int j = 0; j < h; j++) {
                createBrickInitial(cw + bw*i - (w-1)/2*bw - bw/2, ch + bh*j - (h-1)/2*bh - bh/2, Color.HSBtoRGB((float)(i+j)/(w+h), 1f, 1f));
            }
        }

        // Set special bricks
            // Extra ball
            brickMap.get(25).brickType = 1;
            brickMap.get(81).brickType = 1;
            brickMap.get(34).brickType = 1;
            brickMap.get(74).brickType = 1;
            brickMap.get(37).brickType = 1;
            brickMap.get(77).brickType = 1;
            brickMap.get(30).brickType = 1;
            brickMap.get(86).brickType = 1;

            // Powerup
            brickMap.get(88).brickType = 2;
            brickMap.get(43).brickType = 2;
            brickMap.get(68).brickType = 2;
            brickMap.get(23).brickType = 2;
    }

    public void createBrickInitial(int x, int y, int color){
        Brick brick = (Brick) Game.instantiate( x, y, "Brick");
        
        brickMap.put(brickNumber, brick);

        if(!Game.queryValue(0, "brick", brickNumber)) Game.sendValue(0,"brick",brickNumber);
        brick.brickId = brickNumber;
        brick.color = new Color(color);
        brickNumber++;
    }

    public void createBrick(int x, int y, int color, int id, int brickType){
        Brick brick = (Brick) Game.instantiate( x, y, "Brick");
        
        brickMap.put(id, brick);

        if(!Game.queryValue(0, "brick", id)) Game.sendValue(0,"brick",id);
        brick.brickId = id;
        brick.color = new Color(color);
        brick.brickType = brickType;
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

        Game.receiveValue(player.playerId, "destroyBrick", BrickDestructInfo.class)
        .thenAccept(destructInfos -> {
            for(BrickDestructInfo destructInfo : destructInfos) {
                if (destructInfo.ballInfo.team == 0){
                    
                } else if (destructInfo.ballInfo.team == player.playerId+1){
                    player.opponentScore++;
                } else{
                    player.playerScore++;
                }
    
                destroyBrick(destructInfo.ballInfo, destructInfo.id);
            }
        });
    }

    @Override
    public void drawEvent(){
        
    }
}