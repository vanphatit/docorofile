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
                "script-src 'self' 'nonce-{nonce}' https://code.jquery.com https://cdn.jsdelivr.net https://unpkg.com",
                "style-src 'self' 'nonce-{nonce}' https://fonts.googleapis.com https://cdn.jsdelivr.net",
                "img-src 'self' data: blob: https://cdn.jsdelivr.net https://unpkg.com",
                "font-src 'self' data: https://fonts.gstatic.com https://cdn.jsdelivr.net",
                "connect-src 'self'",
                "object-src 'none'",
                "form-action 'self'",
                "upgrade-insecure-requests").replace("{nonce}", nonce); // thay nonce động cho request này

        res.setHeader("Content-Security-Policy", csp);
        chain.doFilter(req, res);
    }
}