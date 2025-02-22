package com.qwerty.mazeagentgame.model;









import com.qwerty.mazeagentgame.util.Orientation;
import lombok.Data;


@Data
public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position move(Orientation orient) {
        return new Position(x + orient.getDx(), y + orient.getDy());
    }
}