package com.jsonnet.ast;

public enum AstObjectFieldKind {
  astObjectAssert, // assert expr2 [: expr3]  where expr3 can be nil
  astObjectFieldID, // id:[:[:]] expr2
  astObjectFieldExpr, // '['expr1']':[:[:]] expr2
  astObjectFieldStr, // expr1:[:[:]] expr2
  astObjectLocal, // local id = expr2
}
