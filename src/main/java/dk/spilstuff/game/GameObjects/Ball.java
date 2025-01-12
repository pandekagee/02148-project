package dk.spilstuff.game.GameObjects;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Logger;
import dk.spilstuff.engine.Mathf;
import dk.spilstuff.engine.Sprite;

public class Ball extends GameObject {
    private  Camera camera;

    public Player player;

    public long ballID;

    private boolean isColliding = false;

    private void playerCollision() {

        if (collisionMeeting(x+hsp, y, player)){
            if(!isColliding) {
                double dir = Mathf.pointDirection(player.x, player.y, x-hsp, y-vsp);

                if(Mathf.dcos(dir) * -Mathf.sign(hsp) < 0.17) { // if very close to a shallow angle, then fix it
                    if(dir > 180) {
                        dir = 270 - Mathf.sign(hsp) * 10;
                    }
                    else {
                        dir = 90 + Mathf.sign(hsp) * 10;
                    }
                }
                
                motionSet(dir, 3);

                sendPosition(player.opponentID);
            }

            isColliding = true;
        }
        else {
            isColliding = false;
        }
    }

    public void motionSet(double angle, double speed){
        hsp = Mathf.lengthDirectionX(speed, angle);
        vsp = Mathf.lengthDirectionY(speed, angle);
    }

    private void sendPosition(int playerID) {
        Game.sendValue(playerID + (int)ballID * 2, "ballInfo", new BallInfo(x, y, hsp, vsp));
    }

    public void setToBallInfo(BallInfo ballInfo) {
        x = ballInfo.x;
        y = ballInfo.y;
        hsp = ballInfo.hsp;
        vsp = ballInfo.vsp;
    }

    void getPosition(){
        BallInfo ballInfo = Game.receiveValue(player.playerId + (int)ballID * 2, "ballInfo", BallInfo.class);
        if (ballInfo != null){
            setToBallInfo(ballInfo);
        }
    }

    @Override
    public void createEvent() {
        super.createEvent();

        sprite = new Sprite("spr_ball", true);

        camera = Game.getCamera();
    }

    @Override
    public void updateEvent() {

        camera = Game.getCamera();
        
        super.updateEvent();

        playerCollision();
        getPosition();

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
