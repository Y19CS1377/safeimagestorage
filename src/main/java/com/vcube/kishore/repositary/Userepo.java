package com.vcube.kishore.repositary;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vcube.kishore.entity.User;

@Repository
public interface Userepo extends JpaRepository<User,Long>{

	Optional<User> findByUsernameAndPassword(String username, String password);

	User findByUsername(String username);
}
