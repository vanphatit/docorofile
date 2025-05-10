package com.group.docorofile.controllers.client;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.PaymentEntity;
import com.group.docorofile.exceptions.UserNotFoundException;
import com.group.docorofile.models.dto.PaymentDTO;
import com.group.docorofile.repositories.PaymentRepository;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.PaymentService;
import com.group.docorofile.services.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/member/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtTokenUtil jwtUtils;
    private final UserServiceImpl userService;
    private final PaymentRepository paymentRepository;

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @GetMapping("/pricing")
    public String showPricingPage(@CookieValue(value = "JWT", required = false) String token,
                                  Model model) {

        String membershipLevel = paymentService.getMembershipLevelFromToken(token);
        model.addAttribute("membershipLevel", membershipLevel); // "FREE" or "PREMIUM"

        return "fragments/payment/pricing";
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PostMapping("/vn-pay")
    public String createPaymentAndRedirect(@CookieValue(value = "JWT", required = false) String token,
                                           HttpServletRequest request) {

        if (token == null || token.isEmpty()) {
            return "redirect:/auth/login"; // fallback nếu chưa đăng nhập
        }

        String email = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Token không hợp lệ hoặc người dùng không tồn tại."))
                .getUserId();

        PaymentDTO.VNPayResponse response = paymentService.createVnPayPayment(userId, request);

        return "redirect:" + response.getPaymentUrl();
    }

    @GetMapping("/vn-pay-callback")
    public String vnPayResult(HttpServletRequest request, Model model) {
        PaymentDTO.VNPayResponse response = paymentService.handleVnPayCallback(request);

        if (!"00".equals(response.getCode())) {
            model.addAttribute("status", "fail");
            model.addAttribute("message", response.getMessage());
        } else {
            model.addAttribute("status", "success");
            model.addAttribute("message", response.getMessage());

            String paymentIdStr = request.getParameter("vnp_TxnRef");
            UUID paymentId = UUID.fromString(paymentIdStr);
            PaymentEntity payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
            MemberEntity member = payment.getPayer();

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedAmount = formatter.format(payment.getAmount()); // Long, BigDecimal...
            model.addAttribute("formattedAmount", formattedAmount);


            model.addAttribute("paymentDate", payment.getPaymentDate());
            model.addAttribute("payerName", member.getFullName());
            model.addAttribute("paymentId", payment.getPaymentId());
        }


        return "fragments/payment/payment_status"; // Trả thẳng view
    }



}


