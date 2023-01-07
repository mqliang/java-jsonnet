package com.jsonnet.lexer;

// StaticErrorException represents an error during parsing/lexing some jsonnet.
public class StaticErrorException extends RuntimeException{
  String msg;
  LocationRange loc;
  public StaticErrorException(String msg, LocationRange loc) {
    this.msg = msg;
    this.loc = loc;
  }
  public StaticErrorException(String msg, String fileName, Location l) {
    this(msg, new LocationRange(fileName, l, l));
  }

  @Override
  public String toString() {
    String loc = this.loc.isSet() ? this.loc.toString() : "";
    return String.format("%s %s", loc, this.msg);
  }
}
