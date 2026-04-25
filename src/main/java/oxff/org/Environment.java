package oxff.org;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import groovy.lang.Script;
import oxff.org.controler.EnviHttpHandler;
import oxff.org.model.Arg;
import oxff.org.model.ArgTableModel;
import oxff.org.model.AutoUpdateType;
import oxff.org.persistence.PersistenceManager;
import oxff.org.ui.EnvironmentTab;
import oxff.org.ui.PopUpMenu;
import oxff.org.utils.GroovyUtils;
import oxff.org.utils.Tools;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment implements BurpExtension {

    MontoyaApi montoyaApi;
    public static Logging logger;
    private final static String extensionName = "environment";
    private final static String extensionVersion = "1.0";
    private final static String extensionAuthor = "oxff";
    private final static String extensionWebsite = "https://github.com/oxff/burp-environment";

    public final static String LEFT_MARKER = "{{";
    public final static String RIGHT_MARKER = "}}";
    public final static String  GROOVY_FUNCTION_NAME = "modify";

    public static List<Arg> args;

    public static ArgTableModel argTableModel;
//    public static Map<String, Arg> argsMap;
    public static Map<AutoUpdateType, Method> autoUpdateMethods;
    public static Map<String, Script> groovyScripts;
    public static PersistenceManager persistenceManager;
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        logger = montoyaApi.logging();
        montoyaApi.extension().setName(extensionName);

        args = new ArrayList<>();

        argTableModel = new ArgTableModel(args);
//        argsMap = new HashMap<>();
        groovyScripts = new HashMap<>();

        autoUpdateMethods = new HashMap<>();
        autoUpdateMethods.put(AutoUpdateType.NONE, null);
        try {
            autoUpdateMethods.put(AutoUpdateType.UUID, Tools.class.getMethod("getRandomUUID"));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            autoUpdateMethods.put(AutoUpdateType.TIMESTAMP, Tools.class.getMethod("getTimestamp"));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            autoUpdateMethods.put(AutoUpdateType.SHA1_OF_TIMESTAMP, Tools.class.getMethod("getSha1OfTimestamp"));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            autoUpdateMethods.put(AutoUpdateType.RANDOM_NUMBER, Tools.class.getMethod("getRandomNumber", Integer.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            autoUpdateMethods.put(AutoUpdateType.RANDOM_TEXT, Tools.class.getMethod("getRandomText", int.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            autoUpdateMethods.put(AutoUpdateType.INCREMENT_NUMBER, Tools.class.getMethod("getRandomIncrementNumber", String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            autoUpdateMethods.put(AutoUpdateType.Groovy_CODE, Tools.class.getMethod("generateByGroovy", Arg.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // 初始化持久化层
        String dbDir = System.getProperty("user.dir") + File.separator + "environment";
        new File(dbDir).mkdirs();
        String dbPath = dbDir + File.separator + "environment.db";
        persistenceManager = new PersistenceManager(dbPath);
        try {
            persistenceManager.initDatabase();
            List<Arg> persistedArgs = persistenceManager.loadAll();
            for (Arg arg : persistedArgs) {
                reconstructMethodAndScript(arg);
                argTableModel.addArg(arg);
            }
            logger.logToOutput("Loaded " + persistedArgs.size() + " persisted args from database.");
        } catch (SQLException e) {
            logger.logToError("Failed to initialize persistence: " + e.getMessage());
        }

        logger.logToOutput("Initialized " + extensionName + " v" + extensionVersion);
        logger.logToOutput("Author: " + extensionAuthor);
        logger.logToOutput("Website: " + extensionWebsite);
        String extensionDescription = "environment var updater";
        logger.logToOutput("Description: " + extensionDescription);
        logger.logToOutput("--------------------------------------------------------------------------------");

        montoyaApi.userInterface().registerSuiteTab(extensionName, new EnvironmentTab(logger, argTableModel));
        montoyaApi.userInterface().registerContextMenuItemsProvider(new PopUpMenu(montoyaApi));
        montoyaApi.http().registerHttpHandler(new EnviHttpHandler(montoyaApi));

    }

    synchronized public static int getArgListSize(){
        return args.size();
    }

    public static void reconstructMethodAndScript(Arg arg) {
        AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
        arg.setMethod(autoUpdateMethods.get(autoUpdateType));

        if (autoUpdateType == AutoUpdateType.Groovy_CODE && arg.getCodePath() != null
                && !arg.getCodePath().isBlank()) {
            try {
                Script script = GroovyUtils.getScript(arg.getCodePath());
                arg.setScript(script);
                groovyScripts.put(arg.getName(), script);
            } catch (Exception e) {
                logger.logToError("Failed to load groovy script for " + arg.getName()
                        + ": " + e.getMessage());
            }
        }
    }
}
