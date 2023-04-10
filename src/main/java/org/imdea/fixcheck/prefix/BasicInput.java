package org.imdea.fixcheck.prefix;

import com.github.javaparser.ast.expr.Expression;

/**
 * BasicInput class: represents inputs that of basic types, such as int, boolean, etc.
 * @author Facundo Molina
 */
public class BasicInput extends Input {

  private final Expression expr;

  public BasicInput(Class<? extends Expression> type, Expression expr) {
    super(type);
    this.expr = expr;
  }

  @Override
  public Expression getExpression() { return expr; }

  @Override
  public boolean isBasic() {
    return true;
  }

  @Override
  public String toString() {
    return "BasicInput: "+expr;
  }

}
