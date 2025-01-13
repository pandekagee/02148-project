package dk.spilstuff.game.GameObjects;

import dk.spilstuff.Server.BallInfo;
import dk.spilstuff.Server.BallTeamInfo;
import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;

public class BallManager extends GameObject {

    Player player;

    public void setToBallInfo(Ball ball, BallInfo ballInfo) {
        ball.x = ballInfo.x;
        ball.y = ballInfo.y;
        ball.hsp = ballInfo.hsp;
        ball.vsp = ballInfo.vsp;
    }

    public void setToBallTeam(Ball ball, BallTeamInfo ballTeamInfo) {
        ball.ballTeam = ballTeamInfo.team;
    }

    private void checkForNewBalls() {
        BallInfo ballInfo = Game.receiveValue(player.opponentID, "ballCreated", BallInfo.class);

        while(ballInfo != null) {
            Ball ball = (Ball)Game.instantiate(0, 0, "Ball");
            ball.ballID = ballInfo.id;
            ball.player = player;
            setToBallInfo(ball, ballInfo);

            ballInfo = Game.receiveValue(player.opponentID, "ballCreated", BallInfo.class);
        }
    }

    public Ball getBall(long id){
        GameObject[] ballList = Game.getInstancesOfType(Ball.class);
        Ball ball;

        for (GameObject ballObj : ballList) {
            ball = (Ball) ballObj;
            if (ball.ballID == id){
                return ball;
            }
        }

        return null;
    }

    public void checkForPosition(){
        BallInfo ballInfo;
        
        do{
            ballInfo = Game.receiveValue(player.playerId, "ballInfo", BallInfo.class);
            
            if (ballInfo != null){
                Ball ball = getBall(ballInfo.id);

                setToBallInfo(ball, ballInfo);
            }
        } while(ballInfo != null);
    }

    public void checkForTeam(){
        BallTeamInfo ballTeamInfo;
        
        do{
            ballTeamInfo = Game.receiveValue(player.playerId, "ballTeamInfo", BallTeamInfo.class);
            
            if (ballTeamInfo != null){
                Ball ball = getBall(ballTeamInfo.id);

                setToBallTeam(ball, ballTeamInfo);
            }
        } while(ballTeamInfo != null);
    }

    @Override
    public void createEvent() {
        super.createEvent();
        
        player = (Player) Game.getInstancesOfType(Player.class)[0];
    }

    @Override
    public void updateEvent(){
        super.updateEvent();

        checkForNewBalls();
        checkForTeam();
        checkForPosition();


    }
}


