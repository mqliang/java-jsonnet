package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


public interface AstNode {
  LocationRange getLoc();
}
