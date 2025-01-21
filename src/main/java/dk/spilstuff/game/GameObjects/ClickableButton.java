package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Keys;
import dk.spilstuff.engine.Sprite;

public class ClickableButton extends GameObject {
    private String name;
    private String description;
    private Runnable voidFunction;

    private boolean hovering = false;

    public void setInfo(String name, String description, Runnable voidFunction) {
        this.name = name;
        this.description = description;
        this.voidFunction = voidFunction;
    }

    @Override
    public void createEvent() {
        super.createEvent();

        depth = -1000;

        sprite = new Sprite("whiteSquareBUILTIN", true);

        xScale = 110;
        yScale = 30;
    }

    @Override
    public void updateEvent() {
        hovering = Game.positionColliding(Game.getMouseX(), Game.getMouseY(), this);

        if(hovering && Game.mouseButtonIsPressed(Keys.MB_LEFT)) {
            voidFunction.run();
        }
    }

    @Override
    public void drawEvent() {
        Game.drawSpriteScaled(sprite, subimg, depth, x, y, xScale+2, yScale+2, rotation, Color.WHITE, alpha);
        Game.drawSpriteScaled(sprite, subimg, depth, x, y, xScale, yScale, rotation, hovering ? Color.GRAY : Color.DARK_GRAY, alpha);

        Game.drawText(Game.getTextFont("Retro.ttf"), name, depth-1, x - 95, y);

        if(hovering)
            Game.drawTextScaled(Game.getTextFont("Retro.ttf"), description, depth - 1, x - 120, 315, 0.7,0.7,0,Color.WHITE,1);
    }
}
