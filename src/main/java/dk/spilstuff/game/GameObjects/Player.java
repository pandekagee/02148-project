package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Keys;
import dk.spilstuff.engine.Mathf;

public class Player extends GameObject {
    
    int playerId = 1;
    int opponentID = 0;
    boolean gameStart = false;
    int ballCounter = 0;
    private int playerIndicatorTimer = 0;

    Camera camera;

    public Opponent opponent;

    private void getOpponentY(){
        Double _y = Game.receiveDouble(opponentID, "y");

        if (_y != null){
            opponent.y = _y;
        }
    }

    private void checkForNewBalls() {
        BallInfo ballInfo = Game.receiveValue(opponentID, "ballCreated", BallInfo.class);

        while(ballInfo != null) {
            Ball ball = (Ball)Game.instantiate(0, 0, "Ball");
            ball.ballID = ballCounter;
            ball.player = this;
            ballCounter++;

            ball.setToBallInfo(ballInfo);

            ballInfo = Game.receiveValue(opponentID, "ballCreated", BallInfo.class);
        }
    }

    private void createBall() {
        int startAngle = Mathf.intRandomRange(-80, 80) + (playerId == 0 ? 0 : 180);

        BallInfo ballInfo = new BallInfo(
            x + (playerId == 0 ? 15 : -15),
            y,
            Mathf.lengthDirectionX(3, startAngle),
            Mathf.lengthDirectionY(3, startAngle)
        );

        Game.sendValue(playerId, "ballCreated", ballInfo);
        Game.sendValue(opponentID, "ballCreated", ballInfo);
    }

    public void assignSide(int playerID) {
        camera = Game.getCamera();
        
        x = playerID == 0 ? 50 : camera.getWidth() - 50;
        color = playerID == 0 ? Color.RED : Color.BLUE;
    }

    @Override
    public void createEvent() {
        super.createEvent();

        Game.sendValue(0, "join", 0);

        xScale = 8;
        yScale = 32;

        opponent = (Opponent)Game.instantiate(0, y, "Opponent");

        assignSide(0);
        opponent.assignSide(1);

    }

    @Override
    public void updateEvent() {
        playerIndicatorTimer--;

        if (!gameStart){
            Integer _playerId = Game.receiveInteger(0, "joinMessage");

            if (_playerId != null){
                playerId = _playerId;
                opponentID = (playerId + 1) % 2;
                gameStart = true;

                assignSide(playerId);
                opponent.assignSide(opponentID);

                createBall();

                playerIndicatorTimer = 8 * 60; // 8 seconds
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

        String fpsString = "FPS: " + Game.getFPS() + "\nRFPS:" + Game.getRealFPS();

        if (!gameStart){
            Game.drawText( Game.getTextFont("Mono"),"Waiting for opponent", -100, camera.getWidth() / 2 - 85, camera.getHeight() / 2 - 25 );    
        }

        Game.drawText( Game.getTextFont("Mono"),fpsString,-100, camera.getX() + 10, camera.getY() + 20 );

        if(playerIndicatorTimer > 0)
            Game.drawTextScaled(Game.getTextFont("Mono"), playerId == 0 ? "<- You" : "You ->", depth, x + (playerId == 0 ? 40 : -80), y,1,1,0,Color.WHITE, Math.clamp(playerIndicatorTimer/120d, 0, 1));
    }
}
