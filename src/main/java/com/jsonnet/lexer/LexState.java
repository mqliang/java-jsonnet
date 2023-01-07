package com.jsonnet.lexer;

// LexState defines the state when use a finite state machine to parse a number literal
enum LexState {
  numBegin,
  numAfterZero,
  numAfterOneToNine,
  numAfterDot,
  numAfterDigit,
  numAfterE,
  numAfterExpSign,
  numAfterExpDigit
}
