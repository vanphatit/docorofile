package com.group.docorofile.utils;

import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.repositories.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UniversitySeeder {

    private final UniversityRepository universityRepository;


    public void seedUniversities() {
        if (universityRepository.count() == 0) {

            // ===== UNIVERSITY 1 =====
            UniversityEntity hcmut = UniversityEntity.builder()
                    .univName("Ho Chi Minh City University of Technology")
                    .address("Ho Chi Minh City")
                    .build();
            universityRepository.save(hcmut);

            // ===== UNIVERSITY 2 =====
            UniversityEntity ptit = UniversityEntity.builder()
                    .univName("Posts and Telecommunications Institute of Technology")
                    .address("Hanoi & HCMC")
                    .build();
            universityRepository.save(ptit);

            // ===== UNIVERSITY 3 =====
            UniversityEntity uit = UniversityEntity.builder()
                    .univName("University of Information Technology")
                    .address("Ho Chi Minh City")
                    .build();
            universityRepository.save(uit);

        }
    }
}