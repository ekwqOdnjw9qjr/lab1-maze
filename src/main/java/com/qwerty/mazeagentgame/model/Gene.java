package com.qwerty.mazeagentgame.model;

import com.qwerty.mazeagentgame.util.Constants;
import lombok.Value;



@Value
public class Gene {
    int action;
    int nextState;

    public Gene(int action, int nextState) {
        this.action = Math.max(0, Math.min(3, action));
        this.nextState = Math.max(0, Math.min(Constants.NUMBER_STATES - 1, nextState));
    }


}