package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import static org.apache.commons.lang3.StringUtils.length;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.Server.OpponentInfo;
import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Keys;
import dk.spilstuff.engine.Mathf;
import dk.spilstuff.engine.Sprite;

public class Player extends GameObject {
    
    int playerId = 0;
    int opponentID = 0;
    boolean gameStart = false;
    private int playerIndicatorTimer = 0;
    public int ballHitTimer = 0;
    public int powerupTimer = 0;
    private int powerupTimerMax = 15*60; //15 seconds
    public int hp = 15;
    public int playerScore = 0;
    public int opponentScore = 0;
    int winner = 0;
    int winnerAlarm = 60 * 3;

    Camera camera;

    public Opponent opponent;

    public int gameMode;

    private void destroyAllBalls(){
        GameObject[] ballList = Game.getInstancesOfType(Ball.class);

        for (GameObject obj : ballList) {
            if (obj instanceof Ball) {
                Game.destroy((Ball) obj);
            }
        }
    }

    private void winEvent(){
        if (hp == 0 && opponent.hp == 0){
            winner = 3;
        } else if (hp == 0){
            winner = opponentID+1;
        } else if (opponent.hp == 0){
            winner = playerId+1;
        }

        if (winner > 0){
            winnerAlarm -= 1;
            if (winnerAlarm <= 0){
                Game.setActiveScene("_rm_menu");
            }
        }
    }

    public void checkForPowerupCollision() {
        GameObject powerup = Game.nearestInstance(x, y, Powerup.class);

        if(powerup != null && powerup.alpha > 0.95 && Game.instanceColliding(this, powerup)) {
            powerup.alpha = 0.9;
            powerupTimer = (powerupTimer > 0) ? powerupTimerMax - 15 : powerupTimerMax;
            sendInfo();
        }
    }

    public void setDamageScale(int hp){
        yScale = (hp+2d) / (15+2d);

        if(powerupTimer > 0) {
            if(powerupTimer < 15) { //about to end
                yScale *= 1 + powerupTimer/15d;
            }
            else if(powerupTimer > powerupTimerMax-15) { //just started
                yScale *= 1 + (1-(powerupTimer - (powerupTimerMax-15))/15d);
            }
            else {
                yScale *= 2;
            }
        }
    }

    private void checkPlayerDeath(){
        if (hp <= 0){
            yScale = 0;
            destroyAllBalls();
        } else {
            setDamageScale(hp);
        }

        winEvent();
    }

    public void sendInfo() {
        Game.sendValue(opponentID, "updateOpponent", new OpponentInfo(y, hp, powerupTimer));
    }

    public void updateOpponent(int playerID){
        OpponentInfo opponentInfo = Game.receiveValue(playerID, "updateOpponent", OpponentInfo.class);

        if (opponentInfo != null){
            opponent.hp = opponentInfo.hp;
            opponent.powerupTimer = opponentInfo.powerupTimer;
            opponent.y = opponentInfo.y;
        
            if (opponentInfo.hp <= 0){
                destroyAllBalls();
            }
        }
    }

    private void createBall() {
        int startAngle = Mathf.intRandomRange(-80, 80) + (playerId == 0 ? 0 : 180);

        BallInfo ballInfo = new BallInfo(
            x + (playerId == 0 ? 30 : -30),
            y,
            Mathf.lengthDirectionX(3, startAngle),
            Mathf.lengthDirectionY(3, startAngle),
            0,
            playerId + 1
        );

        Game.sendValue(playerId, "ballCreated", ballInfo);
        Game.sendValue(opponentID, "ballCreated", ballInfo);
    }

    public void assignSide(int playerID) {
        camera = Game.getCamera();
        
        x = playerID == 0 ? 50 : camera.getWidth() - 50;
        playerId = playerID;
    }

    @Override
    public void createEvent() {
        super.createEvent();

        camera = Game.getCamera();

        gameMode = Game.getActiveScene().getName().equals("rm_game1") ? 0 : 1;

        imgSpeed = 0;

        sprite = new Sprite("spr_paddle", true);

        opponent = (Opponent)Game.instantiate(0, y, "Opponent");

        assignSide(0);
        opponent.assignSide(1);
    }

