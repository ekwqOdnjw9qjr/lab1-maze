package com.qwerty.mazeagentgame.util;




public final class Constants {
    public static final int GRID_SIZE = 32;
    public static final int MAX_STEPS = 200;
    public static final int NUMBER_STATES = 4;
    public static final int NUMBER_ISLANDS = 8;
    public static int GENOTYPE_LENGTH = 16;
    public static int POPULATION_SIZE = 25;
    public static int GENERATIONS = 55;
    public static int MIGRATION_INTERVAL = 10;
    public static int TOURNAMENT_SIZE = 5;
    public static double CROSSOVER_RATE = 0.8;
    public static double MUTATION_RATE = 0.2;
    public static int MAX_ITERATIONS = 15000;
    public static double INITIAL_TEMPERATURE = 4000.0;
    public static double COOLING_RATE = 0.99;

    private Constants() {}
}