package com.hongxeob.view;

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

	@GetMapping("/join")
	public String join() {
		return "member/joinForm";
	}

	@GetMapping("/add")
	public String addArticle(@RequestParam(name = "type") String articleType, Model model) {
		model.addAttribute("articleType", articleType);
		return "article/addArticle";
	}

	@GetMapping("/add-tag")
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

	@GetMapping("/article")
	public String otherArticleDetails(@RequestParam Long id) {
		return "article/otherArticleDetails";
	}

	@GetMapping("/search")
	public String search() {
		return "article/search";
	}

	@GetMapping("/explore/search")
	public String searchExplore() {
		return "article/explore-search";
	}

	@GetMapping("/article/update/{id}")
	public String updateArticle(@PathVariable Long id) {
		return "article/updateArticle";
	}


	@GetMapping("/my-page")
	public String myPage() {
		return "member/myPage";
	}

	@GetMapping("/profile")
	public String profile() {
		return "member/profile";
	}

	@GetMapping("/profile/update")
	public String updateProfile() {
		return "member/updateNickname";
	}

	@GetMapping("/explore")
	public String explore() {
		return "explore";
	}

	@GetMapping("/scrap")
	public String scrap() {
		return "article/scrap-list";
	}

	@GetMapping("/welcome")
	public String welcome() {
		return "welcome";
	}

	@GetMapping("/welcome/search")
	public String welcomeSearch() {
		return "article/welcome-search";
	}
}
