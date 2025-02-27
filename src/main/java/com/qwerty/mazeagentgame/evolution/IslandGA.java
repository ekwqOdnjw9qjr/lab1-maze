package com.qwerty.mazeagentgame.evolution;

import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.simulation.AgentSimulator;
import com.qwerty.mazeagentgame.util.Constants;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IslandGA implements GeneticAlgorithm {
    private final Maze maze;
    private final List<Island> islands;
    private final int generations;
    private final int migrationInterval;
    private final int tournamentSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final int numMigrants;
    private final int eliteSize;
    private final ExecutorService executor;

    public IslandGA(Maze maze, Random rand, int numIslands, int popSize, int generations, int migrationInterval,
                    int tournamentSize, double crossoverRate, double mutationRate, int numMigrants) {
        this.maze = maze;
        this.generations = generations;
        this.migrationInterval = migrationInterval;
        this.tournamentSize = tournamentSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.numMigrants = numMigrants;
        this.eliteSize = Math.max(2, popSize / 10);
        this.islands = IntStream.range(0, numIslands)
                .mapToObj(i -> new Island(popSize, maze, rand))
                .toList();
        this.executor = Executors.newFixedThreadPool(numIslands);
    }

    @Override
    public Individual run(Runnable updateCallback) {
        islands.forEach(Island::evaluate);

        for (int gen = 0; gen < generations; gen++) {
            try {
                List<Future<?>> futures = islands.stream()
                        .map(island -> executor.submit(() ->
                                island.nextGeneration(tournamentSize, crossoverRate, mutationRate, eliteSize)))
                        .collect(Collectors.toList());

                futures.forEach(future -> {
                    try {
                        future.get();
                    } catch (Exception exception) {
                        throw new RuntimeException("Execution error in parallel threads", exception);
                    }
                });
            } catch (Exception exception) {
                throw new RuntimeException("Execution error in parallel threads", exception);
            }

            if (gen % migrationInterval == 0 && gen > 0) {
                migrate();
            }

            if (updateCallback != null) updateCallback.run();

            Individual best = getGlobalBest();
            AgentSimulator simulator = new AgentSimulator(best.getGenotype(), maze);
            simulator.reset();

            while (simulator.getSteps() < Constants.MAX_STEPS && simulator.step()) {
                maze.moveDynamicObstacles();

                if (simulator.getPosition().equals(maze.getExit())) {
                    simulator.setReachedExit(!maze.getDynamicObstacles().contains(maze.getExit()));
                    if (simulator.isReachedExit()) {
                        executor.shutdownNow();
                        return best;
                    }
                }
            }
        }
        executor.shutdownNow();
        return getGlobalBest();
    }


    private void migrate() {
        List<List<Individual>> bestIndividuals = islands.stream()
                .map(island -> island.getBestIndividuals(numMigrants))
                .toList();

        IntStream.range(0, islands.size()).forEach(i -> {
            int sourceIsland = (i + islands.size() - 1) % islands.size();
            islands.get(i).replaceWorstIndividuals(
                    bestIndividuals.get(sourceIsland).stream().map(Individual::copy).toList()
            );
        });
    }

    private Individual getGlobalBest() {
        return islands.stream()
                .map(island -> island.getBestIndividuals(1).get(0))
                .max(Comparator.comparingDouble(Individual::getFitness))
                .orElseThrow(() -> new IllegalStateException("The population is empty it is impossible to choose the best agent"));
    }
}