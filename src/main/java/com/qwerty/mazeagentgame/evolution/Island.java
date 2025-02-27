package com.qwerty.mazeagentgame.evolution;

import com.qwerty.mazeagentgame.model.Gene;
import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.model.Position;
import com.qwerty.mazeagentgame.simulation.AgentSimulator;
import com.qwerty.mazeagentgame.util.Constants;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Island {
    private List<Individual> population;
    private final Maze maze;
    private final Random rand;

    public Island(int popSize, Maze maze, Random rand) {
        this.maze = maze;
        this.rand = rand;
        this.population = IntStream.range(0, popSize)
                .mapToObj(i -> new Individual(rand))
                .toList();
    }

    public void evaluate() {
        population.forEach(ind -> ind.evaluate(maze));
    }

    public void nextGeneration(int tournamentSize, double crossoverRate, double mutationRate, int eliteSize) {
        List<Individual> elite = getBestIndividuals(eliteSize);
        List<Individual> newPopulation = new ArrayList<>(elite);
        newPopulation.addAll(
                IntStream.range(0, population.size() - eliteSize)
                        .mapToObj(i -> {
                            Individual parent1 = Individual.tournamentSelection(population, rand, tournamentSize);
                            Individual parent2 = Individual.tournamentSelection(population, rand, tournamentSize);
                            Individual child = (rand.nextDouble() < crossoverRate) ?
                                    parent1.crossover(parent2)[rand.nextInt(2)] :
                                    parent1.copy();
                            child.mutate(mutationRate);
                            optimizeChild(child);
                            return child;
                        })
                        .toList()
        );
        population = newPopulation;
        evaluate();
    }

    public List<Individual> getBestIndividuals(int num) {
        return population.stream()
                .sorted(Comparator.comparingDouble(Individual::getFitness).reversed())
                .limit(num)
                .toList();
    }

    public void replaceWorstIndividuals(List<Individual> migrants) {
        population = Stream.concat(
                population.stream().sorted(Comparator.comparingDouble(Individual::getFitness)).skip(migrants.size()),
                migrants.stream()).toList();
        evaluate();
    }


    private void optimizeChild(Individual child) {
        AgentSimulator simulator = new AgentSimulator(child.getGenotype(), maze);
        simulator.reset();

        int steps = 0;

        while (steps < Constants.MAX_STEPS && simulator.step()) {
            steps++;

            maze.moveDynamicObstacles();
        }
        if (!simulator.isReachedExit() && steps > 0) {
            Position pos = simulator.getPosition();
            Position exit = maze.getExit();
            List<Gene> genotype = new ArrayList<>(child.getGenotype());
            int lastGeneIndex = Math.min(steps * 2, genotype.size() - 1);
            int distanceX = exit.getX() - pos.getX();
            int distanceY = exit.getY() - pos.getY();

            int action = (Math.abs(distanceX) > Math.abs(distanceY)) ?
                    (distanceX > 0 ? 0 : 2) : // RIGHT или LEFT
                    (distanceY > 0 ? 1 : 3);  // DOWN или UP
            genotype.set(lastGeneIndex, new Gene(action, genotype.get(lastGeneIndex).getNextState()));
            child.setGenotype(genotype);
        }
    }
}