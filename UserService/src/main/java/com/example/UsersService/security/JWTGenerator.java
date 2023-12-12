package com.example.UsersService.security;

import com.example.UsersService.model.UserEntity;
import com.example.UsersService.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;

@Component
public class JWTGenerator {
    //private static final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
//    @Bean
//    public void printSecretKey(){
//        System.out.println(key2);
//    }
    private static final Key key2 = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    public static final String SECRET = "2dae84f846e4f4b158a8d26681707f4338495bc7ab68151d7f7679cc5e56202dd3da0d356da007a7c28cb0b780418f4f3246769972d6feaa8f610c7d1e7ecf6a";
    private final static Key key= getSignKey() ;
//    UserRepository userRepository;
    private final UserRepository userService;

    public JWTGenerator(UserRepository userService) {
        this.userService = userService;
    }

    private static Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> roles =  authentication.getAuthorities();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + SecurityConstants.JWT_EXPIRATION);

        UserEntity user= userService.findByUsername(username).get();
        String token = Jwts.builder()
//                .setSubject(username)
                .setSubject(user.getId())
                .setIssuedAt( new Date())
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, key)
                .claim("roles", roles)
                .compact();
        System.out.println("New token :");
        System.out.println(token);
        return token;
    }
    public String getIdFromJWT(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT was exprired or incorrect",ex.fillInStackTrace());
        }
    }

}