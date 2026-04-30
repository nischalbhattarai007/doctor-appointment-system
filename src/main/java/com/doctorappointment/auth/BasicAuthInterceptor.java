package com.doctorappointment.auth;

import com.doctorappointment.auth.util.JwtUtil;
import com.doctorappointment.patient.exception.InvalidAuthorizationFormatException;
import com.doctorappointment.patient.exception.MissingAuthorizationHeaderException;
import io.grpc.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
@Singleton
public class BasicAuthInterceptor implements ServerInterceptor {
    /*
       gRPC sends everything as bytes over the network, not as text.
       So when a String value travels in metadata it must be converted
       to bytes on the way out and back to String on the way in.
       ASCII_STRING_MARSHALLER is gRPC's builtin converter that handles this
       for plain text values like auth headers. You will always use this for
       normal String headers.
    */
    private static final Metadata.Key<String> AUTH_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    // Context keys to pass credentials to handler
    public static final Context.Key<String> EMAIL_CONTEXT_KEY = Context.key("email");
    public static final Context.Key<String> PASSWORD_CONTEXT_KEY = Context.key("password");
    public static final Context.Key<String> ROLE_CONTEXT_KEY= Context.key("role");
    //no need to verify publicly available for all
    private static final Set<String> PUBLIC_METHODS = Set.of(
            "com.doctorappointment.PatientService/RegisterPatient",
            "com.doctorappointment.PatientService/GetAllPatient",
            "com.doctorappointment.DoctorService/RegisterDoctor",
            "com.doctorappointment.DoctorService/GetAllDoctorList"

    );
    private static final Set<String> LOGIN_METHODS = Set.of(
            "com.doctorappointment.PatientService/Login",
            "com.doctorappointment.DoctorService/DoctorLogin"
    );

    private final BasicAuthValidator basicAuthValidator;
    private final JwtUtil jwtUtil;

    public BasicAuthInterceptor(BasicAuthValidator basicAuthValidator, JwtUtil jwtUtil) {
        this.basicAuthValidator = basicAuthValidator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT>
    interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {

        String method = call.getMethodDescriptor().getFullMethodName();

        if (PUBLIC_METHODS.contains(method)) {
            return Contexts.interceptCall(Context.current(), call, metadata, next);
        }
        if (LOGIN_METHODS.contains(method)) {
            return handleBasicAuth(call, metadata, next);
        }
        return handleJwt(call, metadata, next);
    }
    private <ReqT, RespT> ServerCall.Listener<ReqT> handleBasicAuth(
            ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
        try {
            String[] credentials = decodeHeader(metadata.get(AUTH_HEADER));
            if (!basicAuthValidator.validate(credentials[0], credentials[1])) {
                return reject(call, "Invalid email or password");
            }
            Context context = Context.current()
                    .withValue(EMAIL_CONTEXT_KEY, credentials[0])
                    .withValue(PASSWORD_CONTEXT_KEY, credentials[1]);
            return Contexts.interceptCall(context, call, metadata, next);

        } catch (Exception e) {
            return reject(call, e.getMessage());
        }
    }
    private <ReqT, RespT> ServerCall.Listener<ReqT> handleJwt(
            ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
        String authHeader = metadata.get(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(call, "Missing Bearer token — please login first");
        }

        try {
            String token = authHeader.substring(7).trim();
            var claims = jwtUtil.validateToken(token);

            Context context = Context.current()
                    .withValue(EMAIL_CONTEXT_KEY, jwtUtil.getEmail(claims))
                    .withValue(ROLE_CONTEXT_KEY, jwtUtil.getRole(claims))
                    .withValue(PASSWORD_CONTEXT_KEY, ""); // not needed anymore for JWT requests
            return Contexts.interceptCall(context, call, metadata, next);

        } catch (ExpiredJwtException e) {
            return reject(call, "Token expired — please login again");
        } catch (JwtException e) {
            return reject(call, "Invalid token");
        } catch (Exception e) {
            return reject(call, "Authentication failed");
        }
    }
    private String[] decodeHeader(String header) {
        if (header == null || !header.startsWith("Basic ")) {
            throw new MissingAuthorizationHeaderException("Missing authorization header");
        }
        String decoded = new String(
                Base64.getDecoder().decode(header.substring(6).trim()),
                StandardCharsets.UTF_8
        );
        String[] parts = decoded.split(":", 2);
        if (parts.length != 2) {
            throw new InvalidAuthorizationFormatException("Invalid authorization format");
        }
        return parts;
    }

    private <ReqT, RespT> ServerCall.Listener<ReqT> reject(ServerCall<ReqT, RespT> call, String message) {
        call.close(Status.UNAUTHENTICATED.withDescription(message), new Metadata());
        return new ServerCall.Listener<>() {};
    }}