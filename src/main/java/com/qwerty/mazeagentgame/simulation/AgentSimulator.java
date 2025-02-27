package com.qwerty.mazeagentgame.simulation;

import com.qwerty.mazeagentgame.model.Gene;
import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.model.Position;
import com.qwerty.mazeagentgame.util.Action;
import com.qwerty.mazeagentgame.util.Constants;
import com.qwerty.mazeagentgame.util.Orientation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Getter
@Setter
@Slf4j
public class AgentSimulator {

    private final List<Gene> genotype;
    private final Maze maze;
    private Position position;
    private Orientation orientation;
    private int state;
    private int steps;
    private boolean reachedExit;
    private boolean collided;
    private final List<Position> trace;
    private final Set<Position> visitedCells;

    public AgentSimulator(List<Gene> genotype, Maze maze) {
        this.genotype = genotype;
        this.maze = maze;
        this.trace = new ArrayList<>();
        this.visitedCells = new HashSet<>();
        reset();
    }

    public void reset() {
        position = maze.getStart();
        orientation = Orientation.RIGHT;
        state = 0;
        steps = 0;
        reachedExit = false;
        collided = false;
        trace.clear();
        visitedCells.clear();
        visitedCells.add(position);
    }

    public boolean step() {


        if (maze.getDynamicObstacles().contains(position)) {
            collided = true;

            return false;
        }

        if (collided) {
            return false;
        }

        Position front = position.move(orientation);
        boolean blocked = maze.getWalls().contains(front) || maze.getDynamicObstacles().contains(front);

        state = Math.max(0, state);
        if (state < 0 || state >= Constants.NUMBER_STATES) {
            state = 0;
        }
        Gene gene = genotype.get(state * 2 + (blocked ? 1 : 0));
        state = gene.getNextState();

        return Optional.of(Action.values()[gene.getAction()])
                .map(action -> {
                    boolean continueSimulation = true;
                    switch (action) {
                        case FORWARD:
                            if (!blocked) {
                                position = front;
                                visitedCells.add(position);
                            } else if (maze.getDynamicObstacles().contains(front)) {
                                collided = true;
                                continueSimulation = false;
                            }
                            break;
                        case LEFT:
                            orientation = orientation.turnLeft();
                            break;
                        case RIGHT:
                            orientation = orientation.turnRight();
                            break;
                        case STOP:
                            continueSimulation = false;
                            break;
                    }
                    steps++;
                    trace.add(position);

                    reachedExit = position.equals(maze.getExit()) && !maze.getDynamicObstacles().contains(maze.getExit());
                    if (reachedExit) {
                    } else if (position.equals(maze.getExit()) && maze.getDynamicObstacles().contains(maze.getExit())) {
                    }
                    return continueSimulation && !reachedExit && !collided && steps < Constants.MAX_STEPS;
                })
                .orElse(false);
    }

    public Map<String, Object> getStepState() {

        Map<String, Object> state = new HashMap<>();
        state.put("position", position != null ? Map.of("x", position.getX(), "y", position.getY()) : null);
        state.put("orientation", orientation != null ? orientation.name() : null);
        state.put("steps", steps);
        state.put("reachedExit", reachedExit);
        state.put("collided", collided);
        state.put("dynamicObstacles", maze.getDynamicObstacles());
        return state;
    }

    public boolean isReachedExit() {
        return reachedExit;
    }
}