package com.qwerty.mazeagentgame.util;




public final class Constants {
    public static final int GRID_SIZE = 32;
    public static final int MAX_STEPS = 200;
    public static final int NUMBER_STATES = 8;
    public static int GENOTYPE_LENGTH = NUMBER_STATES * 2;
    public static int POPULATION_SIZE = 25;
    public static int GENERATIONS = 55;
    public static int MIGRATION_INTERVAL = 10;
    public static int TOURNAMENT_SIZE = 5;
    public static double CROSSOVER_RATE = 0.8;
    public static double MUTATION_RATE = 0.2;

    private Constants() {}
}