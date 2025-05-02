package com.group.docorofile.services;
import com.group.docorofile.configs.VNPAYConfig;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.MembershipEntity;
import com.group.docorofile.entities.PaymentEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.enums.EMembershipLevel;
import com.group.docorofile.enums.ENotificationType;
import com.group.docorofile.enums.EPaymentStatus;
import com.group.docorofile.exceptions.AlreadyPremiumException;
import com.group.docorofile.exceptions.UserNotFoundException;
import com.group.docorofile.models.dto.PaymentDTO;
import com.group.docorofile.models.dto.PaymentTableDTO;
import com.group.docorofile.observer.NotificationCenter;
import com.group.docorofile.repositories.MemberRepository;
import com.group.docorofile.repositories.PaymentRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.request.CreateNotificationRequest;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.impl.NotificationServiceImpl;
import com.group.docorofile.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final VNPAYConfig vnPayConfig;
    private final JwtTokenUtil jwtUtils;
    private final iUserService userService;

    public PaymentDTO.VNPayResponse createVnPayPayment(UUID payerId, HttpServletRequest request) {
        // Lấy user từ DB
        MemberEntity payer = memberRepository.findById(payerId)
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        //  Check nếu đã Premium
//        if (payer.getMembership() != null && payer.getMembership().getLevel() == EMembershipLevel.PREMIUM) {
//            throw new IllegalStateException("Bạn đã có tài khoản Premium. Không thể nâng cấp thêm.");
//        }
        if (payer.getMembership() != null && payer.getMembership().getLevel() == EMembershipLevel.PREMIUM) {
            throw new AlreadyPremiumException("Bạn đã có tài khoản Premium. Không thể nâng cấp thêm.");
        }


        // Lấy amount từ query param và nhân 100
        long amountRaw = Long.parseLong(request.getParameter("amount"));
        long amount = amountRaw * 100L;

        // Optional bankCode từ query
        String bankCode = request.getParameter("bankCode");

        // Tạo bản ghi thanh toán trong DB
        PaymentEntity payment = PaymentEntity.builder()
                .amount(amountRaw) // Lưu vào DB đúng đơn vị VND
                .status(EPaymentStatus.PENDING)
                .payer(payer)
                .build();
        paymentRepository.save(payment);

        //Build tham số gửi sang VNPay
        Map<String, String> vnpParams = vnPayConfig.getVNPayConfig();
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_TxnRef", payment.getPaymentId().toString());
        vnpParams.put("vnp_OrderInfo", "Nâng cấp PREMIUM");
        vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParams.put("vnp_BankCode", bankCode);
        }

        // Build query + secure hash
        String queryUrl = VNPayUtil.getPaymentURL(vnpParams, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParams, false);
        String secureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + secureHash;

        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return PaymentDTO.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl)
                .build();
    }

    public PaymentDTO.VNPayResponse handleVnPayCallback(HttpServletRequest request) {
        String responseCode = request.getParameter("vnp_ResponseCode");
        String paymentIdStr = request.getParameter("vnp_TxnRef");

        if (!"00".equals(responseCode)) {
            return PaymentDTO.VNPayResponse.builder()
                    .code("fail")
                    .message("Giao dịch thất bại")
                    .build();
        }

        // Lấy payment theo paymentId (được truyền qua vnp_TxnRef)
        UUID paymentId = UUID.fromString(paymentIdStr);
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        // Cập nhật trạng thái thanh toán
        payment.setStatus(EPaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());

        // Lấy người thanh toán
        MemberEntity member = payment.getPayer();
        MembershipEntity membership = member.getMembership();
        LocalDateTime now = LocalDateTime.now();

        if (membership == null) {
            // Tạo membership mới nếu chưa có
            membership = MembershipEntity.builder()
                    .level(EMembershipLevel.PREMIUM)
                    .startDate(now)
                    .endDate(now.plusMonths(1))
                    .build();
        } else {
            // Nếu đã có membership, cập nhật lên PREMIUM và reset thời hạn
            membership.setLevel(EMembershipLevel.PREMIUM);
            membership.setStartDate(now);
            membership.setEndDate(now.plusMonths(1));
        }

       // membershipRepository.save(membership);
        member.setMembership(membership);
        memberRepository.save(member);
        paymentRepository.save(payment);

        // Tạo notification
        CreateNotificationRequest notiRequest = new CreateNotificationRequest();
        notiRequest.setReceiverId(member.getUserId());
        notiRequest.setType(ENotificationType.SYSTEM);
        notiRequest.setTitle("Giao dịch thành công");
        notiRequest.setContent("Bạn đã nâng cấp lên tài khoản Premium thành công!");

        // Gọi Observer
        NotificationCenter.notifyObservers(notiRequest);


        return PaymentDTO.VNPayResponse.builder()
                .code("00")
                .message("Thanh toán và nâng cấp thành công")
                .build();
    }

    public Page<PaymentEntity> getPaymentsByUser(UUID userId, Pageable pageable) {
        return paymentRepository.findByPayer_UserId(userId, pageable);
    }


    public String getMembershipLevelFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return "FREE";
        }

        String email = jwtUtils.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Token không hợp lệ hoặc người dùng không tồn tại."));

        // Chỉ Member mới có membership
        if (user instanceof MemberEntity member) {
            MembershipEntity membership = member.getMembership();
            return (membership != null) ? membership.getLevel().name() : "FREE";
        }

        return "FREE"; // Nếu là AdminEntity, ModeratorEntity thì cũng return FREE
    }

}
