package com.jsonnet.lexer;

import com.jsonnet.lexer.fodder.FodderElement;
import java.util.List;


public class Token {
  TokenKind kind; // The type of the token
  List<FodderElement> fodder; // Any fodder the occurs before this token
  String data; // Content of the token if it is not a keyword
  // Extra info for when kind == tokenStringBlock
  String stringBlockIndent;     // The sequence of whitespace that indented the block.
  String stringBlockTermIndent; // This is always fewer whitespace characters than in stringBlockIndent.
  LocationRange loc;

  public Token(TokenKind kind, List<FodderElement> fodder, String data, String stringBlockIndent,
      String stringBlockTermIndent, LocationRange loc) {
    this.kind = kind;
    this.fodder = fodder;
    this.data = data;
    this.stringBlockIndent = stringBlockIndent;
    this.stringBlockTermIndent = stringBlockTermIndent;
    this.loc = loc;
  }

  public static Token of(TokenKind kind, String data) {
    return new Token(kind, null, data, null, null, null);
  }

  @Override
  public String toString() {
    return String.format("{kind: %s, data: %s}", kind, data);
  }
}
