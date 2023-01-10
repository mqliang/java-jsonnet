package com.jsonnet.paser;

import com.google.common.collect.ImmutableSet;
import com.jsonnet.ast.*;
import com.jsonnet.common.StaticErrorException;
import com.jsonnet.common.UnexpectedException;
import com.jsonnet.lexer.LocationRange;
import com.jsonnet.lexer.Token;
import com.jsonnet.lexer.TokenKind;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;

import static com.jsonnet.ast.AstObjectFieldHide.*;
import static com.jsonnet.ast.AstObjectFieldKind.*;
import static com.jsonnet.ast.BinaryOp.*;
import static com.jsonnet.ast.CompKind.astCompKindFor;
import static com.jsonnet.ast.CompKind.astCompKindIf;
import static com.jsonnet.ast.LiteralStringKind.*;
import static com.jsonnet.lexer.TokenKind.*;
import static java.util.Map.entry;


public class Parser {

  public static final int applyPrecedence = 2; // Function calls and indexing.
  public static final int unaryPrecedence = 4; // Logical and bitwise negation, unary + -
  public static final int beforeElsePrecedence = 15; // True branch of an if.
  public static final int maxPrecedence = 16; // Local, If, Import, Function, Error

  public static final Map<BinaryOp, Integer> bopPrecedence = Map.ofEntries(
      entry(bopMult,            5),
      entry(bopDiv,             5),
      entry(bopPercent,         5),
      entry(bopPlus,            6),
      entry(bopMinus,           6),
      entry(bopShiftL,          7),
      entry(bopShiftR,          7),
      entry(bopGreater,         8),
      entry(bopGreaterEq,       8),
      entry(bopLess,            8),
      entry(bopLessEq,          8),
      entry(bopManifestEqual,   9),
      entry(bopManifestUnequal, 9),
      entry(bopBitwiseAnd,      10),
      entry(bopBitwiseXor,      11),
      entry(bopBitwiseOr,       12),
      entry(bopAnd,             13),
      entry(bopOr,              14)
  );

  List<Token> tokens;
  int currT;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  static LocationRange locFromTokens(Token begin, Token end) {
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
    if (t.kind != tokenOperator || !t.data.equals(op)) {
      throw new StaticErrorException(String.format("Expected token %s but got %s", op, t), t.loc);
    }
    return t;
  }

  Token peek() {
    return this.tokens.get(currT);
  }

  IdentifierListResult parseIdentifierList(String elementKind) {
    CommaListResult commaList = this.parseCommaList(tokenParenR, elementKind);

    List<String> identifiers = new LinkedList<>();
    for (AstNode expr : commaList.exprs) {
      if (!(expr instanceof AstVar)) {
        throw new StaticErrorException("Expected simple identifier but got a complex expression.", expr.getLoc());
      }
      identifiers.add(((AstVar) expr).id);
    }
    return new IdentifierListResult(identifiers, commaList.gotComma);
  }

