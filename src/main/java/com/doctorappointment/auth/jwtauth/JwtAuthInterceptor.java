package com.doctorappointment.auth.jwtauth;

import com.doctorappointment.auth.basicauth.BasicAuthInterceptor;
import com.doctorappointment.auth.util.JwtUtil;
import io.grpc.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.util.Set;

@Singleton
public class JwtAuthInterceptor implements ServerInterceptor {
    private static final Metadata.Key<String> AUTH_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    //no need validation for this methods no jwt no basic auth
    private static final Set<String> PUBLIC_METHODS = Set.of(
            "RegisterPatient",
            "GetAllPatient",
            "RefreshTokens",
            "GetAllDoctorList",
            "RegisterDoctor",
            "RefreshToken",
            "Login",
            "DoctorLogin"
    );

    private final JwtUtil jwtUtil;
    private final boolean authEnable;

    public JwtAuthInterceptor(JwtUtil jwtUtil,@Value("${auth.enable}") boolean authEnable) {
        this.jwtUtil = jwtUtil;
        this.authEnable = authEnable;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
        if(!authEnable){
            /********************************************************
             if isEnable true this block skipped
                 and if isEnable false this block executed
                    and bypass the interception and authentication
              ************************************************************/
            return Contexts.interceptCall(Context.current(),call,metadata,next);
        }

        String method = call.getMethodDescriptor().getFullMethodName();
        String simpleName=method.substring(method.lastIndexOf("/")+1);
        // Public and login methods don't need a JWT
        if (PUBLIC_METHODS.contains(simpleName)) {
            return Contexts.interceptCall(Context.current(), call, metadata, next);
        }

        String authHeader = metadata.get(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(call, "Missing Bearer token, please login first");
        }

        try {
            String token = authHeader.substring(7).trim();
            var claims = jwtUtil.validateToken(token);

            if (!jwtUtil.isAccessToken(claims)) {
                return reject(call, "Refresh tokens cannot be used for API calls");
            }

            Context context = Context.current()
                    .withValue(BasicAuthInterceptor.EMAIL_CONTEXT_KEY, jwtUtil.getEmail(claims))
                    .withValue(BasicAuthInterceptor.ROLE_CONTEXT_KEY, jwtUtil.getRole(claims));

            return Contexts.interceptCall(context, call, metadata, next);

        } catch (ExpiredJwtException e) {
            return reject(call, "Token expired, please login again");
        } catch (JwtException e) {
            return reject(call, "Invalid token");
        } catch (Exception e) {
            return reject(call, "Authentication failed");
        }
    }

    private <ReqT, RespT> ServerCall.Listener<ReqT> reject(ServerCall<ReqT, RespT> call, String message) {
        call.close(Status.UNAUTHENTICATED.withDescription(message), new Metadata());
        return new ServerCall.Listener<>() {
        };
    }

}
