package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import static org.apache.commons.lang3.StringUtils.length;

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
    private int playerIndicatorTimer = 0;
    public int hp = 3;
    public double playerScore = 0;
    public double opponentScore = 0;
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
        if (yScale == 0 && opponent.yScale == 0){
            winner = 3;
        } else if (yScale == 0){
            winner = opponentID+1;
        } else if (opponent.yScale == 0){
            winner = playerId+1;
        }

        if (winner > 0){
            winnerAlarm -= 1;
            if (winnerAlarm <= 0){
                Game.setActiveScene("_rm_menu");
            }
        }
    }

    private void getOpponentY(){
        Double _y = Game.receiveDouble(opponentID, "y");

        if (_y != null){
            opponent.y = _y;
        }
    }

    public double[] getDamageScale(int hp){
        double scaleOff = 2;
        double xS = 8 * (hp+scaleOff) / (3+scaleOff);
        double yS = 32 * (hp+scaleOff) / (3+scaleOff);

        return new double[] {xS, yS};
    }

    private void checkPlayerDeath(){
        if (hp <= 0){
            yScale = 0;
            destroyAllBalls();
        } else {
            double[] scale = getDamageScale(hp);
            xScale = scale[0];
            yScale = scale[1];
        }

        winEvent();
    }

    public void updateOpponent(int playerID){
        Integer data = Game.receiveValue(playerID, "updateOpponent", Integer.class);

        if (data != null){
            if (data > 0){
                double[] scale = getDamageScale(data);
                opponent.xScale = scale[0];
                opponent.yScale = scale[1];
            } else{
                opponent.yScale = 0;
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
        color = playerID == 0 ? Color.RED : Color.BLUE;
    }

    @Override
    public void createEvent() {
        super.createEvent();

        camera = Game.getCamera();

        gameMode = Game.getActiveScene().getName().equals("rm_game1") ? 0 : 1;

        xScale = 8;
        yScale = 32;

        opponent = (Opponent)Game.instantiate(0, y, "Opponent");

        assignSide(0);
        opponent.assignSide(1);
    }

    @Override
    public void updateEvent() {
        playerIndicatorTimer--;

        checkPlayerDeath();
        updateOpponent(playerId);

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

        if (Game.keyIsPressed(Keys.VK_L)){
            createBall();
        }
        
        double prevY = y;
        y = Game.getMouseY();
        y = Math.clamp(y, yScale, camera.getHeight()-yScale);

        if (y != prevY){
            Game.sendValue(playerId, "y", y);
        }

        getOpponentY();

        camera = Game.getCamera();
        
        super.updateEvent();
    }

    @Override
    public void drawEvent() {
        drawSelf();

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
        Game.drawTextScaled( Game.getTextFont("Retro.ttf"),String.valueOf(playerScore), -100, 10, camera.getHeight() - 25,1,1,0,Color.WHITE,1);
        String str = String.valueOf(opponentScore);
        Game.drawTextScaled( Game.getTextFont("Retro.ttf"), str , -100, camera.getWidth() - length(str) * 30, camera.getHeight() - 25,1,1,0,Color.WHITE,1);

        Game.drawText( Game.getTextFont("Retro.ttf"),fpsString,-100, camera.getX() + 10, camera.getY() + 20 );

        if(playerIndicatorTimer > 0)
            Game.drawTextScaled(Game.getTextFont("Retro.ttf"), playerId == 0 ? "<- YOU" : "YOU ->", depth, x + (playerId == 0 ? 20 : -100), y,1,1,0,Color.WHITE, Math.clamp(playerIndicatorTimer/120d, 0, 1));
    }
}
