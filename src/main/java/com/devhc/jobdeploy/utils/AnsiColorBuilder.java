package com.devhc.jobdeploy.utils;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.util.Random;

public class AnsiColorBuilder {
  private static boolean enable = true;

  public static void setEnable(boolean enable) {
    AnsiColorBuilder.enable = enable;
  }

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
    return build(Ansi.Color.RED, text);
  }

  public static String green(String text) {
    return build(Ansi.Color.GREEN, text);
  }

  public static String yellow(String text) {
    return build(Ansi.Color.YELLOW, text);
  }

  public static String blue(String text) {
    return build(Ansi.Color.BLUE, text);
  }

  public static String black(String text) {
    return build(Ansi.Color.BLACK, text);
  }

  public static String magenta(String text) {
    return build(Ansi.Color.MAGENTA, text);
  }

  public static String cyan(String text) {
    return build(Ansi.Color.CYAN, text);
  }

  public static String white(String text) {
    return build(Ansi.Color.WHITE, text);
  }

  public static String build(Ansi.Color color, String text) {
    return enable ? Ansi.ansi().fg(color).a(text).reset().toString() : text;
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
