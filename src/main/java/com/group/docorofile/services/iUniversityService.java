package com.group.docorofile.services;

import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.models.university.UniversityDTO;
import com.group.docorofile.models.university.UniversityNameDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface iUniversityService {
    UniversityEntity createUniversity(UniversityDTO universityDTO);

    List<UniversityNameDTO> findAllUniversityNames();

    List<UniversityNameDTO> findUniversityNamesByKeyword(String keyword);

    UniversityEntity findByUnivId(UUID universityId);

    UniversityEntity findByUnivName(String name);

    UniversityEntity updateUniversity(UniversityDTO universityDTO);

    void deleteUniversity(UUID univId);

    Page<UniversityDTO> findAllUniversity(Pageable pageable);
}
