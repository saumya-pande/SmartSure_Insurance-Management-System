package com.dev.policy.exception;


public class MissingRequiredFieldException extends RuntimeException {
 public MissingRequiredFieldException(String fieldName) {
     super("Required field is missing: '" + fieldName + "'");
 }
}