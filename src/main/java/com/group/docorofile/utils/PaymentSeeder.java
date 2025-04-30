package com.group.docorofile.utils;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.PaymentEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.enums.EPaymentStatus;
import com.group.docorofile.repositories.PaymentRepository;
import com.group.docorofile.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentSeeder {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public void seedPayments() {
        if(paymentRepository.count() == 0) {
            UserEntity user = userRepository.findById(UUID.fromString("1f024d41-5b21-6262-a89e-e5dc483bbfd8")).get();
            MemberEntity member = (MemberEntity) user;
            PaymentEntity payment1 = PaymentEntity.builder()
                    .amount(80000)
                    .paymentDate(LocalDateTime.of(2024, 1, 1, 10, 0, 30))
                    .payer(member)
                    .status(EPaymentStatus.FAILED)
                    .build();
            paymentRepository.save(payment1);

            PaymentEntity payment2 = PaymentEntity.builder()
                    .amount(80000)
                    .paymentDate(LocalDateTime.of(2024, 1, 2, 20, 0, 30))
                    .payer(member)
                    .status(EPaymentStatus.SUCCESS)
                    .build();
            paymentRepository.save(payment2);

            PaymentEntity payment3 = PaymentEntity.builder()
                    .amount(80000)
                    .paymentDate(LocalDateTime.of(2025, 2, 3, 15, 0, 30))
                    .payer(member)
                    .status(EPaymentStatus.FAILED)
                    .build();
            paymentRepository.save(payment3);

            PaymentEntity payment4 = PaymentEntity.builder()
                    .amount(80000)
                    .paymentDate(LocalDateTime.of(2025, 3, 4, 12, 0, 30))
                    .payer(member)
                    .status(EPaymentStatus.SUCCESS)
                    .build();
            paymentRepository.save(payment4);

            PaymentEntity payment5 = PaymentEntity.builder()
                    .amount(80000)
                    .paymentDate(LocalDateTime.of(2025, 4, 5, 18, 0, 30))
                    .payer(member)
                    .status(EPaymentStatus.SUCCESS)
                    .build();
            paymentRepository.save(payment5);
        }
    }
}
