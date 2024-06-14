package academy.kovalevskyi.zeus.engine.checkstyle;

import academy.kovalevskyi.zeus.util.FileExplorer;
import academy.kovalevskyi.zeus.util.FileType;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

class CheckstyleProcessor {

  private static final String MISSING_JAVADOC_TYPE = "[MissingJavadocType]";
  private static final String MISSING_JAVADOC_METHOD = "[MissingJavadocMethod]";
  private static final String STARTING_AUDIT = "Starting audit...";
  private static final String AUDIT_DONE = "Audit done.";

  public List<String> process(final Style style, final File file) throws IOException {
    validateFile(file);
    Process process = startCheckstyleProcess(style, file);
    String output = captureProcessOutput(process);
    verifyProcessExitCode(process);
    return collectResult(output);
  }

  private void validateFile(File file) throws IOException {
    if (!file.exists()) {
      throw new FileNotFoundException(String.format("%s is absent", file.getAbsolutePath()));
    }
    if (file.isDirectory()) {
      throw new IllegalArgumentException(String.format("%s is not a file", file.getAbsolutePath()));
    }
    if (!FileExplorer.match(file.getName(), FileType.JAVA)) {
      throw new IllegalArgumentException(String.format("%s is not supported", file.getName()));
    }
  }

  private Process startCheckstyleProcess(Style style, File file) throws IOException {
    ProcessBuilder processBuilder =
        new ProcessBuilder(
            "java",
            "-Duser.language=en",
            "-cp",
            System.getProperty("java.class.path"),
            "com.puppycrawl.tools.checkstyle.Main",
            "-c",
            style.config,
            file.getAbsolutePath());
    processBuilder.redirectErrorStream(true);
    return processBuilder.start();
  }

  private String captureProcessOutput(Process process) throws IOException {
    var outputStreamCaptor = new ByteArrayOutputStream();
    try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        var printStream = new PrintStream(outputStreamCaptor)) {
      String line;
      while ((line = reader.readLine()) != null) {
        printStream.println(line);
      }
    }
    return outputStreamCaptor.toString();
  }

  private void verifyProcessExitCode(Process process) {
    try {
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new RuntimeException("Checkstyle process exited with code: " + exitCode);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Checkstyle process was interrupted", e);
    }
  }

  private List<String> collectResult(final String output) {
    final var result = output.trim();
    if (result.isEmpty()) {
      throw new IllegalArgumentException("Checkstyle console captor is empty");
    }
    return Arrays.stream(result.split("\n"))
        .filter(text -> !text.contains(STARTING_AUDIT))
        .filter(text -> !text.contains(AUDIT_DONE))
        .filter(text -> !text.contains(MISSING_JAVADOC_TYPE))
        .filter(text -> !text.contains(MISSING_JAVADOC_METHOD))
        .map(text -> String.format("[ERROR] %s", text.substring(text.lastIndexOf(".java") + 6)))
        .toList();
  }
}