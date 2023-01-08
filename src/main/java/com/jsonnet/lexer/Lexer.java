package com.jsonnet.lexer;

import com.google.common.collect.ImmutableSet;
import com.jsonnet.common.StaticErrorException;
import com.jsonnet.lexer.fodder.FodderElement;
import com.jsonnet.lexer.fodder.FodderKind;
import java.util.LinkedList;
import java.util.List;

import static com.jsonnet.lexer.LexState.*;
import static com.jsonnet.lexer.TokenKind.*;
import static com.jsonnet.lexer.fodder.FodderKind.fodderCommentCpp;
import static com.jsonnet.lexer.fodder.FodderKind.fodderCommentHash;
import static com.jsonnet.lexer.fodder.FodderKind.fodderWhitespace;


// ---------------------------------------------------------------------------------------------------------------------
// Lexer scans the input file and produces list of matching tokens
public class Lexer {
  static final int lexEOF = -1; // a special rune indicating the lexer has reached the EOF

  String fileName; // The file name being lexed, only used for errors
  String input; // The input string
  int[] runes; // The runes (code point in java terminology) of input string
  // Current position of the lexer
  position currPos;
  // Previous position of the lexer (before previous call to 'next')
  // If this state is lost then prevPos is set to lexEOF and panic ensues.
  position prevPos;
  List<Token> tokens; // The tokens that we've generated so far
  // Information about the token we are working on right now
  List<FodderElement> fodder; // fodder (if there are) around the current token
  int tokenStart; // start rune number of the current token
  Location tokenStartLoc; // location of the first rune of the current token

  public Lexer(String fileName, String input) {
    this.fileName = fileName;
    this.input = input;
    this.runes = input.codePoints().toArray();
    this.tokens = new LinkedList<>();
    this.fodder = new LinkedList<>();
    this.currPos = new position(0, 1, 0);
    this.prevPos = new position(lexEOF, 1, 0);
    this.tokenStartLoc = new Location(1, 1);
  }

  // nextRune returns the next rune (code point in java terminology) in the input.
  private int nextRune() {
    this.prevPos.runeNo = this.currPos.runeNo;
    this.prevPos.lineNo = this.currPos.lineNo;
    this.prevPos.lineStart = this.currPos.lineStart;

    // reached the EOF, return the lexEOF special rune
    if (this.currPos.runeNo >= this.runes.length) {
      return lexEOF;
    }

    // increment the rune cursor
    int rune = this.runes[this.currPos.runeNo];
    this.currPos.runeNo += 1;

    // if encounter a new line, increase the line number
    if (rune == '\n') {
      this.currPos.lineNo += 1;
      this.currPos.lineStart = this.currPos.runeNo;
    }

    return rune;
  }

  // acceptN consume N rune in the input.
  private void acceptN(int n) {
    for (int i = 0; i < n; i++) {
      this.nextRune();
    }
  }

  // peek returns but does not consume the next rune in the input (by moving the cursor one rune back).
  private int peek() {
    int rune = this.nextRune();
    this.stepBack();
    return rune;
  }

  // stepBack steps back one rune. Can only be called once per call of next.
  private void stepBack() {
    if (this.prevPos.runeNo == lexEOF) {
      throw new RuntimeException("backup called with no valid previous rune");
    }
    // move the cursor one rune back
    this.currPos.runeNo = this.prevPos.runeNo;
    this.currPos.lineNo = this.prevPos.lineNo;
    this.currPos.lineStart = this.prevPos.lineStart;
    // set prev runeNo as -1 so that stepBack() can only be called once per call of next
    this.prevPos.runeNo = lexEOF;
  }

  // location returns the location the lexer current working on
  private Location location() {
    return new Location(this.currPos.lineNo, this.currPos.runeNo - this.currPos.lineStart + 1);
  }

  // prevLocation returns the location the lexer previously worked on
  private Location prevLocation() {
    if (this.prevPos.runeNo == lexEOF) {
      throw new RuntimeException("prevLocation called with no valid previous rune");
    }
    return new Location(this.prevPos.lineNo, this.prevPos.runeNo - this.prevPos.lineStart + 1);
  }

  // Reset the current working token start to the current cursor position.
  // - this may throw away some characters.
  // - this does not throw away any accumulated fodder.
  private void resetTokenStart() {
    this.tokenStart = this.currPos.runeNo;
    this.tokenStartLoc = this.location();
  }

