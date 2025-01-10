package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import org.jspace.ActualField;
import org.jspace.FormalField;

import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Keys;
import dk.spilstuff.engine.Sprite;

public class Player extends GameObject {
    
    int playerId = 1;
    int opponentY = 0;
    boolean gameStart = false;

    Camera camera;

    void getOpponentY(){
        Integer _y = Game.receiveInteger((playerId+1) % 2, "y");

        if (_y != null){
            opponentY = _y;
        }
    }

    @Override
    public void createEvent() {
        super.createEvent();

        Game.sendInteger(0, "join", 0);

        xScale = 8;
        yScale = 32;
    }

    @Override
    public void updateEvent() {

        if (!gameStart){
            Integer _playerId = Game.receiveInteger(0, "joinMessage");

            if (_playerId != null){
                playerId = _playerId;
                gameStart = true;
                Game.instantiate(400, 250, xScale, yScale, 0, 0, new Sprite("whiteSquareBUILTIN", 0, 0), "Ball");
            }
        }

        int yChange = ((Game.keyIsHeld(Keys.VK_S) ? 1 : 0) - (Game.keyIsHeld(Keys.VK_W) ? 1 : 0)) * 2;

        if (Game.keyIsPressed(Keys.VK_L)){
            Game.instantiate(400, 250, xScale, yScale, 0, 0, new Sprite("whiteSquareBUILTIN", 0, 0), "Ball");
        }
        
        y += yChange;

        if (yChange != 0){
            Game.sendInteger(playerId, "y", (int) y);
        }

        getOpponentY();

        camera = Game.getCamera();
        
        super.updateEvent();
    }

    @Override
    public void drawEvent() {
        Game.drawSquare(depth,x,y,8,32,rotation,Color.RED,alpha);
        
        //draw other player
        Game.drawSquare(depth, camera.getWidth() - 50, opponentY, xScale, yScale, rotation, Color.BLUE, alpha);

        String fpsString = "FPS: " + Game.getFPS() + "\nRFPS:" + Game.getRealFPS();

        if (!gameStart){
            Game.drawText( Game.getTextFont("Mono"),"Waiting for opponent", -100, camera.getWidth() / 2 - 85, camera.getHeight() / 2 - 25 );    
        }

        Game.drawText( Game.getTextFont("Mono"),fpsString,-100, camera.getX() + 10, camera.getY() + 20 );
    }
}
