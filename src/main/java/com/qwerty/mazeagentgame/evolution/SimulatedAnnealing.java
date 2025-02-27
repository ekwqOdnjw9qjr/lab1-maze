package com.qwerty.mazeagentgame.evolution;

import com.qwerty.mazeagentgame.model.Gene;
import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.simulation.AgentSimulator;
import com.qwerty.mazeagentgame.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Getter
@AllArgsConstructor
@Slf4j
public class SimulatedAnnealing implements GeneticAlgorithm {

    private final Maze maze;
    private final Random rand;
    private final int maxIterations;
    private final double initialTemperature;
    private final double coolingRate;

    @Override
    public Individual run(Runnable updateCallback) {
        Individual current = new Individual(rand);
        current.evaluate(maze);
        double temperature = initialTemperature;

        for (int iteration = 0; iteration < maxIterations && temperature > 0.001; iteration++) {
            Individual neighbor = mutate(current);
            neighbor.evaluate(maze);

            double delta = neighbor.getFitness() - current.getFitness();

            if (delta > 0 || (delta > -1000 && rand.nextDouble() < Math.exp(delta / temperature))) {
                current = neighbor;
            }

            AgentSimulator simulator = new AgentSimulator(current.getGenotype(), maze);
            simulator.reset();
            while (simulator.getSteps() < Constants.MAX_STEPS && simulator.step()) {
                maze.moveDynamicObstacles();
            }

            if (simulator.isReachedExit() && current.getFitness() >= 18000) {
                return current;
            }

            temperature *= coolingRate;
            if (updateCallback != null) {
                updateCallback.run();
            }
        }

        current.evaluate(maze);
        return current;
    }

    private Individual mutate(Individual individual) {
        List<Gene> newGenotype = new ArrayList<>(individual.getGenotype());
        int numMutations = Math.min(5, newGenotype.size() / 3);

        List<Integer> mutationIndices = IntStream.range(0, numMutations)
                .map(i -> rand.nextInt(newGenotype.size()))
                .boxed()
                .toList();

        mutationIndices.forEach(index -> {
            int action = (int) (newGenotype.get(index).getAction() + rand.nextGaussian() * 3.0);
            action = Math.max(0, Math.min(3, action));
            int nextState = (int) (newGenotype.get(index).getNextState() + rand.nextGaussian() * 3.0);
            nextState = Math.max(0, Math.min(Constants.NUMBER_STATES - 1, nextState));
            newGenotype.set(index, new Gene(action, nextState));
        });

        return new Individual(newGenotype, rand);
    }
}