  private void emitFullToken(TokenKind kind, String data, String stringBlockIndent, String stringBlockTermIndent) {
    LocationRange locationRange = new LocationRange(this.fileName, this.tokenStartLoc, this.location());
    this.tokens.add((new Token(kind, this.fodder, data, stringBlockIndent, stringBlockTermIndent, locationRange)));
    this.fodder.clear(); // clear fodder if token emitted
  }

  private void emitToken(TokenKind kind) {
    this.emitFullToken(kind, subStringByRuneIndex(this.input, this.tokenStart, this.currPos.runeNo), "", "");
    this.resetTokenStart();
  }

  private void addWhitespaceFodder() {
    String fodderData = subStringByRuneIndex(this.input, this.tokenStart, this.currPos.runeNo);
    if (this.fodder.size() == 0 || this.fodder.get(this.fodder.size() - 1).fodderKind != fodderWhitespace) {
      this.fodder.add(new FodderElement(fodderWhitespace, fodderData));
    } else {
      this.fodder.get(this.fodder.size() - 1).data += fodderData;
    }
    this.resetTokenStart();
  }

  private void addCommentFodder(FodderKind kind) {
    String fodderData = subStringByRuneIndex(this.input, this.tokenStart, this.currPos.runeNo);
    fodder.add(new FodderElement(kind, fodderData));
    this.resetTokenStart();
  }

  private void addCommentCFodder(String data) {
    this.fodder.add(new FodderElement(FodderKind.fodderCommentC, data));
  }

  // lexNumber will consume a number and emit a token.
  // It is assumed that the next rune to be served by the lexer will be a leading digit.
  private void lexNumber() {
    // Note, we deviate from the json.org documentation as follows:
    // There is no reason to lex negative numbers as atomic tokens, it is better to parse them
    // as a unary operator combined with a numeric literal.  This avoids x-1 being tokenized as
    // <identifier> <number> instead of the intended <identifier> <binop> <number>.

    LexState state = numBegin;

    Loop:
    while (true) {
      int rune = this.nextRune();
      switch (state) {
        case numBegin:
          if (rune == '0') {
            state = numAfterZero;
          } else if (isDigit(rune)) {
            state = numAfterOneToNine;
          } else {
            throw new StaticErrorException("Couldn't lex number", this.fileName, this.prevLocation());
          }
          break;

        case numAfterZero:
          if (rune == '.') {
            state = numAfterDot;
          } else if (isScientificNotation(rune)) {
            state = numAfterE;
          } else {
            break Loop;
          }
          break;

        case numAfterOneToNine:
          if (rune == '.') {
            state = numAfterDot;
          } else if (isScientificNotation(rune)) {
            state = numAfterE;
          } else if (isDigit(rune)) {
            state = numAfterOneToNine;
          } else {
            break Loop;
          }
          break;

        case numAfterDot:
          if (isDigit(rune)) {
            state = numAfterDigit;
          } else {
            throw new StaticErrorException(
                String.format("Couldn't lex number, junk after decimal point: '%s'", (char) rune), this.fileName,
                this.prevLocation());
          }
          break;

        case numAfterDigit:
          if (isScientificNotation(rune)) {
            state = numAfterE;
          } else if (isDigit(rune)) {
            state = numAfterDigit;
          } else {
            break Loop;
          }
          break;

        case numAfterE:
          if (isSign(rune)) {
            state = numAfterExpSign;
          } else if (isDigit(rune)) {
            state = numAfterExpDigit;
          } else {
            throw new StaticErrorException(String.format("Couldn't lex number, junk after 'E': '%c'", (char) rune),
                this.fileName, this.prevLocation());
          }
          break;

        case numAfterExpSign:
          if (isDigit(rune)) {
            state = numAfterExpDigit;
          } else {
            throw new StaticErrorException(
                String.format("Couldn't lex number, junk after exponent sign: '%c'", (char) rune), this.fileName,
                this.prevLocation());
          }
          break;

        case numAfterExpDigit:
          if (isDigit(rune)) {
            state = numAfterExpDigit;
          } else {
            break Loop;
          }
          break;
      }
    }

    this.stepBack();
    this.emitToken(tokenNumber);
  }

