package com.kitchen.sink.utils;

import com.kitchen.sink.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.kitchen.sink.constants.JWTConstant.ANONYMOUS_USER;
import static com.kitchen.sink.constants.JWTConstant.ROLES;

@Component
public class JWTUtils {

    @Value("${jwt.secret.key}")
    private String secretKey = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    // Use a strong secret key
    @Value("${jwt.expiration.time}")
    private long expirationTime = 1000 * 60 * 60;// 1 hour


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String email) {
        final String extractEmail = extractUsername(token);
        return (extractEmail.equals(email) && !isTokenExpired(token));
    }

    public boolean validateRoles(String token, Collection<? extends GrantedAuthority> roles) {
        Set<String> existingRoles = roles.stream().map(va->va.getAuthority()).collect(Collectors.toSet());
        Set<String> userRoles = extractRoles(token);
        if (userRoles.size()!=existingRoles.size()){
            return false;
        }

        return findSymmetricDifference(existingRoles, userRoles).isEmpty();
    }
    public  List<String> findSymmetricDifference(Set<String> set1, Set<String> set2) {
        Set<String> onlyInA = set1.stream()
                .filter(element -> !set2.contains(element))
                .collect(Collectors.toSet());

        Set<String> onlyInB = set2.stream()
                .filter(element -> !set1.contains(element))
                .collect(Collectors.toSet());

        onlyInA.addAll(onlyInB);
        return List.copyOf(onlyInA);
    }
    private Set<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return new HashSet<>(claims.get(ROLES, List.class));
    }


    public String generateToken(String userName, Set<UserRole> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES, roles);
        return createToken(claims, userName);
    }

    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals(ANONYMOUS_USER)) {
                return null;
            }
            return authentication.getName();
        } catch (Exception ex) {
            return null;
        }
    }
}
