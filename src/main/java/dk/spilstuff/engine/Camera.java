package dk.spilstuff.engine;

public class Camera {
    private double x;
    private double y;
    private int w;
    private int h;

    public Camera(double x, double y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setWidth(int w) {
        this.w = w;
    }

    public int getWidth() {
        return w;
    }

    public void setHeight(int h) {
        this.h = h;
    }

    public int getHeight() {
        return h;
    }
}
