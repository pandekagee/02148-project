package dk.spilstuff.game.GameObjects;

import java.awt.Color;

import dk.spilstuff.engine.Game;
import dk.spilstuff.engine.GameObject;

public class Controller extends GameObject {
    ClickableButton[] buttons = new ClickableButton[2];

    @Override
    public void createEvent() {
        super.createEvent();
        
        buttons[0] = (ClickableButton)Game.instantiate(x, y - 50, "ClickableButton");
        buttons[1] = (ClickableButton)Game.instantiate(x, y + 50, "ClickableButton");

        buttons[0].setInfo("            JOIN\nBALLBLOCKADE", "Missing a ball damages you,\nbut your ball can bounce off\nthe opponent's back wall.", () -> {
            Game.sendValue(0, "join", 0);
            Game.setActiveScene("rm_game1");
        });

        buttons[1].setInfo("            JOIN\n   COLOURSWAP", "Missing a ball gives it to\nyour opponent. Balls can\nchange color after their\ninitial touch.", () -> {
            Game.sendValue(0, "join", 1);
            Game.setActiveScene("rm_game2");
        });

        depth = 1000;

        x = 0;
        y = 0;
        xScale = 1000;
        yScale = 1000;
        color = Color.BLACK;
    }
}
