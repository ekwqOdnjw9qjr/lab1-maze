package com.qwerty.mazeagentgame.service;






import com.qwerty.mazeagentgame.evolution.GeneticAlgorithm;
import com.qwerty.mazeagentgame.evolution.Individual;
import com.qwerty.mazeagentgame.evolution.IslandGA;
import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.simulation.AgentSimulator;
import com.qwerty.mazeagentgame.util.Constants;
import lombok.Getter;

import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Getter
public class MazeService {
    private final Maze maze = new Maze();
    private Individual bestIndividual;
    private final Random rand = new Random();
    private String message = "Выберите действие для запуска эволюции или симуляции";

    // Настройки по умолчанию (обновлены согласно Constants)
    private int maxSteps = Constants.MAX_STEPS;
    private int numIslands = Constants.NUM_STATES;
    private int popSize = Constants.POP_SIZE;
    private int generations = Constants.GENERATIONS;
    private int migrationInterval = Constants.MIGRATION_INTERVAL;
    private int tournamentSize = Constants.TOURNAMENT_SIZE;
    private double crossoverRate = Constants.CROSSOVER_RATE;
    private double mutationRate = Constants.MUTATION_RATE;

    public void setSettings(int maxSteps, int numIslands, int popSize, int generations, int migrationInterval,
                            int tournamentSize, double crossoverRate, double mutationRate) {
        this.maxSteps = maxSteps;
        this.numIslands = numIslands;
        this.popSize = popSize;
        this.generations = generations;
        this.migrationInterval = migrationInterval;
        this.tournamentSize = tournamentSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        message = "Настройки применены";
    }

    public void evolve() {
        if (bestIndividual != null) {
            message = "Эволюция уже выполнена. Сбросьте настройки или регенерируйте карту.";
            return;
        }
        GeneticAlgorithm ga = new IslandGA(maze, rand, numIslands, popSize, generations, migrationInterval, tournamentSize, crossoverRate, mutationRate);
        bestIndividual = ga.run(() -> {});
        moveObstacles(); // Двигаем препятствия после эволюции
        message = "Новая эволюция завершена. Лучший фитнес: " + bestIndividual.getFitness();
    }

    public void newEvolution() {
        bestIndividual = null; // Сброс текущего лучшего агента
        evolve(); // Запускаем новую эволюцию
    }

    public void simulate() {
        if (bestIndividual == null) {
            message = "Сначала запустите эволюцию!";
            return;
        }

        // Двигаем препятствия один раз перед началом для минимальной случайности
        moveObstacles();

        AgentSimulator simulator = new AgentSimulator(bestIndividual.getGenotype(), maze);
        simulator.reset();
        while (simulator.getSteps() < maxSteps && simulator.step()) {
            moveObstacles(); // Двигаем препятствия на каждом шаге симуляции
        }
        message = simulator.isReachedExit() ?
                "Симуляция завершена. Выход достигнут за " + simulator.getSteps() + " шагов." :
                "Симуляция завершена. Выход не достигнут за " + simulator.getSteps() + " шагов.";
    }

    public void regenerate() {
        maze.regenerate(); // Полная регенерация карты
        moveObstacles(); // Двигаем препятствия после регенерации
        message = "Новая карта сгенерирована";
    }

    private void moveObstacles() {
        maze.moveDynamicObstacles();
    }

    public Map<String, Object> getMazeData() {
        Map<String, Object> mazeData = new HashMap<>();
        mazeData.put("walls", maze.getWalls());
        mazeData.put("obstacles", maze.getDynamicObstacles());
        mazeData.put("start", maze.getStart());
        mazeData.put("exit", maze.getExit());
        mazeData.put("settings", getSettingsMap());
        return mazeData;
    }

    public Map<String, Object> getMazeDataWithSimulation() {
        Map<String, Object> mazeData = getMazeData();
        if (bestIndividual != null) {
            moveObstacles(); // Двигаем препятствия один раз перед симуляцией
            AgentSimulator simulator = new AgentSimulator(bestIndividual.getGenotype(), maze);
            simulator.reset();
            while (simulator.getSteps() < maxSteps && simulator.step()) {
                moveObstacles(); // Двигаем препятствия на каждом шаге симуляции
            }
            mazeData.put("agentTrace", simulator.getTrace());
            mazeData.put("agent", Map.of(
                    "x", simulator.getPosition().getX(),
                    "y", simulator.getPosition().getY(),
                    "orientation", simulator.getOrientation().name()
            ));
        }
        return mazeData;
    }

    private Map<String, Object> getSettingsMap() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("maxSteps", maxSteps);
        settings.put("numIslands", numIslands);
        settings.put("popSize", popSize);
        settings.put("generations", generations);
        settings.put("migrationInterval", migrationInterval);
        settings.put("tournamentSize", tournamentSize);
        settings.put("crossoverRate", crossoverRate);
        settings.put("mutationRate", mutationRate);
        return settings;
    }

    public String getMessage() {
        return message;
    }
}