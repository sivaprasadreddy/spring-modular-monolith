package com.sivalabs.bookstore.users;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class UserContextUtils {
    public static Long getCurrentUserIdOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Access denied");
        }
        var principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Long userId = jwt.getClaim("user_id");
            if (userId != null) {
                return userId;
            }
            throw new AccessDeniedException("Access denied");
        }
        throw new AccessDeniedException("Access denied");
    }
}
