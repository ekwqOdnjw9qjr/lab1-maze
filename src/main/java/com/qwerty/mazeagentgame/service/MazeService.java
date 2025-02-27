package com.qwerty.mazeagentgame.service;

import com.qwerty.mazeagentgame.evolution.*;
import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.simulation.AgentSimulator;
import com.qwerty.mazeagentgame.util.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Getter
@Slf4j
public class MazeService {


    private final Maze maze = new Maze();
    private Individual bestIndividual;
    private final Random rand = new Random();
    private String message = "Выберите действие для запуска эволюции или симуляции";

    private int maxSteps = Constants.MAX_STEPS;
    private int numIslands = Constants.NUMBER_ISLANDS;
    private int popSize = Constants.POPULATION_SIZE;
    private int generations = Constants.GENERATIONS;
    private int migrationInterval = Constants.MIGRATION_INTERVAL;
    private int tournamentSize = Constants.TOURNAMENT_SIZE;
    private double crossoverRate = Constants.CROSSOVER_RATE;
    private double mutationRate = Constants.MUTATION_RATE;
    private int maxIterations = Constants.MAX_ITERATIONS;
    private double initialTemperature = Constants.INITIAL_TEMPERATURE;
    private double coolingRate = Constants.COOLING_RATE;
    private String gaType = "IslandGA";

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public MazeService() {
        startDynamicObstacleMovement();
    }

    private void startDynamicObstacleMovement() {
        executorService.scheduleAtFixedRate(() -> {
            moveObstacles();
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void setSettings(int maxSteps, int numIslands, int popSize, int generations, int migrationInterval,
                            int tournamentSize, double crossoverRate, double mutationRate, int maxIterations,
                            double initialTemperature, double coolingRate, String gaType) {
        this.maxSteps = maxSteps;
        this.numIslands = numIslands;
        this.popSize = popSize;
        this.generations = generations;
        this.migrationInterval = migrationInterval;
        this.tournamentSize = tournamentSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.maxIterations = maxIterations;
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.gaType = gaType != null ? gaType : "IslandGA";
        message = "Настройки применены";
    }

    public void evolve() {
        if (bestIndividual != null) {
            message = "Эволюция уже выполнена. Сбросьте настройки или регенерируйте карту.";
            return;
        }
        try {
            GeneticAlgorithm geneticAlgorithm;
            if ("SimulatedAnnealing".equals(gaType)) {
                geneticAlgorithm = new SimulatedAnnealing(maze, rand, maxIterations, initialTemperature, coolingRate);
            } else {
                geneticAlgorithm = new IslandGA(maze, rand, numIslands, popSize, generations, migrationInterval,
                        tournamentSize, crossoverRate, mutationRate, 3);
            }
            bestIndividual = geneticAlgorithm.run(null);
            moveObstacles();
            message = "Новая эволюция завершена. Лучший фитнес: " + (bestIndividual != null ? bestIndividual.getFitness() : 0);
        } catch (Exception e) {
            message = "Ошибка при эволюции: " + e.getMessage();
            e.printStackTrace();
        }
    }

    public void newEvolution() {
        bestIndividual = null;
        evolve();
    }

    public List<Map<String, Object>> simulateSteps() {
        if (bestIndividual == null) {
            message = "Сначала запустите эволюцию!";
            return Collections.emptyList();
        }

        AgentSimulator simulator = new AgentSimulator(bestIndividual.getGenotype(), maze);
        List<Map<String, Object>> steps = new ArrayList<>();
        simulator.reset();

        while (steps.size() < Constants.MAX_STEPS && simulator.step()) {
            maze.moveDynamicObstacles();
            steps.add(simulator.getStepState());
        }

        if (simulator.isReachedExit()) {
            message = "Агент добрался до выхода за " + steps.size() + " шагов.";
        } else if (simulator.isCollided()) {
            message = "Проигрыш: агент столкнулся с препятствием.";
        } else {
            message = "Проигрыш: агент не достиг выхода.";
        }

        return steps;
    }


    public void regenerate() {
        maze.regenerate();
        moveObstacles();
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
            moveObstacles();
            AgentSimulator simulator = new AgentSimulator(bestIndividual.getGenotype(), maze);
            simulator.reset();
            while (simulator.getSteps() < maxSteps && simulator.step()) {
                moveObstacles();
            }
            mazeData.put("agentTrace", simulator.getTrace());
            mazeData.put("agent", Map.of(
                    "x", simulator.getPosition().getX(),
                    "y", simulator.getPosition().getY(),
                    "orientation", simulator.getOrientation().name(),
                    "reachedExit", simulator.isReachedExit(),
                    "collided", simulator.isCollided()
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
        settings.put("gaType", gaType);
        settings.put("maxIterations", maxIterations);
        settings.put("initialTemperature",initialTemperature);
        settings.put("coolingRate", coolingRate);
        return settings;
    }


    public Map<String, Object> getStepByStepSimulation() {
        List<Map<String, Object>> steps = simulateSteps();
        Map<String, Object> response = new HashMap<>();
        response.put("steps", steps);
        response.put("message", message);
        return response;
    }

}