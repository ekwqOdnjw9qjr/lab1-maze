package com.qwerty.mazeagentgame.model;



import com.qwerty.mazeagentgame.util.Constants;
import lombok.Value;

import java.util.Random;

@Value
public class Gene {
    int action;
    int nextState;

    public static Gene random(Random rand) {
        return new Gene(rand.nextInt(4), rand.nextInt(Constants.NUM_STATES));
    }
}