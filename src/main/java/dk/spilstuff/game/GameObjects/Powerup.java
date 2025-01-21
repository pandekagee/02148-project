package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Sprite;

public class Powerup extends GameObject{
    private int timer = 0;

    @Override
    public void createEvent() {
        super.createEvent();

        sprite = new Sprite("spr_powerup", true);
    }

    @Override
    public void drawEvent() {
        timer++;
        if(x < 50 || x > Game.getCamera().getWidth() - 50)
            alpha -= 0.025;
        
        if(alpha < 0)
            Game.destroy(this);

        drawSelf();

        Game.drawSpriteScaled(sprite, 0, depth - 1, x, y, 1 + (timer % 30)/30d,  1 + (timer % 30)/30d, 0, Color.WHITE, (1-(timer % 30)/30d) * alpha);
    }
}
