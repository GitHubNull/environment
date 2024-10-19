package oxff.org;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import oxff.org.controler.EnviHttpHandler;
import oxff.org.ui.EnvironmentTab;
import oxff.org.ui.PopUpMenu;

public class Environment implements BurpExtension {

    MontoyaApi montoyaApi;
    private Logging logger;
    private final String extensionName = "environment";
    private final String extensionVersion = "1.0";
    private final String extensionAuthor = "oxff";
    private final String extensionWebsite = "https://github.com/oxff/burp-environment";

    public final static String LEFT_MARKER = "{{ ";
    public final static String RIGHT_MARKER = " }}";
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        logger = montoyaApi.logging();
        montoyaApi.extension().setName(extensionName);

        logger.logToOutput("Initialized " + extensionName + " v" + extensionVersion);
        logger.logToOutput("Author: " + extensionAuthor);
        logger.logToOutput("Website: " + extensionWebsite);
        String extensionDescription = "environment var updater";
        logger.logToOutput("Description: " + extensionDescription);
        logger.logToOutput("--------------------------------------------------------------------------------");

        montoyaApi.userInterface().registerSuiteTab(extensionName, new EnvironmentTab());
        montoyaApi.userInterface().registerContextMenuItemsProvider(new PopUpMenu(montoyaApi));
        montoyaApi.http().registerHttpHandler(new EnviHttpHandler(montoyaApi));

    }
}
