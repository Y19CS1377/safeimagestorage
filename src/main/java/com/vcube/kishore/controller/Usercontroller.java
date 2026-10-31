package com.vcube.kishore.controller;



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

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

	@GetMapping({"/", "/login"})
	public String getLoginPage() {
		return "login";
	}

	@GetMapping("/getregisterpage")
	public String getregisterpage(Model model) {
		model.addAttribute("user", new User());
		return "register";
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
	public String SaveUser(@ModelAttribute User user, Model model) {
		Map<String, String> validationErrors = new LinkedHashMap<>();
		if (user.getName() == null || user.getName().isBlank()) {
			validationErrors.put("nameError", "Full name is required.");
		}
		if (user.getUsername() == null || user.getUsername().isBlank()) {
			validationErrors.put("usernameError", "Username is required.");
		}
		if (user.getPassword() == null || user.getPassword().isBlank()) {
			validationErrors.put("passwordError", "Password is required.");
		}
		if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
			validationErrors.put("phoneNumberError", "Phone number is required.");
		}

		if (!validationErrors.isEmpty()) {
			model.addAllAttributes(validationErrors);
			return "register";
		}

		service.saveUserdetails(user);
		return "redirect:/";
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
	
	@GetMapping("/file-images/{id}")
	public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
		Optional<Files> optionalFile = fs.getFileOptional(id);
		if (optionalFile.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Files file = optionalFile.get();
		byte[] imageData = file.getImageData();
		if (imageData == null || imageData.length == 0) {
			return ResponseEntity.notFound().build();
		}

		MediaType mediaType = getImageMediaType(file.getType(), imageData);
		if (mediaType == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok()
				.contentType(mediaType)
				.header("X-Content-Type-Options", "nosniff")
				.body(imageData);
	}

	private MediaType getImageMediaType(String storedType, byte[] imageData) {
		if (storedType != null && !storedType.isBlank()) {
			try {
				MediaType mediaType = MediaType.parseMediaType(storedType.trim());
				if ("image".equalsIgnoreCase(mediaType.getType()) && !"*".equals(mediaType.getSubtype())) {
					return mediaType;
				}
			} catch (IllegalArgumentException ignored) {
				// Older uploads may have stored the generic value "image".
			}
		}

		return detectImageMediaType(imageData);
	}

	private MediaType detectImageMediaType(byte[] imageData) {
		try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(imageData))) {
			if (input == null) {
				return null;
			}

			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) {
				return null;
			}

			ImageReader reader = readers.next();
			try {
				reader.setInput(input, true, true);
				return switch (reader.getFormatName().toLowerCase(Locale.ROOT)) {
					case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
					case "png" -> MediaType.IMAGE_PNG;
					case "gif" -> MediaType.IMAGE_GIF;
					case "bmp" -> MediaType.parseMediaType("image/bmp");
					case "wbmp" -> MediaType.parseMediaType("image/vnd.wap.wbmp");
					default -> null;
				};
			} finally {
				reader.dispose();
			}
		} catch (IOException e) {
			return null;
		}
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
