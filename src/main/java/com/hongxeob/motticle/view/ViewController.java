package com.hongxeob.motticle.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

	@GetMapping("/kakao")
	public String login() {
		return "kakao";
	}

	@GetMapping("/joinForm")
	public String join() {
		return "member/joinForm";
	}

	@GetMapping("/home")
	public String home() {
		return "home";
	}

	@GetMapping("/addArticle")
	public String addArticle(@RequestParam(name = "type") String articleType, Model model) {
		model.addAttribute("articleType", articleType);
		return "article/addArticle";
	}

	@GetMapping("/addTag")
	public String addTag() {
		return "tag/addTag";
	}
}
