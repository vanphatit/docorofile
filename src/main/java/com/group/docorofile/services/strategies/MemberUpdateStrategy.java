package com.group.docorofile.services.strategies;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.models.users.UpdateProfileRequest;
import com.group.docorofile.repositories.UniversityRepository;
import com.group.docorofile.response.NotFoundError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUpdateStrategy implements iUserUpdateStrategy {

    private final UniversityRepository universityRepository;

    @Override
    public boolean supports(UserEntity user) {
        return user instanceof MemberEntity;
    }

    @Override
    public void update(UserEntity user, CreateUserRequest request) {
        MemberEntity member = (MemberEntity) user;
        member.setFullName(request.getFullName());
        member.setEmail(request.getEmail());
        member.setDownloadLimit(
                request.getDownloadLimit() != null ? request.getDownloadLimit() : member.getDownloadLimit());
        member.setChat(Boolean.TRUE.equals(request.getIsChat()));
        member.setComment(Boolean.TRUE.equals(request.getIsComment()));
    }

    @Override
    public void updateProfile(UserEntity user, UpdateProfileRequest request) {
        MemberEntity member = (MemberEntity) user;
        UniversityEntity univ = universityRepository.findById(request.getUniversityId())
                .orElseThrow(() -> new NotFoundError("University not found with ID: " + request.getUniversityId()));

        member.setStudyAt(univ);
        member.setFullName(request.getFullname());
    }
}
