package com.example.demo.health;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp(){
        taskService = new TaskService(taskRepository);
    }

    @Test
    void canGetTasks() {
        // when
        taskService.getTasks();
        //then
        verify(taskRepository).findAll();
    }

    @Test
    void canAddNewTask() {
        //Given a new task
        Task task = new Task("Title", "Description", "Pending");
        //And that task gets created
        taskService.newTask(task);
        //Then verify that the repository's 'save' method is called with a Task object
        //Note: To capture and examine the exact Task object passed to the repository, an ArgumentCaptor is used
        ArgumentCaptor<Task> taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        //Verify that the 'save' method of the taskRepository was invoked
        verify(taskRepository).save(taskArgumentCaptor.capture());
        //Retrieves task object
        Task capturedTask = taskArgumentCaptor.getValue();
        //Checks that the captured Task object is exactly the same as the Task object provided to the service
        assertThat(capturedTask).isEqualTo(task);
    }

    @Test
    void willThrowExceptionWhenTitleIsTaken() {
        //Given a new task
        Task task = new Task("Title", "Description", "Pending");
        //Simulates scenario where title is already taken
        Optional<Task> existingTask = Optional.of(task);
        given(taskRepository.findTaskByTitle(task.getTitle())).willReturn(existingTask);
        //When the service under test is given the above value then verify that an exception is thrown
        assertThatThrownBy(()->taskService.newTask(task))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Task with the same title already exists!");
        //Ensures 'save' method is never called
        verify(taskRepository, never()).save(any());
    }

    @Test
    void canDeleteTaskWithExistingID() {
        //Given a new task
        Task task = new Task("Title", "Description", "Pending");
        //And simulating the existence of a task with the provided ID
        given(taskRepository.existsById(task.getId())).willReturn(true);
        //When trying to delete task
        taskService.deleteTask(task.getId());
        //Then task gets deleted
        then(taskRepository).should().deleteById(task.getId());
    }

    @Test
    void willThrowExceptionWhenDeletingTaskThatDoesNotExist() {
        //Given a new task
        Task task = new Task("Title", "Description", "Pending");
        //And task does not exist
        given(taskRepository.existsById(task.getId())).willReturn(false);
        //When the task is tried to be deleted then throw an error message
        assertThatThrownBy(()->taskService.deleteTask(task.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Task with ID: " + task.getId() + " does not exist!");
    }

    @Test
    void canUpdateTaskIfAllIsValid() {
        //Given an old task and new updated attributes
        Task existingTask = new Task("Old Title", "Old Description", "Pending");
        Task updatedTask = new Task("New Title", "New Description", "Completed");
        //And repository returns that there's a valid existing task
        given(taskRepository.findById(updatedTask.getId())).willReturn(Optional.of(existingTask));
        //And the new title is not repeated
        given(taskRepository.findTaskByTitle("New Title")).willReturn(Optional.empty());
        //When updating the existing task
        taskService.updateTask(updatedTask.getId(), updatedTask.getTitle(), updatedTask.getDescription(), updatedTask.getStatus());
        //Then assert that the task was correctly updated and no more interactions are done after the update
        assertThat(existingTask.getTitle()).isEqualTo("New Title");
        assertThat(existingTask.getDescription()).isEqualTo("New Description");
        assertThat(existingTask.getStatus()).isEqualTo("Completed");
        then(taskRepository).shouldHaveNoMoreInteractions();
    }
}