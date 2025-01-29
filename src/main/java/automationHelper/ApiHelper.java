package automationHelper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;

import java.util.Map;
import java.util.function.Function;

import static utility.FrameworkConstants.*;

public class ApiHelper extends BaseClass {
  private final Logger ltLogger = LogManager.getLogger(ApiHelper.class);

  public Response httpMethod(String method, String uri, String body, ContentType contentType,
    Map<String, Object> headers, Map<String, Object> queryParam, int expectedStatus) {
    ltLogger.info(
      "Hitting Method: {} on URI: {} with Body: {}, Headers: {}, Query Param: {} and Content Type: {} and Expected Status is: {}",
      method, uri, body, headers, queryParam, contentType, expectedStatus);

    RequestSpecification req = RestAssured.given().headers(headers != null ? headers : Map.of())
      .queryParams(queryParam != null ? queryParam : Map.of()).body(body != null ? body : "")
      .contentType(contentType != null ? contentType : ContentType.JSON);

    Map<String, Function<RequestSpecification, Response>> methodMap = Map.of(GET,
      r -> r.get(uri).then().statusCode(expectedStatus).extract().response(), POST,
      r -> r.post(uri).then().statusCode(expectedStatus).extract().response(), PUT,
      r -> r.put(uri).then().statusCode(expectedStatus).extract().response(), DELETE,
      r -> r.delete(uri).then().statusCode(expectedStatus).extract().response(), PATCH,
      r -> r.patch(uri).then().statusCode(expectedStatus).extract().response(), GET_REDIRECT,
      r -> r.redirects().follow(false).get(uri).then().extract().response(), GET_WITHOUT_STATUS_CODE_VERIFICATION,
      RequestSpecification::get);

    return methodMap.getOrDefault(method, reqType -> {
      throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }).apply(req);
  }

}
