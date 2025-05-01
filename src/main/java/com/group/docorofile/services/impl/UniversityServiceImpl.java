package com.group.docorofile.services.impl;

import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.models.university.UniversityDTO;
import com.group.docorofile.models.university.UniversityNameDTO;
import com.group.docorofile.repositories.CourseRepository;
import com.group.docorofile.repositories.UniversityRepository;
import com.group.docorofile.response.ConflictError;
import com.group.docorofile.services.iUniversityService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UniversityServiceImpl implements iUniversityService {
    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    CourseRepository courseRepository;

    @Override
    public UniversityEntity createUniversity(UniversityDTO universityDTO) {
        if (universityRepository.existsByUnivName(universityDTO.getUnivName())) {
            throw new ConflictError("University already exists with name: " + universityDTO.getUnivName());
        }

        UniversityEntity university = UniversityEntity.builder()
                .univId(UUID.randomUUID()) // hoặc để null và dùng @PrePersist
                .univName(universityDTO.getUnivName())
                .address(universityDTO.getAddress())
                .build();

        return universityRepository.save(university);
    }

    @Override
    public List<UniversityNameDTO> findAllUniversityNames() {
        return universityRepository.findAllUniversityNames();
    }

    @Override
    public List<UniversityNameDTO> findUniversityNamesByKeyword(String keyword) {
        return universityRepository.findUniversityNamesByKeyword(keyword);
    }

    @Override
    public UniversityEntity findByUnivId(UUID universityId) {
        return universityRepository.findByUnivId(universityId)
                .orElseThrow(() -> new RuntimeException("University not found with ID: " + universityId));
    }

    @Override
    public UniversityEntity findByUnivName(String name) {
        return universityRepository.findByUnivName(name)
                .orElseThrow(() -> new RuntimeException("University not found with name: " + name));
    }

    @Override
    public UniversityEntity updateUniversity(UniversityDTO universityDTO) {
        UniversityEntity university = universityRepository.findByUnivId(universityDTO.getUnivId())
                .orElseThrow(() -> new RuntimeException("University not found with ID: " + universityDTO.getUnivId()));

        // Kiểm tra tên mới đã được sử dụng bởi trường khác chưa
        universityRepository.findByUnivName(universityDTO.getUnivName())
                .filter(u -> !u.getUnivId().equals(universityDTO.getUnivId()))
                .ifPresent(u -> {
                    throw new RuntimeException("University name '" + universityDTO.getUnivName() + "' already exists.");
                });

        university.setUnivName(universityDTO.getUnivName());
        university.setAddress(universityDTO.getAddress());

        return universityRepository.save(university);
    }

    @Override
    public void deleteUniversity(UUID univId) {
        boolean hasCourses = courseRepository.existsByUniversity_UnivId(univId);

        if (hasCourses) {
            throw new ConflictError("Cannot delete university. There are still courses linked to it.");
        }

        UniversityEntity university = universityRepository.findByUnivId(univId)
                .orElseThrow(() -> new EntityNotFoundException("University not found"));

        universityRepository.delete(university);
    }

    @Override
    public List<UniversityEntity> findAllUniversities() {
        return universityRepository.findAll();
    }
}



