package com.example.demo.Controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

public class CustomErrorController implements ErrorController {
	@RequestMapping("/error")
	public ErrorResponse handleError(HttpServletRequest request) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		Integer statusCode = Integer.parseInt(status.toString());

		String errorMessage = "";

		switch (statusCode) {
		case 401:
			errorMessage = "Unauthorized. Please check your username and password.";
		case 403:
			errorMessage = "Access Denied";
		case 404:
			errorMessage = "Resource Not found";
		case 500:
			errorMessage = "Server Error";
		default:
			errorMessage = "An unexpected error occured";
		}
		return new ErrorResponse(statusCode, errorMessage);

	}

	public String getErrorPath() {
		return "/error";
	}

	class ErrorResponse {
		private int statusCode;
		private String errorMessage;

		public ErrorResponse(int statusCode, String errorMessage) {
			this.statusCode = statusCode;
			this.errorMessage = errorMessage;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getErrorMessage() {
			return errorMessage;
		}
	}
}
