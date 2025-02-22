package com.qwerty.mazeagentgame.model;







import com.qwerty.mazeagentgame.util.Constants;
import lombok.Getter;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
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

    public void regenerate() {
        do {
            grid = new boolean[Constants.GRID_SIZE][Constants.GRID_SIZE];
            for (int y = 0; y < Constants.GRID_SIZE; y++) {
                for (int x = 0; x < Constants.GRID_SIZE; x++) {
                    grid[y][x] = rand.nextDouble() < 0.25; // Уменьшаем вероятность стен до 25% для проходимости
                }
            }
            setBorders();
            grid[start.getY()][start.getX()] = grid[exit.getY()][exit.getX()] = false;
        } while (!hasPath());
        walls = collectWalls();
        initDynamicObstacles();
    }

    private void setBorders() {
        IntStream.range(0, Constants.GRID_SIZE).forEach(x -> {
            grid[0][x] = grid[Constants.GRID_SIZE - 1][x] = true;
            grid[x][0] = grid[x][Constants.GRID_SIZE - 1] = true;
        });
    }

    private boolean hasPath() {
        Set<Position> visited = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(start);
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            Position curr = queue.poll();
            if (curr.equals(exit)) return true;
            Arrays.stream(dirs)
                    .map(dir -> new Position(curr.getX() + dir[0], curr.getY() + dir[1]))
                    .filter(this::isValid)
                    .filter(pos -> !grid[pos.getY()][pos.getX()])
                    .filter(pos -> !visited.contains(pos))
                    .forEach(pos -> {
                        visited.add(pos);
                        queue.add(pos);
                    });
        }
        return false;
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
        dynamicObstacles = IntStream.range(0, Constants.GRID_SIZE)
                .boxed()
                .flatMap(y -> IntStream.range(0, Constants.GRID_SIZE)
                        .mapToObj(x -> new Position(x, y)))
                .filter(pos -> !grid[pos.getY()][pos.getX()] && !pos.equals(start) && !pos.equals(exit))
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Collections.shuffle(list, rand);
                    return list.subList(0, Math.min(5, list.size()));
                }));
    }

    public void moveDynamicObstacles() {
        dynamicObstacles = dynamicObstacles.stream()
                .map(pos -> {
                    int[][] moves = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    return Arrays.stream(moves)
                            .map(m -> new Position(pos.getX() + m[0], pos.getY() + m[1]))
                            .filter(this::isValid)
                            .filter(p -> !grid[p.getY()][p.getX()])
                            .filter(p -> !dynamicObstacles.contains(p))
                            .filter(p -> !p.equals(start) && !p.equals(exit))
                            .findFirst()
                            .orElse(pos);
                })
                .collect(Collectors.toList());
    }

    private boolean isValid(Position pos) {
        return pos.getX() >= 0 && pos.getX() < Constants.GRID_SIZE && pos.getY() >= 0 && pos.getY() < Constants.GRID_SIZE;
    }
}