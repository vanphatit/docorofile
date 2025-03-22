package com.group.docorofile.services.impl;

import com.group.docorofile.repositories.MembershipRepository;
import com.group.docorofile.services.iMembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MembershipServiceImpl implements iMembershipService {
    @Autowired
    private MembershipRepository membershipRepository;
}
