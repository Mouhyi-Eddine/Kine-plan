package com.kineplan.shared.api;

import com.kineplan.shared.infrastructure.tenancy.TenantNotSetException;
import com.kineplan.auth.application.MembershipException;
import com.kineplan.patient.application.PatientNotFoundException;
import com.kineplan.appointment.application.AppointmentException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.kineplan.auth.application.AuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return problem(HttpStatus.BAD_REQUEST, "Validation error", detail, "validation-error");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Validation error", exception.getMessage(), "validation-error");
    }

    @ExceptionHandler(TenantNotSetException.class)
    ProblemDetail handleMissingTenant(TenantNotSetException exception) {
        return problem(HttpStatus.FORBIDDEN, "Tenant context required", exception.getMessage(), "tenant-required");
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException exception) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentication failed", "Authentication failed", "authentication-failed");
    }

    @ExceptionHandler(MembershipException.class)
    ProblemDetail handleMembership(MembershipException exception) {
        return problem(HttpStatus.CONFLICT, "Membership operation rejected", exception.getMessage(), "membership-operation-rejected");
    }

    @ExceptionHandler(PatientNotFoundException.class)
    ProblemDetail handlePatientNotFound(PatientNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Patient not found", "Patient not found", "patient-not-found");
    }

    @ExceptionHandler(AppointmentException.class)
    ProblemDetail handleAppointment(AppointmentException exception) {
        return problem(HttpStatus.CONFLICT, "Appointment operation rejected", exception.getMessage(), "appointment-operation-rejected");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred", "internal-error");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://kineplan.fr/problems/" + type));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}