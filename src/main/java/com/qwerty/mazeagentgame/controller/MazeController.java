package com.qwerty.mazeagentgame.controller;









import com.qwerty.mazeagentgame.service.MazeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class MazeController {
    private final MazeService mazeService;

    public MazeController(MazeService mazeService) {
        this.mazeService = mazeService;
    }

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
        mazeService.simulate();
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
                                @RequestParam double mutationRate, Model model) {
        mazeService.setSettings(maxSteps, numIslands, popSize, generations, migrationInterval,
                tournamentSize, crossoverRate, mutationRate);
        model.addAttribute("maze", mazeService.getMazeData());
        model.addAttribute("message", mazeService.getMessage());
        return "index";
    }
}