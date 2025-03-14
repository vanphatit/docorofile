package com.group.docorofile.security;

import com.group.docorofile.entities.ModeratorEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.lang.Function;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenUtil {
    // 1 hour (ms)
    private static final long EXPIRATION_TIME = 3600000;
    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key KEY;

    @PostConstruct
    public void init() {
        this.KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(CustomUserDetails userDetails) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME));

        boolean isModerator = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MODERATOR"));
        if(isModerator) {
            ModeratorEntity moderator = (ModeratorEntity) userDetails.getUser();
            builder.claim("isReportManage", moderator.isReportManage());
        }

        return builder.signWith(KEY, SignatureAlgorithm.HS256).compact();
    }

    public String getUsernameFromToken(String token) {
        String username = null;
        try {
            Claims claims = extractAllClaims(token);
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                if(entry.getKey().equals("sub")) {
                    username = entry.getValue().toString();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return username;
    }

    public String getRoleFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role").toString();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith((SecretKey) KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean getIsReportManageFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("isReportManage", Boolean.class);
    }

    public Boolean getIsChatManageFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("isChatManage", Boolean.class);
    }
}