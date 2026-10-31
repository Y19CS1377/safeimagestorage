package com.vcube.kishore.repositary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vcube.kishore.entity.Files;

@Repository
public interface FilesRepository extends JpaRepository<Files, Long> {
//    List<Files> findByUserId(Long userId);
}

