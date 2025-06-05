package utility;

import org.testng.Assert;

public class CustomAssert {
  public static void assertTrue(boolean condition, String message) {
    if (!condition) {
      new BaseClass().pushCustomFailureDataToThreadLocal(message);
    }
    Assert.assertTrue(condition, message);
  }

  public static void assertFalse(boolean condition, String message) {
    if (condition) {
      new BaseClass().pushCustomFailureDataToThreadLocal(message);
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

  public static void assertEquals(Object actual, Object expected, String message) {
    if (!areEqualImpl(actual, expected)) {
      new BaseClass().pushCustomFailureDataToThreadLocal(message);
    }
    Assert.assertEquals(actual, expected, message);
  }

  public static void assertNotEquals(Object actual, Object expected, String message) {
    if (areEqualImpl(actual, expected)) {
      new BaseClass().pushCustomFailureDataToThreadLocal(message);
    }
    Assert.assertNotEquals(actual, expected, message);
  }

  public static void fail(String message) {
    new BaseClass().pushCustomFailureDataToThreadLocal(message);
    throw new AssertionError(message);
  }
}
