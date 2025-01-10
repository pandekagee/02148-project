package dk.spilstuff.engine;

import java.util.ArrayList;
import java.awt.Color;

public class GameObject {

    private class ScheduledEvent {
        public int frameDelay;
        public Script script;

        public ScheduledEvent(int frameDelay, Script script) {
            this.frameDelay = frameDelay;
            this.script = script;
        }
    }

    public ArrayList<ScheduledEvent> scheduledEvents = new ArrayList<ScheduledEvent>();
    public boolean instantiated = false;
    public boolean destroyed = false;
    public int depth = 0;
    public Sprite sprite = null;
    public double x = 0;
    public double y = 0;
    public double subimg = 0;
    public double imgSpeed = 1;
    public double spd = 0;
    public double direction = 0;
    public double hsp = 0;
    public double vsp = 0;
    public double xScale = 1;
    public double yScale = 1;
    public double rotation = 0;
    public double friction = 0;
    public boolean visible = true;
    public Color color = null;
    public double alpha = 1;

    public void createEvent() {
        instantiated = true;
    }

    public void updateEvent() {
        for(ScheduledEvent e : scheduledEvents.toArray(new ScheduledEvent[0])) {
            e.frameDelay--;

            if(e.frameDelay <= 0) {
                e.script.script();
                scheduledEvents.remove(e);
            }
        }

        subimg += imgSpeed;

        int hSign = (hsp == 0 || friction == 0) ? 0 : ((hsp > 0) ? 1 : -1);
        if(hSign != 0) {
            setHsp(hsp + friction * -hSign);
            if(((hsp == 0) ? 0 : ((hsp > 0) ? 1 : -1)) != hSign) {
                setHsp(0);
            }
        }

        int vSign = (vsp == 0 || friction == 0) ? 0 : ((vsp > 0) ? 1 : -1);
        if(vSign != 0) {
            setVsp(vsp + friction * -vSign);
            if(((vsp == 0) ? 0 : ((vsp > 0) ? 1 : -1)) != vSign) {
                setVsp(0);
            }
        }
        
        x += hsp;
        y += vsp;
    }

    public void drawEvent() {
        drawSelf();
    }

    public boolean collisionMeeting(double x, double y, GameObject instance){
        return (Game.instanceCollidingOffset(x, y, this, instance));
    }

    protected void drawSelf() {
        if(sprite != null) {
            Game.drawSpriteScaled(sprite, subimg, depth, x, y, xScale, yScale, rotation, color, alpha);
        }
    }

    protected void setDirection(double direction) {
        this.direction = direction;

        this.hsp = this.spd * Math.cos(Math.toRadians(this.direction));
        this.vsp = this.spd * Math.sin(Math.toRadians(this.direction));
    }

    protected void setSpd(double spd) {
        this.spd = spd;

        this.hsp = this.spd * Math.cos(Math.toRadians(this.direction));
        this.vsp = this.spd * Math.sin(Math.toRadians(this.direction));
    }

    protected void setHsp(double hsp) {
        this.hsp = hsp;

        this.direction = Mathf.pointDirection(0, 0, this.hsp, this.vsp);
        this.spd = Math.sqrt(Math.pow(this.hsp,2) + Math.pow(this.vsp,2));
    }

    protected void setVsp(double vsp) {
        this.vsp = vsp;

        this.direction = Mathf.pointDirection(0, 0, this.hsp, this.vsp);
        this.spd = Math.sqrt(Math.pow(this.hsp,2) + Math.pow(this.vsp,2));
    }

    protected void schedule(int frameDelay, Script script) {
        scheduledEvents.add(new ScheduledEvent(frameDelay, script));
    }

}
