package com.qwerty.mazeagentgame.evolution;

import com.qwerty.mazeagentgame.model.Gene;
import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.model.Position;
import com.qwerty.mazeagentgame.simulation.AgentSimulator;
import com.qwerty.mazeagentgame.util.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
@Slf4j
public class Individual {
    private List<Gene> genotype;
    private double fitness;
    private final Random rand;

    public Individual(Random rand) {
        this.rand = rand;
        this.genotype = initializeDirectedGenotype();
    }

    public Individual(List<Gene> genotype, Random rand) {
        this.genotype = new ArrayList<>(genotype);
        this.rand = rand;
    }


    private List<Gene> initializeDirectedGenotype() {
        Position start = new Position(1, 1);
        Position exit = new Position(Constants.GRID_SIZE - 2, Constants.GRID_SIZE - 2);
        return IntStream.range(0, Constants.GENOTYPE_LENGTH)
                .mapToObj(i -> {
                    int action = rand.nextDouble() < 0.7 ?
                            (start.getX() < exit.getX() ? 0 : // RIGHT
                                    start.getX() > exit.getX() ? 2 : // LEFT
                                            start.getY() < exit.getY() ? 1 : 3) // DOWN : UP
                            : rand.nextInt(4);
                    return new Gene(action, rand.nextInt(Constants.NUMBER_STATES));
                })
                .toList();
    }

    public void setGenotype(List<Gene> genotype) {
        this.genotype = new ArrayList<>(genotype);
    }

    public double evaluate(Maze maze) {
        AgentSimulator simulator = new AgentSimulator(genotype, maze);
        simulator.reset();

        while (simulator.getSteps() < Constants.MAX_STEPS && simulator.step()) {
            maze.moveDynamicObstacles();
        }

        int steps = simulator.getSteps();
        Position pos = simulator.getPosition();
        Position exit = maze.getExit();
        int distance = Math.abs(pos.getX() - exit.getX()) + Math.abs(pos.getY() - exit.getY());
        int visitedCount = simulator.getSteps();

        if (simulator.isReachedExit()) {
            fitness = 10000 + (Constants.MAX_STEPS - steps) * 100;
        } else if (simulator.isCollided()) {
            fitness = 50 + visitedCount * 10;
        } else {
            fitness = 5000 - distance * 100 + visitedCount * 50;
            if (distance <= 5) fitness += 3000;
            if (distance <= 2) fitness += 2000;
        }

        return fitness;
    }

    public Individual copy() {
        return new Individual(genotype, rand);
    }

    public Individual[] crossover(Individual other) {
        if (rand.nextDouble() < Constants.CROSSOVER_RATE) {
            int point1 = rand.nextInt(1, Constants.GENOTYPE_LENGTH / 2);
            int point2 = rand.nextInt(point1, Constants.GENOTYPE_LENGTH);

            return new Individual[]{
                    new Individual(
                            Stream.concat(
                                            Stream.concat(genotype.stream().limit(point1),
                                                    other.genotype.stream().skip(point1).limit(point2 - point1)),
                                            genotype.stream().skip(point2))
                                    .toList(), rand),
                    new Individual(
                            Stream.concat(
                                            Stream.concat(other.genotype.stream().limit(point1),
                                                    genotype.stream().skip(point1).limit(point2 - point1)),
                                            other.genotype.stream().skip(point2))
                                    .toList(), rand)
            };
        }
        return new Individual[]{copy(), other.copy()};
    }

    public void mutate(double mutationRate) {
        double adjustedMutationRate = Math.min(Math.max(mutationRate * (1.5 - fitness / 15000), 0.05), 0.3);

        genotype = genotype.stream()
                .map(g -> {
                    if (rand.nextDouble() < adjustedMutationRate) {
                        int action;
                        if (rand.nextDouble() < 0.8) {
                            action = (g.getAction() == 0 || g.getAction() == 2)
                                    ? (rand.nextBoolean() ? 0 : 2)
                                    : (rand.nextBoolean() ? 1 : 3);
                        } else {
                            action = rand.nextInt(4);
                        }
                        return new Gene(action, rand.nextInt(Constants.NUMBER_STATES));
                    }
                    return g;
                })
                .toList();
    }


    public static Individual tournamentSelection(List<Individual> population, Random rand, int tournamentSize) {

        List<Individual> candidates = IntStream.range(0, tournamentSize)
                .mapToObj(i -> population.get(rand.nextInt(population.size())))
                .sorted(Comparator.comparingDouble(Individual::getFitness).reversed())
                .toList();

        return (rand.nextDouble() < 0.98) ? candidates.get(0) : candidates.get(1);
    }
}