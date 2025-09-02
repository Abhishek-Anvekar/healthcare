package com.healthcare.auth_service.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    private JwtHelper jwtHelper;
    private UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtHelper jwtHelper, UserDetailsService userDetailsService) {
        this.jwtHelper = jwtHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Authorization
        String requestHeader = request.getHeader("Authorization");
        logger.info("Header : {}",requestHeader);
        String username = null;
        String token = null;

        if (requestHeader != null && requestHeader.startsWith("Bearer")){
            //looking good
            //Bearer 2334444ghhvgvhv
            //Substring starting with 7th index we will store in token, Because till Bearer and white space 6 indexes are there
            token = requestHeader.substring(7);
            try{
                username = jwtHelper.getUsernameFromToken(token);

            }catch (IllegalArgumentException e){
                logger.info("Illegal argument while fetching the username !!");
                e.printStackTrace();
            }catch (ExpiredJwtException e){
                logger.info("Given JWT is expired !!");
                e.printStackTrace();
            }catch (MalformedJwtException e){
                logger.info("Some changes has done in token !! Invalid Token !!");
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            logger.info("Invalid Header Value !!");
        }

        //if username found and Security context is null i.e. no-one is logged in
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            //Before setting the Authentication we need to validate the token, So we need username from this token to validate
            //Fetch user details by username - for this we need to Autowire UserDetailsService
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Boolean validateToken = jwtHelper.validateToken(token, userDetails);// This will validate token by comparing username from token and userDetails

            if (validateToken){
                //Set Authentication in Security Context Holder
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else {
                logger.info("Validation fails !!");
            }
        }
        filterChain.doFilter(request,response);
    }
}