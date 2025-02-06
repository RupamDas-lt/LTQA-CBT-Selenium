package TestManagers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static utility.FrameworkConstants.*;

public abstract class ApiManager extends BaseClass {
  private final Logger ltLogger = LogManager.getLogger(ApiManager.class);

  private Response httpMethod(String method, String uri, String body, ContentType contentType,
    Map<String, Object> headers, Map<String, Object> queryParam, int expectedStatus, String... basicAuthHeaders) {

    String username = basicAuthHeaders.length > 0 ? basicAuthHeaders[0] : "";
    String password = basicAuthHeaders.length > 1 ? basicAuthHeaders[1] : "";

    ltLogger.info(
      "Hitting Method: {} on URI: {} with Body: {}, Headers: {}, Query Param: {}, Content Type: {}, Expected Status is: {}, Username is: {} and Password is: {}",
      method, uri, body, headers, queryParam, contentType, expectedStatus, username, password);

    // Add Basic Auth header if username or password is provided
    if (!username.isEmpty() || !password.isEmpty()) {
      if (headers == null) {
        headers = new HashMap<>();
      }
      String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
      ltLogger.info("Encoded Auth Headers: {}", encodedAuth);
      headers.put("Authorization", "Basic " + encodedAuth);
      ltLogger.info("Updated Request Headers: {}", headers);
    }

    // Initialize the request
    RequestSpecification req = RestAssured.given().headers(headers != null ? headers : Map.of())
      .queryParams(queryParam != null ? queryParam : Map.of()).body(body != null ? body : "")
      .contentType(contentType != null ? contentType : ContentType.JSON);

    // Determine if status code verification is required
    boolean verifyStatusCode = !method.endsWith("_WITHOUT_STATUS_CODE_VERIFICATION");

    // Map HTTP methods to their corresponding REST Assured actions
    Map<String, Function<RequestSpecification, Response>> methodMap = Map.of(GET, r -> r.get(uri), POST,
      r -> r.post(uri), PUT, r -> r.put(uri), DELETE, r -> r.delete(uri), PATCH, r -> r.patch(uri), GET_REDIRECT,
      r -> r.redirects().follow(false).get(uri));

    // Get the appropriate HTTP method action
    Function<RequestSpecification, Response> action = methodMap.getOrDefault(
      method.replace("_WITHOUT_STATUS_CODE_VERIFICATION", ""),
      // Remove the suffix to match the method
      reqType -> {
        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
      });

    // Execute the request and handle status code verification
    Response response = action.apply(req);
    if (verifyStatusCode) {
      response.then().statusCode(expectedStatus);
    }

    return response;
  }

  public int getStatusCode(String uri, String body, ContentType contentType, Map<String, Object> headers,
    Map<String, Object> queryParam) {
    return httpMethod(GET_WITHOUT_STATUS_CODE_VERIFICATION, uri, body, contentType, headers, queryParam,
      0).getStatusCode();
  }

  public Response getRequestWithBasicAuth(String uri, String username, String password) {
    return httpMethod(GET_WITHOUT_STATUS_CODE_VERIFICATION, uri, null, ContentType.JSON, null, null, 0, username,
      password);
  }

  public Response patchRequestWithBasicAuth(String uri, String username, String password, String body) {
    return httpMethod(PATCH_WITHOUT_STATUS_CODE_VERIFICATION, uri, body, ContentType.JSON, null, null, 0, username,
      password);
  }

  public Response getRequest(String uri) {
    return httpMethod(GET_WITHOUT_STATUS_CODE_VERIFICATION, uri, null, ContentType.JSON, null, null, 0);
  }

  public String getRequestAsString(String uri) {
    Response response = httpMethod(GET_WITHOUT_STATUS_CODE_VERIFICATION, uri, null, ContentType.JSON, null, null, 0);
    ltLogger.info("Response: {}", response.asString());
    return response.asString();
  }

  public String getRequestWithBasicAuthAsString(String uri, String userName, String password) {
    Response response = httpMethod(GET_WITHOUT_STATUS_CODE_VERIFICATION, uri, null, ContentType.JSON, null, null, 0,
      userName, password);
    ltLogger.info("Response of get request with basic auth: {}", response.body().asString());
    return response.body().asString();
  }

  public String getRequestWithBasicAuthAndExtractJsonPath(String uri, String jsonPath, String userName,
    String password) {
    Response response = getRequestWithBasicAuth(uri, userName, password);
    ltLogger.info("Response body: {}", response.body().asString());
    String fieldValue = response.jsonPath().get(jsonPath).toString();
    ltLogger.info("Field Value: {}", fieldValue);
    return fieldValue;
  }

}
