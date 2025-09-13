package com.sivalabs.bookstore.users;

import com.sivalabs.bookstore.users.domain.SecurityUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContextUtils {
    public static Long getCurrentUserIdOrThrow() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getId();
        }
        throw new AccessDeniedException("Access denied");
    }
}
