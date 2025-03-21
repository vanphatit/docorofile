package com.group.docorofile.controllers.v1.api.university;

import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.models.university.UniversityDTO;
import com.group.docorofile.models.university.UniversityNameDTO;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.services.impl.UniversityServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/v1/api/universities")
public class UniversityController {
    @Autowired
    private UniversityServiceImpl universityService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UniversityEntity> createUniversity(@Valid @RequestBody UniversityDTO universityDTO) {
        UniversityEntity createdUniversity = universityService.createUniversity(universityDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUniversity);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/names")
    public ResponseEntity<List<UniversityNameDTO>> getAllUniversityNames() {
        List<UniversityNameDTO> names = universityService.findAllUniversityNames();
        return ResponseEntity.ok(names);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/names/search")
    public ResponseEntity<List<UniversityNameDTO>> searchUniversityNames(@RequestParam("keyword") String keyword) {
        List<UniversityNameDTO> names = universityService.findUniversityNamesByKeyword(keyword);
        return ResponseEntity.ok(names);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/detail")
    public ResponseEntity<UniversityEntity> getUniversityByName(@RequestParam String name) {
        UniversityEntity university = universityService.findByUnivName(name);
        return ResponseEntity.ok(university);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<SuccessResponse> updateUniversity(@Valid @RequestBody UniversityDTO universityDTO) {
        UniversityEntity updated = universityService.updateUniversity(universityDTO);

        SuccessResponse response = new SuccessResponse(
                "University updated successfully",
                HttpStatus.OK.value(),
                updated,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

}
