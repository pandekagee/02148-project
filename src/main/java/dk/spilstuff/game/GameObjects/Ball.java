package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import org.jspace.ActualField;
import org.jspace.FormalField;

import dk.spilstuff.engine.Camera;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;
import dk.spilstuff.engine.Keys;

public class Ball extends GameObject {

    double hsp = 0;
    double vsp = 0;

    int ballScale = 4;

    Camera camera;

    Player player;

    void playerCollision(){
        int id = (player.playerId + 1) % 2;

        if (collisionMeeting(x+hsp, y, player)){
            hsp = -hsp;

            Game.sendInteger(id, "ballX", (int) (camera.getWidth() - x));
            Game.sendInteger(id, "ballY", (int) y);
            Game.sendInteger(id, "ballHsp", (int) -hsp);
            Game.sendInteger(id, "ballVsp", (int) vsp);
        }

        if (collisionMeeting(x, y+vsp, player)){
            vsp = -vsp;
            Game.sendInteger(id, "ballX", (int) (camera.getWidth() - x));
            Game.sendInteger(id, "ballY", (int) y);
            Game.sendInteger(id, "ballHsp", (int) -hsp);
            Game.sendInteger(id, "ballVsp", (int) vsp);
        }
    }

    void motionSet(double angle, double speed){
        double rad = Math.toRadians(angle); 
        hsp = Math.cos(rad) * speed;
        vsp = Math.sin(rad) * speed;
    }

    void getPosition(){
        Integer _x = Game.receiveInteger(player.playerId, "ballX");
        if (_x != null){ x = _x; System.out.print("ACTIVE"); }

        Integer _y = Game.receiveInteger(player.playerId, "ballY");
        if (_y != null){ y = _y; }

        Integer _hsp = Game.receiveInteger(player.playerId, "ballHsp");
        if (_hsp != null){ hsp = _hsp; }

        Integer _vsp = Game.receiveInteger(player.playerId, "ballVsp");
        if (_vsp != null){ vsp = _vsp; }
    }

    @Override
    public void createEvent() {
        super.createEvent();

        xScale = ballScale;
        yScale = ballScale;
        
        player = (Player) Game.getInstancesOfType(Player.class)[0];

        camera = Game.getCamera();
        x = camera.getWidth() / 2;
        y = camera.getHeight() / 2;

        motionSet(45+180, 3);
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
