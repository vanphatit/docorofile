package com.group.docorofile.repositories;

import com.group.docorofile.entities.UniversityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UniversityRepository extends JpaRepository<UniversityEntity, UUID> {
    Optional<UniversityEntity> findByUnivName(String universityName);
    Boolean existsByUnivName(String universityName);
}
