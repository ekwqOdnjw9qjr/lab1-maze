package com.qwerty.mazeagentgame.model;

import com.qwerty.mazeagentgame.util.Constants;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Component
public class Maze {

    private boolean[][] grid;
    private Set<Position> walls;
    private List<Position> dynamicObstacles;
    private final Position start = new Position(1, 1);
    private final Position exit = new Position(Constants.GRID_SIZE - 2, Constants.GRID_SIZE - 2);
    private final Random rand = new Random();

    public Maze() {
        regenerate();
    }

    private void setBorders() {
        IntStream.range(0, Constants.GRID_SIZE).forEach(i -> {
            grid[0][i] = grid[Constants.GRID_SIZE - 1][i] = true;
            grid[i][0] = grid[i][Constants.GRID_SIZE - 1] = true;
        });
    }

    public void regenerate() {
        grid = new boolean[Constants.GRID_SIZE][Constants.GRID_SIZE];
        setBorders();


        IntStream.range(1, Constants.GRID_SIZE - 1)
                .forEach(y -> IntStream.range(1, Constants.GRID_SIZE - 1)
                        .forEach(x -> grid[y][x] = rand.nextDouble() < 0.10));


        grid[start.getY()][start.getX()] = false;
        grid[exit.getY()][exit.getX()] = false;


        ensureSimplePath();

        walls = collectWalls();
        initDynamicObstacles();
    }

    private void ensureSimplePath() {
        int x = start.getX(), y = start.getY();
        while (x < exit.getX() || y < exit.getY()) {
            grid[y][x] = false;
            if (x < exit.getX() && rand.nextDouble() < 0.7) {
                x++;
            } else if (y < exit.getY()) {
                y++;
            }
        }
        grid[exit.getY()][exit.getX()] = false;
    }

    private Set<Position> collectWalls() {
        return IntStream.range(0, Constants.GRID_SIZE)
                .boxed()
                .flatMap(y -> IntStream.range(0, Constants.GRID_SIZE)
                        .filter(x -> grid[y][x])
                        .mapToObj(x -> new Position(x, y)))
                .collect(Collectors.toSet());
    }

    private void initDynamicObstacles() {
        dynamicObstacles = IntStream.range(0, 4)
                .mapToObj(i -> {
                    Position pos;
                    do {
                        pos = new Position(rand.nextInt(Constants.GRID_SIZE - 2) + 1,
                                rand.nextInt(Constants.GRID_SIZE - 2) + 1);
                    } while (pos.equals(start) || pos.equals(exit) || grid[pos.getY()][pos.getX()]);
                    return pos;
                })
                .collect(Collectors.toList());
    }

    public void moveDynamicObstacles() {
        dynamicObstacles = dynamicObstacles.stream()
                .map(pos -> {
                    int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    List<Position> possibleMoves = Arrays.stream(directions)
                            .map(dir -> new Position(pos.getX() + dir[0], pos.getY() + dir[1]))
                            .filter(this::isValid)
                            .filter(p -> !grid[p.getY()][p.getX()])
                            .filter(p -> !p.equals(start) && !p.equals(exit))
                            .filter(p -> dynamicObstacles.stream().noneMatch(p::equals))
                            .collect(Collectors.toList());

                    return possibleMoves.isEmpty() || rand.nextDouble() >= 0.8
                            ? pos
                            : possibleMoves.get(rand.nextInt(possibleMoves.size()));
                })
                .collect(Collectors.toList());
    }

    private boolean isValid(Position pos) {
        return pos.getX() >= 0 && pos.getX() < Constants.GRID_SIZE &&
                pos.getY() >= 0 && pos.getY() < Constants.GRID_SIZE;
    }
}