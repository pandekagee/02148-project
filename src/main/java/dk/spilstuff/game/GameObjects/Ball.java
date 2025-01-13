package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.Server.BallTeamInfo;
import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Mathf;
import dk.spilstuff.engine.Sprite;

public class Ball extends GameObject {
    private  Camera camera;

    public Player player;
    public BrickManager brickLayout;

    public long ballID;
    public int ballTeam = 0;
    public Color[] ballColors = {Color.white, Color.red, Color.blue};

    private boolean isColliding = false;

    private void damagePlayer(){
        player.hp -= 1;
        Game.sendValue(player.opponentID, "updateOpponent", player.hp);
    }

    private void playerCollision() {

        if (collisionMeeting(x+hsp, y, player)){
            if(!isColliding) {
                double dir = Mathf.pointDirection(player.x, player.y, x-hsp, y-vsp);

                if (ballTeam == 0){
                    ballTeam = player.playerId+1;
                } else{
                    if (ballTeam != player.playerId + 1){
                        damagePlayer();
                    }
                }

                if(Mathf.dcos(dir) * -Mathf.sign(hsp) < 0.17) { 
                    if(dir > 180) {
                        dir = 270 - Mathf.sign(hsp) * 10;
                    }
                    else {
                        dir = 90 + Mathf.sign(hsp) * 10;
                    }
                }
                
                motionSet(dir, 3);

                sendTeam(player.opponentID);
                sendPosition(player.opponentID);
            }

            isColliding = true;
        }
        else {
            isColliding = false;
        }
    }

    private void brickCollision(){
        Brick brick = (Brick) Game.nearestInstance(x, y, Brick.class);

        if (brick != null){
            if (collisionMeeting(x+hsp, y, brick)){
                hsp = -hsp;
                brickLayout.destroyBrick(brick.brickId);
                Game.sendValue(player.opponentID, "destroyBrick", brick.brickId);
                sendPosition(player.opponentID);
            }

            if (collisionMeeting(x, y+vsp, brick)){
                vsp = -vsp;
                brickLayout.destroyBrick(brick.brickId);
                Game.sendValue(player.opponentID, "destroyBrick", brick.brickId);
                sendPosition(player.opponentID);
            }
        }
    }

    public void motionSet(double angle, double speed){
        hsp = Mathf.lengthDirectionX(speed, angle);
        vsp = Mathf.lengthDirectionY(speed, angle);
    }

    private void sendPosition(int playerID) {
        BallInfo ballInfo = new BallInfo(x, y, hsp, vsp, ballID);
        Game.sendValue(playerID, "ballInfo", ballInfo);
    }

    private void sendTeam(int playerID) {
        BallTeamInfo ballTeamInfo = new BallTeamInfo(ballID, ballTeam);
        Game.sendValue(playerID, "ballTeamInfo", ballTeamInfo);
    }

    @Override
    public void createEvent() {
        super.createEvent();

        brickLayout = (BrickManager) Game.getInstancesOfType(BrickManager.class)[0];

        sprite = new Sprite("spr_ball", true);

        camera = Game.getCamera();
    }

    @Override
    public void updateEvent() {

        color = ballColors[ballTeam];

        camera = Game.getCamera();
        
        super.updateEvent();

        playerCollision();
        brickCollision();

        if (y < 0 || y > camera.getHeight() - sprite.getWidth() / 2){
            vsp = -vsp;
        }

        //screen-wrapping (if you miss a ball, your opponent gets it)
        if(x < 0 || x > camera.getWidth()) {
            Game.destroy(this);
        }
    }

    @Override
    public void drawEvent() {
        drawSelf();

        Game.drawText(Game.getTextFont("Mono"),""+ballID,0,x + 6, y + 6);
    }
}
