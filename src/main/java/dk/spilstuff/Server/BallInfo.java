package dk.spilstuff.Server;

public class BallInfo {
    public final double x;
    public final double y;
    public final double hsp;
    public final double vsp;
    public final long id;
    public final int team;

    public BallInfo(double x, double y, double hsp, double vsp, long id) {
        this.x = x;
        this.y = y;
        this.hsp = hsp;
        this.vsp = vsp;
        this.id = id;
        this.team = 0;
    }

    public BallInfo(double x, double y, double hsp, double vsp, long id, int team) {
        this.x = x;
        this.y = y;
        this.hsp = hsp;
        this.vsp = vsp;
        this.id = id;
        this.team = team;
    }
}
