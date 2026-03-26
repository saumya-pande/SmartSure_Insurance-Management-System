package com.dev.policy.exception;


public class InvalidFieldValueException extends RuntimeException {
 public InvalidFieldValueException(String fieldName, Object value, String reason) {
     super(String.format(
         "Invalid value '%s' for field '%s': %s",
         value, fieldName, reason
     ));
 }
}
