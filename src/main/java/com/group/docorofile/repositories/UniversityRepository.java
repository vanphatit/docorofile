package com.group.docorofile.repositories;

import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.models.university.UniversityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.group.docorofile.models.university.UniversityNameDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UniversityRepository extends JpaRepository<UniversityEntity, UUID> {
    Optional<UniversityEntity> findByUnivName(String universityName);

    Boolean existsByUnivName(String universityName);

    @Query("SELECT new com.group.docorofile.models.university.UniversityNameDTO(u.univName) FROM UniversityEntity u")
    List<UniversityNameDTO> findAllUniversityNames();

    @Query("SELECT new com.group.docorofile.models.university.UniversityDTO(u.univId, u.univName, u.address) FROM UniversityEntity u")
    Page<UniversityDTO> findAllUniversityNamesPaged(Pageable pageable);

    @Query("SELECT new com.group.docorofile.models.university.UniversityNameDTO(u.univName) FROM UniversityEntity u WHERE u.univName LIKE %:keyword%")
    List<UniversityNameDTO> findUniversityNamesByKeyword(@Param("keyword") String keyword);

    Optional<UniversityEntity> findByUnivId(UUID universityId);
}
