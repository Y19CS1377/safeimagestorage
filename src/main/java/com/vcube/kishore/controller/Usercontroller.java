package com.vcube.kishore.controller;



import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vcube.kishore.entity.Files;
import com.vcube.kishore.entity.User;
import com.vcube.kishore.repositary.Userepo;
import com.vcube.kishore.service.FilesService;
import com.vcube.kishore.service.Userservice;

import jakarta.servlet.http.HttpSession;

@Controller
public class Usercontroller {

	@Autowired
	private Userservice service;

	@Autowired
	private Userepo repo;
//	@Autowired
//    private HttpSession session;
	
	@Autowired
	private FilesService fs;

	@GetMapping("/")
	public String getLoginPage() {
		return "login";
	}

	@GetMapping("/getregisterpage")
	public String getregisterpage() {
		return "register.html";
	}

	@PostMapping("/checkloginuser")
	public String checkUsercredationals(@RequestParam String username, @RequestParam String password, HttpSession session) {
		Optional<User> res = service.FindbyUsernameandPassword(username, password);
		if (res.isPresent()) {
			User user = res.get();
			session.setAttribute("User",user );
			return "redirect:/getuserhome";
		} else {
			return "redirect:/";
		}

	}

	@PostMapping("/saveUser")
	public String SaveUser(@ModelAttribute User user) {
		try {
			service.saveUserdetails(user);
			return "redirect:/";
		} catch (Exception e) {
			return "redirect:/getregisterpage";
		}
	}

	@GetMapping("/getuserhome")
	public String getUserHomepage() {
		return "UserHome";
	}

	@GetMapping("/getnav")
	public String getnav() {
		return "UserNavbar";
	}

//	@PostMapping("/updateProfile")
//	public String updateProfile(@ModelAttribute User user, Principal principal, RedirectAttributes redirectAttributes) {
//		service.updateUser(user, principal.getName());
//		redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
//		return "redirect:/profile";
//	}
	
	@PostMapping("/updateProfile")
	public String updateProfile(@ModelAttribute User user, HttpSession session, RedirectAttributes redirectAttributes) {
	    // Retrieve the logged-in user from the session
	    User loggedInUser = (User) session.getAttribute("User");

	    if (loggedInUser == null) {
	        redirectAttributes.addFlashAttribute("error", "Session expired. Please log in again.");
	        return "redirect:/login";
	    }
	    System.out.println(user+" ===================================");

	    User save = repo.save(user);
   session.removeAttribute("User");
	    // Update session with new user data
	    session.setAttribute("User", save);

	    redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
	    return "redirect:/Profile";
	}
	
	@GetMapping("/images/{id}")
	public ResponseEntity<Files> getImage(@PathVariable Long id) {
	     Files img = fs.getFileOptional(id);
	    return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(img);
	}


	@GetMapping("/profilepage")
	public String showProfile( Model model, HttpSession session) {
		User user=(User)session.getAttribute("User");
	    if (user == null) {
	        return "redirect:/"; 
	    }

	    model.addAttribute("user", user);
	    return "Profile";
	}

	@GetMapping("/logout")
	public String logoutUser(HttpSession session) {
		session.invalidate();
		System.out.println("Logout User");
		return "redirect:/";
	}
	
}
