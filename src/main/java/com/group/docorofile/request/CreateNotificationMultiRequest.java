package com.group.docorofile.request;

import com.group.docorofile.enums.ENotificationType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateNotificationMultiRequest {
    private List<UUID> receiverIds;
    private ENotificationType type;
    private String title;
    private String content;
}
