package com.vcube.kishore.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vcube.kishore.entity.User;
import com.vcube.kishore.repositary.Userepo;

@Service
public class Userservice {

	@Autowired
	private Userepo repo;
	
	public Optional<User> FindbyUsernameandPassword(String username,String password) {
		
		return repo.findByUsernameAndPassword(username, password);
	}

	public void saveUserdetails(User user) {
		repo.save(user);
		
	}
	
//	public User getUserByUsername(String username) {
//        return repo.findByUsername(username).orElse(null);
//    }
	
	public User getUserByUsername(String username) {
        return repo.findByUsername(username);
    }

    public void updateUser(User updatedUser, String username) {
        User existingUser = repo.findByUsername(username);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setPassword(updatedUser.getPassword());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            repo.save(existingUser);
        }
    }
}
