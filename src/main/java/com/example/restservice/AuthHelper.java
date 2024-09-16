package com.example.restservice;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCrypt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

public class AuthHelper {

  static String secretKey = "8133c56afcb96411a19e8a3be0f3c636bd4120b0c0f2030a59f734c57840b97e84c745ff0d037f0847bc6021e7d9c0ab4a71ca886eefcaf622a486653be87fc0";
  static long jwtExpiration = (long) 86400000 * (long) 365;

  public static boolean verifyPassword(String plainPassword, String hashedPassword) {
    return BCrypt.checkpw(plainPassword, hashedPassword);
  }

  public static String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public static String generateToken(String username) {
    return generateToken(new HashMap<>(), username);
  }

  public static String generateToken(Map<String, Object> extraClaims, String username) {
    return buildToken(extraClaims, username, jwtExpiration);
  }

  public static long getExpirationTime() {
    return jwtExpiration;
  }

  public static String buildToken(
      Map<String, Object> extraClaims,
      String username,
      long expiration) {
    return Jwts
        .builder()
        .setClaims(extraClaims)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date((long) System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public static boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public static Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public static Claims extractAllClaims(String token) {
    return Jwts
        .parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public static Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

}