package com.kovalevskyi.academy.codingbootcamp.suite;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.maven.shared.invoker.*;
import picocli.CommandLine;

@CommandLine.Command(
    name = "zeus",
    mixinStandardHelpOptions = true,
    version = Constants.VERSION,
    description = "Zeus the Mighty")
public class Zeus implements Callable<Integer> {

  private final String[][][] classNames = {
    {
      {"com.kovalevskyi.academy.codingbootcamp.suite.tests.week0.day0.MainTest"},
      {
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week0.day1.AlphabetTest",
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week0.day1.NumbersTest"
      },
      {"com.kovalevskyi.academy.codingbootcamp.suite.tests.week0.day2.NumbersTest"},
      {"com.kovalevskyi.academy.codingbootcamp.suite.tests.week0.day3.PointTest"}
    },
    {
      {},
      {
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week1.day1.StringUtilsTest",
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week1.day1.StdStringTest"
      },
      {"com.kovalevskyi.academy.codingbootcamp.suite.tests.week1.day2.ListTest"}
    },
    {
      {
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week2.day0.MainPrintParamTest",
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week2.day0.MainPrintReversedParamTest",
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week2.day0.CalculatorTest",
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week2.day0.MainPrintSortedParamTest"
      },
      {
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week2.day1.BoxGeneratorTest",
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week2.day1.TextPrinter2Test",
        "com.kovalevskyi.academy.codingbootcamp.suite.tests.week2.day1.TextPrinterTes"
      },
      {}
    }
  };

  @CommandLine.Option(
      names = {"-w", "--week"},
      description = "number of the week to test",
      defaultValue = "-1")
  private int week;

  @CommandLine.Option(
      names = {"-d", "--day"},
      description = "number of the day to test",
      defaultValue = "-1")
  private int day;

  @CommandLine.Option(
      names = {"-a", "--all"},
      description = "run all tests",
      defaultValue = "false")
  private boolean all;

  @CommandLine.Option(
      names = {"-m", "--maven"},
      description = "path to the maven home",
      required = true)
  private String mavenHome;

  @CommandLine.Option(
          names = {"-s", "--show"},
          description = "show tests for week/day")
  private boolean show;

  @CommandLine.Option(
          names = {"-t", "--test"},
          description = "specific test to executed")
  private String test;

  @Override
  public Integer call() {
    try {
      if (this.test != null && !this.all && (this.week == -1 || this.day == -1)) {
        System.out.println("you need to specify week and day like this: --week X --day Y");
        return 0;
      }
      if (this.show) {
        for (var dayTest : this.classNames[this.week][this.day]) {
          System.out.printf(String.format("test: %s\n", dayTest));
        }
        return 0;
      }
      System.out.println("Zeus is about to start compiling your project");
      mavenCompile();
      System.out.println("Zeus happy");
      if (this.all) {
        if (this.week != -1 && this.day != -1) {
          System.out.println(
              "one can not ask Zeus to run all test and specific tests (week/day) "
                  + "at the same time!");
          return 0;
        }
        executeAllTests();
      } else if (this.test != null) {
        executeDayTest(this.test);
      } else {
        executeDayTests(this.classNames[this.week][this.day]);
      }
    } catch (Exception e) {
      System.out.println("Zeus is VERY unhappy!!!!");
      e.printStackTrace();
      return -1;
    }
    return 0;
  }

  private void mavenCompile() throws Exception {
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(new File("./pom.xml"));
    request.setGoals(
        new ArrayList<>() {
          {
            add("clean");
            add("compile");
            add("package");
          }
        });
    Invoker invoker = new DefaultInvoker();
    invoker.setMavenHome(new File(this.mavenHome));
    System.out.println("Zeus is about to execute 'mvn clean compile package'");
    invoker.execute(request);
  }

  private void executeAllTests() throws Exception {
    for (var weekTests : this.classNames) {
      for (var dayTests : weekTests) {
        executeDayTests(dayTests);
      }
    }
  }

  private void executeDayTest(String className) throws Exception {
    var testExecutor =
            (AbstractTestExecutor) Class.forName(className).getConstructors()[0].newInstance();
    testExecutor.executeTest();
  }

  private void executeDayTests(String[] classNames) throws Exception {
    for (var className : classNames) {
      executeDayTest(className);
    }
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new Zeus()).execute(args);
    System.exit(exitCode);
  }
}
