package com.devhc.jobdeploy.utils;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.util.Random;

public class AnsiColorBuilder {
  private AnsiColorBuilder() {
  }

  private static Random random = new Random();

  public static void install() {
    AnsiConsole.systemInstall();
  }

  public static void uninstall() {
    AnsiConsole.systemUninstall();
  }

  public static String red(String text) {
    return Ansi.ansi().fg(Ansi.Color.RED).a(text).reset().toString();
  }

  public static String green(String text) {
    return Ansi.ansi().fg(Ansi.Color.GREEN).a(text).reset().toString();
  }

  public static String yellow(String text) {
    return Ansi.ansi().fg(Ansi.Color.YELLOW).a(text).reset().toString();
  }

  public static String blue(String text) {
    return Ansi.ansi().fg(Ansi.Color.BLUE).a(text).reset().toString();
  }

  public static String black(String text) {
    return Ansi.ansi().fg(Ansi.Color.BLACK).a(text).reset().toString();
  }

  public static String magenta(String text) {
    return Ansi.ansi().fg(Ansi.Color.MAGENTA).a(text).reset().toString();
  }

  public static String cyan(String text) {
    return Ansi.ansi().fg(Ansi.Color.CYAN).a(text).reset().toString();
  }

  public static String white(String text) {
    return Ansi.ansi().fg(Ansi.Color.WHITE).a(text).reset().toString();
  }

  public static String build(Ansi.Color color, String text) {
    return Ansi.ansi().fg(color).a(text).reset().toString();
  }

  public static Ansi.Color getRandomColor() {
    Ansi.Color color = null;
    while (true) {
      int index = Math.abs(random.nextInt() % Ansi.Color.values().length);
      color = Ansi.Color.values()[index];
      if (color != Ansi.Color.BLACK && color != Ansi.Color.RED) {
        return color;
      }
    }
  }
}
