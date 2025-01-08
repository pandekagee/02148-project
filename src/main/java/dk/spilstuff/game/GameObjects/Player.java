package dk.spilstuff.game.GameObjects;

import dk.spilstuff.engine.*;

import java.awt.Color;

public class Player extends GameObject {
    

    @Override
    public void createEvent() {
        super.createEvent();
    }

    @Override
    public void updateEvent() {
        int xChange = (Game.keyIsHeld(Keys.VK_D) ? 1 : 0) - (Game.keyIsHeld(Keys.VK_A) ? 1 : 0);
        int yChange = (Game.keyIsHeld(Keys.VK_S) ? 1 : 0) - (Game.keyIsHeld(Keys.VK_W) ? 1 : 0);

        x += xChange;
        y += yChange;

        Camera camera = Game.getCamera();

        camera.setX(x - camera.getWidth()/2);
        camera.setY(y - camera.getHeight()/2);
        
        super.updateEvent();
    }

    @Override
    public void drawEvent() {
        Game.drawSquare(depth,x,y,8,8,rotation,Color.RED,alpha);
        
        Camera camera = Game.getCamera();
        String fpsString = "FPS: " + Game.getFPS() + "\nRFPS:" + Game.getRealFPS();

        Game.drawText(      Game.getTextFont("Mono"),fpsString,-100, camera.getX() + 10, camera.getY() + 20);
        Game.drawTextScaled(Game.getTextFont("Mono"),fpsString,-99 , camera.getX() + 11, camera.getY() + 21,1,1,0,Color.BLACK,1);

        Game.drawTextScaled(Game.getTextFont("Roboto.ttf"),"Hello OSKARMUS!!!!",depth - 1, x + 30, y + 30, 2, 2, 0, Color.BLACK, alpha);
    }
}
