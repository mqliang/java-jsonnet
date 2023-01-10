package com.jsonnet.lexer;

import lombok.AllArgsConstructor;


// Location represents a single location in an (unspecified) file.
@AllArgsConstructor
public class Location {
  int line;
  int column;

  // IsSet returns if this Location has been set.
  boolean isSet() {
    return line != 0;
  }

  @Override
  public String toString() {
    return String.format("%s:%s", line, column);
  }
}