  CommaListResult parseCommaList(TokenKind endTokenKind, String elementKind) {
    List<AstNode> exprs = new LinkedList<>();
    boolean gotComma = false;
    boolean isFirstToken = true;
    while (true) {
      Token next = this.peek();
      if (!isFirstToken) {
        if (next.kind == tokenComma) {
          this.pop();
          next = this.peek();
          gotComma = true;
        }
      }
      if (next.kind == endTokenKind) {
        // gotComma can be true or false here.
        return new CommaListResult(this.pop(), exprs, gotComma);
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
        if (this.peek().kind == tokenOperator && this.pop().data.equals(":")) {
          this.pop();
          msg = this.parse(maxPrecedence);
        }
        this.popExpect(tokenSemicolon);
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
        this.popExpect(tokenThen);
        AstNode branchTrue = this.parse(maxPrecedence);
        AstNode branchFalse = null;
        LocationRange lr = locFromTokenAST(begin, branchTrue);
        if (this.peek().kind == tokenElse) {
          this.pop();
          branchFalse = this.parse(maxPrecedence);
          lr = locFromTokenAST(begin, branchFalse);
        }
        return new AstConditional(lr, cond, branchTrue, branchFalse);
      }
      case tokenFunction: {
        this.pop();
        Token next = this.pop();
        if (next.kind == tokenParenL) {
          IdentifierListResult identifierList = this.parseIdentifierList("function parameter");
          AstNode body = this.parse(maxPrecedence);
          return new AstFunction(locFromTokenAST(begin, body), identifierList.parameters, identifierList.gotComma, body);
        } else {
          throw new StaticErrorException(String.format("Expected ( but got %s", next), next.loc);
        }
      }
      case tokenImport: {
        this.pop();
        AstNode body = this.parse(maxPrecedence);
        if (body instanceof AstLiteralString) {
          return new AstImport(locFromTokenAST(begin, body), ((AstLiteralString) body).value);
        }
        throw new StaticErrorException("Computed imports are not allowed", body.getLoc());
      }
      case tokenLocal: {
        this.pop();
        List<AstLocalBind> binds = new LinkedList<>();
        while (true) {
          this.parseBind(binds);
          Token delim = this.pop();
          if (delim.kind != tokenSemicolon && delim.kind != tokenComma) {
            throw new StaticErrorException(String.format("Expected , or ; but got %s", delim), delim.loc);
          }
          if (delim.kind == tokenSemicolon) {
            break;
          }
        }
        AstNode body = this.parse(maxPrecedence);
        return new AstLocal(locFromTokenAST(begin, body), binds, body);
      }
      default: {
        // Unary operator
        if (begin.kind == tokenOperator) {
          if (!UnaryOp.contains(begin.data)) {
            throw new StaticErrorException(String.format("Not a unary operator: %s", begin.data), begin.loc);
          }
          if (precedence == unaryPrecedence) {
            Token op = this.pop();
            AstNode expr = this.parse(precedence);
            return new AstUnary(locFromTokenAST(op, expr), UnaryOp.valueOf(begin.data), expr);
          }
        }

        // Base case
        if (precedence == 0) {
          return this.parseTerminal();
        }

        AstNode lhs = this.parse(precedence - 1);
        while (true) {
          // Then next token must be a binary operator.
          BinaryOp binaryOp = null;
          // Check precedence is correct for this level.  If we're parsing operators
          // with higher precedence, then return lhs and let lower levels deal with
          // the operator.
          switch (this.peek().kind) {
            case tokenOperator:
              if (this.peek().data.equals(":")) {
                // Special case for the colons in assert. Since COLON is no-longer a
                // special token, we have to make sure it does not trip the
                // op_is_binary test below.  It should terminate parsing of the
                // expression here, returning control to the parsing of the actual
                // assert AST.
                return lhs;
              }
              if (!BinaryOp.contains(this.peek().data)) {
                throw new StaticErrorException(String.format("Not a binary operator: %s", this.peek().data), this.peek().loc);
              }
              binaryOp = BinaryOp.valueOf(this.peek().data);
              if (bopPrecedence.get(binaryOp) != precedence) {
                return lhs;
              }
              break;
            case tokenDot:
            case tokenBraceL:
            case tokenParenL:
            case tokenBracketL:
              if (precedence != applyPrecedence) {
                return lhs;
              }
              break;
            default:
              return lhs;
          }

          assert binaryOp != null;

          Token op = this.pop();
          switch (op.kind) {
            case tokenBracketL:
              AstNode index = this.parse(maxPrecedence);
              Token end = this.popExpect(tokenBracketR);
              lhs = new AstIndex(locFromTokens(begin, end), lhs, index, null);
              break;
            case tokenDot:
              Token fieldID = this.popExpect(tokenIdentifier);
              lhs = new AstIndex(locFromTokens(begin, fieldID), lhs, null, fieldID.data);
              break;
            case tokenParenL:
              CommaListResult commaListResult = this.parseCommaList(tokenParenR, "function argument");
              boolean tailStrict = false;
              if (this.peek().kind == tokenTailStrict) {
                this.pop();
                tailStrict = true;
              }
              lhs = new AstApply(locFromTokens(begin, commaListResult.token), lhs, commaListResult.exprs, commaListResult.gotComma,
                  tailStrict);
              break;
            case tokenBraceL:
              ObjectRemainderResult objectRemainderResult = this.parseObjectRemainder(op);
              lhs = new AstApplyBrace(locFromTokens(begin, objectRemainderResult.lastToken), lhs, objectRemainderResult.node);
              break;
            default:
              AstNode rhs = this.parse(precedence - 1);
              lhs = new AstBinary(locFromTokenAST(begin, rhs), lhs, binaryOp, rhs);
          }
        }
      }
    }
  }

