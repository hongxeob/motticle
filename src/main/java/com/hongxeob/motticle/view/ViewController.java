package com.hongxeob.motticle.view;

import javax.swing.plaf.PanelUI;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/kakao")
	public String login() {
		return "kakao";
	}

	@GetMapping("/joinForm")
	public String join() {
		return "member/joinForm";
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

	@GetMapping("/tagging")
	public String tagging(@RequestParam(name = "articleId") Long articleId) {
		return "tag/extraAddTag";
	}


	@GetMapping("/article/{id}")
	public String articleDetails(@PathVariable Long id) {
		return "article/articleDetails";
	}

	@GetMapping("/search")
	public String search() {
		return "article/search";
	}

	@GetMapping("/article/update/{id}")
	public String updateArticle(@PathVariable Long id) {
		return "article/updatePage";
	}

}
