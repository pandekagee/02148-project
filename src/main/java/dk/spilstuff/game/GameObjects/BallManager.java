package dk.spilstuff.game.GameObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;

public class BallManager extends GameObject {

    public ArrayList<Ball> ballList = new ArrayList<Ball>();
    Player player;
    public int gameMode;

    public void setToBallInfo(Ball ball, BallInfo ballInfo) {
        ball.x = ballInfo.x;
        ball.y = ballInfo.y;
        ball.hsp = ballInfo.hsp;
        ball.vsp = ballInfo.vsp;
        ball.changeTeam(ballInfo.team);
    }

    public Ball getBall(long id){
        try {
            return ballList.get((int)id);
        }
        catch(IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void checkForBallInfo(){
        if(player.performUpdate)
        Game.receiveValue(player.playerId, "ballInfo", BallInfo.class)
        .thenAccept(ballInfos -> {
            if(ballInfos.size() > 1)
                Collections.sort(ballInfos, new Comparator<BallInfo>() {
                    public int compare (BallInfo o1, BallInfo o2) {
                        int comp = (int)o1.id - (int)o2.id;
                        return comp;
                    }
                });

            for(BallInfo ballInfo : ballInfos) {
                Ball ball = getBall(ballInfo.id);
                
                if(ballInfo.instantiate) {
                    ball = (Ball)Game.instantiate(0, 0, "Ball");
                    ball.ballID = ballList.size();
                    ballList.add(ball);
                    ball.player = player;
                }
                
                setToBallInfo(ball, ballInfo);

                if(ballInfo.hitByPaddle) {
                    player.opponent.ballHitTimer = 30;
                }
            }
        });
    }

    @Override
    public void createEvent() {
        super.createEvent();
        
        player = (Player) Game.getInstancesOfType(Player.class)[0];

        gameMode = Game.getActiveScene().getName().equals("rm_game1") ? 1 : 0;
    }

    @Override
    public void updateEvent(){
        checkForBallInfo();
    }
}


