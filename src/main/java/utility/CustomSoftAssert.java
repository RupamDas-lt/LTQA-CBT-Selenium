package utility;

import factory.SoftAssertionMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import java.util.Map;

public class CustomSoftAssert extends SoftAssert {
  private final Logger ltLogger = LogManager.getLogger(CustomSoftAssert.class);
  private static final String KEY = "key";
  private static final String MESSAGE_TEMPLATE = "message_template";

  public String softAssertMessageFormat(SoftAssertionMessages messageWithPlaceHolders, Object... args) {
    String hashKey = BaseClass.stringToSha256Hex(messageWithPlaceHolders.getValue());
    String message = String.format(messageWithPlaceHolders.getValue(), args);
    EnvSetup.ASSERTION_ERROR_TO_HASH_KEY_MAP.get()
      .put(message, Map.of(KEY, hashKey, MESSAGE_TEMPLATE, messageWithPlaceHolders.getValue()));
    return message;
  }

  private void pushCustomFailureDataToThreadLocal(String message) {
    Map<String, String> hashKeyToMessageTemplateMap = EnvSetup.ASSERTION_ERROR_TO_HASH_KEY_MAP.get()
      .getOrDefault(message, null);
    if (hashKeyToMessageTemplateMap == null) {
      ltLogger.error("No hash key found for the message: {}", message);
      EnvSetup.FAILED_ASSERTION_ERROR_TO_HASH_KEY_MAP.get().put(message, "NA");
    } else {
      ltLogger.info("Hash key found for the message: {}. Map: {}", message, hashKeyToMessageTemplateMap);
      EnvSetup.FAILED_ASSERTION_ERROR_TO_HASH_KEY_MAP.get().put(message, hashKeyToMessageTemplateMap.get(KEY));
    }
  }

  public void fail(final String message) {
    pushCustomFailureDataToThreadLocal(message);
    super.fail(message);
  }

  private void customAssertTrue(boolean condition, String message) {
    if (!condition) {
      pushCustomFailureDataToThreadLocal(message);
    }
    Assert.assertTrue(condition, message);
  }

  private void customAssertFalse(boolean condition, String message) {
    if (condition) {
      pushCustomFailureDataToThreadLocal(message);
    }
    Assert.assertFalse(condition, message);
  }

  private static boolean areEqualImpl(Object actual, Object expected) {
    if (expected == null && actual == null) {
      return true;
    } else if (expected != null && actual != null) {
      return expected.equals(actual) && actual.equals(expected);
    } else {
      return false;
    }
  }

  private void customAssertEquals(Object actual, Object expected, String message) {
    boolean equal = areEqualImpl(actual, expected);
    if (!equal) {
      pushCustomFailureDataToThreadLocal(message);
    }
    Assert.assertEquals(actual, expected, message);
  }

  public void assertTrue(final boolean condition, final String message) {
    doAssert(new SimpleAssert<Boolean>(condition, Boolean.TRUE, message) {
      public void doAssert() {
        customAssertTrue(condition, message);
      }
    });
  }

  public void assertFalse(final boolean condition, final String message) {
    this.doAssert(new SimpleAssert<Boolean>(condition, Boolean.FALSE, message) {
      public void doAssert() {
        customAssertFalse(condition, message);
      }
    });
  }

  public void assertEquals(final String actual, final String expected, final String message) {
    this.doAssert(new SimpleAssert<String>(actual, expected, message) {
      public void doAssert() {
        customAssertEquals(actual, expected, message);
      }
    });
  }

  private abstract static class SimpleAssert<T> implements IAssert<T> {
    private final T actual;
    private final T expected;
    private final String m_message;

    public SimpleAssert(T actual, T expected) {
      this(actual, expected, null);
    }

    public SimpleAssert(T actual, T expected, String message) {
      this.actual = actual;
      this.expected = expected;
      this.m_message = message;
    }

    public String getMessage() {
      return this.m_message;
    }

    public T getActual() {
      return this.actual;
    }

    public T getExpected() {
      return this.expected;
    }

    public abstract void doAssert();
  }
}
