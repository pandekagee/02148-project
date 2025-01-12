package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import org.jspace.ActualField;
import org.jspace.FormalField;

import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Keys;
import dk.spilstuff.engine.Mathf;
import dk.spilstuff.engine.Sprite;

public class Player extends GameObject {
    
    int playerId = 1;
    int opponentID = 0;
    double opponentY = 0;
    boolean gameStart = false;
    int ballCounter = 0;

    Camera camera;

    private void getOpponentY(){
        Double _y = Game.receiveDouble(opponentID, "y");

        if (_y != null){
            opponentY = _y;
        }
    }

    private void checkForNewBalls() {
        Integer ballStartAngle = Game.receiveInteger(opponentID, "ballCreated");

        while(ballStartAngle != null) {
            Ball ball = (Ball)Game.instantiate(0, 0, "Ball");
            ball.ballID = ballCounter;
            ball.player = this;
            ballCounter++;

            ball.motionSet(ballStartAngle, 3);

            ballStartAngle = Game.receiveInteger(opponentID, "ballCreated");
        }
    }

    private void createBall() {
        int startAngle = Mathf.intRandomRange(100, 260);
        Game.sendValue(playerId, "ballCreated", 180 - startAngle);
        Game.sendValue(opponentID, "ballCreated", startAngle);
    }

    @Override
    public void createEvent() {
        super.createEvent();

        Game.sendValue(0, "join", 0);

        xScale = 8;
        yScale = 32;
        color = Color.RED;
    }

    @Override
    public void updateEvent() {

        if (!gameStart){
            Integer _playerId = Game.receiveInteger(0, "joinMessage");

            if (_playerId != null){
                playerId = _playerId;
                opponentID = (playerId + 1) % 2;
                gameStart = true;

                createBall();
            }
        }

        int yChange = ((Game.keyIsHeld(Keys.VK_S) ? 1 : 0) - (Game.keyIsHeld(Keys.VK_W) ? 1 : 0)) * 2;

        if (Game.keyIsPressed(Keys.VK_L)){
            createBall();
        }
        
        y += yChange;

        if (yChange != 0){
            Game.sendValue(playerId, "y", y);
        }

        getOpponentY();

        checkForNewBalls();

        camera = Game.getCamera();
        
        super.updateEvent();
    }

    @Override
    public void drawEvent() {
        drawSelf();
        
        //draw other player
        Game.drawSpriteScaled(sprite, 0, depth, camera.getWidth() - 50, opponentY, xScale, yScale, rotation, Color.BLUE, alpha);

        String fpsString = "FPS: " + Game.getFPS() + "\nRFPS:" + Game.getRealFPS();

        if (!gameStart){
            Game.drawText( Game.getTextFont("Mono"),"Waiting for opponent", -100, camera.getWidth() / 2 - 85, camera.getHeight() / 2 - 25 );    
        }

        Game.drawText( Game.getTextFont("Mono"),fpsString,-100, camera.getX() + 10, camera.getY() + 20 );
    }
}
