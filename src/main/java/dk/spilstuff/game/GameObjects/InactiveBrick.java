package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Mathf;
import dk.spilstuff.engine.Sprite;

public class InactiveBrick extends GameObject {

    public int brickId;
    public int brickType = 0;

    private int respawnTimer = 0;

    private final double respawnTimerMax = 40 * 60; // 40 seconds

    public void applyEffect(BallInfo ballInfo) {
        switch(brickType) {
            case 0: break; //do nothing
            case 1: // extra ball
                int startAngle = Mathf.intRandomRange(-40, 40) + (ballInfo.team == 2 ? 0 : 180);

                BallInfo _ballInfo = new BallInfo(
                    x + (ballInfo.team == 2 ? 30 : -30),
                    y,
                    Mathf.lengthDirectionX(3, startAngle),
                    Mathf.lengthDirectionY(3, startAngle),
                    0
                );

                Game.sendValue(0, "ballCreated", _ballInfo);
                Game.sendValue(1, "ballCreated", _ballInfo);
            break;
        }
    }

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

            Game.drawSpriteScaled(sprite, 0, depth-1, x, y, _prog+1d, _prog+1d, 0, color, 1d-_prog);
        }

        drawSelf();

        switch(brickType) {
            case 0: break; //do nothing
            case 1: Game.drawSpriteScaled(new Sprite("spr_ball",true),0,depth-1,x,y,xScale,yScale,0,Color.BLACK,alpha); break;
        }
    }
}
