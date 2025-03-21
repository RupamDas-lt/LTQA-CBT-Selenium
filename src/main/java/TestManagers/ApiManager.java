package TestManagers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;
import utility.CustomSoftAssert;
import utility.EnvSetup;
import utility.FileLockUtility;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static utility.FrameworkConstants.*;

public abstract class ApiManager extends BaseClass {
  private final Logger ltLogger = LogManager.getLogger(ApiManager.class);

  public String constructAPIUrl(String uriBase, String endpoint, String... sessionDetails) {
    // In sessionDetails, first param should always be session id and then the session API end points
    int sessionDetailsLength = sessionDetails.length;
    String url = switch (sessionDetailsLength) {
      case 1 -> HTTPS + uriBase + endpoint + sessionDetails[0];
      case 2 -> HTTPS + uriBase + endpoint + sessionDetails[0] + sessionDetails[1];
      default -> HTTPS + uriBase + endpoint;
    };
    ltLogger.info("URL: {}", url);
    return url;
  }

  public String constructAPIUrlWithBasicAuth(String uriBase, String endpoint, String username, String password,
    String... sessionDetails) {
    ltLogger.info("Constructing API with Base: {}, Endpoint: {}, Username: {}, Password: {}, Session details: {}",
      uriBase, endpoint, username, password, sessionDetails);
    String uriBaseWithBasicAuth = username + ":" + password + "@" + uriBase;
    return constructAPIUrl(uriBaseWithBasicAuth, endpoint, sessionDetails);
  }

