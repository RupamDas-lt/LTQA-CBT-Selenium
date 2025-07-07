package utility;

import factory.Locator;
import factory.LocatorTypes;

public class UrlsAndLocators {
    public static final String GOOGLE_URL = "https://www.google.com/";

    public static final String BASIC_AUTH = "https://admin:admin@the-internet.herokuapp.com/basic_auth";
    public static final String BASIC_AUTH_URL_WITHOUT_AUTH_HEADERS = "https://the-internet.herokuapp.com/basic_auth";
    public static final Locator basicAuthHeading = new Locator(LocatorTypes.CSS, "#content h3");

    public static final String LOCAL_URL = "http://localhost:8000/";
    public static final String LOCAL_LAMBDA_URL = "http://locallambda.com:8000/"; // Add entry for locallambda in etc/hosts and point it to 127.0.0.1
    public static final String LOCAL_PRIVATE_PROXY_URL = "http://privateproxy.network.com:8000/"; // Add entry for privateproxy.network in etc/hosts and point it to 127.0.0.1
    public static final String LOCAL_IOS = "http://localhost.lambdatest.com:8000/";
    public static final Locator localUrlHeading = new Locator(LocatorTypes.XPATH,
            "//h1[contains(text(),'Directory listing for')]");

    public static final String SELF_SIGNED_URL = "https://self-signed.badssl.com/";
    public static final String SELF_SIGNED_URL_2 = "https://untrusted-root.badssl.com/";
    public static final Locator selfSignedPageHeading = new Locator(LocatorTypes.CSS, "#content > h1:nth-child(1)");
    public static final String SELF_SIGNED_URL_FALLBACK = "https://expired-rsa-dv.ssl.com/?_gl=1*jiqbno*_gcl_au*MTAzODU4MTU3MC4xNzM2MjI3MDkx";
    public static final Locator selfSignedFallbackPageHeading = new Locator(LocatorTypes.TAG_NAME, "h1");

    public static final String HEROKU_APP_LOGIN_PAGE = "https://the-internet.herokuapp.com/login";
    public static final Locator herokuLoginPageHeading = new Locator(LocatorTypes.CSS, "[class='subheader']");
    public static final Locator herokuLoginPageUsernameInput = new Locator(LocatorTypes.ID, "username");
    public static final Locator herokuLoginPagePasswordInput = new Locator(LocatorTypes.ID, "password");
    public static final Locator herokuLoginPageLoginButton = new Locator(LocatorTypes.CSS, "button[type='submit']");
    public static final Locator herokuAfterLoginPageHeading = new Locator(LocatorTypes.ID, "flash");

    public static final String TODO_APP_URL = "https://lambdatest.github.io/sample-todo-app";
    public static final Locator todoListItem1 = new Locator(LocatorTypes.NAME, "li1");
    public static final Locator todoListItem2 = new Locator(LocatorTypes.NAME, "li2");
    public static final Locator todoListItem3 = new Locator(LocatorTypes.NAME, "li3");
    public static final Locator todoListItem4 = new Locator(LocatorTypes.NAME, "li4");
    public static final Locator todoListItem5 = new Locator(LocatorTypes.NAME, "li5");
    public static final Locator todoInput = new Locator(LocatorTypes.XPATH, "//*[@id='sampletodotext']");
    public static final Locator todoAddButton = new Locator(LocatorTypes.ID, "addbutton");
    public static final Locator todoNewEnteredText = new Locator(LocatorTypes.XPATH,
            "/html/body/div/div/div/ul/li[6]/span");

    public static final String BROWSER_DETAILS_URL = "https://www.whatismybrowser.com/";
    public static final Locator browserDetailsText = new Locator(LocatorTypes.CSS,
            "#primary-browser-detection-backend .string-major a");

    public static final String FILE_UPLOAD_URL = "https://the-internet.herokuapp.com/upload";
    public static final Locator chooseFileButton = new Locator(LocatorTypes.ID, "file-upload");
    public static final Locator uploadFileButton = new Locator(LocatorTypes.ID, "file-submit");
    public static final Locator uploadedFileHeading = new Locator(LocatorTypes.XPATH,
            "//h3[contains(text(),'File Uploaded!')]");

    public static final String GEOLOCATION_VERIFICATION_URL = "https://geotargetly.com/my-ip-geolocation";
    public static final Locator countryCode = new Locator(LocatorTypes.ID, "geotargetly_country_code");
    public static final Locator countryName = new Locator(LocatorTypes.ID, "geotargetly_country_name");

    public static final String INTERNET_HEROKU_APP_ABTEST = "https://the-internet.herokuapp.com/abtest";
    public static final Locator abtestHeadingIHA = new Locator(LocatorTypes.TAG_NAME, "h3");

    public static final String INTERNET_HEROKU_APP_ADD_REMOVE_ELEMENT_URL = "https://the-internet.herokuapp.com/add_remove_elements/";
    public static final Locator addElementButtonIHA = new Locator(LocatorTypes.CSS, "[onclick='addElement()']");
    public static final Locator deleteElementButtonIHA = new Locator(LocatorTypes.CSS, "[onclick='deleteElement()']");

