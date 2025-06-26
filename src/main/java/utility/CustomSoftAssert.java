package utility;

import org.testng.Assert;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

public class CustomSoftAssert extends SoftAssert {

    public void fail(final String message) {
        new BaseClass().pushCustomFailureDataToThreadLocal(message);
        super.fail(message);
    }

    private void customAssertTrue(boolean condition, String message) {
        if (!condition) {
            new BaseClass().pushCustomFailureDataToThreadLocal(message);
        }
        Assert.assertTrue(condition, message);
    }

    private void customAssertFalse(boolean condition, String message) {
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

    private void customAssertEquals(Object actual, Object expected, String message) {
        boolean equal = areEqualImpl(actual, expected);
        if (!equal) {
            new BaseClass().pushCustomFailureDataToThreadLocal(message);
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