  private Response httpMethod(String method, String uri, Object body, ContentType contentType,
    Map<String, Object> headers, Map<String, Object> queryParam, int expectedStatus, String... basicAuthHeaders) {

    String username = basicAuthHeaders.length > 0 ? basicAuthHeaders[0] : "";
    String password = basicAuthHeaders.length > 1 ? basicAuthHeaders[1] : "";

    ltLogger.info(
      "Hitting Method: {} on URI: {} with Body: {}, Headers: {}, Query Param: {}, Content Type: {}, Expected Status is: {}, Username is: {} and Password is: {}",
      method, uri, body, headers, queryParam, contentType, expectedStatus, username, password);

    if (!username.isEmpty() || !password.isEmpty()) {
      if (headers == null) {
        headers = new HashMap<>();
      }
      String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
      ltLogger.info("Encoded Auth Headers: {}", encodedAuth);
      headers.put("Authorization", "Basic " + encodedAuth);
      ltLogger.info("Updated Request Headers: {}", headers);
    }

    RequestSpecification req = RestAssured.given().headers(headers != null ? headers : Map.of())
      .queryParams(queryParam != null ? queryParam : Map.of())
      .contentType(contentType != null ? contentType : ContentType.JSON);

    // Handle multipart requests
    if (contentType == ContentType.MULTIPART && body instanceof RequestSpecification) {
      req = ((RequestSpecification) body).headers(headers != null ? headers : Map.of());
    } else {
      req.body(body != null ? body : "");
    }

    boolean verifyStatusCode = !method.endsWith("_WITHOUT_STATUS_CODE_VERIFICATION");

    Map<String, Function<RequestSpecification, Response>> methodMap = Map.of(GET, r -> r.get(uri), POST,
      r -> r.post(uri), PUT, r -> r.put(uri), DELETE, r -> r.delete(uri), PATCH, r -> r.patch(uri), GET_REDIRECT,
      r -> r.redirects().follow(false).get(uri));

    Function<RequestSpecification, Response> action = methodMap.getOrDefault(
      method.replace("_WITHOUT_STATUS_CODE_VERIFICATION", ""), reqType -> {
        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
      });

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

  public Response patchRequestWithBasicAuth(String uri, String username, String password, Object body) {
    return httpMethod(PATCH_WITHOUT_STATUS_CODE_VERIFICATION, uri, body, ContentType.JSON, null, null, 0, username,
      password);
  }

  public Response putRequestWithURLEncoding(String uri, HashMap<String, Object> body) {
    RestAssured.urlEncodingEnabled = false;
    return RestAssured.given().body(body).contentType(ContentType.JSON).put(uri).then().statusCode(200).extract()
      .response();
  }

  public Response postRequestWithBasicAuth(String uri, HashMap<String, Object> body, String username, String password) {
    ltLogger.info("POST Request body: {}", body);
    ContentType contentType = ContentType.JSON; // Default content type
    if (body.containsKey("contentType")) {
      contentType = ContentType.fromContentType((String) body.get("contentType"));
      body.remove("contentType");
    }

    // Prepare the request body based on content type
    Object requestBody = body;
    if (contentType == ContentType.MULTIPART) {
      RequestSpecification multiPartRequest = RestAssured.given();
      for (Map.Entry<String, Object> entry : body.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        if (value instanceof File) {
          multiPartRequest.multiPart(key, (File) value);
        } else {
          multiPartRequest.multiPart(key, value.toString());
        }
      }
      requestBody = multiPartRequest;
    }

    return httpMethod(POST, uri, requestBody, contentType, null, null, 200, username, password);
  }

  public Response getRequest(String uri) {
    return httpMethod(GET_WITHOUT_STATUS_CODE_VERIFICATION, uri, null, ContentType.JSON, null, null, 0);
  }

  public Response putRequest(String uri, Object... body) {
    Object requestBody = body.length == 1 ? body[0] : null;
    return httpMethod(PUT_WITHOUT_STATUS_CODE_VERIFICATION, uri, requestBody, ContentType.JSON, null, null, 0);
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

  public void fetchDataAndWriteResponseToFile(String uri, String filePath) {
    FileLockUtility.fileLock.lock();
    try {
      File file = new File(filePath);
      if (file.exists()) {
        ltLogger.info("File already exists: {}", filePath);
        return;
      }
      String responseString = getRequestAsString(uri);
      writeStringToFile(filePath, responseString);
    } catch (Exception e) {
      throw new RuntimeException("Error occurred while writing to file", e);
    } finally {
      FileLockUtility.fileLock.unlock();
    }
  }

  public boolean downloadFile(String uri, String desiredFileName, String filePath, int... expectedRetryCount) {
    int retryCount = expectedRetryCount.length == 0 ? 3 : expectedRetryCount[0];
    String expectedLogFilePath = filePath + desiredFileName;
    boolean success = false;
    ltLogger.info("Downloading file to : {}", expectedLogFilePath);
    for (int i = 1; i <= retryCount; i++) {
      try {
        ltLogger.info("Attempt number: {} > Downloading file from uri: {}", i, uri);
        FileUtils.copyURLToFile(new URL(uri), new File(expectedLogFilePath));
        success = fileExists(filePath, 3, 5);
        ltLogger.info("Download status: {}", success);
        if (success) {
          break;
        }
      } catch (Exception e) {
        ltLogger.error("Unable to copy file from uri: {}", uri, e);
      }
    }
    return success;
  }

  public String downloadFileFromUrlAndExtractContentAsString(String uri, String desiredFileName, String directory) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    uri = uri.replace(" ", "%20");
    ltLogger.info("Downloading file from URI: {} , with name: {} , in directory: {}", uri, desiredFileName, directory);
    boolean fileDownloadStatus = downloadFile(uri, desiredFileName, directory);
    softAssert.assertTrue(fileDownloadStatus, "File not downloaded");
    if (fileDownloadStatus) {
      String logsData;
      try {
        logsData = readFileData(directory + desiredFileName);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      String logsFromDownloadedFile = handleUnicodeEscapes(logsData);
      ltLogger.info("Data downloaded file: {}", logsFromDownloadedFile);
      return logsFromDownloadedFile;
    }
    return null;
  }
}
