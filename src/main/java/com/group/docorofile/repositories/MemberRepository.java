package com.group.docorofile.repositories;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemberRepository extends JpaRepository<MemberEntity, UUID> {
}
