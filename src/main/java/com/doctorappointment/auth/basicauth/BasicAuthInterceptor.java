package com.doctorappointment.auth.basicauth;

import com.doctorappointment.auth.BasicAuthValidator;
import com.doctorappointment.patient.exception.InvalidAuthorizationFormatException;
import com.doctorappointment.patient.exception.MissingAuthorizationHeaderException;
import io.grpc.*;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

@Singleton
@Slf4j

public class BasicAuthInterceptor implements ServerInterceptor {
    // Context keys shared between BasicAuthInterceptor and JwtAuthInterceptor.
    // BasicAuth runs first (login flow), JwtAuth runs second (all other calls).
    public static final Context.Key<String> EMAIL_CONTEXT_KEY = Context.key("email");
    public static final Context.Key<String> ROLE_CONTEXT_KEY = Context.key("role");
    private static final Metadata.Key<String> AUTH_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private static final Set<String> LOGIN_METHODS = Set.of(
            "Login",
            "DoctorLogin"
    );
    private final BasicAuthValidator basicAuthValidator;
    private final boolean authEnable;

    public BasicAuthInterceptor(BasicAuthValidator basicAuthValidator,@Value("${auth.enable}") boolean authEnable) {
        this.basicAuthValidator = basicAuthValidator;
        this.authEnable = authEnable;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
        if(!authEnable){
            /*
                isEnable=true -> skips this block
                isEnable=false-> triggered this block and execute this block with no authorization code
             */
            return Contexts.interceptCall(Context.current(),call,metadata,next);
        }

        String method = call.getMethodDescriptor().getFullMethodName();
        String simpleName=method.substring(method.lastIndexOf('/')+1);

        // Skip - not a login method
        if (!LOGIN_METHODS.contains(simpleName)) {
            return Contexts.interceptCall(Context.current(), call, metadata, next);
        }

        try {
            String[] credentials = decodeHeader(metadata.get(AUTH_HEADER));
            String email = credentials[0];
            String password = credentials[1];

            // validate() now returns the role string, or null if invalid
            String role = basicAuthValidator.validate(email, password);
            if (role == null) {
                return reject(call, "Invalid email or password");
            }

            Context context = Context.current()
                    .withValue(EMAIL_CONTEXT_KEY, email)
                    .withValue(ROLE_CONTEXT_KEY, role);

            return Contexts.interceptCall(context, call, metadata, next);

        } catch (MissingAuthorizationHeaderException | InvalidAuthorizationFormatException e) {
            return reject(call, e.getMessage());
        } catch (Exception e) {
            log.error("BasicAuth failed with exception", e);
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
        log.debug("Decoded authorization header: {} and password: {}", parts[0], parts[1]);
        return parts;
    }

    private <ReqT, RespT> ServerCall.Listener<ReqT> reject(ServerCall<ReqT, RespT> call, String message) {
        call.close(Status.UNAUTHENTICATED.withDescription(message), new Metadata());
        return new ServerCall.Listener<>() {
        };
    }
}