  ObjectAssignResult parseObjectAssignOp() {
    Token op = this.popExpectOp(tokenOperator.name());
    String opStr = op.data;
    boolean plusSugar = opStr.startsWith("+");
    if (plusSugar) {
      opStr = opStr.substring(1);
    }
    AstObjectFieldHide hide;
    switch (opStr) {
      case ":":
        hide = astObjectFieldInherit;
        break;
      case "::":
        hide = AstObjectFieldHide.astObjectFieldHidden;
        break;
      case ":::":
        hide = AstObjectFieldHide.astObjectFieldVisible;
        break;
      default:
        throw new StaticErrorException(String.format("Expected one of :, ::, :::, +:, +::, +:::, got: %s", op.data),
            op.loc);
    }
    return new ObjectAssignResult(hide, plusSugar);
  }

  ObjectRemainderResult parseObjectRemainder(Token token) {
    List<AstObjectField> fields = new LinkedList<>();
    Set<String> literalFields = new HashSet<>();
    Set<String> binds = new HashSet<>();

    boolean gotComma = false;
    boolean isFirstToken = true;
    while (true) {
      Token next = this.pop();
      if (!gotComma && !isFirstToken) {
        if (next.kind == tokenComma) {
          next = this.pop();
          gotComma = true;
        }
      }
      if (next.kind == tokenBraceR) {
        return new ObjectRemainderResult(new AstObject(locFromTokens(token, next), fields, gotComma), next);
      }
      if (next.kind == tokenFor) {
        // It's a comprehension
        if (fields.size() != 1) {
          throw new StaticErrorException("Object comprehension can only have one field.", next.loc);
        }

        int numAsserts = 0;
        for (AstObjectField field : fields) {
          if (field.kind == astObjectAssert) {
            numAsserts++;
          }
        }
        if (numAsserts > 0) {
          throw new StaticErrorException("Object comprehension cannot have asserts.", next.loc);
        }
        AstObjectField field = fields.get(0);
        if (field.hide != astObjectFieldInherit) {
          throw new StaticErrorException("Object comprehensions cannot have hidden fields.", next.loc);
        }
        if (field.kind != astObjectFieldExpr) {
          throw new StaticErrorException("Object comprehensions can only have [e] fields.", next.loc);
        }
        CompSpecsResult compSpecsResult = this.parseComprehensionSpecs(tokenBraceR);
        return new ObjectRemainderResult(
            new AstObjectComp(locFromTokens(token, compSpecsResult.lastToken), fields, gotComma, compSpecsResult.specs),
            compSpecsResult.lastToken);
      }
      if (!gotComma && !isFirstToken) {
        throw new StaticErrorException("Expected a comma before next field.", next.loc);
      }
      isFirstToken = false;

      if (token.kind == tokenBracketL || token.kind == tokenIdentifier || token.kind == tokenStringDouble
          || token.kind == tokenStringSingle || token.kind == tokenStringBlock) {
        AstObjectFieldKind kind;
        AstNode expr1 = null;
        String id = null;
        switch (next.kind) {
          case tokenIdentifier:
            kind = astObjectFieldID;
            id = next.data;
            break;
          case tokenStringDouble:
            kind = astObjectFieldStr;
            expr1 = new AstLiteralString(next.loc, next.data, astStringDouble, null);
            break;
          case tokenStringSingle:
            kind = astObjectFieldStr;
            expr1 = new AstLiteralString(next.loc, next.data, astStringSingle, null);
            break;
          case tokenStringBlock:
            kind = astObjectFieldStr;
            expr1 = new AstLiteralString(next.loc, next.data, astStringBlock, null);
            break;
          default:
            kind = astObjectFieldStr;
            expr1 = this.parse(maxPrecedence);
            this.popExpectOp(tokenBraceR.name());
        }

        boolean hasMethodSugar = false;
        boolean hasMethodComma = false;
        List<String> params = new LinkedList<>();
        if (this.peek().kind == tokenParenL) {
          this.pop();
          IdentifierListResult identifierList = this.parseIdentifierList("method parameter");
          params = identifierList.parameters;
          hasMethodComma = identifierList.gotComma;
          hasMethodSugar = true;
        }

        ObjectAssignResult objectAssign = this.parseObjectAssignOp();
        if (objectAssign.plusSugar && hasMethodSugar) {
          throw new StaticErrorException(String.format("Cannot use +: syntax sugar in a method: %s", next.data), next.loc);
        }
        if (literalFields.contains(next.data)) {
          throw new StaticErrorException(String.format("Duplicate field: %s", next.data), next.loc);
        }
        literalFields.add(next.data);

        AstNode body = this.parse(maxPrecedence);
        fields.add(new AstObjectField(kind, objectAssign.hide, objectAssign.plusSugar, hasMethodSugar, expr1, id, params, hasMethodComma, body, null));
      } else if (token.kind == tokenLocal) {
        Token varID = this.popExpect(tokenIdentifier);
        String id = varID.data;
        if (binds.contains(id)) {
          throw new StaticErrorException(String.format("Duplicate local var: %s", id), varID.loc);
        }
        boolean hasMethodSugar = false;
        boolean hasMethodComma = false;
        List<String> params = new LinkedList<>();
        if (this.peek().kind == tokenParenL) {
          this.pop();
          hasMethodSugar = true;
          IdentifierListResult identifierList = this.parseIdentifierList("function parameter");
          params = identifierList.parameters;
          hasMethodComma = identifierList.gotComma;
        }
        this.popExpectOp("=");
        AstNode body = this.parse(maxPrecedence);
        binds.add(id);
        fields.add(new AstObjectField(astObjectLocal, astObjectFieldVisible, false, hasMethodSugar, null, id, params, hasMethodComma, body, null));
      } else if (token.kind == tokenAssert) {
        AstNode cond = this.parse(maxPrecedence);
        if (this.peek().kind == tokenOperator && this.peek().data.equals(":")) {
          this.pop();
          AstNode msg = this.parse(maxPrecedence);
          fields.add(new AstObjectField(astObjectAssert, astObjectFieldVisible, false, false, null, null, null, false, cond, msg));
        }
      } else {
        throw new UnexpectedException(next, "parsing field definition");
      }
      // reset gotComma
      gotComma = false;
    }
  }

