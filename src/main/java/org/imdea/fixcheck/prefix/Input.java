package org.imdea.fixcheck.prefix;

import com.github.javaparser.ast.expr.Expression;

import java.util.Objects;

/**
 * Input class: represents an input for a prefix.
 * @author Facundo Molina
 */
public abstract class Input {

  Class<? extends Expression> type;

  /**
   * Constructor
   * @param type Type of the input
   */
  public Input(Class<? extends Expression> type) {
    this.type = Objects.requireNonNull(type, "type cannot be null");
  }

  /**
   * Get the type of the input
   * @return Type of the input
   */
  public Class<? extends Expression> getType() {
    return type;
  }

  /**
   * Get the value of the input
   * @return Value of the input
   */
  public abstract Expression getExpression();

  /**
   * Check if the input is basic
   */
  public abstract boolean isBasic();

}
