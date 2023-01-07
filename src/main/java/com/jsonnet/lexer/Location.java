package com.jsonnet.lexer;

// Location represents a single location in an (unspecified) file.
class Location {
  int line;
  int column;

  public Location(int line, int column) {
    this.line = line;
    this.column = column;
  }

  // IsSet returns if this Location has been set.
  boolean isSet() {
    return line != 0;
  }

  @Override
  public String toString() {
    return String.format("%s:%s", line, column);
  }
}
