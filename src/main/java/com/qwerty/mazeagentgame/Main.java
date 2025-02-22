package com.qwerty.mazeagentgame;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.qwerty.mazeagentgame.controller", "com.qwerty.mazeagentgame.model",
        "com.qwerty.mazeagentgame.evolution", "com.qwerty.mazeagentgame.simulation", "com.qwerty.mazeagentgame.util"
,"com.qwerty.mazeagentgame.service"})
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}