  // lexIdentifier will consume an identifier and emit a token. It is assumed that the next rune to be served by the
  // lexer will be a leading digit. This may emit a keyword or an identifier.
  private void lexIdentifier() {
    int rune = this.nextRune();
    if (!isIdentifierFirst(rune)) {
      throw new RuntimeException("Unexpected character in lexIdentifier");
    }

    while (!isEOF(rune) && isIdentifier(rune)) {
      rune = this.nextRune();
    }

    this.stepBack();

    switch (subStringByRuneIndex(this.input, this.tokenStart, this.currPos.runeNo)) {
      case "assert":
        this.emitToken(tokenAssert);
        break;
      case "else":
        this.emitToken(tokenElse);
        break;
      case "error":
        this.emitToken(tokenError);
        break;
      case "false":
        this.emitToken(tokenFalse);
        break;
      case "for":
        this.emitToken(tokenFor);
        break;
      case "function":
        this.emitToken(tokenFunction);
        break;
      case "if":
        this.emitToken(tokenIf);
        break;
      case "import":
        this.emitToken(tokenImport);
        break;
      case "importstr":
        this.emitToken(tokenImportStr);
        break;
      case "in":
        this.emitToken(tokenIn);
        break;
      case "local":
        this.emitToken(tokenLocal);
        break;
      case "null":
        this.emitToken(tokenNullLit);
        break;
      case "self":
        this.emitToken(tokenSelf);
        break;
      case "super":
        this.emitToken(tokenSuper);
        break;
      case "tailstrict":
        this.emitToken(tokenTailStrict);
        break;
      case "then":
        this.emitToken(tokenThen);
        break;
      case "true":
        this.emitToken(tokenTrue);
        break;
      default:
        // Not a keyword, assume it is an identifier
        this.emitToken(tokenIdentifier);
    }
  }

  // lexSymbol will lex a token that starts with a symbol. This could be a comment, block quote or an operator.
  // This function assumes that the next rune to be served by the lexer will be the first rune of the new token.
  private void lexSymbol() {
    int rune = this.nextRune();

    // Single line C++ style comment
    if (rune == '/' && this.peek() == '/') {
      this.nextRune();
      this.resetTokenStart(); // Throw out the leading //
      // consume the whole line as it's comments
      while (!isEOF(rune) && !isNewLine(rune)) {
        rune = this.nextRune();
      }

      // Leave the '\n' in the lexer to be fodder for the next round
      this.stepBack();
      this.addCommentFodder(fodderCommentCpp);
      return;
    }

    // python style comment
    if (rune == '#') {
      this.resetTokenStart(); // Throw out the leading #
      // consume the whole line as it's comments
      while (!isEOF(rune) && !isNewLine(rune)) {
        rune = this.nextRune();
      }
      // Leave the '\n' in the lexer to be fodder for the next round
      this.stepBack();
      this.addCommentFodder(fodderCommentHash);
      return;
    }

    // Multi line C++ style comment
    if (rune == '/' && this.peek() == '*') {
      Location commentStartLoc = this.tokenStartLoc;
      this.nextRune(); // consume the '*'
      this.resetTokenStart(); // Throw out the leading /*

      // consume runes until meet the close "*/"
      for (rune = this.nextRune(); ; rune = this.nextRune()) {
        if (isEOF(rune)) {
          throw new StaticErrorException("Multi-line comment has no terminating */", this.fileName, commentStartLoc);
        }
        if (rune == '*' && this.peek() == '/') {
          String commentData =
              subStringByRuneIndex(this.input, this.tokenStart, this.currPos.runeNo - 1); // Don't include trailing */
          this.addCommentCFodder(commentData);
          this.nextRune();            // Skip past '/'
          this.resetTokenStart(); // Start next token at this point
          return;
        }
      }
    }

    // Block literal strings (multiline strings surrounding by "|||")
    if (rune == '|' && subStringByRuneIndex(this.input, this.currPos.runeNo).startsWith("||\n")) {
      Location commentStartLoc = this.tokenStartLoc;
      this.acceptN(3); // Skip "||\n"

      StringBuilder blockStringBuilder = new StringBuilder();
      // Skip leading blank lines
      rune = this.nextRune();
      while (isNewLine(rune)) {
        blockStringBuilder.append(Character.toChars(rune));
        rune = this.nextRune();
      }
      this.stepBack();
      int numWhiteSpace = checkWhitespace(subStringByRuneIndex(this.input, this.currPos.runeNo),
          subStringByRuneIndex(this.input, this.currPos.runeNo));
      String stringBlockIndent =
          subStringByRuneIndex(this.input, this.currPos.runeNo, this.currPos.runeNo + numWhiteSpace);
      if (numWhiteSpace == 0) {
        throw new StaticErrorException("Text block's first line must start with whitespace", this.fileName,
            commentStartLoc);
      }

      // consume runes until meet the close "|||"
      while (true) {
        if (numWhiteSpace <= 0) {
          throw new RuntimeException("Unexpected value for numWhiteSpace");
        }
        this.acceptN(numWhiteSpace);
        for (rune = this.nextRune(); rune != '\n'; rune = this.nextRune()) {
          if (rune == lexEOF) {
            throw new StaticErrorException("Unexpected EOF", this.fileName, commentStartLoc);
          }
          blockStringBuilder.append(Character.toChars(rune));
        }
        blockStringBuilder.append("\n");
        // Skip any blank lines
        for (rune = this.nextRune(); rune == '\n'; rune = this.nextRune()) {
          blockStringBuilder.append(Character.toChars(rune));
        }
        this.stepBack();

        // Look at the next line
        numWhiteSpace = checkWhitespace(stringBlockIndent, subStringByRuneIndex(this.input, this.currPos.runeNo));
        if (numWhiteSpace == 0) {
          // End of the text block
          StringBuilder termIndentStringBuilder = new StringBuilder();
          for (rune = this.nextRune(); isHorizontalWhitespace(rune); rune = this.nextRune()) {
            termIndentStringBuilder.append(String.valueOf(Character.toChars(rune)));
          }
          String stringBlockTermIndent = termIndentStringBuilder.toString();
          this.stepBack();
          if (!subStringByRuneIndex(this.input, this.currPos.runeNo).startsWith("|||")) {
            throw new StaticErrorException("Text block not terminated with |||", this.fileName, commentStartLoc);
          }
          this.acceptN(3); // Skip '|||'
          this.emitFullToken(tokenStringBlock, blockStringBuilder.toString(), stringBlockIndent, stringBlockTermIndent);
          this.resetTokenStart();
          return;
        }
      }
    }

    // Assume any string of symbols is a single operator.
    rune = this.nextRune();
    while (isSymbol(rune)) {
      rune = this.nextRune();
    }

    this.stepBack();
    this.emitToken(tokenOperator);
  }

