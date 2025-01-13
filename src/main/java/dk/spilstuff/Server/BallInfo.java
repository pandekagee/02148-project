package dk.spilstuff.Server;

public class BallInfo {
    public final double x;
    public final double y;
    public final double hsp;
    public final double vsp;
    public final long id;
    public int team = 1;

    public BallInfo(double x, double y, double hsp, double vsp, long id) {
        this.x = x;
        this.y = y;
        this.hsp = hsp;
        this.vsp = vsp;
        this.id = id;
    }
}
