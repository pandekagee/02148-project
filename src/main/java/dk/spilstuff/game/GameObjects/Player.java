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
    int opponentID = 0;
    int opponentY = 0;
    boolean gameStart = false;

    Camera camera;

    private void getOpponentY(){
        Integer _y = Game.receiveInteger(opponentID, "y");

        if (_y != null){
            opponentY = _y;
        }
    }

    private void checkForNewBalls() {
        while(Game.receiveInteger(opponentID, "ballCreated") != null) {
            createBall(false);
        }
    }

    private void createBall(boolean sendUpdate) {
        Ball ball = (Ball)Game.instantiate(0, 0, "Ball");
        ball.ballID = Game.getInstanceCount(Ball.class) - 1;
        ball.player = this;

        if(sendUpdate) {
            Game.sendInteger(playerId, "ballCreated", 0);
            ball.motionSet(45+180, 3); //point it towards own paddle
        }
        else {
            ball.motionSet(45+270, 3); //point it towards opponent's paddle
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
                opponentID = (playerId + 1) % 2;
                gameStart = true;
                createBall(true); //each player gets a ball
            }
        }

        int yChange = ((Game.keyIsHeld(Keys.VK_S) ? 1 : 0) - (Game.keyIsHeld(Keys.VK_W) ? 1 : 0)) * 2;

        if (Game.keyIsPressed(Keys.VK_L)){
            createBall(true);
        }
        
        y += yChange;

        if (yChange != 0){
            Game.sendInteger(playerId, "y", (int) y);
        }

        getOpponentY();

        checkForNewBalls();

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
