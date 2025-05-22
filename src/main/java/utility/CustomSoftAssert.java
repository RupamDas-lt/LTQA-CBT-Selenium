package utility;

import factory.SoftAssertionMessages;
import org.testng.asserts.SoftAssert;

import java.util.Map;

public class CustomSoftAssert extends SoftAssert {
  public String softAssertMessageFormat(SoftAssertionMessages messageWithPlaceHolders, Object... args) {
    String hashKey = BaseClass.stringToSha256Hex(messageWithPlaceHolders.getValue());
    String message = String.format(messageWithPlaceHolders.getValue(), args);
    EnvSetup.ASSERTION_ERROR_TO_HASH_KEY_MAP.get().put(message, Map.of(hashKey, messageWithPlaceHolders));
    return message;
  }
}
