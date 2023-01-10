package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.List;


// AstObjectComp represents object comprehension: { [e]: e for x in e for.. if... }.
public class AstObjectComp extends AstBaseNode {
  List<AstObjectField> fields;
  boolean trailingComma;
  List<AstCompSpec> specs;

  public AstObjectComp(LocationRange loc, List<AstObjectField> fields, boolean trailingComma, List<AstCompSpec> specs) {
    super(loc);
    this.fields = fields;
    this.trailingComma = trailingComma;
    this.specs = specs;
  }
}
