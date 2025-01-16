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
    private double teamChangeTimer = 0;
    public Color[] ballColors = {Color.white, Color.red, Color.blue};

    private boolean isColliding = false;

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
        
        if(x < 0) {
            if(player.gameMode == 0) {
                hsp = -hsp;
            }
            else { //screenwrap and swap if colourswap gamemode
                x = camera.getWidth();
                changeTeam(2); //blue team's ball
            }
        }
        else if(x > camera.getWidth()) {
            if(player.gameMode == 0) {
                hsp = -hsp;
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
