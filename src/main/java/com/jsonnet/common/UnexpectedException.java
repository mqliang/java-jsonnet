package com.jsonnet.common;

import com.jsonnet.lexer.Token;


// UnexpectedException represents encountering an unexpected token during parsing some jsonnet.
public class UnexpectedException extends StaticErrorException{
  public UnexpectedException(Token token, String context) {
    super(String.format("Unexpected: %s while %s", token, context), token.loc);
  }
}
