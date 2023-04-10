package org.imdea.fixcheck.prefix;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedDeclaration;
import com.github.javaparser.resolution.model.SymbolReference;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserConstructorDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

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
  public boolean isBasic() {
    return false;
  }

  @Override
  public String toString() {
    return "ObjectInput: "+expr;
  }

  /**
   * Get the type in which the current expression is being used
   * @return Type in which the expression is being used
   */
  public String getTypeInDeclaration() {
    Node parent = expr.getParentNode().get();
    if (parent instanceof ObjectCreationExpr) {
      ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) parent;
      TypeSolver typeSolver = new ReflectionTypeSolver();
      JavaParserFacade javaParserFacade = JavaParserFacade.get(typeSolver);
      SymbolReference resolution = javaParserFacade.solve(objectCreationExpr);
      ResolvedDeclaration resolvedDeclaration = resolution.getCorrespondingDeclaration();
      if (resolvedDeclaration instanceof JavaParserConstructorDeclaration) {
        JavaParserConstructorDeclaration javaParserConstructorDeclaration = (JavaParserConstructorDeclaration) resolvedDeclaration;
        String typeName = javaParserConstructorDeclaration.getWrappedNode().getParameter(0).getTypeAsString();
        return typeName;
      }
    }
    throw new IllegalArgumentException("Don't know how to get the actual type for expression: " + expr.getClass().getName());
  }

}
