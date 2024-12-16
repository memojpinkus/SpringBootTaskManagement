package com.example.demo.health;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getTasks(){
        return taskRepository.findAll();
    }

    public void newTask(Task task) {
        Optional<Task> taskOptional = taskRepository.findTaskByTitle(task.getTitle());
        if(taskOptional.isPresent()){
            throw new IllegalStateException("Task with the same title already exists!");
        }
        taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        boolean exists = taskRepository.existsById(taskId);
        if(!exists){
            throw new IllegalStateException("Task with ID: "+taskId+" does not exist!");
        }
        taskRepository.deleteById(taskId);
    }

    @Transactional
    public void updateTask(Long taskId, String title, String description, String status) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalStateException("Task with ID: " + taskId + " does not exist."));
        if(title != null && !title.isEmpty() && !Objects.equals(task.getTitle(), title)) {
            Optional<Task> taskOptional = taskRepository.findTaskByTitle(title);
            if(taskOptional.isEmpty()) {
                task.setTitle(title);
            }
        }
        if(description != null && !description.isEmpty() && !Objects.equals(task.getDescription(), description)) {
            task.setDescription(description);
        }
        if(status != null && !status.isEmpty() && !Objects.equals(task.getStatus(), status)) {
            task.setStatus(status);
        }
    }
}
