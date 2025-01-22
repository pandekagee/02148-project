package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Sprite;

public class InactiveBrick extends GameObject {

    public int brickId;
    public int brickType = 0;

    private int respawnTimer = 0;

    private final double respawnTimerMax = 75 * 60; // 75 seconds

    @Override
    public void updateEvent(){
        respawnTimer++;

        if(respawnTimer >= respawnTimerMax) {
            Game.destroy(this);

            ((BrickManager)Game.getInstancesOfType(BrickManager.class)[0]).createBrick((int)x, (int)y, color.getRGB(), brickId, brickType);
        }

        alpha = (double)respawnTimer / respawnTimerMax / 2d;
        xScale = alpha * 2;
        yScale = alpha * 2;
    }

    @Override
    public void drawEvent(){
        if(respawnTimer < 30) {
            double _prog = respawnTimer/30d;

            if (sprite != null)
                Game.drawSpriteScaled(sprite, 0, depth-1, x, y, _prog+1d, _prog+1d, 0, color, 1d-_prog);
        }

        drawSelf();

        switch(brickType) {
            case 0: break; //do nothing
            case 1: Game.drawSpriteScaled(new Sprite("spr_ball",true),0,depth-1,x,y,xScale,yScale,0,Color.BLACK,alpha); break;
        }
    }
}
