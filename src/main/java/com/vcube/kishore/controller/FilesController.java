package com.vcube.kishore.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.vcube.kishore.entity.Files;
import com.vcube.kishore.entity.User;
import com.vcube.kishore.repositary.Userepo;
import com.vcube.kishore.service.FilesService;

import jakarta.servlet.http.HttpSession;

@Controller
public class FilesController {

	@Autowired
    private FilesService fileService;
	@Autowired
    private HttpSession session;
     @Autowired
     private Userepo userrepo;
  
    
    @GetMapping("/getallfiles")
    public String getAllUserFiles(Model model) {
        User user = (User) session.getAttribute("User");
        if (user == null) {
            return "redirect:/login"; // Redirect to login if user is not in session
        }
        Optional<User> userOptional = userrepo.findById(user.getId());
        if (userOptional.isEmpty()) {
            return "redirect:/login"; // Redirect if user not found in DB
        }
        User user2 = userOptional.get();
        List<Files> mypersonal = user2.getMypersonal();
        
        model.addAttribute("files", mypersonal);
        return "ViewMyFiles"; // Make sure this HTML file exists in "templates" folder
    }

    
    @PostMapping("/uploadfile")
    public String uploadFile(@RequestParam("name") String name,
                             @RequestParam("type") String type,
                             @RequestParam("imageData") MultipartFile file) {

        User user = (User) session.getAttribute("User");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Files newFile = new Files();
            newFile.setName(name);
            newFile.setType(type);
            newFile.setImageData(file.getBytes());
   
            Optional<User> byId = userrepo.findById(user.getId());
            User user2 = byId.get();
            List<Files> mypersonal = user2.getMypersonal();
            mypersonal.add(newFile);
            user2.setMypersonal(mypersonal);
            userrepo.save(user2);
            
//            fileService.saveFile(newFile);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/getallfiles";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        Files file = fileService.getFile(id);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(file.getImageData());
    }


    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id) {
    	  User user = (User) session.getAttribute("User");
    	  Optional<User> res = userrepo.findById(user.getId());
    	  User user2 = res.get();
    	  List<Files> files = user2.getMypersonal();
    	  for(Files f:files) {
    		  if(f.getId()==id) {
    			  files.remove(f);
    			  break;
    		  }
    	  }
    	  System.out.println("==============================================");
    	  System.out.println(files);
    	  user2.setMypersonal(files);
    	  userrepo.save(user2);
    	  
    	  
        return "redirect:/getallfiles";
    }
    
  
    @GetMapping("/getaddfilepage")
    public String getAddFilePage() {
        return "AddFilePage";
    }
    
    @GetMapping("/home")
    public String homePage() {
        return "UserHome";
    }
    
//    @GetMapping("/images/{id}")
//    public ResponseEntity<byte[]> getFileImage(@PathVariable Long id) {
//        Files file = fileService.getFile(id);
//        if (file == null || !file.getType().startsWith("image")) {
//            return ResponseEntity.notFound().build();
//        }
//
//        // Ensure correct content type
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType(file.getType()));
//
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(file.getImageData());
//    }


}