    @Override
    public void updateEvent() {
        playerIndicatorTimer--;
        ballHitTimer--;
        if(powerupTimer > 0)
            powerupTimer--;

        checkPlayerDeath();
        updateOpponent(playerId);
        setDamageScale(hp);

        checkForPowerupCollision();

        if(Game.keyIsPressed(Keys.VK_ESCAPE)) {
            Game.setActiveScene("_rm_menu"); //leave game
        }

        if (!gameStart){
            Integer _playerId = Game.receiveInteger(gameMode, "joinMessage");

            if (_playerId != null){
                playerId = _playerId;
                opponentID = (playerId + 1) % 2;
                gameStart = true;

                assignSide(playerId);
                opponent.assignSide(opponentID);

                createBall();

                updateOpponent(opponentID);

                playerIndicatorTimer = 8 * 60; // 8 seconds
            }
        }
        
        double prevY = y;
        y = Game.getMouseY();
        y = Math.clamp(y, 32*yScale, camera.getHeight()-32*yScale);

        if (y != prevY){
            sendInfo();
        }

        camera = Game.getCamera();
        
        super.updateEvent();
    }

    @Override
    public void drawEvent() {
        Game.drawSpriteScaled(sprite, playerId * 2 + (ballHitTimer > 0 ? 1 : 0), depth, x + (playerId == 0 ? -1 : 1) * Math.clamp(ballHitTimer, 0, 30)/3, y, xScale, yScale, rotation, color, alpha);

        String fpsString = "FPS: " + Game.getFPS() + "\nRFPS:" + Game.getRealFPS();

        if (!gameStart){
            Game.drawTextScaled( Game.getTextFont("Retro.ttf"),"WAITING FOR OPPONENT", -100, camera.getWidth() / 2 - 140, camera.getHeight() / 2 - 25,1,1,0,Color.BLACK,1);
            Game.drawText( Game.getTextFont("Retro.ttf"),"WAITING FOR OPPONENT", -100, camera.getWidth() / 2 - 140-2, camera.getHeight() / 2 - 25-2);    
        }

        if (winner > 0){
            String winner_string = winner == 1 ? "PLAYER RED" : winner == 2 ? "PLAYER BLUE" : "DRAW";
            Color winnerColor = winner == 1 ? Color.red : winner == 2 ? Color.blue : Color.green;
            Game.drawTextScaled( Game.getTextFont("Retro.ttf"),"THE WINNER IS " + winner_string, -100, camera.getWidth() / 2 - 137+2, camera.getHeight() / 2 - 25+2, 1, 1, 0, Color.black, 1);
            Game.drawTextScaled( Game.getTextFont("Retro.ttf"),"THE WINNER IS " + winner_string, -100, camera.getWidth() / 2 - 137, camera.getHeight() / 2 - 25, 1, 1, 0, winnerColor, 1);
        }

        //draw scores
        int leftScore = playerId == 1 ? playerScore : opponentScore;
        int rightScore = playerId == 1 ? opponentScore : playerScore;

        Game.drawTextScaled( Game.getTextFont("Retro.ttf"),"Score: "+leftScore, -100, 10, camera.getHeight() - 25,1,1,0,Color.WHITE,1);
        Game.drawTextScaled( Game.getTextFont("Retro.ttf"), "Score: "+rightScore , -100, camera.getWidth() - length("Score: "+rightScore) * 15, camera.getHeight() - 25,1,1,0,Color.WHITE,1);

        Game.drawText( Game.getTextFont("Retro.ttf"),fpsString,-100, camera.getX() + 10, camera.getY() + 20 );

        if(playerIndicatorTimer > 0)
            Game.drawTextScaled(Game.getTextFont("Retro.ttf"), playerId == 0 ? "<- YOU" : "YOU ->", depth, x + (playerId == 0 ? 20 : -100), y,1,1,0,Color.WHITE, Math.clamp(playerIndicatorTimer/120d, 0, 1));
    }
}
