package com.upgrad.FoodOrderingApp.api.exception;

import com.upgrad.FoodOrderingApp.api.model.ErrorResponse;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(AuthorizationFailedException.class)
    public ResponseEntity<ErrorResponse> authorizationFailedExceptionHandler(final AuthorizationFailedException exc, final WebRequest request) {
        return new ResponseEntity<>(
                new ErrorResponse().code(exc.getCode()).message(exc.getErrorMessage()), HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> authenticationFailedExceptionHandler(final AuthenticationFailedException exc, final WebRequest request) {
        return new ResponseEntity<>(
                new ErrorResponse().code(exc.getCode()).message(exc.getErrorMessage()), HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(SignUpRestrictedException.class)
    public ResponseEntity<ErrorResponse> signupRestrictedExceptionHandler(final SignUpRestrictedException exc, final WebRequest request) {
        return new ResponseEntity<>(
                new ErrorResponse().code(exc.getCode()).message(exc.getErrorMessage()), HttpStatus.CONFLICT
        );
    }

//    @ExceptionHandler(SignOutRestrictedException.class)
//    public ResponseEntity<ErrorResponse> signOutRestrictedExceptionHandler(final SignOutRestrictedException exc,
//                                                                           final WebRequest request) {
//        return new ResponseEntity<>(
//                new ErrorResponse().code(exc.getCode()).message(exc.getErrorMessage()), HttpStatus.UNAUTHORIZED
//        );
//    }

}
