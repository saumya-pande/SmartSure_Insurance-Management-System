package com.dev.policy.exception;


public class PolicyNotFoundException extends RuntimeException {
 public PolicyNotFoundException(String message) {
     super(message);
 }
 public PolicyNotFoundException(Long id) {
     super("Policy not found with id: " + id);
 }
}