package com.jsonnet;

import com.jsonnet.common.StaticErrorException;
import com.jsonnet.lexer.Lexer;
import com.jsonnet.lexer.Token;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TestParser {
  List<String> happyCases = new LinkedList<>() {{
    add("true");
    add("1");
    add("1.2e3");
    add("!true");
    add("null");

    add("$.foo.bar");
    add("self.foo.bar");
    add("super.foo.bar");
    add("super[1]");
    add("error \"Error!\"");

    add("\"world\"");
    add("'world'");
    add("|||\n" + "   world\n" + "|||");

    add("foo(bar)");
    add("foo(bar) tailstrict");
    add("foo.bar");
    add("foo[bar]");

    add("true || false");
    add("0 && 1 || 0");
    add("0 && (1 || 0)");

    add("local foo = \"bar\"; foo");
    add("local foo(bar) = bar; foo(1)");
    add("{ local foo = \"bar\", baz: 1}");
    add("{ local foo(bar) = bar, baz: foo(1)}");

    add("{ foo(bar, baz): bar+baz }");

    add("{ [\"foo\" + \"bar\"]: 3 }");
    add("{ [\"field\" + x]: x for x in [1, 2, 3] }");
    add("{ local y = x, [\"field\" + x]: x for x in [1, 2, 3] }");
    add("{ [\"field\" + x]: x for x in [1, 2, 3] if x <= 2 }");
    add("{ [\"field\" + x + y]: x + y for x in [1, 2, 3] if x <= 2 for y in [4, 5, 6]}");

    add("[]");
    add("[a, b, c]");
    add("[x for x in [1,2,3] ]");
    add("[x for x in [1,2,3] if x <= 2]");
    add("[x+y for x in [1,2,3] if x <= 2 for y in [4, 5, 6]]");

    add("{}");
    add("{ hello: \"world\" }");
    add("{ hello +: \"world\" }");
    add("{\n" + "  hello: \"world\",\n" + "\t\"name\":: joe,\n" + "\t'mood'::: \"happy\",\n" + "\t|||\n"
        + "\t  key type\n" + "|||: \"block\",\n" + "}");

    add("assert true: 'woah!'; true");
    add("{ assert true: 'woah!', foo: bar }");

    add("if n > 1 then 'foos' else 'foo'");

    add("local foo = function(x) x + 1; true");

    add("import 'foo.jsonnet'");
    add("importstr 'foo.text'");

    add("{a: b} + {c: d}");
    add("{a: b}{c: d}");
  }};

  List<errorCase> errorCases = new LinkedList<>() {{
    add(new errorCase("function(a, b c)", "test:1:15-16 Expected a comma before next function parameter."));
    add(new errorCase("function(a, 1)", "test:1:13-14 Expected simple identifier but got a complex expression."));
    add(new errorCase("a b", "test:1:3-4 Did not expect: (IDENTIFIER, \"b\")"));
    add(new errorCase("foo(a, bar(a b))", "test:1:14-15 Expected a comma before next function argument."));

    add(new errorCase("local", "test:1:6 Expected token IDENTIFIER but got end of file"));
    add(new errorCase("local foo = 1, foo = 2; true", "test:1:16-19 Duplicate local var: foo"));
    add(new errorCase("local foo(a b) = a; true", "test:1:13-14 Expected a comma before next function parameter."));
    add(new errorCase("local foo(a): a; true", "test:1:13-14 Expected operator = but got \":\""));
    add(new errorCase("local foo(a) = bar(a b)); true", "test:1:22-23 Expected a comma before next function argument."));
    add(new errorCase("local foo: 1; true", "test:1:10-11 Expected operator = but got \":\""));
    add(new errorCase("local foo = bar(a b)); true", "test:1:19-20 Expected a comma before next function argument."));

    add(new errorCase("{a b}", "test:1:4-5 Expected token OPERATOR but got (IDENTIFIER, \"b\")"));
    add(new errorCase("{a = b}", "test:1:4-5 Expected one of :, ::, :::, +:, +::, +:::, got: ="));
    add(new errorCase("{a :::: b}", "test:1:4-8 Expected one of :, ::, :::, +:, +::, +:::, got: ::::"));

    add(new errorCase("{assert x for x in [1, 2, 3]}", "test:1:11-14 Object comprehension cannot have asserts."));
    add(new errorCase("{['foo' + x]: true, [x]: x for x in [1, 2, 3]}", "test:1:28-31 Object comprehension can only have one field."));
    add(new errorCase("{foo: x for x in [1, 2, 3]}", "test:1:9-12 Object comprehensions can only have [e] fields."));
    add(new errorCase("{[x]:: true for x in [1, 2, 3]}", "test:1:13-16 Object comprehensions cannot have hidden fields."));
    add(new errorCase("{[x]: true for 1 in [1, 2, 3]}", "test:1:16-17 Expected token IDENTIFIER but got (NUMBER, \"1\")"));
    add(new errorCase("{[x]: true for x at [1, 2, 3]}", "test:1:18-20 Expected token in but got (IDENTIFIER, \"at\")"));
    add(new errorCase("{[x]: true for x in [1, 2 3]}", "test:1:27-28 Expected a comma before next array element."));
    add(new errorCase("{[x]: true for x in [1, 2, 3] if (a b)}", "test:1:37-38 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("{[x]: true for x in [1, 2, 3] if a b}", "test:1:36-37 Expected for, if or \"}\" after for clause, got: (IDENTIFIER, \"b\")"));

    add(new errorCase("{a: b c:d}", "test:1:7-8 Expected a comma before next field."));

    add(new errorCase("{[(x y)]: z}", "test:1:6-7 Expected token \")\" but got (IDENTIFIER, \"y\")"));
    add(new errorCase("{[x y]: z}", "test:1:5-6 Expected token \"]\" but got (IDENTIFIER, \"y\")"));

    add(new errorCase("{foo(x y): z}", "test:1:8-9 Expected a comma before next method parameter."));
    add(new errorCase("{foo(x)+: z}", "test:1:2-5 Cannot use +: syntax sugar in a method: foo"));
    add(new errorCase("{foo: 1, foo: 2}", "test:1:10-13 Duplicate field: foo"));
    add(new errorCase("{foo: (1 2)}", "test:1:10-11 Expected token \")\" but got (NUMBER, \"2\")"));

    add(new errorCase("{local 1 = 3, true}", "test:1:8-9 Expected token IDENTIFIER but got (NUMBER, \"1\")"));
    add(new errorCase("{local foo = 1, local foo = 2, true}", "test:1:23-26 Duplicate local var: foo"));
    add(new errorCase("{local foo(a b) = 1, a: true}", "test:1:14-15 Expected a comma before next function parameter."));
    add(new errorCase("{local foo(a): 1, a: true}", "test:1:14-15 Expected operator = but got \":\""));
    add(new errorCase("{local foo(a) = (a b), a: true}", "test:1:20-21 Expected token \")\" but got (IDENTIFIER, \"b\")"));

    add(new errorCase("{assert (a b), a: true}", "test:1:12-13 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("{assert a: (a b), a: true}", "test:1:15-16 Expected token \")\" but got (IDENTIFIER, \"b\")"));

    add(new errorCase("{function(a, b) a+b: true}", "test:1:2-10 Unexpected: (function, \"function\") while parsing field definition"));

    add(new errorCase("[(a b), 2, 3]", "test:1:5-6 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("[1, (a b), 2, 3]", "test:1:8-9 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("[a for b in [1 2 3]]", "test:1:16-17 Expected a comma before next array element."));

    add(new errorCase("for", "test:1:1-4 Unexpected: (for, \"for\") while parsing terminal"));
    add(new errorCase("", "test:1:1 Unexpected end of file."));
    add(new errorCase("((a b))", "test:1:5-6 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("a.1", "test:1:3-4 Expected token IDENTIFIER but got (NUMBER, \"1\")"));
    add(new errorCase("super.1", "test:1:7-8 Expected token IDENTIFIER but got (NUMBER, \"1\")"));
    add(new errorCase("super[(a b)]", "test:1:10-11 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("super[a b]", "test:1:9-10 Expected token \"]\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("super", "test:1:1-6 Expected . or [ after super."));

    add(new errorCase("assert (a b)); true", "test:1:11-12 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("assert a: (a b)); true", "test:1:14-15 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("assert a: 'foo', true", "test:1:16-17 Expected token \";\" but got (\",\", \",\")"));
    add(new errorCase("assert a: 'foo'; (a b)", "test:1:21-22 Expected token \")\" but got (IDENTIFIER, \"b\")"));

    add(new errorCase("error (a b)", "test:1:10-11 Expected token \")\" but got (IDENTIFIER, \"b\")"));

    add(new errorCase("if (a b) then c", "test:1:7-8 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("if a b c", "test:1:6-7 Expected token then but got (IDENTIFIER, \"b\")"));
    add(new errorCase("if a then (b c)", "test:1:14-15 Expected token \")\" but got (IDENTIFIER, \"c\")"));
    add(new errorCase("if a then b else (c d)", "test:1:21-22 Expected token \")\" but got (IDENTIFIER, \"d\")"));

    add(new errorCase("function(a) (a b)", "test:1:16-17 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("function a a", "test:1:10-11 Expected ( but got (IDENTIFIER, \"a\")"));

    add(new errorCase("import (a b)", "test:1:11-12 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("import (a+b)", "test:1:9-12 Computed imports are not allowed"));
    add(new errorCase("importstr (a b)", "test:1:14-15 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("importstr (a+b)", "test:1:12-15 Computed imports are not allowed"));

    add(new errorCase("local a = b ()", "test:1:15 Expected , or ; but got end of file"));
    add(new errorCase("local a = b; (a b)", "test:1:17-18 Expected token \")\" but got (IDENTIFIER, \"b\")"));

    add(new errorCase("1+ <<", "test:1:4-6 Not a unary operator: <<"));
    add(new errorCase("-(a b)", "test:1:5-6 Expected token \")\" but got (IDENTIFIER, \"b\")"));
    add(new errorCase("1~2", "test:1:2-3 Not a binary operator: ~"));

    add(new errorCase("a[(b c)]", "test:1:6-7 Expected token \")\" but got (IDENTIFIER, \"c\")"));
    add(new errorCase("a[b c]", "test:1:5-6 Expected token \"]\" but got (IDENTIFIER, \"c\")"));

    add(new errorCase("a{b c}", "test:1:5-6 Expected token OPERATOR but got (IDENTIFIER, \"c\")"));
  }};

  @Test
  public void testParserHappyCase() {
    for (int i = 0; i < happyCases.size(); i++) {
      Lexer lexer = new Lexer("test_" + i, happyCases.get(i));
      List<Token> tokens = lexer.lex();
      System.out.println(tokens);
    }
  }

  @Test
  public void testParserErrorCase() {
    for (int i = 0; i < errorCases.size(); i++) {
      try {
        Lexer lexer = new Lexer("test_" + i, errorCases.get(i).input);
        lexer.lex();
        Assert.fail(String.format("Case %d failed, exception expected, but succeeded", i));
      } catch (StaticErrorException e) {
        if (!e.toString().equals(errorCases.get(i).error)) {
          Assert.fail(String.format("Case %d failed, expected error message: %s, got: %s", i, errorCases.get(i).error, e));
        }
      }
    }
  }

  @AllArgsConstructor
  static class errorCase {
    String input;
    String error;
  }
}
