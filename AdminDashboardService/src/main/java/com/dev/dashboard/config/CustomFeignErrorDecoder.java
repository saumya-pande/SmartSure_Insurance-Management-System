package com.dev.dashboard.config;

import com.dev.dashboard.exception.DownstreamServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.io.InputStream;

public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.resolve(response.status());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (status.is4xxClientError()) {
            String error = status.getReasonPhrase();
            String message = "Downstream validation failed";

            try (InputStream bodyIs = response.body().asInputStream()) {
                JsonNode node = mapper.readTree(bodyIs);
                if (node.has("message")) {
                    message = node.get("message").asText();
                }
                if (node.has("error")) {
                    error = node.get("error").asText();
                }
            } catch (Exception ignored) {
                // If body cannot be parsed, use default titles
                if (status == HttpStatus.NOT_FOUND) {
                    message = "Requested resource not found.";
                } else if (status == HttpStatus.BAD_REQUEST) {
                    message = "Bad request to downstream service.";
                }
            }
            
            return new DownstreamServiceException(status, error, message);
        }
        
        // Let standard Feign exception logic handle 5xx and others
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
