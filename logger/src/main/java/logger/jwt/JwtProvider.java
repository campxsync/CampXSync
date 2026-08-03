package logger.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import logger.constants.SecurityConstants;
import logger.dto.UserPrincipal;
import logger.exception.JwtException;

import java.util.Date;
import java.util.List;

public class JwtProvider {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final String issuer;

    public JwtProvider(String secret, String issuer) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new JwtException("JWT secret key must not be null or empty");
        }
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer != null ? issuer : "campxsync";
        this.verifier = JWT.require(algorithm)
                .withIssuer(this.issuer)
                .build();
    }

    /**
     * Generates a JWT token for the given UserPrincipal and duration.
     */
    public String createToken(UserPrincipal principal, long expirationMs) {
        if (principal == null) {
            throw new JwtException("UserPrincipal must not be null");
        }
        try {
            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(principal.getUserId())
                    .withClaim(SecurityConstants.CLAIM_USERNAME, principal.getUsername())
                    .withClaim(SecurityConstants.CLAIM_EMAIL, principal.getEmail())
                    .withClaim(SecurityConstants.CLAIM_TENANT_ID, principal.getTenantId())
                    .withClaim(SecurityConstants.CLAIM_ROLES, principal.getRoles())
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                    .sign(algorithm);
        } catch (Exception e) {
            throw new JwtException("Failed to sign JWT token", e);
        }
    }

    /**
     * Validates and decodes the token, returning the parsed UserPrincipal.
     */
    public UserPrincipal validateAndDecode(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtException("Token must not be null or empty");
        }
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            String userId = decodedJWT.getSubject();
            String username = decodedJWT.getClaim(SecurityConstants.CLAIM_USERNAME).asString();
            String email = decodedJWT.getClaim(SecurityConstants.CLAIM_EMAIL).asString();
            String tenantId = decodedJWT.getClaim(SecurityConstants.CLAIM_TENANT_ID).asString();
            List<String> roles = decodedJWT.getClaim(SecurityConstants.CLAIM_ROLES).asList(String.class);

            return new UserPrincipal(userId, username, email, roles, tenantId);
        } catch (Exception e) {
            throw new JwtException("JWT validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves the raw token string from HTTP Authorization header.
     */
    public String resolveToken(String bearerHeader) {
        if (bearerHeader != null && bearerHeader.startsWith(SecurityConstants.TOKEN_PREFIX_BEARER)) {
            return bearerHeader.substring(SecurityConstants.TOKEN_PREFIX_BEARER.length()).trim();
        }
        return null;
    }
}
