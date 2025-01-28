package utility;

public class ScenarioUtils {
  public static Object[][] repeatScenarios(Object[][] originalScenarios, int runNTimes) {
    int totalScenarios = originalScenarios.length;
    Object[][] newScenarios = new Object[totalScenarios * runNTimes][];

    int index = 0;
    for (Object[] originalScenario : originalScenarios) {
      Object cucumberPickle = originalScenario[0];
      Object featureWrapperImpl = originalScenario[1];
      for (int j = 0; j < runNTimes; j++) {
        newScenarios[index++] = new Object[] { cucumberPickle, featureWrapperImpl };
      }
    }
    return newScenarios;
  }
}
