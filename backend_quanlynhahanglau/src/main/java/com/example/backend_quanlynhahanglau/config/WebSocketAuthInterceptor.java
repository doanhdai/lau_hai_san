package com.example.backend_quanlynhahanglau.config;

import com.example.backend_quanlynhahanglau.security.JwtUtils;
import com.example.backend_quanlynhahanglau.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Try to get token from query parameter (from URL)
            String token = accessor.getFirstNativeHeader("token");
            
            // If not in header, try to get from session attributes (set during handshake)
            if (!StringUtils.hasText(token)) {
                Object tokenObj = accessor.getSessionAttributes().get("token");
                if (tokenObj != null) {
                    token = tokenObj.toString();
                }
            }
            
            // If still no token, try Authorization header
            if (!StringUtils.hasText(token)) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }
            
            if (StringUtils.hasText(token) && jwtUtils.validateJwtToken(token)) {
                try {
                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(authentication);
                    log.debug("WebSocket authenticated user: {}", username);
                } catch (Exception e) {
                    log.error("Error authenticating WebSocket user: {}", e.getMessage());
                }
            } else {
                log.debug("WebSocket connection attempt without valid token");
            }
        }
        
        return message;
    }
}

