package factory;

import lombok.Getter;
import utility.BaseClass;

@Getter
public enum SoftAssertionMessagesAccessibility implements BaseClass.MessageHolder {

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

    ERROR_IN_STARTING_TEST("Test is not getting Started for %s"),
    ERROR_IN_SCANNING("Scan is not working for %s"),
    ERROR_IN_TEST_SAVING("Test has not been saved"),
    ERROR_IN_TAB_SWITCHING("Test screen is not switched to %s"),
    ERROR_IN_APP_INSTALLING("App is not getting installed"),
    ERROR_IN_GALLERY_VERIFICATION("Incorrect number of %s are getting generated i.e: "),
    ERROR_IN_DEVICE_ROTATION("Device not rotated"),
    ERROR_IN_ENDING_TEST("%s Test not Ended Successfully"),
    ERROR_IN_OPENING_PAGE("%s page is not opened"),
    REPORT_NOT_VISIBLE("%s %s is not visible");
    private final String value;

    SoftAssertionMessagesAccessibility(String value) {
        this.value = value;
    }
}