  /* parses for x in expr for y in expr if expr for z in expr ... */
  CompSpecsResult  parseComprehensionSpecs(TokenKind stopTokenKind) {
    List<AstCompSpec> specs = new LinkedList<>();
    while (true) {
      Token varID = this.popExpect(tokenIdentifier);
      String id = varID.data;
      this.popExpect(tokenIn);
      AstNode arr = this.parse(maxPrecedence);
      specs.add(new AstCompSpec(astCompKindFor, id, arr));

      Token maybeIf = this.pop();
      for (; maybeIf.kind == tokenIf; maybeIf = this.pop()) {
        AstNode cond = this.parse(maxPrecedence);
        specs.add(new AstCompSpec(astCompKindIf, null, cond));
      }
      if (maybeIf.kind == stopTokenKind) {
        return new CompSpecsResult(specs, maybeIf);
      }
      if (maybeIf.kind != tokenFor) {
        throw new StaticErrorException(String.format("Expected for, if or %s after for clause, got: %s", stopTokenKind, maybeIf), maybeIf.loc);
      }
    }
  }

  // Assumes that the leading '[' has already been consumed and passed as tok.
  // Should read up to and consume the trailing ']'
  AstNode parseArray(Token token) {
    Token next = this.peek();
    if (next.kind == tokenBraceR) {
      this.pop();
      return new AstArray(token.loc);
    }
    AstNode first = this.parse(maxPrecedence);
    boolean gotComma = false;
    next = this.peek();
    if (next.kind == tokenComma) {
      this.pop();
      next = this.peek();
      gotComma = true;
    }
    if (next.kind == tokenFor) {
      // It's a comprehension
      this.pop();
      CompSpecsResult compSpecsResult = this.parseComprehensionSpecs(tokenBraceR);
      return new AstArrayComp(locFromTokens(token, compSpecsResult.lastToken), first, gotComma, compSpecsResult.specs);
    }

    // Not a comprehension: It can have more elements.
    List<AstNode> elements = new LinkedList<>(Collections.singletonList(first));
    while (true) {
      if (next.kind == tokenBraceR) {
        // TODO (mqliang): SYNTAX SUGAR HERE (preserve comma)
        this.pop();
        break;
      }
      if (!gotComma) {
        throw new StaticErrorException("Expected a comma before next array element.", next.loc);
      }
      AstNode nextElement = this.parse(maxPrecedence);
      elements.add(nextElement);
      next = this.peek();
      if (next.kind == tokenComma) {
        this.pop();
        next = this.peek();
        gotComma = true;
      } else {
        gotComma =false;
      }
    }
    return new AstArray(locFromTokens(token, next), elements, gotComma);
  }

