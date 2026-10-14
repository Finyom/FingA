package com.example.finga.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

 // 404 - Resource not found
 @ExceptionHandler(ResourceNotFoundException.class)
 public ResponseEntity<ApiError> notFound(
         ResourceNotFoundException e,
         HttpServletRequest request) {

  return ResponseEntity
          .status(404)
          .body(new ApiError(
                  Instant.now(),
                  404,
                  "Not Found",
                  e.getMessage(),
                  request.getRequestURI(),
                  null
          ));
 }

 // 400 - Validation error
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<ApiError> validation(
         MethodArgumentNotValidException e,
         HttpServletRequest request) {

  Map<String, String> errors = new LinkedHashMap<>();

  e.getBindingResult()
          .getFieldErrors()
          .forEach(error ->
                  errors.put(
                          error.getField(),
                          error.getDefaultMessage()
                  )
          );

  return ResponseEntity
          .badRequest()
          .body(new ApiError(
                  Instant.now(),
                  400,
                  "Validation Failed",
                  "Request contains invalid data",
                  request.getRequestURI(),
                  errors
          ));
 }

 // 403 - Authenticated but not allowed
 @ExceptionHandler(AccessDeniedException.class)
 public ResponseEntity<ApiError> accessDenied(
         AccessDeniedException e,
         HttpServletRequest request) {

  return ResponseEntity
          .status(403)
          .body(new ApiError(
                  Instant.now(),
                  403,
                  "Forbidden",
                  "You do not have permission to access this resource",
                  request.getRequestURI(),
                  null
          ));
 }

 // 409 - Conflict
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<ApiError> illegalArgument(
         IllegalArgumentException e,
         HttpServletRequest request) {

  return ResponseEntity
          .status(409)
          .body(new ApiError(
                  Instant.now(),
                  409,
                  "Conflict",
                  e.getMessage(),
                  request.getRequestURI(),
                  null
          ));
 }

 // 500 - Unexpected error
 @ExceptionHandler(Exception.class)
 public ResponseEntity<ApiError> generic(
         Exception e,
         HttpServletRequest request) {

  return ResponseEntity
          .status(500)
          .body(new ApiError(
                  Instant.now(),
                  500,
                  "Internal Server Error",
                  "Unexpected server error",
                  request.getRequestURI(),
                  null
          ));
 }
}