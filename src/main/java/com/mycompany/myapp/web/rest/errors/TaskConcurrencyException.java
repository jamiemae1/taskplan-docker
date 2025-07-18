package com.mycompany.myapp.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TaskConcurrencyException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    public TaskConcurrencyException() {
        this("error.taskconcurrency");
    }

    public TaskConcurrencyException(String message) {
        super(HttpStatus.CONFLICT, createProblemDetail(message), null);
    }

    private static ProblemDetail createProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Task Concurrency Error");
        problemDetail.setDetail(message);
        return problemDetail;
    }
}
