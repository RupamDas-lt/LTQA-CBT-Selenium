package automationHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;
import utility.EnvSetup;
import utility.FrameworkConstants;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.TEST_TAGS;

public class MultipleAutomationSessionsHelper extends BaseClass {
  private static final Logger ltLogger = LogManager.getLogger(MultipleAutomationSessionsHelper.class);
  private static final int DEFAULT_CONCURRENCY_LIMIT = 5;
  private static final int SESSION_TIMEOUT_SECONDS = 300;

  /// Tracks success/failure counts and execution time for concurrent sessions
  private static class SessionMetrics {
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final long startTime = System.currentTimeMillis();

    public void incrementSuccess() {
      successCount.incrementAndGet();
    }

    public void incrementFailure() {
      failureCount.incrementAndGet();
    }

    public void logMetrics() {
      long duration = System.currentTimeMillis() - startTime;
      ltLogger.info("Session execution metrics - Success: {}, Failures: {}, Duration: {}ms", successCount.get(),
        failureCount.get(), duration);
    }
  }

  /// Main method to execute multiple test sessions concurrently with specified capabilities
  public static void runMultipleConcurrentSessions(int numberOfSessions, boolean quitTestDriver, String testCapability,
    String testActions, int... customConcurrencyLimit) {
    validateInputs(numberOfSessions, testCapability, testActions);

    int concurrencyLimit = calculateConcurrencyLimit(customConcurrencyLimit);
    SessionMetrics metrics = new SessionMetrics();

    try (ExecutorService executor = Executors.newFixedThreadPool(concurrencyLimit)) {
      HashMap<String, HashMap<FrameworkConstants.testVerificationDataKeys, Object>> results = new HashMap<>();
      Map<String, Map<String, Object>> capsForEachSession = new HashMap<>();
      Map<String, Object> mainThreadContext = getThreadLocalSnapshot();

      List<Future<?>> futures = submitSessions(executor, numberOfSessions, quitTestDriver, testCapability, testActions,
        results, capsForEachSession, mainThreadContext, metrics);

      waitForCompletion(futures);
      processResults(results, capsForEachSession);
      metrics.logMetrics();
    } catch (Exception e) {
      ltLogger.error("Failed to execute concurrent sessions", e);
      throw new RuntimeException("Failed to execute concurrent sessions", e);
    }
  }

  /// Validates input parameters for session execution
  private static void validateInputs(int numberOfSessions, String testCapability, String testActions) {
    if (numberOfSessions <= 0) {
      throw new IllegalArgumentException("Number of sessions must be positive");
    }
    if (testCapability == null || testCapability.trim().isEmpty()) {
      throw new IllegalArgumentException("Test capability cannot be null or empty");
    }
    if (testActions == null || testActions.trim().isEmpty()) {
      throw new IllegalArgumentException("Test actions cannot be null or empty");
    }
  }

  /// Determines the number of concurrent sessions based on input or default limit
  private static int calculateConcurrencyLimit(int[] customConcurrencyLimit) {
    return customConcurrencyLimit.length > 0 ? Math.max(1, customConcurrencyLimit[0]) : DEFAULT_CONCURRENCY_LIMIT;
  }

