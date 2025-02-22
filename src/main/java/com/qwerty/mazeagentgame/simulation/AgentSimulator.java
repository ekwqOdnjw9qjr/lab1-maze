package com.qwerty.mazeagentgame.simulation;



import com.qwerty.mazeagentgame.model.Gene;
import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.model.Position;
import com.qwerty.mazeagentgame.util.Action;
import com.qwerty.mazeagentgame.util.Orientation;
import lombok.Getter;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class AgentSimulator {
    private final List<Gene> genotype;
    private final Maze maze;
    private Position position;
    private Orientation orientation;
    private int state;
    private int steps;
    private boolean reachedExit;
    private final List<Position> trace;

    public AgentSimulator(List<Gene> genotype, Maze maze) {
        this.genotype = genotype;
        this.maze = maze;
        this.trace = new ArrayList<>();
        reset();
    }

    public void reset() {
        position = maze.getStart();
        orientation = Orientation.RIGHT;
        state = 0;
        steps = 0;
        reachedExit = false;
        trace.clear();
        trace.add(position);
    }

    public boolean step() {
        Position front = position.move(orientation);
        boolean blocked = maze.getWalls().contains(front) || maze.getDynamicObstacles().contains(front);
        Gene gene = genotype.get(state * 2 + (blocked ? 1 : 0));
        state = gene.getNextState();

        return Optional.of(Action.values()[gene.getAction()])
                .map(action -> {
                    switch (action) {
                        case FORWARD:
                            if (!blocked) position = front;
                            break;
                        case LEFT:
                            orientation = orientation.turnLeft();
                            break;
                        case RIGHT:
                            orientation = orientation.turnRight();
                            break;
                        case STOP:
                            return false;
                    }
                    steps++;
                    trace.add(position);
                    reachedExit = position.equals(maze.getExit());
                    return !reachedExit;
                })
                .orElse(false);
    }

    public double run(int maxSteps) {
        reset();
        while (steps < maxSteps && step()) {
            maze.moveDynamicObstacles();
        }
        return reachedExit ? 1000 + (maxSteps - steps) : Math.max(0, 1000 - manhattanDistance() * 10);
    }

    private int manhattanDistance() {
        return Math.abs(position.getX() - maze.getExit().getX()) + Math.abs(position.getY() - maze.getExit().getY());
    }
}