package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Sprite;

public class Opponent extends GameObject {
    public void assignSide(int playerID) {
        Camera camera = Game.getCamera();
        
        x = playerID == 0 ? 50 : camera.getWidth() - 50;
        color = playerID == 0 ? Color.RED : Color.BLUE;
    }

    @Override
    public void createEvent() {
        super.createEvent();

        sprite = new Sprite("whiteSquareBUILTIN", true);

        xScale = 8;
        yScale = 32;
    }
}
