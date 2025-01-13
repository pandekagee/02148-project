package dk.spilstuff.game.GameObjects;

import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Sprite;

public class Brick extends GameObject {

    public int brickId;

    @Override
    public void createEvent() {
        super.createEvent();

        sprite = new Sprite("whiteSquareBUILTIN", true);

        xScale = 4;
        yScale = 16;
    }

    @Override
    public void updateEvent(){
        super.updateEvent();
    }

    @Override
    public void drawEvent(){
        drawSelf();
    }
}
