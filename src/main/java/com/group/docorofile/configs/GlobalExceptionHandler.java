package com.group.docorofile.configs;

import com.group.docorofile.response.ErrorResponse;
import com.group.docorofile.response.ForbiddenError;
import com.group.docorofile.response.UnauthorizedError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorResponse.class)
    public ResponseEntity<ErrorResponse> handleErrorResponse(ErrorResponse ex) {
        // Vì ErrorResponse đã có đủ message, statusCode, timestamp
        // nên ta trả về chính đối tượng ex
        return ResponseEntity.status(ex.getStatusCode()).body(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        // Xử lý lỗi không lường trước, mặc định 500
        ErrorResponse error = new ErrorResponse("Internal Server Error", 500);
        return ResponseEntity.status(500).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        UnauthorizedError error = new UnauthorizedError("Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ForbiddenError error = new ForbiddenError("Access Denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}