package com.qwerty.mazeagentgame.evolution;



public interface GeneticAlgorithm {
    Individual run(Runnable updateCallback);
}