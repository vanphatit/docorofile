package com.group.docorofile.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class CspNonceFilter extends OncePerRequestFilter {
    public static final String ATTR = "cspNonce";
    private static final SecureRandom RNG = new SecureRandom();

    private static String genNonce() {
        byte[] b = new byte[16]; // >=128 bits
        RNG.nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String nonce = genNonce();
        req.setAttribute(ATTR, nonce);

        String csp = String.join("; ",
                "default-src 'self'",
                "base-uri 'self'",
                "frame-ancestors 'none'",
                "script-src 'self' 'nonce-{nonce}'",      // {nonce} sẽ được thay bên dưới
                "style-src 'self' 'nonce-{nonce}'",       // nếu còn inline style nhỏ; nếu không cần thì bỏ
                "img-src 'self' data: blob:",
                "font-src 'self' data:",
                "connect-src 'self'",
                "object-src 'none'",
                "form-action 'self'",
                "upgrade-insecure-requests"
        ).replace("{nonce}", nonce); // thay nonce động cho request này

        res.setHeader("Content-Security-Policy", csp);
        chain.doFilter(req, res);
    }
}