package com.dev.policy.exception;



public class PolicyNotActiveException extends RuntimeException {
 public PolicyNotActiveException(Long id) {
     super("Policy with id " + id + " is not active and cannot be purchased.");
 }
}