package todoapp.web.todo;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import todoapp.core.todos.application.TodoEditor;
import todoapp.core.todos.application.TodoFinder;
import todoapp.core.todos.domain.Todo;
import todoapp.security.UserSession;

//@RolesAllowed("ROLE_ADMIN")
@RolesAllowed(UserSession.ROLE_USER)
@RestController
@RequestMapping("/api/todos")
public class TodoRestController {
	
	private final Logger log = LoggerFactory.getLogger(TodoRestController.class);
	
	private TodoFinder finder;
	private TodoEditor editor;
	
	public TodoRestController(TodoFinder finder, TodoEditor editor) {
		this.finder = finder;
		this.editor = editor;
	}

	@GetMapping
//	@RequestMapping(method = RequestMethod.GET, value = "/api/todos")
	public List<Todo> todos() {
		return finder.getAll();
	}
	
	@PostMapping
	public void create(@RequestBody @Valid TodoWriteCommand command) {
		log.debug("command.title: {}", command.getTitle());
		editor.create(command.getTitle());
	}
	
	@PutMapping("/{id}")
	public void update(@PathVariable Long id, @RequestBody @Valid TodoWriteCommand command) {
		log.debug("command.title: {}, command.completed: {}", command.getTitle(), command.isCompleted());
		editor.update(id, command.getTitle(), command.isCompleted());
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		log.debug("command.id: {}", id);
		editor.delete(id);
	}
	
	public static class TodoWriteCommand {
		
		@Size(min = 4, max = 140)
		private String title;
		private boolean completed;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public boolean isCompleted() {
			return completed;
		}

		public void setCompleted(boolean completed) {
			this.completed = completed;
		}
	}
}
