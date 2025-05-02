package com.group.docorofile.controllers.admin;
import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.models.course.CourseDetailResponseDTO;
import com.group.docorofile.services.iCourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/admin")
public class CourseManageController {

    @Autowired
    private iCourseService courseService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/courses")
    public String showCourseList() {
        return "fragments/course/courses";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/course/detail")
    public String showCourseDetail(@RequestParam("courseName") String courseName,
                                   @RequestParam("universityName") String universityName,
                                   Model model) {
        CourseEntity course = courseService.findByCourseNameAndUniversityName(courseName, universityName);
        CourseDetailResponseDTO dto = new CourseDetailResponseDTO(course);
        model.addAttribute("course", dto);
        return "fragments/course/course_detail";
    }

}
