package factory;

import lombok.Getter;
import utility.BaseClass;

@Getter
public enum SoftAssertionMessagesFalcon implements BaseClass.MessageHolder {

    /*************************************************************
     * If you are adding new values to this enum, please          *
     * follow the below steps:                                    *
     *                                                            *
     * - While raising any PR, make sure you add the tag          *
     *   `change-report-database`.                                *
     * - After this, a commit will be made by the GHA             *
     *   workflow to update the database with the new             *
     *   enum value.                                              *
     * - After the commit is made, make sure you pull the         *
     *   latest code, then update the new values in the           *
     *   `testFailureAnalysis.json` file.                         *
     * - Then commit the changes to the `testFailureAnalysis.json`*
     *   file and push the changes.                               *
     *                                                            *
     * Do not change the values of these enums, as they           *
     * will change the hashcode of the enum and break the         *
     * test reporting.                                            *
     * If you are willing to change the value, make sure          *
     * you follow the same procedure of adding a new              *
     * enum value.                                                *
     ************************************************************/

    SAMPLE_MESSAGE("Sample assertion failure message. Actual value: %s, Expected value: %s");

    private final String value;

    SoftAssertionMessagesFalcon(String value) {
        this.value = value;
    }
}
