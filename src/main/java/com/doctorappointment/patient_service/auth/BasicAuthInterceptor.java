package com.doctorappointment.patient_service.auth;

import com.doctorappointment.patient_service.exception.InvalidAuthorizationFormatException;
import com.doctorappointment.patient_service.exception.MissingAuthorizationHeaderException;
import io.grpc.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicAuthInterceptor implements ServerInterceptor {
    private final BasicAuthValidator basicAuthValidator;
    private static final Metadata.Key<String> AUTH_HEADER =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    public BasicAuthInterceptor(BasicAuthValidator basicAuthValidator) {
        this.basicAuthValidator = basicAuthValidator;
    }
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT>
    interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
        try{
            String[] credentials=decodeHeader(metadata.get(AUTH_HEADER));
            if(!basicAuthValidator.validate(credentials[0],credentials[1])){
                return reject(serverCall,"Invalid email and password");
            }
        }catch (Exception e){
            return reject(serverCall,e.getMessage());
        }
        return Contexts.interceptCall(Context.current(), serverCall,metadata,next);
    }
    private String[] decodeHeader(String header){
        if(header == null || !header.startsWith("Basic ")){
            throw new MissingAuthorizationHeaderException("Missing authorization header");
        }
        String decoded=new String(
                Base64.getDecoder().decode(
                        header.substring("Basic ".length()).trim()
        ),
        StandardCharsets.UTF_8);
        String[] parts=decoded.split(":",2);
        if(parts.length!=2){
            throw new InvalidAuthorizationFormatException("Invalid authorization format");
        }
        return parts;
    }
    private <ReqT,RespT> ServerCall.Listener<ReqT> reject(ServerCall<ReqT, RespT> call, String message) {
        call.close(
                Status.UNAUTHENTICATED.withDescription(message),
                new Metadata());
        return new  ServerCall.Listener<ReqT>() {};
    }
}
