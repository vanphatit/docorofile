package com.group.docorofile.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "members")
public class MemberEntity extends UserEntity {
    @OneToOne
    private MembershipEntity membership;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentViewEntity> docViewed;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FollowCourseEntity> followedCourses;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteListEntity> favoriteDocuments;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReactionEntity> reactions;
  
    @ManyToMany(mappedBy = "members")
    private Set<ChatRoomEntity> chatRooms = new HashSet<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageEntity> messagesSent = new ArrayList<>();

    private int downloadLimit;
    private boolean isChat;
    private boolean isComment;

    // Chỉ lưu profile với các key hợp lệ: firstname, lastname, studyat
    @ElementCollection
    @CollectionTable(name = "member_profile", joinColumns = @JoinColumn(name = "member_id"))
    @MapKeyColumn(name = "profile_key")
    @Column(name = "profile_value")
    @JsonIgnore
    private Map<String, String> myProfile = new HashMap<>();

    // Chỉ cho phép lưu các key hợp lệ
    public void setProfileValue(String key, String value) {
        if (key.equals("firstname") || key.equals("lastname") || key.equals("studyat")) {
            this.myProfile.put(key, value);
        } else {
            throw new IllegalArgumentException("Chỉ được phép cập nhật firstname, lastname, studyat");
        }
    }

    // Lấy giá trị từ profile theo key
    public String getProfileValue(String key) {
        return this.myProfile.get(key);
    }
}