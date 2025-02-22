package com.qwerty.mazeagentgame.evolution;





import com.qwerty.mazeagentgame.model.Gene;
import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.simulation.AgentSimulator;
import com.qwerty.mazeagentgame.util.Constants;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public class Individual {
    private List<Gene> genotype;
    private double fitness;
    private final Random rand;

    public Individual(Random rand) {
        this.rand = rand;
        genotype = IntStream.range(0, Constants.GENOTYPE_LENGTH)
                .mapToObj(i -> Gene.random(rand))
                .collect(Collectors.toList());
    }

    public Individual(List<Gene> genotype, Random rand) {
        this.genotype = new ArrayList<>(genotype);
        this.rand = rand;
    }

    public double evaluate(Maze maze) {
        AgentSimulator simulator = new AgentSimulator(genotype, new Maze()); // Новый лабиринт для оценки
        simulator.reset();
        while (simulator.getSteps() < Constants.MAX_STEPS && simulator.step()) {
            maze.moveDynamicObstacles(); // Двигаем препятствия для тестирования
        }
        // Улучшенная функция фитнеса: больше очков за достижение выхода, меньше штраф за шаги
        if (simulator.isReachedExit()) {
            fitness = 1000 + (Constants.MAX_STEPS - simulator.getSteps()) * 2; // Удваиваем бонус за успех
        } else {
            int distance = Math.abs(simulator.getPosition().getX() - maze.getExit().getX()) +
                    Math.abs(simulator.getPosition().getY() - maze.getExit().getY());
            fitness = Math.max(0, 1000 - distance * 5 - simulator.getSteps() * 0.1); // Уменьшаем штраф за шаги
        }
        return fitness;
    }

    public Individual copy() {
        return new Individual(genotype, rand);
    }

    public Individual[] crossover(Individual other) {
        if (rand.nextDouble() < Constants.CROSSOVER_RATE) {
            int point = rand.nextInt(1, Constants.GENOTYPE_LENGTH);
            return new Individual[]{
                    new Individual(
                            Stream.concat(genotype.stream().limit(point), other.genotype.stream().skip(point))
                                    .collect(Collectors.toList()), rand),
                    new Individual(
                            Stream.concat(other.genotype.stream().limit(point), genotype.stream().skip(point))
                                    .collect(Collectors.toList()), rand)
            };
        }
        return new Individual[]{copy(), other.copy()};
    }

    public void mutate(double mutationRate) {
        genotype = genotype.stream()
                .map(g -> rand.nextDouble() < mutationRate ?
                        (rand.nextBoolean() ? new Gene(rand.nextInt(4), g.getNextState()) : new Gene(g.getAction(), rand.nextInt(Constants.NUM_STATES))) : g)
                .collect(Collectors.toList());
    }

    public static Individual tournamentSelection(List<Individual> population, Random rand, int tournamentSize) {
        return IntStream.range(0, tournamentSize)
                .mapToObj(i -> population.get(rand.nextInt(population.size())))
                .max(Comparator.comparingDouble(Individual::getFitness))
                .orElseThrow();
    }
}