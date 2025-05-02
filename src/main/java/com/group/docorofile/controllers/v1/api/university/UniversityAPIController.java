package com.group.docorofile.controllers.v1.api.university;

import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.models.university.UniversityDTO;
import com.group.docorofile.models.university.UniversityNameDTO;
import com.group.docorofile.response.CreatedResponse;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.services.iUniversityService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/v1/api/universities")
public class UniversityAPIController {
    @Autowired
    private iUniversityService universityService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<CreatedResponse> createUniversity(@Valid @RequestBody UniversityDTO universityDTO) {
        UniversityEntity createdUniversity = universityService.createUniversity(universityDTO);

        CreatedResponse response = new CreatedResponse(
                "University created successfully",
                createdUniversity
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/list")
    public Page<UniversityDTO> getUniversities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return universityService.findAllUniversity(pageable);
    }

    @GetMapping("/names")
    public ResponseEntity<SuccessResponse> getAllUniversityNames() {
        List<UniversityNameDTO> names = universityService.findAllUniversityNames();

        SuccessResponse response = new SuccessResponse(
                "University names retrieved successfully",
                HttpStatus.OK.value(),
                names,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/names/search")
    public ResponseEntity<SuccessResponse> searchUniversityNames(@RequestParam("keyword") String keyword) {
        List<UniversityNameDTO> names = universityService.findUniversityNamesByKeyword(keyword);

        SuccessResponse response = new SuccessResponse(
                "University name search completed successfully",
                HttpStatus.OK.value(),
                names,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/detail")
    public ResponseEntity<SuccessResponse> getUniversityByName(@RequestParam String name) {
        UniversityEntity university = universityService.findByUnivName(name);

        SuccessResponse response = new SuccessResponse(
                "University detail retrieved successfully",
                HttpStatus.OK.value(),
                university,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{univId}")
    public ResponseEntity<SuccessResponse> deleteUniversity(@PathVariable UUID univId) {
        universityService.deleteUniversity(univId);

        SuccessResponse response = new SuccessResponse(
                "University deleted successfully",
                HttpStatus.OK.value(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }


}
