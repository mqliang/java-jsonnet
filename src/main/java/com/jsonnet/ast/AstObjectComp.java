package com.jsonnet.ast;

import java.util.LinkedList;
import java.util.List;


// AstObjectComp represents object comprehension: { [e]: e for x in e for.. if... }.
public class AstObjectComp {
  List<AstObjectField> fields = new LinkedList<>();
  boolean trailingComma;
  List<AstCompSpec> specs = new LinkedList<>();
}
