package com.jsonnet.lexer;

import lombok.AllArgsConstructor;


// LocationRange represents a range of a source file.
@AllArgsConstructor
public class LocationRange {
  public String fileName;
  public Location begin;
  public Location end;

  // IsSet returns if this LocationRange has been set.
  public boolean isSet() {
    return this.begin.isSet();
  }

  @Override
  public String toString() {
    if (!isSet()) {
      return this.fileName;
    }
    String filePrefix = "";
    if (this.fileName != null && this.fileName.length() > 0) {
      filePrefix = this.fileName + ":";
    }
    if (this.begin.line == this.end.line) {
      if (this.begin.column == this.end.column) {
        return String.format("%s%s", filePrefix, this.begin);
      }
      return String.format("%s%s-%s", filePrefix, this.begin, this.end.column);
    }
    return String.format("%s(%s)-(%s)", filePrefix, this.begin, this.end);
  }
}