  public List<Token> lex() {
    for (int rune = this.nextRune(); rune != lexEOF; rune = this.nextRune()) {
      if (isWhitespace(rune)) {
        this.addWhitespaceFodder();
      } else if (rune == '{') {
        this.emitToken(tokenBraceL);
      } else if (rune == '}') {
        this.emitToken(tokenBraceR);
      } else if (rune == '[') {
        this.emitToken(tokenBracketL);
      } else if (rune == ']') {
        this.emitToken(tokenBracketR);
      } else if (rune == ':') {
        this.emitToken(tokenColon);
      } else if (rune == ',') {
        this.emitToken(tokenComma);
      } else if (rune == '$') {
        this.emitToken(tokenDollar);
      } else if (rune == '.') {
        this.emitToken(tokenDot);
      } else if (rune == '(') {
        this.emitToken(tokenParenL);
      } else if (rune == ')') {
        this.emitToken(tokenParenR);
      } else if (rune == ';') {
        this.emitToken(tokenSemicolon);
      } else if (rune == '!') { // Operators
        if (this.peek() == '=') {
          this.nextRune();
        }
        this.emitToken(tokenOperator);
      } else if (rune == '~' || rune == '+' || rune == '-') {
        this.emitToken(tokenOperator);
      } else if (isDigit(rune)) {
        this.stepBack();
        this.lexNumber();
      } else if (rune == '"') {
        Location stringStartLoc = this.prevLocation();
        this.resetTokenStart(); // Don't include the quotes in the token data
        // meet a double quote string literal (e.g "hello world"), consume runes until meet the close "
        for (rune = this.nextRune(); ; rune = this.nextRune()) {
          if (rune == lexEOF) {
            throw new StaticErrorException("Unterminated String", this.fileName, stringStartLoc);
          }
          if (rune == '"') {
            this.stepBack();
            this.emitToken(tokenStringDouble);
            this.nextRune();
            this.resetTokenStart();
            break;
          }
          if (rune == '\\' && this.peek() != lexEOF) {
            this.nextRune();
          }
        }
      } else if (rune == '\'') {
        Location stringStartLoc = this.prevLocation();
        this.resetTokenStart();
        // meet a single quote string literal (e.g 'hello world'), consume runes until meet the close '
        for (rune = this.nextRune(); ; rune = this.nextRune()) {
          if (rune == lexEOF) {
            throw new StaticErrorException("Unterminated String", this.fileName, stringStartLoc);
          }
          if (rune == '\'') {
            this.stepBack();
            this.emitToken(tokenStringSingle);
            this.nextRune();
            this.resetTokenStart();
            break;
          }
          if (rune == '\\' && this.peek() != lexEOF) {
            this.nextRune();
          }
        }
      } else {
        if (isIdentifierFirst(rune)) {
          this.stepBack();
          this.lexIdentifier();
        } else if (isSymbol(rune)) {
          this.stepBack();
          this.lexSymbol();
        } else {
          throw new StaticErrorException(
              String.format("Could not lex the character %s", String.valueOf(Character.toChars(rune))), this.fileName,
              this.prevLocation());
        }
      }
    }

    // We are currently at the EOF.  Emit a special token to capture any trailing fodder
    this.emitToken(tokenEndOfFile);
    return this.tokens;
  }

