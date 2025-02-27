package com.qwerty.mazeagentgame.controller;

import com.qwerty.mazeagentgame.model.Maze;
import com.qwerty.mazeagentgame.model.Position;
import com.qwerty.mazeagentgame.service.MazeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MazeController {

    private final MazeService mazeService;
    private final Maze maze;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("maze", mazeService.getMazeData());
        model.addAttribute("message", mazeService.getMessage());
        return "index";
    }

    @PostMapping("/evolve")
    public String evolve(Model model) {
        mazeService.evolve();
        model.addAttribute("maze", mazeService.getMazeData());
        model.addAttribute("message", mazeService.getMessage());
        return "index";
    }

    @PostMapping("/new-evolution")
    public String newEvolution(Model model) {
        mazeService.newEvolution();
        model.addAttribute("maze", mazeService.getMazeData());
        model.addAttribute("message", mazeService.getMessage());
        return "index";
    }

    @PostMapping("/simulate")
    public String simulate(Model model) {
        mazeService.simulateSteps();
        model.addAttribute("maze", mazeService.getMazeDataWithSimulation());
        model.addAttribute("message", mazeService.getMessage());
        return "index";
    }

    @PostMapping("/regenerate")
    public String regenerate(Model model) {
        mazeService.regenerate();
        model.addAttribute("maze", mazeService.getMazeData());
        model.addAttribute("message", mazeService.getMessage());
        return "index";
    }

    @PostMapping("/new-map")
    public String newMap(Model model) {
        mazeService.regenerate();
        model.addAttribute("maze", mazeService.getMazeData());
        model.addAttribute("message", mazeService.getMessage());
        return "index";
    }

    @PostMapping("/settings")
    public String applySettings(@RequestParam int maxSteps, @RequestParam int numIslands, @RequestParam int popSize,
                                @RequestParam int generations, @RequestParam int migrationInterval,
                                @RequestParam int tournamentSize, @RequestParam double crossoverRate,
                                @RequestParam double mutationRate, @RequestParam int maxIterations,
                                @RequestParam double initialTemperature, @RequestParam double coolingRate,
                                @RequestParam String gaType, Model model) {
        mazeService.setSettings(maxSteps, numIslands, popSize, generations, migrationInterval,
                tournamentSize, crossoverRate, mutationRate, maxIterations, initialTemperature, coolingRate, gaType);
        model.addAttribute("maze", mazeService.getMazeData());
        model.addAttribute("message", mazeService.getMessage());
        return "index";
    }

    @GetMapping("/api/obstacles")
    public Map<String, List<Position>> getObstacles() {
        return Map.of("obstacles", maze.getDynamicObstacles());
    }

    @GetMapping("/api/steps")
    @ResponseBody
    public Map<String, Object> getStepByStepSimulation() {
        return mazeService.getStepByStepSimulation();
    }

}