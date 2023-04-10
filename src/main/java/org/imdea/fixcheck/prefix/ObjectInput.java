package org.imdea.fixcheck.prefix;

import com.github.javaparser.ast.expr.Expression;

import java.util.Objects;

/**
 * ObjectInput class: represents inputs that are objects.
 * @author Facundo Molina
 */
public class ObjectInput extends Input {

  private final Expression expr; //

  public ObjectInput(Class<? extends Expression> type, Expression expr) {
    super(type);
    this.expr = Objects.requireNonNull(expr, "expr cannot be null");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ObjectInput)) return false;
    ObjectInput that = (ObjectInput) o;
    return type.equals(that.getType()) && expr.equals(that.expr);
  }

  @Override
  public Expression getExpression() { return expr; }

  @Override
  public String toString() {
    return "ObjectInput: "+expr;
  }

}
