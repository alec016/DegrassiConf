package es.degrassi.degrassiconf.util;

import es.degrassi.degrassiconf.DegrassiConf;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescriptionImpl;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("unused")
public class DegrassiLogger {

  public static final Logger INSTANCE = DegrassiConf.LOGGER;
  public static String NAME;
  private static boolean shouldReset = false;

  public static void init(String path, String filename, String name) {
    NAME = name;
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    PatternLayout logPattern = PatternLayout.newBuilder()
      .withPattern("[%d{HH:mm:ss.SSS}][%level]: %msg%n%throwable")
      .build();

    TriggeringPolicy policy = new TriggeringPolicy() {
      @Override
      public void initialize(RollingFileManager manager) {

      }

      @Override
      public boolean isTriggeringEvent(LogEvent logEvent) {
        if(shouldReset) {
          shouldReset = false;
          return true;
        }
        return false;
      }
    };

    RolloverStrategy strategy = manager -> new RolloverDescriptionImpl(manager.getFileName(), true, new AbstractAction() {
      @Override
      public boolean execute() throws IOException {
        new FileWriter(manager.getFileName(), false).close();
        return false;
      }
    }, null);

    RollingFileAppender cmAppender = RollingFileAppender.newBuilder()
      .withFileName("logs/" + path + "/" + filename + ".log")
      .withAppend(false)
      .withFilePattern("logs/" + path + "/" + filename + "-%i.log.gz")
      .withPolicy(policy)
      .withStrategy(strategy)
      .setName(NAME)
      .withImmediateFlush(true)
      .setIgnoreExceptions(false)
      .setConfiguration(config)
      .setLayout(logPattern)
      .build();

    cmAppender.start();

    config.addAppender(cmAppender);

    LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.ALL, NAME, "true", new AppenderRef[0], null, config, null);
    loggerConfig.addAppender(cmAppender, Level.INFO, null);

    Appender debug = config.getAppender("DebugFile");
    if(debug != null)
      loggerConfig.addAppender(debug, Level.WARN, null);

    Appender file = config.getAppender("File");
    if(file != null)
      loggerConfig.addAppender(file, Level.WARN, null);

    Appender console = config.getAppender("Console");
    if(console != null)
      loggerConfig.addAppender(console, Level.WARN, null);

    Appender serverGuiConsole = config.getAppender("ServerGuiConsole");
    if(serverGuiConsole != null)
      loggerConfig.addAppender(serverGuiConsole, Level.WARN, null);

    config.addLogger(NAME, loggerConfig);
    ctx.updateLoggers();
  }

  public static void reset() {
    shouldReset = true;
  }

  public static void setDebugLevel(Level level) {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    final LoggerConfig cmConfig = config.getLoggers().get(NAME);
    if(cmConfig == null)
      throw new IllegalStateException(NAME + " logger not present!");

    final Appender cmAppender = cmConfig.getAppenders().get(NAME);
    if(cmAppender == null)
      throw new IllegalStateException(NAME + " appender not present");

    cmConfig.removeAppender(NAME);
    cmConfig.addAppender(cmAppender, level, null);
  }
}
