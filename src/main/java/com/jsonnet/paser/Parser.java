package com.jsonnet.paser;

import com.jsonnet.ast.AstAssert;
import com.jsonnet.ast.AstConditional;
import com.jsonnet.ast.AstError;
import com.jsonnet.ast.AstFunction;
import com.jsonnet.ast.AstNode;
import com.jsonnet.ast.AstVar;
import com.jsonnet.common.StaticErrorException;
import com.jsonnet.lexer.LocationRange;
import com.jsonnet.lexer.Token;
import com.jsonnet.lexer.TokenKind;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


public class Parser {

  public static final int applyPrecedence = 2; // Function calls and indexing.
  public static final int unaryPrecedence = 4; // Logical and bitwise negation, unary + -
  public static final int beforeElsePrecedence = 15; // True branch of an if.
  public static final int maxPrecedence = 16; // Local, If, Import, Function, Error

  List<Token> tokens;
  int currT;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  static LocationRange locFromToken(Token begin, Token end) {
    return new LocationRange(begin.loc.fileName, begin.loc.begin, end.loc.end);
  }

  static LocationRange locFromTokenAST(Token begin, AstNode end) {
    return new LocationRange(begin.loc.fileName, begin.loc.begin, end.getLoc().end);
  }

  Token pop() {
    Token t = this.tokens.get(currT);
    currT++;
    return t;
  }

  Token popExpect(TokenKind tk) {
    Token t = this.pop();
    if (t.kind != tk) {
      throw new StaticErrorException(String.format("Expected token %s but got %s", tk, t), t.loc);
    }
    return t;
  }

  Token popExpectOp(String op) {
    Token t = this.pop();
    if (t.kind != TokenKind.tokenOperator || !t.data.equals(op)) {
      throw new StaticErrorException(String.format("Expected token %s but got %s", op, t), t.loc);
    }
    return t;
  }

  Token peek() {
    return this.tokens.get(currT);
  }

  Pair<List<String>, Boolean> parseIdentifierList(String elementKind) {
    commaList commaList = this.parseCommaList(TokenKind.tokenParenR, elementKind);

    List<String> identifiers = new LinkedList<>();
    for (AstNode expr : commaList.exprs) {
      if (!(expr instanceof AstVar)) {
        throw new StaticErrorException(String.format("Not an identifier: %s", expr), expr.getLoc());
      }
      identifiers.add(((AstVar) expr).id);
    }
    return ImmutablePair.of(identifiers, commaList.gotComma);
  }

  commaList parseCommaList(TokenKind endTokenKind, String elementKind) {
    List<AstNode> exprs = new LinkedList<>();
    boolean gotComma = false;
    boolean isFirstToken = true;
    while (true) {
      Token next = this.peek();
      if (!isFirstToken) {
        if (next.kind == TokenKind.tokenComma) {
          this.pop();
          next = this.peek();
          gotComma = true;
        }
      }
      if (next.kind == endTokenKind) {
        // got_comma can be true or false here.
        return new commaList(this.pop(), exprs, gotComma);
      }
      if (!isFirstToken && !gotComma) {
        throw new StaticErrorException(String.format("Expected a comma before next %s.", elementKind), next.loc);
      }
      AstNode expr = this.parse(maxPrecedence);
      exprs.add(expr);
      gotComma = false;
      isFirstToken = false;
    }
  }

  AstNode parse(int precedence) {
    Token begin = this.peek();
    switch (begin.kind) {
      // These cases have effectively maxPrecedence as the first call to parse will parse them.
      case tokenAssert: {
        this.pop();
        AstNode cond = this.parse(maxPrecedence);
        AstNode msg = null;
        if (this.peek().kind == TokenKind.tokenColon) {
          this.pop();
          msg = this.parse(maxPrecedence);
        }
        this.popExpect(TokenKind.tokenSemicolon);
        AstNode rest = this.parse(maxPrecedence);
        return new AstAssert(locFromTokenAST(begin, rest), cond, msg, rest);
      }
      case tokenError: {
        this.pop();
        AstNode expr = this.parse(maxPrecedence);
        return new AstError(locFromTokenAST(begin, expr), expr);
      }
      case tokenIf: {
        this.pop();
        AstNode cond = this.parse(maxPrecedence);
        this.popExpect(TokenKind.tokenThen);
        AstNode branchTrue = this.parse(maxPrecedence);
        AstNode branchFalse = null;
        LocationRange lr = locFromTokenAST(begin, branchTrue);
        if (this.peek().kind == TokenKind.tokenElse) {
          this.pop();
          branchFalse = this.parse(maxPrecedence);
          lr = locFromTokenAST(begin, branchFalse);
        }
        return new AstConditional(lr, cond, branchTrue, branchFalse);
      }
      case tokenFunction: {
        this.pop();
        Token next = this.pop();
        if (next.kind == TokenKind.tokenParenL) {
          Pair<List<String>, Boolean> pair = this.parseIdentifierList("function parameter");
          List<String> params = pair.getLeft();
          boolean gotComma = pair.getRight();
          AstNode body = this.parse(maxPrecedence);
          return new AstFunction(locFromTokenAST(begin, body), params, gotComma, body);
        } else {
          throw new StaticErrorException(String.format("Expected ( but got %s", next), next.loc);
        }
      }
    }
    return null;
  }

  public AstNode parse(List<Token> tokens) {
    AstNode expr = this.parse(maxPrecedence);
    if (this.peek().kind != TokenKind.tokenEndOfFile) {
      throw new StaticErrorException(String.format("Did not expect: %s", this.peek()), this.peek().loc);
    }
    return expr;
  }

  // -------------------------------------------------------------------------------------------------------------------
  // commaList represents the result when parsing a comma list
  static class commaList {
    Token token;
    List<AstNode> exprs;
    boolean gotComma;
    public commaList(Token token, List<AstNode> exprs, boolean gotComma) {
      this.token = token;
      this.exprs = exprs;
      this.gotComma = gotComma;
    }
  }
}
