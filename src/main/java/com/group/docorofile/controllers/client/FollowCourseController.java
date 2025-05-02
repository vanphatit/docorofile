package com.group.docorofile.controllers.client;

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
@RequestMapping("/member")
public class FollowCourseController {

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping("/courses")
    public String showAllCoursesPage() {
        return "fragments/course/member/courses";
    }


}