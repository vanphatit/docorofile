package com.group.docorofile.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model,
                              @RequestParam(value = "status", required = false) Integer statusOverride) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int statusCode = (statusOverride != null) ? statusOverride :
                (status != null ? Integer.parseInt(status.toString()) : 500);

        model.addAttribute("statusCode", statusCode);

        String msg = switch (statusCode) {
            case 404 -> "Trang không tồn tại";
            case 403 -> "Bạn không có quyền truy cập";
            case 401 -> "Vui lòng đăng nhập để tiếp tục";
            default -> "Đã xảy ra lỗi không xác định! ";
        };
        model.addAttribute("message", msg);

        String lottieSrc = switch (statusCode) {
            case 404, 401 -> "/assets/json/error2.json";
            default -> "/assets/json/error.json";
        };
        model.addAttribute("lottieSrc", lottieSrc);

        return "error";
    }

}