  /// Submits individual test sessions to the thread pool for concurrent execution
  private static List<Future<?>> submitSessions(ExecutorService executor, int numberOfSessions, boolean quitTestDriver,
    String testCapability, String testActions,
    Map<String, HashMap<FrameworkConstants.testVerificationDataKeys, Object>> results,
    Map<String, Map<String, Object>> capsForEachSession, Map<String, Object> mainThreadContext,
    SessionMetrics metrics) {

    List<Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < numberOfSessions; i++) {
      String finalTestCapability = addSpecificSessionIdentifierInCaps(testCapability, i);
      futures.add(executor.submit(
        () -> runSession(quitTestDriver, finalTestCapability, testActions, results, capsForEachSession,
          mainThreadContext, metrics)));
    }
    return futures;
  }

  private static String addSpecificSessionIdentifierInCaps(String testCapability, int sessionIndex) {
    // Get the scenario name, defaulting to "default_scenario" if not set
    String scenarioName = Optional.ofNullable(TEST_SCENARIO_NAME.get()).orElse("default_scenario");

    // Truncate scenario name if it exceeds 200 characters
    scenarioName = scenarioName.length() > 200 ? scenarioName.substring(0, 200) : scenarioName;

    // Create the execution value
    String textExecutionValue = "session_index_" + sessionIndex;

    // Append the tags if they are not already present in the capability
    if (!testCapability.contains(TEST_TAGS + "=")) {
      return testCapability + "," + TEST_TAGS + "=[" + scenarioName + "," + textExecutionValue + "]";
    }

    return testCapability;  // Return original capability if tags are already present
  }

  /// Executes a single test session with the specified capabilities and actions
  private static void runSession(boolean quitTestDriver, String testCapability, String testActions,
    Map<String, HashMap<FrameworkConstants.testVerificationDataKeys, Object>> results,
    Map<String, Map<String, Object>> capsForEachSession, Map<String, Object> mainThreadContext,
    SessionMetrics metrics) {

    AutomationHelper automationHelper = new AutomationHelper();
    restoreThreadLocalSnapshot(mainThreadContext);

    try {
      automationHelper.startSessionWithSpecificCapabilities(quitTestDriver, testCapability, testActions);
      String sessionId = EnvSetup.TEST_SESSION_ID.get();
      if (sessionId != null) {
        results.put(sessionId, new HashMap<>(EnvSetup.TEST_VERIFICATION_DATA.get()));
        capsForEachSession.put(sessionId, new HashMap<>(EnvSetup.TEST_CAPS_MAP.get()));
        metrics.incrementSuccess();
      }
    } catch (Exception e) {
      metrics.incrementFailure();
      ltLogger.error("Failed to execute session", e);
    }
  }

  /// Waits for all submitted sessions to complete with timeout
  private static void waitForCompletion(List<Future<?>> futures) {
    futures.forEach(f -> {
      try {
        f.get(SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
        ltLogger.error("Session execution timed out", e);
      } catch (Exception e) {
        ltLogger.error("Session execution failed", e);
      }
    });
  }

  /// Processes and stores the results from all completed sessions
  private static void processResults(
    HashMap<String, HashMap<FrameworkConstants.testVerificationDataKeys, Object>> results,
    Map<String, Map<String, Object>> capsForEachSession) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      ltLogger.info("Accumulated results for all sessions: {}", objectMapper.writeValueAsString(results));
      ltLogger.info("Accumulated test capabilities for all sessions: {}",
        objectMapper.writeValueAsString(capsForEachSession));

      MULTIPLE_TEST_VERIFICATION_DATA.set(results);
      MULTIPLE_TEST_CAPS_MAP.set(capsForEachSession);

      results.keySet().forEach(sessionId -> TEST_SESSION_ID_QUEUE.get().add(sessionId));
    } catch (Exception e) {
      ltLogger.error("Failed to process results", e);
      throw new RuntimeException("Failed to process results", e);
    }
  }

  /// Creates a snapshot of the current thread's context for session execution
  private static Map<String, Object> getThreadLocalSnapshot() {
    try {
      Map<String, Object> snapshot = new HashMap<>();
      snapshot.put("TEST_VERIFICATION_DATA", new HashMap<>(TEST_VERIFICATION_DATA.get()));
      snapshot.put("TEST_REPORT", new HashMap<>(TEST_REPORT.get()));
      snapshot.put("USERNAME", testUserName.get());
      snapshot.put("ACCESS_KEY", testAccessKey.get());
      snapshot.put("EMAIL", testEmail.get());
      snapshot.put("PASSWORD", testPassword.get());
      snapshot.put("GRID_URL", testGridUrl.get());
      return snapshot;
    } catch (Exception e) {
      ltLogger.error("Failed to create thread local snapshot", e);
      return new HashMap<>();
    }
  }

  /// Restores the thread context from a previously created snapshot
  private static void restoreThreadLocalSnapshot(Map<String, Object> snapshot) {
    if (snapshot == null) {
      return;
    }
    try {
      testUserName.set((String) snapshot.get("USERNAME"));
      testAccessKey.set((String) snapshot.get("ACCESS_KEY"));
      testEmail.set((String) snapshot.get("EMAIL"));
      testPassword.set((String) snapshot.get("PASSWORD"));
      testGridUrl.set((String) snapshot.get("GRID_URL"));

      if (snapshot.get("TEST_VERIFICATION_DATA") instanceof Map) {
        @SuppressWarnings("unchecked") Map<FrameworkConstants.testVerificationDataKeys, Object> verificationData = (Map<FrameworkConstants.testVerificationDataKeys, Object>) snapshot.get(
          "TEST_VERIFICATION_DATA");
        TEST_VERIFICATION_DATA.set(new HashMap<>(verificationData));
      }

      if (snapshot.get("TEST_REPORT") instanceof Map) {
        @SuppressWarnings("unchecked") Map<String, Map<String, String>> testReport = (Map<String, Map<String, String>>) snapshot.get(
          "TEST_REPORT");
        TEST_REPORT.set(new HashMap<>(testReport));
      }
    } catch (Exception e) {
      ltLogger.error("Failed to restore thread local snapshot", e);
    }
  }
}