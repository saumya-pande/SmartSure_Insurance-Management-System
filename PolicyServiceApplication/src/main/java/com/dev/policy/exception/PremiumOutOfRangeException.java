package com.dev.policy.exception;

public class PremiumOutOfRangeException extends RuntimeException {
 public PremiumOutOfRangeException(Double entered, Double min, Double max) {
     super(String.format(
         "Premium amount %.2f is out of range. Must be between %.2f and %.2f.",
         entered, min, max
     ));
 }
}