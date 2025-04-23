package com.group.docorofile.services.impl;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.FollowCourseEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.enums.EMembershipLevel;
import com.group.docorofile.exceptions.UserNotFoundException;
import com.group.docorofile.models.course.CourseCreatedResponseDTO;
import com.group.docorofile.repositories.*;
import com.group.docorofile.response.ConflictError;
import com.group.docorofile.services.iFollowCourseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FollowCourseServiceImpl implements iFollowCourseService {
    @Autowired
    private FollowCourseRepository followCourseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private MemberRepository memberRepo;

    @Autowired
    private ChatServiceImpl chatService;

    @Autowired
    private ChatRoomRepository chatRoomRepo;

    public void followCourse(UUID userId, UUID courseId) {
        MemberEntity member = memberRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khóa học."));

        if (followCourseRepo.existsByFollowerAndCourse(member, course)) {
            throw new ConflictError("Bạn đã theo dõi khóa học này rồi.");
        }

        // Mặc định là FREE nếu không có membership
        EMembershipLevel level = EMembershipLevel.FREE;
        if (member.getMembership() != null && member.getMembership().getLevel() != null) {
            level = member.getMembership().getLevel();
        }

        // Chỉ giới hạn số lượng nếu là FREE
        if (level == EMembershipLevel.FREE) {
            long count = followCourseRepo.countByFollower(member);
            if (count >= 3) {
                throw new ConflictError("Tài khoản FREE chỉ được theo dõi tối đa 3 khóa học.");
            }
        }

        FollowCourseEntity follow = FollowCourseEntity.builder()
                .follower(member)
                .course(course)
                .followedAt(LocalDateTime.now())
                .build();

        followCourseRepo.save(follow);
        chatRoomRepo.findByCourse_CourseId(courseId)
                .ifPresent(chatRoom -> chatService.addMemberToChatRoom(chatRoom.getChatId(), userId));

    }


    public List<CourseCreatedResponseDTO> getFollowedCourses(UUID userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return followCourseRepo.findByFollower(user).stream()
                .map(follow -> {
                    CourseEntity course = follow.getCourse();
                    return new CourseCreatedResponseDTO(
                            course.getCourseId(),
                            course.getCourseName(),
                            course.getUniversity().getUnivName()
                    );
                }).toList();
    }


    public void unfollowCourse(UUID userId, UUID courseId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found" + userId));
        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        FollowCourseEntity follow = followCourseRepo.findByFollowerAndCourse(user, course)
                .orElseThrow(() -> new EntityNotFoundException("Bạn chưa theo dõi khóa học này."));

        followCourseRepo.delete(follow);
        chatRoomRepo.findByCourse_CourseId(courseId)
                .ifPresent(chatRoom -> chatService.removeMemberFromChatRoom(chatRoom.getChatId(), userId));
    }

}
