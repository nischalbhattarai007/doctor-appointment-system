package com.doctorappointment.patient_service.auth;

import com.doctorappointment.patient_service.exception.InvalidAuthorizationFormatException;
import com.doctorappointment.patient_service.exception.MissingAuthorizationHeaderException;
import io.grpc.*;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
@Singleton
@Slf4j
public class BasicAuthInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTH_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    // Context keys to pass credentials to handler
    public static final Context.Key<String> EMAIL_CONTEXT_KEY = Context.key("email");
    public static final Context.Key<String> PASSWORD_CONTEXT_KEY = Context.key("password");

    private static final Set<String> PUBLIC_METHODS = Set.of(
            "com.doctorappointment.PatientService/RegisterPatient"
    );

    private final BasicAuthValidator basicAuthValidator;

    public BasicAuthInterceptor(BasicAuthValidator basicAuthValidator) {
        this.basicAuthValidator = basicAuthValidator;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT>
    interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {

        String method = call.getMethodDescriptor().getFullMethodName();
        log.info("Incoming method: {}", method);

        if (PUBLIC_METHODS.contains(method)) {
            return Contexts.interceptCall(Context.current(), call, metadata, next);
        }

        try {
            String[] credentials = decodeHeader(metadata.get(AUTH_HEADER));
            if (!basicAuthValidator.validate(credentials[0], credentials[1])) {
                return reject(call, "Invalid email or password");
            }
            log.info("Authenticated: {}", credentials[0]);

            // ← Store credentials in context so handler can use them
            Context context = Context.current()
                    .withValue(EMAIL_CONTEXT_KEY, credentials[0])
                    .withValue(PASSWORD_CONTEXT_KEY, credentials[1]);
            return Contexts.interceptCall(context, call, metadata, next);

        } catch (Exception e) {
            log.error("Auth error: {}", e.getMessage());
            return reject(call, e.getMessage());
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
    }
}