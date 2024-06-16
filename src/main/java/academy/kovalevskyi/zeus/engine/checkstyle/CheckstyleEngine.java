package academy.kovalevskyi.zeus.engine.checkstyle;

import academy.kovalevskyi.testing.service.State;
import academy.kovalevskyi.testing.util.AnsiConsoleInstaller;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import org.fusesource.jansi.Ansi;

/**
 * This class is responsible for running Checkstyle on Java source files. It provides methods to
 * check individual files and lists of files.
 */
public final class CheckstyleEngine {

  /**
   * Runs Checkstyle on a single file using the specified style configuration.
   *
   * @param style the style configuration to use
   * @param file the file to check
   * @return the number of warnings found
   * @throws IOException if an I/O error occurs
   */
  public static int check(final Style style, final File file) throws IOException {
    final var warnings = new CheckstyleProcessor().process(style, file);
    displayResults(file.getName(), warnings);
    return warnings.size();
  }

  /**
   * Runs Checkstyle on a list of files using the specified style configuration.
   *
   * @param style the style configuration to use
   * @param files the list of files to check
   * @return the total number of warnings found across all files
   * @throws IOException if an I/O error occurs
   */
  public static int check(final Style style, final List<File> files) throws IOException {
    if (files.isEmpty()) {
      System.out.println(underline("You have nothing to verify with checkstyle!"));
      return 0;
    }
    var successfulChecks = 0;
    var totalWarnings = 0;
    for (var file : files) {
      var result = check(style, file);
      if (result == 0) {
        successfulChecks++;
      } else {
        totalWarnings += result;
      }
    }
    displayFooter(files.size(), successfulChecks, files.size() - successfulChecks, totalWarnings);
    return totalWarnings;
  }

  private static void displayResults(final String fileName, final List<String> warnings) {
    AnsiConsoleInstaller.INSTANCE.systemInstall();
    final var template = "%s%s%n";
    if (warnings.isEmpty()) {
      System.out.printf(template, fileName, prepareStatus(State.SUCCESSFUL));
    } else {
      System.out.printf(template, fileName, prepareStatus(State.FAILED));
      warnings.forEach(
          line -> System.out.println(Ansi.ansi().fg(State.FAILED.color).a(line.trim()).reset()));
    }
    AnsiConsoleInstaller.INSTANCE.systemUninstall();
  }

  private static void displayFooter(
      final int files, final int successful, final int failed, final int errors) {
    if (errors > 0) {
      System.out.println(prepareFooter(files, successful, failed, errors));
    } else {
      System.out.println(underline("Your source files are verified by checkstyle successfully!"));
    }
  }

  private static String prepareStatus(final State state) {
    return String.format(" - %s", Ansi.ansi().fg(state.color).a(state.status).reset());
  }

  private static String underline(final String text) {
    return String.format("%s%n%s", text, "-".repeat(text.length()));
  }

  private static String prepareFooter(
      final int files, final int successful, final int failed, final int errors) {
    final var result = new StringJoiner(" | ");
    result.add(String.format("%nFILES %d", files));
    if (successful > 0) {
      result.add(String.format("SUCCESSFUL %d", successful));
    }
    if (failed > 0) {
      result.add(String.format("FAILED %d", failed));
    }
    if (errors > 0) {
      result.add(String.format("ERRORS %d", errors));
    }
    final var bar = result.toString();
    return String.format("%s%n%s", bar, "-".repeat(bar.trim().length()));
  }
}
