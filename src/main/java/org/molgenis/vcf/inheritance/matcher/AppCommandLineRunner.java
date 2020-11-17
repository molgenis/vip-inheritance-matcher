package org.molgenis.vcf.inheritance.matcher;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions.OPT_DEBUG;
import static org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions.OPT_FORCE;
import static org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions.OPT_INPUT;
import static org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions.OPT_OUTPUT;

import ch.qos.logback.classic.Level;
import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class AppCommandLineRunner implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppCommandLineRunner.class);

  private static final int STATUS_MISC_ERROR = 1;
  private static final int STATUS_COMMAND_LINE_USAGE_ERROR = 64;

  private final String appName;
  private final String appVersion;
  private final CommandLineParser commandLineParser;
  private final InheritanceMatcher inheritanceMatcher;

  AppCommandLineRunner(
      @Value("${app.name}") String appName,
      @Value("${app.version}") String appVersion,
      InheritanceMatcher inheritanceMatcher) {
    this.appName = requireNonNull(appName);
    this.appVersion = requireNonNull(appVersion);
    this.inheritanceMatcher = inheritanceMatcher;

    this.commandLineParser = new DefaultParser();
  }

  @Override
  public void run(String... args) {
    if (args.length == 1
        && (
        args[0].equals("-" + org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions.OPT_VERSION)
            || args[0].equals(
            "--" + org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions.OPT_VERSION_LONG))) {
      LOGGER.info("{} {}", appName, appVersion);
      return;
    }

    for (String arg : args) {
      if (arg.equals('-' + OPT_DEBUG)
          || arg.equals(
          '-' + org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions.OPT_DEBUG_LONG)) {
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (!(rootLogger instanceof ch.qos.logback.classic.Logger)) {
          throw new ClassCastException("Expected root logger to be a logback logger");
        }
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.DEBUG);
        break;
      }
    }

    CommandLine commandLine = getCommandLine(args);
    org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions.validateCommandLine(commandLine);
    Settings settings = mapSettings(commandLine);
    try {
      inheritanceMatcher.mapAndMatch(settings);
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage(), e);
      System.exit(STATUS_MISC_ERROR);
    }
  }

  private Settings mapSettings(CommandLine commandLine) {
    String inputPathValue = commandLine.getOptionValue(OPT_INPUT);
    Path inputPath = Path.of(inputPathValue);

    Path outputPath;
    if (commandLine.hasOption(OPT_OUTPUT)) {
      outputPath = Path.of(commandLine.getOptionValue(OPT_OUTPUT));
    } else {
      outputPath = Path.of(commandLine.getOptionValue(OPT_INPUT) + ".html");
    }

    boolean overwriteOutput = commandLine.hasOption(OPT_FORCE);

    boolean debugMode = commandLine.hasOption(OPT_DEBUG);

    return Settings.builder().inputVcfPath(inputPath).outputPath(outputPath)
        .overwrite(overwriteOutput).debug(debugMode)
        .build();
  }

  private CommandLine getCommandLine(String[] args) {
    CommandLine commandLine = null;
    try {
      commandLine = commandLineParser.parse(
          org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions.getAppOptions(), args);
    } catch (ParseException e) {
      logException(e);
      System.exit(STATUS_COMMAND_LINE_USAGE_ERROR);
    }
    return commandLine;
  }

  @SuppressWarnings("java:S106")
  private void logException(ParseException e) {
    LOGGER.error(e.getLocalizedMessage(), e);

    // following information is only logged to system out
    System.out.println();
    HelpFormatter formatter = new HelpFormatter();
    formatter.setOptionComparator(null);
    String cmdLineSyntax = "java -jar " + appName + ".jar";
    formatter.printHelp(cmdLineSyntax, org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions
        .getAppOptions(), true);
    System.out.println();
    formatter.printHelp(cmdLineSyntax, org.molgenis.vcf.inheritance.matcher.AppCommandLineOptions
        .getAppVersionOptions(), true);
  }
}
