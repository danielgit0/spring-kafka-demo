package com.example.kafka.errors;

import jakarta.servlet.http.HttpServletRequest;

public record RequestContext(String path, String query, String fullPath) {

  public static RequestContext from(HttpServletRequest request) {
    String path = request.getRequestURI();
    String query = request.getQueryString();
    String full = (query != null) ? path + "?" + query : path;

    return new RequestContext(path, query, full);
  }
}