  // Check that b has at least the same whitespace prefix as a and returns the amount of this whitespace, otherwise
  // returns 0. If a has no whitespace prefix than return 0.
  private int checkWhitespace(String a, String b) {
    int i = 0;
    while (i < a.length()) {
      if (a.charAt(i) != ' ' && a.charAt(i) != '\t') {
        // a has run out of whitespace and b matched up to this point. Return result.
        return i;
      }
      if (i >= b.length()) {
        // We ran off the edge of b while a still has whitespace. Return 0 as failure.
        return 0;
      }
      if (a.charAt(i) != b.charAt(i)) {
        // a has whitespace but b does not. Return 0 as failure.
        return 0;
      }
      i++;
    }
    // We ran off the end of a and b kept up
    return i;
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Helpers

  private String subStringByRuneIndex(String input, int runeIndexStart) {
    int indexStart = input.offsetByCodePoints(0, runeIndexStart);
    return input.substring(indexStart);
  }

  private String subStringByRuneIndex(String input, int runeIndexStart, int runeIndexEnd) {
    int indexStart = input.offsetByCodePoints(0, runeIndexStart);
    int indexEnd = input.offsetByCodePoints(indexStart, runeIndexEnd - runeIndexStart);
    return input.substring(indexStart, indexEnd);
  }

  static boolean isUpper(int rune) {
    return Character.isUpperCase(rune);
  }

  static boolean isLower(int rune) {
    return Character.isLowerCase(rune);
  }

  static boolean isDigit(int rune) {
    return Character.isDigit(rune);
  }

  static boolean isSign(int rune) {
    return rune == '+' || rune == '-';
  }

  static boolean isScientificNotation(int rune) {
    return rune == 'e' || rune == 'E';
  }

  static boolean isIdentifierFirst(int rune) {
    return isUpper(rune) || isLower(rune) || rune == '_';
  }

  static boolean isIdentifier(int rune) {
    return isIdentifierFirst(rune) || isDigit(rune);
  }

  static boolean isEOF(int rune) {
    return rune == lexEOF;
  }

  static boolean isNewLine(int rune) {
    return rune == '\n';
  }

  static boolean isSymbol(int rune) {
    return ImmutableSet.of('!', '$', ':', '~', '+', '-', '&', '|', '^', '=', '<', '>', '*', '/', '%', '#')
        .contains((char) rune);
  }

  static boolean isHorizontalWhitespace(int rune) {
    return rune == ' ' || rune == '\t' || rune == '\r';
  }

  static boolean isWhitespace(int rune) {
    return rune == '\n' || isHorizontalWhitespace(rune);
  }

  // -------------------------------------------------------------------------------------------------------------------
  // position represents the position of a rune (line number, column number, rune number) in the src file
  static class position {
    int runeNo; // Column number
    int lineNo; // Line number
    int lineStart; // Rune number of the last newline

    public position(int runeNo, int lineNo, int lineStart) {
      this.runeNo = runeNo;
      this.lineNo = lineNo;
      this.lineStart = lineStart;
    }
  }
}
