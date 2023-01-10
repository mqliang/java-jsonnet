package com.jsonnet.lexer;

import com.jsonnet.lexer.fodder.FodderElement;
import java.util.List;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class Token {
  public TokenKind kind; // The type of the token
  public List<FodderElement> fodder; // Any fodder the occurs before this token
  public String data; // Content of the token if it is not a keyword
  // Extra info for when kind == tokenStringBlock
  public String stringBlockIndent;     // The sequence of whitespace that indented the block.
  public String stringBlockTermIndent; // This is always fewer whitespace characters than in stringBlockIndent.
  public LocationRange loc;

  @Override
  public String toString() {
    return String.format("{kind: %s, data: %s}", kind, data);
  }
}
