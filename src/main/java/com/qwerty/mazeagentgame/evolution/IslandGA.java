package com.qwerty.mazeagentgame.evolution;






import com.qwerty.mazeagentgame.model.Maze;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IslandGA implements GeneticAlgorithm {
    private final Maze maze;
    private final List<Island> islands;
    private final int generations;
    private final Random rand;
    private final int numIslands;
    private final int popSize;
    private final int migrationInterval;
    private final int tournamentSize;
    private final double crossoverRate;
    private final double mutationRate;

    private static class Island {
        private List<Individual> population;
        private final Maze maze;
        private final Random rand;

        Island(int popSize, Maze maze, Random rand) {
            this.maze = maze;
            this.rand = rand;
            population = IntStream.range(0, popSize)
                    .mapToObj(i -> new Individual(rand))
                    .collect(Collectors.toList());
        }

        void evaluate() {
            population.forEach(ind -> ind.evaluate(maze));
        }

        void nextGeneration(int tournamentSize, double crossoverRate, double mutationRate) {
            population = IntStream.range(0, population.size())
                    .mapToObj(i -> Individual.tournamentSelection(population, rand, tournamentSize)
                            .crossover(Individual.tournamentSelection(population, rand, tournamentSize))[i % 2])
                    .peek(ind -> ind.mutate(mutationRate))
                    .collect(Collectors.toList());
            evaluate();
        }

        Individual getBest() {
            return population.stream()
                    .max(Comparator.comparingDouble(Individual::getFitness))
                    .orElseThrow();
        }
    }

    public IslandGA(Maze maze, Random rand, int numIslands, int popSize, int generations, int migrationInterval,
                    int tournamentSize, double crossoverRate, double mutationRate) {
        this.maze = maze;
        this.rand = rand;
        this.numIslands = numIslands;
        this.popSize = popSize;
        this.generations = generations;
        this.migrationInterval = migrationInterval;
        this.tournamentSize = tournamentSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        islands = IntStream.range(0, numIslands)
                .mapToObj(i -> new Island(popSize, maze, rand))
                .collect(Collectors.toList());
    }

    @Override
    public Individual run(Runnable updateCallback) {
        islands.forEach(Island::evaluate);
        IntStream.range(0, generations).forEach(gen -> {
            islands.forEach(island -> island.nextGeneration(tournamentSize, crossoverRate, mutationRate));
            if (gen % migrationInterval == 0) migrate();
            if (updateCallback != null) updateCallback.run();
        });
        return islands.stream()
                .map(Island::getBest)
                .max(Comparator.comparingDouble(Individual::getFitness))
                .orElseThrow();
    }

    private void migrate() {
        List<Individual> migrants = islands.stream().map(Island::getBest).map(Individual::copy).collect(Collectors.toList());
        IntStream.range(0, islands.size()).forEach(i -> {
            Island island = islands.get(i);
            Individual source = migrants.get((i + islands.size() - 1) % islands.size());
            Individual worst = island.population.stream()
                    .min(Comparator.comparingDouble(Individual::getFitness))
                    .orElseThrow();
            if (source.getFitness() > worst.getFitness()) {
                island.population = Stream.concat(
                                island.population.stream().filter(ind -> !ind.equals(worst)),
                                Stream.of(source))
                        .collect(Collectors.toList());
                island.evaluate();
            }
        });
    }
}