package com.mycompany.myapp.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    public TaskNotFoundException() {
        this("error.tasknotfound");
    }

    public TaskNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, createProblemDetail(message), null);
    }

    private static ProblemDetail createProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Task Not Found");
        problemDetail.setDetail(message);
        return problemDetail;
    }
}
