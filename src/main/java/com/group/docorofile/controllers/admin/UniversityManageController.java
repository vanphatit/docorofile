package com.group.docorofile.controllers.admin;

import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.models.course.CourseCreatedResponseDTO;
import com.group.docorofile.models.university.UniversityDTO;
import com.group.docorofile.services.iCourseService;
import com.group.docorofile.services.iUniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class UniversityManageController {
    @Autowired
    private iUniversityService universityService;

    @Autowired
    private iCourseService courseService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/universities")
    public String listUniversities() {
        return "fragments/university/universities";
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/university/create")
    public String showCreateForm(Model model) {
        model.addAttribute("university", new UniversityDTO());
        return "fragments/university/university_form";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/university/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        UniversityEntity entity = universityService.findByUnivId(id);
        UniversityDTO dto = new UniversityDTO(entity.getUnivId(), entity.getUnivName(), entity.getAddress());
        model.addAttribute("university", dto);
        return "fragments/university/university_form";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/university/detail")
    public String viewUniversityDetail(@RequestParam String name, Model model) {
        UniversityEntity university = universityService.findByUnivName(name);
        List<CourseCreatedResponseDTO> courses = courseService.findCourseDTOsByUniversityName(name);

        model.addAttribute("university", university);
        model.addAttribute("courses", courses);
        return "fragments/university/university_detail";
    }

}


