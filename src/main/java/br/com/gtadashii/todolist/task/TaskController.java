package br.com.gtadashii.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gtadashii.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity<?> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var userId = request.getAttribute("userId");
    taskModel.setUserId((UUID) userId);

    var currentDate = LocalDateTime.now();
    if(currentDate.isAfter(taskModel.getStarstAt()) || currentDate.isAfter(taskModel.getEndsAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio/término deve ser maior que a data atual");
    }

    if(taskModel.getStarstAt().isAfter(taskModel.getEndsAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser anterior a data de término");
    }

    var createdTask = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
  }

  @GetMapping("/")
  public ResponseEntity<?> listTasks(HttpServletRequest request) {
    var userId = request.getAttribute("userId");
    var tasks = this.taskRepository.findByUserId((UUID) userId);
    return ResponseEntity.status(HttpStatus.OK).body(tasks);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateTask(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    var task = this.taskRepository.findById(id).orElse(null);

    if (task == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarefa não encontrada");
    }

    var userId = request.getAttribute("userId");

    if(!task.getUserId().equals(userId)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não tem permissão para alterar esta tarefa");
    }

    Utils.copyNonNullProperties(taskModel, task);

    var updatedTask = this.taskRepository.save(task);

    return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
  }
}
