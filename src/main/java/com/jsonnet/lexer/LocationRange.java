package com.jsonnet.lexer;

// LocationRange represents a range of a source file.
public class LocationRange {
  String fileName;
  Location begin;
  Location end;

  public LocationRange(String fileName, Location begin, Location end) {
    this.fileName = fileName;
    this.begin = begin;
    this.end = end;
  }

  // IsSet returns if this LocationRange has been set.
  boolean isSet() {
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
