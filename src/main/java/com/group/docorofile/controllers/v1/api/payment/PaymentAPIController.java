package com.group.docorofile.controllers.v1.api.payment;

import com.group.docorofile.entities.PaymentEntity;
import com.group.docorofile.exceptions.UserNotFoundException;
import com.group.docorofile.models.dto.DataTableResponse;
import com.group.docorofile.models.dto.PaymentDTO;
import com.group.docorofile.models.dto.PaymentTableDTO;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.PaymentService;
import com.group.docorofile.services.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/member/payment")
@RequiredArgsConstructor
public class PaymentAPIController {

    private final PaymentService paymentService;
    private final JwtTokenUtil jwtUtils;
    private final UserServiceImpl userService;

    @PostMapping("/vn-pay")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public Object createPayment(@CookieValue(value = "JWT", required = false) String token,
                                HttpServletRequest request) {

        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        String email = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Token không hợp lệ hoặc người dùng không tồn tại."))
                .getUserId();

        PaymentDTO.VNPayResponse response = paymentService.createVnPayPayment(userId, request);

//        return new SuccessResponse(
//                "Tạo URL thanh toán thành công!",
//                200,
//                response,
//                LocalDateTime.now()
//        );
        return "redirect:" + response.getPaymentUrl();

    }
    
    @GetMapping("/vn-pay-callback")
    public Object handleVnPayCallback(HttpServletRequest request) {
        PaymentDTO.VNPayResponse response = paymentService.handleVnPayCallback(request);
        request.getParameterMap().forEach((k, v) -> System.out.println(k + ": " + Arrays.toString(v)));

        if (!"00".equals(response.getCode())) {
            return new SuccessResponse(
                    "Giao dịch thất bại.",
                    400,
                    response,
                    LocalDateTime.now()
            );
//            return "notification/all_notifications";
        }

        return new SuccessResponse(
                "Thanh toán và nâng cấp thành công!",
                200,
                response,
                LocalDateTime.now()
        );
//        return "notification/all_notifications";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<DataTableResponse<PaymentTableDTO>> getPaymentsByUser(
            @PathVariable UUID userId,
            @RequestParam int draw,
            @RequestParam int start,
            @RequestParam int length) {

        PageRequest pageable = PageRequest.of(start / length, length, Sort.by(Sort.Direction.DESC, "paymentDate"));
        Page<PaymentEntity> page = paymentService.getPaymentsByUser(userId, pageable);

        List<PaymentTableDTO> data = page.getContent().stream()
                .map(PaymentTableDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(new DataTableResponse<>(draw, page.getTotalElements(), page.getTotalElements(), data));
    }

}
