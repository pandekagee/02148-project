package dk.spilstuff.game.GameObjects;

import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Sprite;

public class Opponent extends GameObject {
    int hp = 15;
    public int playerId = 1;
    public int ballHitTimer = 0;
    
    public void assignSide(int playerID) {
        Camera camera = Game.getCamera();
        
        x = playerID == 0 ? 50 : camera.getWidth() - 50;
        playerId = playerID;
    }

    @Override
    public void createEvent() {
        super.createEvent();

        imgSpeed = 0;

        sprite = new Sprite("spr_paddle", true);
    }

    @Override
    public void updateEvent() {
        ballHitTimer--;
    }

    @Override
    public void drawEvent() {
        Game.drawSpriteScaled(sprite, playerId * 2 + (ballHitTimer > 0 ? 1 : 0), depth, x + (playerId == 0 ? -1 : 1) * Math.clamp(ballHitTimer, 0, 30)/3, y, xScale, yScale, rotation, color, alpha);
    }
}
