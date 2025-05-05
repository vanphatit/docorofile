package com.group.docorofile.controllers.v1.api.report;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.enums.EReportStatus;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.request.CreateReportRequest;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.iReportService;
import com.group.docorofile.services.iUserService;
import com.group.docorofile.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/reports")
public class ReportAPIController {
    private final iReportService reportService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private iUserService userService;

    public ReportAPIController(iReportService reportService) {
        this.reportService = reportService;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_MEMBER')")
    @PostMapping("/create")
    public ResponseEntity<SuccessResponse> createReport(@RequestBody CreateReportRequest reportRequest,
                                                        @CookieValue(value = "JWT", required = false) String token
                                                        ) {
        String email = jwtTokenUtil.getUsernameFromToken(token);
        UserEntity current = userService.findByEmail(email).get();
        reportRequest.setReporterId(current.getUserId());
        reportService.createReport(reportRequest);

        SuccessResponse response = new SuccessResponse(
                "Report created successfully",
                HttpStatus.CREATED.value(),
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @GetMapping("")
    public ResponseEntity<ResultPaginationDTO> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok().body(reportService.getTopReportedDocuments(pageable));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @PutMapping("/{documentId}")
    public ResponseEntity<SuccessResponse> updateReportsStatusByDocumentId(
            @PathVariable UUID documentId,
            @RequestBody Map<String, String> requestBody) {

        String statusStr = requestBody.get("status");
        EReportStatus status = EReportStatus.valueOf(statusStr.toUpperCase());

        reportService.updateReportsStatusByDocumentId(documentId, status);

        SuccessResponse response = new SuccessResponse(
                "Report status updated successfully",
                HttpStatus.OK.value(),
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @GetMapping("/{documentId}")
    public ResponseEntity<ResultPaginationDTO> getDetailReportByDocumentId(
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok().body(reportService.getReportDetailsByDocumentId(documentId, pageable));
    }
}