    public static final String INTERNET_HEROKU_APP_BROKEN_IMAGES_URL = "https://the-internet.herokuapp.com/add_remove_elements/";
    public static final Locator brokenImagesIHA = new Locator(LocatorTypes.TAG_NAME, "img");

    public static final String INTERNET_HEROKU_APP_CHECK_BOXES_URL = "https://the-internet.herokuapp.com/checkboxes";
    public static final Locator checkboxesIHA = new Locator(LocatorTypes.CSS, "input:first-child");

    public static final String INTERNET_HEROKU_APP_DYNAMIC_CONTENT_URL = "https://the-internet.herokuapp.com/dynamic_content";
    public static final Locator dynamicContentClickIHA = new Locator(LocatorTypes.CSS, "p a");
    public static final Locator staticParagraphIHA = new Locator(LocatorTypes.CSS,
            "#content > div:first-child.row > div.large-10");

    public static final String INTERNET_HEROKU_APP_DYNAMIC_CONTROLS_URL = "https://the-internet.herokuapp.com/dynamic_controls";
    public static final Locator checkBoxSwapButtonIHA = new Locator(LocatorTypes.CSS, "[onclick='swapCheckbox()']");
    public static final Locator textBoxEnableButtonIHA = new Locator(LocatorTypes.CSS, "[onclick='swapInput()']");
    public static final Locator messageInDynamicControlsPageIHA = new Locator(LocatorTypes.CSS, "#message");

    public static final String INTERNET_HEROKU_APP_LOGIN_PAGE_URL = "https://the-internet.herokuapp.com/login";
    public static final Locator userNameInputTHA = new Locator(LocatorTypes.ID, "username");
    public static final Locator passwordInputTHA = new Locator(LocatorTypes.ID, "password");
    public static final Locator loginButtonTHA = new Locator(LocatorTypes.CSS, ".radius");
    public static final Locator loginSuccessHeaderTHA = new Locator(LocatorTypes.CSS, ".subheader");
    public static final Locator logoutButtonTHA = new Locator(LocatorTypes.CSS, ".button");
    public static final Locator loginPageHeadingTHA = new Locator(LocatorTypes.TAG_NAME, "h2");

    public static final String LT_LOGIN_URL = EnvSetup.TEST_ENV.contains("stage") ?
            "https://stage-accounts.lambdatestinternal.com/login" :
            "https://accounts.lambdatest.com/login";
    public static final Locator ltPageHeading = new Locator(LocatorTypes.CSS, "[aria-label*='LambdaTest']");
    public static final Locator ltLoginPageEmailInput = new Locator(LocatorTypes.ID, "email");
    public static final Locator ltLoginPagePasswordInput = new Locator(LocatorTypes.ID, "password");
    public static final Locator ltLoginPageSubmitButton = new Locator(LocatorTypes.ID, "login-button");
    public static final Locator ltLoginSuccessVerification = new Locator(LocatorTypes.XPATH,
            "//*[@id='profile__dropdown__parent']|//*[@id='profile__dropdown']|//div[@role='dialog' and .//*[text()='How would you like to use LambdaTest?' or text()='Check Your Email']]|//*[@name='authentication_code']|//button[text()='Setup MFA']");

    public static final String IP_INFO_IO_URL = "https://ipinfo.io/what-is-my-ip";
    public static final Locator ipInfoIOIP = new Locator(LocatorTypes.CSS, "[class*='main-content'] h1");
    public static final Locator ipInfoIOLocation = new Locator(LocatorTypes.CSS, "[class*='main-content'] h2[class*=h5]");

    public static final String CHROME_BROWSER_VERSION_DETAILS_URL = "chrome://version/";
    public static final String FIREFOX_BROWSER_VERSION_DETAILS_URL = "about:profiles";
    public static final Locator chromeBrowserProfilePath = new Locator(LocatorTypes.CSS, "#profile_path");

    public static final String DOWNLOAD_SAMPLE_DOC_FILE_URL = "https://filesamples.com/samples/document/docx/sample1.docx";
    public static final String DOWNLOAD_SAMPLE_TXT_FILE_URL = "https://www.learningcontainer.com/download/sample-text-file/?wpdmdl=1669&refresh=61abbc77584a51638644855";
    public static final String MAC_LT_USER_DIRECTORY_URL_FORMAT = "file:///Users/ltuser/%s";
    public static final String WIN_LT_USER_DIRECTORY_URL_FORMAT = "file:///C:/Users/ltuser/%s";
    public static final Locator fileInLtUserDirectoryLocatorFormat = new Locator(LocatorTypes.CSS,
            "td[sortable-data*='%s']");

    public static final String CROSS_SITE_BLOCKING_CHECK_URL = "https://alanhogan.github.io/web-experiments/3rd/third-party-cookies.html";
    public static final Locator crossSiteBlockingCheckHeading = new Locator(LocatorTypes.TAG_NAME, "h1");
    public static final Locator crossSiteBlockingResult = new Locator(LocatorTypes.ID, "3rd_party_cookie_test_results");
    public static final Locator crossSiteBlockingError = new Locator(LocatorTypes.ID, "3rd_party_cookie_test_error");

