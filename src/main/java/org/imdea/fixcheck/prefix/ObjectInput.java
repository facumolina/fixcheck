package org.imdea.fixcheck.prefix;

import com.github.javaparser.ast.expr.Expression;
import soot.Local;
import soot.Value;

import java.util.Objects;

/**
 * ObjectInput class: represents inputs that are objects.
 * @author Facundo Molina
 */
public class ObjectInput extends Input {

  private final Expression expr; //

  public ObjectInput(String typeName, Expression expr) {
    super(typeName);
    this.expr = Objects.requireNonNull(expr, "expr cannot be null");
  }

  /**
   * Get the Soot local
   * @return Soot local
   */

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ObjectInput)) return false;
    ObjectInput that = (ObjectInput) o;
    return typeName.equals(that.getType()) && expr.equals(that.expr);
  }

  @Override
  public Expression getExpression() { return expr; }

  @Override
  public String toString() {
    return "ObjectInput: "+expr;
  }

}
