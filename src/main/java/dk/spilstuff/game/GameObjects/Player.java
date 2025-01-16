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
    public int hp = 3;

    Camera camera;

    public Opponent opponent;

    public int gameMode;

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
            Game.destroy(this);
        } else {
            double[] scale = getDamageScale(hp);
            xScale = scale[0];
            yScale = scale[1];
        }
    }

    public void updateOpponent(int playerID){
        Integer data = Game.receiveValue(playerID, "updateOpponent", Integer.class);

        if (data != null){
            if (data > 0){
                double[] scale = getDamageScale(data);
                opponent.xScale = scale[0];
                opponent.yScale = scale[1];
            } else{
                Game.destroy(opponent);
            }
        }
    }

    private void createBall() {
        int startAngle = Mathf.intRandomRange(-80, 80) + (playerId == 0 ? 0 : 180);

        ballCounter++;

        BallInfo ballInfo = new BallInfo(
            x + (playerId == 0 ? 30 : -30),
            y,
            Mathf.lengthDirectionX(3, startAngle),
            Mathf.lengthDirectionY(3, startAngle),
            ballCounter
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

        int yChange = ((Game.keyIsHeld(Keys.VK_S) ? 1 : 0) - (Game.keyIsHeld(Keys.VK_W) ? 1 : 0)) * 5;

        if (Game.keyIsPressed(Keys.VK_L)){
            createBall();
        }
        
        y += yChange;

        if (yChange != 0){
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

        Game.drawText( Game.getTextFont("Retro.ttf"),fpsString,-100, camera.getX() + 10, camera.getY() + 20 );

        if(playerIndicatorTimer > 0)
            Game.drawTextScaled(Game.getTextFont("Retro.ttf"), playerId == 0 ? "<- YOU" : "YOU ->", depth, x + (playerId == 0 ? 20 : -100), y,1,1,0,Color.WHITE, Math.clamp(playerIndicatorTimer/120d, 0, 1));
    }
}
