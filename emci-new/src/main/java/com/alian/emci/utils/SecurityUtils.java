package com.alian.emci.utils;

import com.alian.emci.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getId() : null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUsername() : null;
    }

    /**
     * 获取当前用户信息
     */
    public static UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isAdmin() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null && userDetails.isAdmin();
    }
}
