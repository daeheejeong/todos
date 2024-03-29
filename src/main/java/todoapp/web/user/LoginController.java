package todoapp.web.user;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import todoapp.core.user.application.UserJoinder;
import todoapp.core.user.application.UserPasswordVerifier;
import todoapp.core.user.domain.User;
import todoapp.core.user.domain.UserEntityNotFoundException;
import todoapp.core.user.domain.UserPasswordNotMatchedException;
import todoapp.security.UserSession;
import todoapp.security.UserSessionRepository;

@Controller
public class LoginController {
	
	private final Logger log = LoggerFactory.getLogger(LoginController.class);
	
	private UserPasswordVerifier verifier;
	private UserJoinder joinder;
	
	private UserSessionRepository sessionRepository;
	
	public LoginController(UserPasswordVerifier verifier, UserJoinder joinder, UserSessionRepository sessionRepository) {
		this.verifier = verifier;
		this.joinder = joinder;
		this.sessionRepository = sessionRepository;
	}

	@GetMapping("/login")
	public void loginForm() {
	}
	
	@PostMapping("/login")
	public String loginProcess(@Valid LoginCommand command, Model model) {
		log.info("username: {}, password: {}", command.getUsername(), command.getPassword());
		
		/*if (bindingResult.hasErrors()) {
			model.addAttribute("bindingResult", bindingResult);
			model.addAttribute("message", "사용자 입력 값이 올바르지 않습니다.");
			return "login";
		}*/
		
		// 사용자가 입력한 아이디와 비밀번호로 사용자를 검증
		// verifier.verify(command.getUsername(), command.getPassword());
		
		// 등록된 사용자가 없으면, 신규 사용자로 가입을 시켜줌
		// joinder.join(command.getUsername(), command.getPassword());
		
		// Request form, query string 데이터 바인딩, @RequestParam
		// request.getPrameter("")
		
		// Request Payload(body) 데이터 바인딩, @RequestParam, Command Object
		// request.getInputStream()
		
		User user;
		try {
			user = verifier.verify(command.getUsername(), command.getPassword());
		} catch (UserPasswordNotMatchedException error) {
			model.addAttribute("message", error.getMessage());
			return "login";
		} catch (UserEntityNotFoundException error) {
			user = joinder.join(command.getUsername(), command.getPassword());
		}
		log.info("current user: {}", user);
		
		// httpSession.setAttribute ~ 의 문제점, 로드밸런서가 앞에 있다면 어쩔때는 되고 안되고의 문제 발생
		// 이러한상황 해결을 위해서는 세션클러스터라는 기술이 필요 (유지시키려고한다면)
		// 수평확장성이 있다면 특정한기술에 의존하면 안됨
		// 보통 인메모리 디비(레디스 등)를 두고 사용 또는 유사하게 구현
		
		sessionRepository.set(new UserSession(user));
		
		return "redirect:/todos"; // 내부적으로 RedirectView로 바꿔버린다
	}
	
	@ExceptionHandler(BindException.class)
	public String handlerBindException(BindException error, Model model) {
		// 이러한 기능을 전역으로 발효하고 싶다면, @ControllerAdvice와 함께 사용하면 된다.
		model.addAttribute("bindingResult", error.getBindingResult());
		model.addAttribute("message", "사용자 입력 값이 올바르지 않습니다.");
		return "login";
	}
	
	@GetMapping("/logout")
	public View logout() {
		sessionRepository.clear();
		
		// return "redirect:/todos";
		return new RedirectView("/todos");
	}
	
	public static class LoginCommand {
		
		@Size(min = 4, max = 20)
		private String username;
		private String password;
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		
		
	}
}
