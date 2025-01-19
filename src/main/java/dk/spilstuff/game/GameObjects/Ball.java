package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.Server.BallTeamInfo;
import dk.spilstuff.Server.BrickDestructInfo;
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
    private double teamChangeTimer = 0;
    public Color[] ballColors = {Color.white, Color.red, Color.blue};

    private boolean isColliding = false;

    private void increasePlayerScore(){
        if (ballTeam == 0){

        } else if (ballTeam == player.playerId+1){
            player.opponentScore += 1;
        } else{
            player.playerScore += 1;
        }
    }

    private void changeTeam(int ballTeam) {
        this.ballTeam = ballTeam;
        teamChangeTimer = 30;
    }

    private void damagePlayer(){
        player.hp -= 1;
        Game.sendValue(player.opponentID, "updateOpponent", player.hp);
    }

    private void playerCollision() {

        if (collisionMeeting(x+hsp, y, player)){
            if(!isColliding && (player.gameMode == 0 || Mathf.sign(hsp) == (player.playerId == 0 ? -1 : 1))) {
                double dir = Mathf.pointDirection(player.x, player.y, x-hsp, y-vsp);

                if (ballTeam == 0){
                    changeTeam(player.playerId+1);
                } else{
                    if (ballTeam != player.playerId + 1){
                        damagePlayer();

                        if(player.gameMode == 1) {
                            changeTeam(player.playerId+1);
                        }
                    }
                }

                //correct the angle if it's very shallow
                if(Mathf.dcos(dir) * -Mathf.sign(hsp) < 0.258) { 
                    if(dir > 180) {
                        dir = 270 - Mathf.sign(hsp) * 15;
                    }
                    else {
                        dir = 90 + Mathf.sign(hsp) * 15;
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
            boolean collided = false;
            
            if (collisionMeeting(x+hsp, y, brick)){
                hsp = -hsp;

                collided = true;
            }

            if (collisionMeeting(x, y+vsp, brick)){
                vsp = -vsp;

                collided = true;
            }

            if(collided) {
                BallInfo _ballInfo = new BallInfo(x,y,hsp,vsp,ballID,ballTeam); 

                brickLayout.destroyBrick(_ballInfo, brick.brickId, true);
                
                increasePlayerScore();

                Game.sendValue(player.opponentID, "destroyBrick", new BrickDestructInfo(brick.brickId, _ballInfo));
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

        if (y < -vsp || y > camera.getHeight() - sprite.getWidth() / 2 - vsp){
            vsp = -vsp;
        }

        double angle = Mathf.pointDirection(x, y, x+hsp, y+vsp);

        if (ballTeam == 0){
            motionSet(angle, 3);
        } else if (player.playerId+1 == ballTeam){
            motionSet(angle, (3 + player.playerScore / 5));
        } else{
            motionSet(angle, 3 + player.opponentScore / 5);
        }
        
        if(x < 0) {
            if(player.gameMode == 0) {
                if (ballTeam == 2){
                    hsp = -hsp;
                } else if (player.playerId == 0){
                    x = player.x;
                    y = player.y;
                    sendPosition(player.opponentID);
                    damagePlayer();
                }
            }
            else { //screenwrap and swap if colourswap gamemode
                x = camera.getWidth();
                changeTeam(2); //blue team's ball
            }
        }
        else if(x > camera.getWidth()) {
            if(player.gameMode == 0) {
                if (ballTeam == 1){
                    hsp = -hsp;
                } else if (player.playerId == 1){
                    x = player.x;
                    y = player.y;
                    sendPosition(player.opponentID);
                    damagePlayer();
                }
            }
            else { //screenwrap and swap if colourswap gamemode
                x = 0;
                changeTeam(1); //red team's ball
            }
        }

        teamChangeTimer--;
    }

    @Override
    public void drawEvent() {
        drawSelf();

        if(teamChangeTimer > 0)
            Game.drawSpriteScaled(sprite, subimg, depth - 1, x, y, teamChangeTimer/15+1, teamChangeTimer/15+1, rotation, color, (30-teamChangeTimer)/30);
    }
}
