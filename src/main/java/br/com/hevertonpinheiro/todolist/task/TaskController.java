package br.com.hevertonpinheiro.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.hevertonpinheiro.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @SuppressWarnings("rawtypes")
	@PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID)idUser);
        
        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartAt()) || taskModel.getEndAt().isBefore(taskModel.getStartAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("A data de início deve ser maior que a data atual e menor que a data de término");
        }
        
        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }
    
    @GetMapping("/")
    public List<TaskModel> listTasks(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) idUser);
        
        return tasks;
        
    }
    

    // http://localhost:8080/tasks/65416476546-gfdsgagSFASD-6584684 --> Path variable
    @SuppressWarnings("rawtypes")
	@PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        
        var task = this.taskRepository.findById(id).orElse(null);
        
        if(task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Tarefa não existe");
        }
        
        var idUser = request.getAttribute("idUser");

        if(!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Usuário não tem permissão para alterar essa tarefa");
        }
        
        Utils.copyNonNullProperties(taskModel, task);
        
        var taskUpdated = this.taskRepository.save(task);

        return ResponseEntity.ok().body(taskUpdated);

    }
    
}
