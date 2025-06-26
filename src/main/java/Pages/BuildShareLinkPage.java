package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import utility.BaseClass;
import utility.CustomSoftAssert;

import java.util.List;

import static factory.SoftAssertionMessages.*;

public class BuildShareLinkPage extends BaseClass {
    private final Logger ltLogger = LogManager.getLogger(BuildShareLinkPage.class);

    DriverManager driver;
    CustomSoftAssert softAssert;

    private final String SHARE_LINK;
    private static final Locator buildName = new Locator(LocatorTypes.CSS,
            "#testHeader>div:nth-child(1)>div:nth-child(1) span");
    private static final Locator testName = new Locator(LocatorTypes.ID, "testName");
    private static final Locator sideBarTestNames = new Locator(LocatorTypes.CSS,
            "a[class*='SIdebarTestList_testCard'] span[id*='testName']>div");

    public BuildShareLinkPage(DriverManager driverManager, CustomSoftAssert softAssert, String shareLink) {
        driver = driverManager;
        this.softAssert = softAssert;
        SHARE_LINK = shareLink;
    }

    public boolean navigateToShareLink() {
        driver.getURL(SHARE_LINK);
        boolean isSharePageOpened = driver.isDisplayed(testName, 20);
        softAssert.assertTrue(isSharePageOpened,
                softAssertMessageFormat(UNABLE_TO_OPEN_SHARE_PAGE_ERROR_MESSAGE, "build", SHARE_LINK));
        return isSharePageOpened;
    }

    public void verifyBuildName(String expectedBuildName) {
        String actualBuildNameText = driver.getText(buildName);
        softAssert.assertTrue(expectedBuildName.equals(actualBuildNameText),
                softAssertMessageFormat(BUILD_NAME_MISMATCH_CLIENT_ERROR_MESSAGE, "build share page", expectedBuildName,
                        actualBuildNameText));
    }

    public void verifyTestList(String expectedTestName) {
        if (driver.isDisplayed(sideBarTestNames, 20)) {
            List<WebElement> testNamesElements = driver.findElements(sideBarTestNames);
            for (WebElement testNameElement : testNamesElements) {
                String actualTestName = testNameElement.getText();
                if (!actualTestName.contains(expectedTestName)) {
                    softAssert.fail(softAssertMessageFormat(TESTS_LIST_MISMATCH_IN_BUILD_SHARE_PAGE_CLIENT_ERROR_MESSAGE));
                    return;
                }
            }
        } else {
            softAssert.fail(softAssertMessageFormat(TESTS_LIST_MISMATCH_IN_BUILD_SHARE_PAGE_CLIENT_ERROR_MESSAGE));
        }
    }
}
