package com.example.demo.health;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/health")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getTasks() {
        return taskService.getTasks();
    }

    @PostMapping
    public void newTask(@Valid @RequestBody Task task) {
        taskService.newTask(task);
    }

    @DeleteMapping(path = "{taskId}")
    public void deleteTask(@PathVariable("taskId")Long id){
        taskService.deleteTask(id);
    }

    @PutMapping(path="{taskId}")
    public void updateTask(
            @PathVariable("taskId") Long taskId,
            @Valid @RequestParam(required = false) String title,
            @Valid @RequestParam(required = false) String description,
            @Valid @RequestParam(required = false) String status){
        taskService.updateTask(taskId, title, description, status);
    }
}
