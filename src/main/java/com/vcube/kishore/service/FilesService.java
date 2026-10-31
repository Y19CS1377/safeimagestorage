package com.vcube.kishore.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vcube.kishore.entity.Files;
import com.vcube.kishore.repositary.FilesRepository;

@Service
public class FilesService {

    private final FilesRepository filesRepository;

    public FilesService(FilesRepository filesRepository) {
        this.filesRepository = filesRepository;
    }

    public void saveFile(Files file) {
        filesRepository.save(file);
    }

    public Files getFile(Long id) {
        return filesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public Optional<Files> getFileOptional(Long id) {
        return filesRepository.findById(id);
    }

    @Transactional
    public void deleteFile(Long id) {
        Optional<Files> file = filesRepository.findById(id);
        if (file.isPresent()) {
            filesRepository.deleteById(id);
        } else {
            throw new RuntimeException("File not found, cannot delete");
        }
    }
}
