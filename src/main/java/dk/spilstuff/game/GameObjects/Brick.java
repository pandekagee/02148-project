package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Mathf;
import dk.spilstuff.engine.Sprite;

public class Brick extends GameObject {

    public int brickId;
    public int brickType = 0;

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
    public void createEvent() {
        super.createEvent();

        sprite = new Sprite("whiteSquareBUILTIN", true);

        xScale = 10;
        yScale = 29;
    }

    @Override
    public void drawEvent(){
        switch(brickType) {
            case 0: break; //do nothing
            case 1: Game.drawSpriteScaled(new Sprite("spr_ball",true),0,depth-1,x,y,1,1,0,Color.BLACK,1); break;
        }

        drawSelf();
    }
}
