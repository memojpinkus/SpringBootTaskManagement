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
        //given
        Task task = new Task("Title", "Description", "Pending");
        //when the service under test is give the above value
        taskService.newTask(task);
        //then verify that repository captures the same value invoked under test
        ArgumentCaptor<Task> taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskArgumentCaptor.capture());

        Task capturedTask = taskArgumentCaptor.getValue();

        assertThat(capturedTask).isEqualTo(task);
    }

    @Test
    void willThrowExceptionWhenTitleIsTaken() {
        //given a new task
        Task task = new Task("Title", "Description", "Pending");
        Optional<Task> existingTask = Optional.of(task);
        given(taskRepository.findTaskByTitle(task.getTitle())).willReturn(existingTask);
        //when the service under test is given the above value then verify that an exception is thrown
        assertThatThrownBy(()->taskService.newTask(task))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Task with the same title already exists!");
        verify(taskRepository, never()).save(any());
    }

    @Test
    void canDeleteTaskWithExistingID() {
        //given
        Task task = new Task("Title", "Description", "Pending");
        given(taskRepository.existsById(task.getId())).willReturn(true);
        // when
        taskService.deleteTask(task.getId());
        // then
        then(taskRepository).should().deleteById(task.getId());
    }

    @Test
    void willThrowExceptionWhenDeletingTaskThatDoesNotExist() {
        // given
        Task task = new Task("Title", "Description", "Pending");
        given(taskRepository.existsById(task.getId())).willReturn(false);
        // when / then
        assertThatThrownBy(()->taskService.deleteTask(task.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Task with ID: " + task.getId() + " does not exist!");
    }

    @Test
    void canUpdateTaskIfAllIsValid() {
        // given an old task and new updated attributes
        Task existingTask = new Task("Old Title", "Old Description", "Pending");
        Task updatedTask = new Task("New Title", "New Description", "Completed");
        given(taskRepository.findById(updatedTask.getId())).willReturn(Optional.of(existingTask));
        given(taskRepository.findTaskByTitle("New Title")).willReturn(Optional.empty());
        // when
        taskService.updateTask(updatedTask.getId(), updatedTask.getTitle(), updatedTask.getDescription(), updatedTask.getStatus());
        // then
        assertThat(existingTask.getTitle()).isEqualTo("New Title");
        assertThat(existingTask.getDescription()).isEqualTo("New Description");
        assertThat(existingTask.getStatus()).isEqualTo("Completed");
        then(taskRepository).shouldHaveNoMoreInteractions();
    }
}