package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import org.jspace.ActualField;
import org.jspace.FormalField;

import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Keys;
import dk.spilstuff.engine.Logger;
import dk.spilstuff.engine.Mathf;
import dk.spilstuff.engine.Sprite;

public class Ball extends GameObject {
    private  Camera camera;

    public Player player;

    public long ballID;

    private boolean isColliding = false;

    private void playerCollision(){
        int id = (player.playerId + 1) % 2;

        if (collisionMeeting(x+hsp, y, player)){
            if(!isColliding) {
                double dir = Mathf.pointDirection(player.x, player.y, x-hsp, y-vsp);
                dir = dir < 280 && dir > 180 ? 280 : (dir > 80 && dir <= 180 ? 80 : dir); //clamp dir between 280-360 and 0-80 like this angle (<)
                Logger.addLog("Direction: " + dir);
                
                motionSet(dir, 3);

                sendPosition(id);
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
        Game.sendValue(playerID + (int)ballID*2, "ballX", camera.getWidth() - x);
        Game.sendValue(playerID + (int)ballID*2, "ballY", y);
        Game.sendValue(playerID + (int)ballID*2, "ballHsp", -hsp);
        Game.sendValue(playerID + (int)ballID*2, "ballVsp", vsp);
    }

    void getPosition(){
        Double _x = Game.receiveDouble(player.playerId + (int)ballID*2, "ballX");
        if (_x != null){
            Logger.addLog("ACTIVE");
            x = _x;
            
            Double _y = Game.receiveDouble(player.playerId + (int)ballID*2, "ballY");
            if (_y != null){ y = _y; }

            Double _hsp = Game.receiveDouble(player.playerId + (int)ballID*2, "ballHsp");
            if (_hsp != null){ hsp = _hsp; }

            Double _vsp = Game.receiveDouble(player.playerId + (int)ballID*2, "ballVsp");
            if (_vsp != null){ vsp = _vsp; }
        }
    }

    @Override
    public void createEvent() {
        super.createEvent();

        sprite = new Sprite("spr_ball", true);

        camera = Game.getCamera();
        x = camera.getWidth() / 2;
        y = camera.getHeight() / 2;
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
