package dk.spilstuff.Server;

public class BallInfo {
    public final double x;
    public final double y;
    public final double hsp;
    public final double vsp;
    public final long id;
    public final int team;
    public final boolean hitByPaddle;
    public final boolean instantiate;

    public BallInfo(double x, double y, double hsp, double vsp, long id, int team, boolean hitByPaddle) {
        this.x = x;
        this.y = y;
        this.hsp = hsp;
        this.vsp = vsp;
        this.id = id;
        this.team = team;
        this.hitByPaddle = hitByPaddle;
        this.instantiate = false;
    }

    public BallInfo(double x, double y, double hsp, double vsp, long id, int team, boolean hitByPaddle, boolean instantiate) {
        this.x = x;
        this.y = y;
        this.hsp = hsp;
        this.vsp = vsp;
        this.id = id;
        this.team = team;
        this.hitByPaddle = hitByPaddle;
        this.instantiate = instantiate;
    }
}
