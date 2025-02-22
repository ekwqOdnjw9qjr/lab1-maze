package com.qwerty.mazeagentgame.util;




public final class Constants {
    public static final int GRID_SIZE = 32;
    public static final int CELL_SIZE = 20;
    public static final int MAX_STEPS = 200; // Фиксируем количество шагов
    public static final int NUM_STATES = 4;
    public static int GENOTYPE_LENGTH = NUM_STATES * 2;
    public static int POP_SIZE = 100; // Увеличиваем размер популяции
    public static int GENERATIONS = 150; // Увеличиваем количество поколений для лучшей оптимизации
    public static int MIGRATION_INTERVAL = 10;
    public static int TOURNAMENT_SIZE = 5; // Увеличиваем размер турнира
    public static double CROSSOVER_RATE = 0.85; // Увеличиваем вероятность кроссовера
    public static double MUTATION_RATE = 0.2; // Увеличиваем вероятность мутации для большей вариативности

    private Constants() {}
}