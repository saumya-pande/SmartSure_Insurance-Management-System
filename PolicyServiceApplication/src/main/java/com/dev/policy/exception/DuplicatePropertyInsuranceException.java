package com.dev.policy.exception;


import com.dev.policy.entity.PolicyType;

public class DuplicatePropertyInsuranceException extends RuntimeException {
 public DuplicatePropertyInsuranceException(String identifier, PolicyType type) {
     super(buildMessage(identifier, type));
 }

 private static String buildMessage(String identifier, PolicyType type) {
     return switch (type) {
         case HOME     -> "A home policy already exists for property: " + identifier +
                          ". The same house/flat cannot be insured twice.";
         case VEHICLE  -> "A vehicle policy already exists for vehicle: " + identifier +
                          ". The same vehicle cannot be insured twice.";
     };
 }
}