  AstNode parseTerminal() {
    Token token = this.pop();
    if (ImmutableSet.of(
        tokenAssert, tokenBraceR, tokenBracketR, tokenComma, tokenDot, tokenElse,
        tokenError, tokenFor, tokenFunction, tokenIf, tokenIn, tokenImport, tokenImportStr,
        tokenLocal, tokenOperator, tokenParenR, tokenSemicolon, tokenTailStrict, tokenThen
    ).contains(token.kind)) {
      throw new StaticErrorException("parsing terminal", token.loc);
    }
    if (token.kind == tokenEndOfFile) {
      throw new StaticErrorException("Unexpected end of file.", token.loc);
    }

    switch (token.kind) {
      case tokenBraceL:
        return this.parseObjectRemainder(token).node;
      case tokenBracketL:
        return this.parseArray(token);
      case tokenParenL:
        AstNode inner = this.parse(maxPrecedence);
        this.popExpect(tokenParenR);
        return inner;
      // Literals
      case tokenNumber:
        // This shouldn't fail as the lexer should make sure we have good input but
        // we handle the error regardless.
        try {
          double num = Float.parseFloat(token.data);
          return new AstLiteralNumber(token.loc, num, token.data);
        } catch (NumberFormatException e) {
          throw new StaticErrorException("Could not parse floating point number.", token.loc);
        }
      case tokenStringSingle:
        return new AstLiteralString(token.loc, token.data, astStringSingle, null);
      case tokenStringDouble:
        return new AstLiteralString(token.loc, token.data, astStringDouble, null);
      case tokenStringBlock:
        return new AstLiteralString(token.loc, token.data, astStringDouble, token.stringBlockIndent);
      case tokenFalse:
        return new AstLiteralBoolean(token.loc, false);
      case tokenTrue:
        return new AstLiteralBoolean(token.loc, true);
      case tokenNullLit:
        return new AstLiteralNull(token.loc);
      // Variables
      case tokenDollar:
        return new AstDollar(token.loc);
      case tokenIdentifier:
        return new AstVar(token.loc, token.data, token.data);
      case tokenSelf:
        return new AstSelf(token.loc);
      case tokenSuper:
        Token next = this.pop();
        AstNode index = null;
        String id = null;
        switch (next.kind) {
          case tokenDot:
            Token fieldID = this.popExpect(tokenIdentifier);
            id = fieldID.data;
            break;
          case tokenBracketL:
            index = this.parse(maxPrecedence);
            this.popExpect(tokenBracketR);
            break;
          default:
            throw new StaticErrorException("Expected . or [ after super.", token.loc);
        }
        return new AstSuperIndex(token.loc, index, id);
    }
    throw new StaticErrorException(String.format("INTERNAL ERROR: Unknown token kind: %s", token.kind), token.loc);
  }


  void parseBind(List<AstLocalBind> binds) {
    Token varID = this.popExpect(tokenIdentifier);
    for (AstLocalBind bind : binds) {
      if (bind.variable.equals(varID.data)) {
        throw new StaticErrorException(String.format("Duplicate local var: %s", varID.data), varID.loc);
      }
    }
    if (this.peek().kind == tokenParenL) {
      this.pop();
      IdentifierListResult identifierList = this.parseIdentifierList("function parameter");
      this.popExpectOp("=");
      AstNode body = this.parse(maxPrecedence);
      binds.add(new AstLocalBind(varID.data, body, true, identifierList.parameters, identifierList.gotComma));
    } else {
      this.popExpectOp("=");
      AstNode body = this.parse(maxPrecedence);
      binds.add(new AstLocalBind(varID.data, body));
    }
  }


  public AstNode parse(List<Token> tokens) {
    AstNode expr = this.parse(maxPrecedence);
    if (this.peek().kind != tokenEndOfFile) {
      throw new StaticErrorException(String.format("Did not expect: %s", this.peek()), this.peek().loc);
    }
    return expr;
  }

  // -------------------------------------------------------------------------------------------------------------------
  // commaList represents the result when parsing a comma list
  @AllArgsConstructor
  static class CommaListResult {
    Token token;
    List<AstNode> exprs;
    boolean gotComma;
  }

  // -------------------------------------------------------------------------------------------------------------------
  // commaList represents the result when parsing a identifier list
  @AllArgsConstructor
  static class IdentifierListResult {
    List<String> parameters;
    boolean gotComma;
  }

  // -------------------------------------------------------------------------------------------------------------------
  // objectAssign represents the result when parsing a object assign operator
  @AllArgsConstructor
  static class ObjectAssignResult {
    AstObjectFieldHide hide;
    boolean plusSugar;
  }

  @AllArgsConstructor
  static class CompSpecsResult {
    List<AstCompSpec> specs;
    Token lastToken;
  }

  @AllArgsConstructor
  static class ObjectRemainderResult {
    AstNode node;
    Token lastToken;
  }

}
