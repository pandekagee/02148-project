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

    double hsp = 0;
    double vsp = 0;

    int ballScale = 4;

    Camera camera;

    Player player;

    public long ballID;

    private void playerCollision(){
        int id = (player.playerId + 1) % 2;

        if (collisionMeeting(x+hsp, y, player)){
            hsp = -hsp;

            sendPosition(id);
        }

        if (collisionMeeting(x, y+vsp, player)){
            vsp = -vsp;

            sendPosition(id);
        }
    }

    public void motionSet(double angle, double speed){
        hsp = Mathf.lengthDirectionX(speed, angle);
        vsp = Mathf.lengthDirectionY(speed, angle);
    }

    private void sendPosition(int playerID) {
        Game.sendInteger(playerID + (int)ballID*2, "ballX", (int) (camera.getWidth() - x));
        Game.sendInteger(playerID + (int)ballID*2, "ballY", (int) y);
        Game.sendInteger(playerID + (int)ballID*2, "ballHsp", (int) -hsp);
        Game.sendInteger(playerID + (int)ballID*2, "ballVsp", (int) vsp);
    }

    void getPosition(){
        Integer _x = Game.receiveInteger(player.playerId + (int)ballID*2, "ballX");
        if (_x != null){
            Logger.addLog("ACTIVE");
            x = _x;
            
            Integer _y = Game.receiveInteger(player.playerId + (int)ballID*2, "ballY");
            if (_y != null){ y = _y; }

            Integer _hsp = Game.receiveInteger(player.playerId + (int)ballID*2, "ballHsp");
            if (_hsp != null){ hsp = _hsp; }

            Integer _vsp = Game.receiveInteger(player.playerId + (int)ballID*2, "ballVsp");
            if (_vsp != null){ vsp = _vsp; }
        }
    }

    @Override
    public void createEvent() {
        super.createEvent();

        sprite = new Sprite("whiteSquareBUILTIN", true);
        
        xScale = ballScale;
        yScale = ballScale;

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

        x += hsp;
        y += vsp;

        if (y < 0 || y > camera.getHeight() - ballScale){
            vsp = -vsp;
        }

        if (Game.keyIsPressed(Keys.VK_R)){
            x = camera.getWidth() / 2;
            y = camera.getHeight() / 2;
            motionSet(45+180, 3);
        }
    }

    @Override
    public void drawEvent() {
        Game.drawSquare(depth, x, y, ballScale, ballScale, rotation, Color.WHITE, alpha);
    }
}
