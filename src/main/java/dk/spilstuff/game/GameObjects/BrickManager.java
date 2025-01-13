package dk.spilstuff.game.GameObjects;

import java.util.HashMap;
import java.util.Map;

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

    public void destroyBrick(int Id){
        Brick brick = brickMap.get(Id);
        
        if (brick != null){
            Game.destroy(brick);
            brickMap.remove(Id);
        }
    }

    private void createBrick(int x, int y){
        Brick brick = (Brick) Game.instantiate( x + camera.getWidth() / 2 - brickWidth / 2, y + camera.getHeight() / 2 - brickHeight / 2, "Brick");
        
        brickMap.put(brickNumber, brick);

        brick.brickId = brickNumber;
        brickNumber++;
    }

    @Override
    public void createEvent() {
        super.createEvent();
        camera = Game.getCamera();

        player = (Player) Game.getInstancesOfType(Player.class)[0];

        createBrick(0,0);
        createBrick(0,brickHeight + 3);
        createBrick(0,-(brickHeight + 3));
        createBrick(0,(brickHeight + 3) * 2);
        createBrick(0,-(brickHeight + 3) * 2);
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