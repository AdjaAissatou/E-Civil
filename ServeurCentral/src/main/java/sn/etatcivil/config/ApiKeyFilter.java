package sn.etatcivil.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

//@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    
    private final String validApiKey;
    
    
    //@Autowired
    public ApiKeyFilter(String validApiKey) {
        this.validApiKey = validApiKey;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestApiKey = request.getHeader("Authorization");
        
        if (requestApiKey == null || !requestApiKey.equals("Bearer " + validApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"API Key invalide ou manquante\"}");
            return;
        }
        
        // Authentification réussie
//        UsernamePasswordAuthenticationToken auth = 
//            new UsernamePasswordAuthenticationToken("api-client", null, Collections.emptyList());
//        SecurityContextHolder.getContext().setAuthentication(auth);
//        
        filterChain.doFilter(request, response);
    }
}