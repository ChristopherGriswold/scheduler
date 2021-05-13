package com.iceybones.scheduler.controllers;

/**
 * Used to workaround IntelliJ limitations and make the application deployable to a jar file.
 */
public class Main {

  /**
   * Required by the JVM to launch the application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    ApplicationManager.main(args);
  }
}