    public static final String FAKE_MEDIA_CHECK_URL = "https://webcamtests.com/check";
    public static final Locator fakeMediaCheckPageLoadingState = new Locator(LocatorTypes.XPATH, "//ul//li[contains(text(),'Detecting your media devices. Please wait...')]");
    public static final Locator webcamAccessBlockedNotification = new Locator(LocatorTypes.CLASS_NAME, "notice-injected");
    public static final Locator webcamSelectorDropDown = new Locator(LocatorTypes.ID, "webcam-selecter");

    public static final String BROWSER_LANGUAGE_CHECK_URL = "https://manytools.org/http-html-text/browser-language/";
    public static final Locator browserLanguage = new Locator(LocatorTypes.CSS, "tr:nth-of-type(2) td:nth-of-type(1)");

    public static final String CHECK_WSS_URL = "https://www.piesocket.com/websocket-tester";
    public static final Locator checkWssPageAcceptConsentButton = new Locator(LocatorTypes.CSS, "button[aria-label='Consent']");
    public static final Locator checkWssConnectDisconnectButton = new Locator(LocatorTypes.CSS, "[type='submit']");
    public static final Locator checkWssConnectionInputField = new Locator(LocatorTypes.CSS, "[name='email']");
    public static final Locator checkWssConnectionSendMessageButton = new Locator(LocatorTypes.XPATH, "(//button[@type='submit'])[1]");
    public static final Locator checkWssConnectionStatusText = new Locator(LocatorTypes.CSS, ".text-lg.text-white span");
    public static final Locator checkWssConnectionConsoleLog = new Locator(LocatorTypes.ID, "consoleLog");

    /// ---- Auto heal tests urls and locators, BaseLine locators are the original which will be mocked ----
    public static final String CHALLENGING_DOM_PAGE_URL = "https://the-internet.herokuapp.com/challenging_dom";
    public static final Locator challengingDomPageHeading = new Locator(LocatorTypes.CSS, ".example>h3");
    public static final Locator challengingDomPageWorkOption = new Locator(LocatorTypes.CSS,
            "tbody>tr:nth-of-type(1)>td:nth-of-type(2)");
    public static final String AUTO_HEAL_TO_DO_LIST_SAMPLE_URL = "https://rupamdas-ts.github.io/ToDoList/";
    public static final Locator autoHealToDoListSampleHeading = new Locator(LocatorTypes.CSS, "h1");
    public static final Locator autoHealToDoListSampleInput = new Locator(LocatorTypes.ID, "input");
    public static final Locator autoHealToDoListSampleAddButton = new Locator(LocatorTypes.CSS, "#add-button");
    public static final Locator autoHealToDoListSampleListItemForBaseLine = new Locator(LocatorTypes.XPATH,
            "//*[@class='custom-class']");
    public static final Locator autoHealToDoListSampleListItem = new Locator(LocatorTypes.XPATH,
            "//*[@class=\"custom-class-for-test\" or @class=\"list\"]/../../..//*[@class=\"custom-class-for-test\" or @class=\"list\"]/../../..//*[@class=\"custom-class-for-test\" or @class=\"list\"]/../../..//*[@class=\"custom-class-for-test\" or @class=\"list\"]/../../..//*[@class=\"custom-class-for-test\" or @class=\"list\"]/li/li/label");
    public static final String AUTO_HEAL_IMDB_SAMPLE_URL = "https://rupamdas-ts.github.io/IMDb-Clone/";
    public static final Locator autoHealImdbFavButton = new Locator(LocatorTypes.ID, "favourite-button");
    public static final Locator autoHealImdbInput = new Locator(LocatorTypes.TAG_NAME, "input");
    public static final Locator autoHealImdbSearchResultList = new Locator(LocatorTypes.ID, "searchList");
    public static final Locator autoHealImdbListItemBaseLine = new Locator(LocatorTypes.CSS,
            "#searchList .custom-class-li:nth-of-type(1)");
    public static final Locator autoHealImdbListItem = new Locator(LocatorTypes.CSS,
            "#searchList .custom-class-for-test-li:nth-of-type(1) span");
    public static final Locator autoHealImdbAddFavButtonBaseLine = new Locator(LocatorTypes.CSS,
            "#searchList .custom-class-for-test-li:nth-of-type(1) .custom-class-button");
    public static final Locator autoHealImdbAddFavButton = new Locator(LocatorTypes.CSS,
            "#searchList .custom-class-for-test-li:nth-of-type(1) .custom-class-for-test-button");
    public static final Locator autoHealImdbFavList = new Locator(LocatorTypes.XPATH,
            "//h5[contains(@class,'custom-class-title')]");
    public static final Locator autoHealImdbFavListItemBaseLine = new Locator(LocatorTypes.XPATH,
            "(//h5[contains(@class,'custom-class-title')])[1]");
    public static final Locator autoHealImdbFavListItem = new Locator(LocatorTypes.XPATH,
            "(//h5[contains(@class,'custom-class-for-test-title')])[1]");
}
