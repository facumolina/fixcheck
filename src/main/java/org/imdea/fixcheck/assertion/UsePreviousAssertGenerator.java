package org.imdea.fixcheck.assertion;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.common.TransformationHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UsePreviousAssertGenerator class: assertion generator that uses the same assertion as the parent prefix.
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class UsePreviousAssertGenerator extends AssertionGenerator {

  @Override
  public void generateAssertions(Prefix prefix) {
    // Take the method in the parent prefix
    MethodDeclaration parentMethod = prefix.getParent().getMethod();
    // Take assertions from the parent method
    List<ExpressionStmt> originalAssertions = getAssertions(parentMethod);
    System.out.println("---> Original assertions to use: " + originalAssertions.size());
    System.out.println(originalAssertions);

    // NOTE: Assertions are no longer appended in this step, since removal is not done when using previous assertions
    //MethodDeclaration method = prefix.getMethod();
    //originalAssertions.forEach(assertion -> method.getBody().get().addStatement(assertion));

    // Update the class name
    updateClassName(prefix);
  }

  /**
   * Returns the assert statements present in the given method declaration.
   */
  private List<ExpressionStmt> getAssertions(MethodDeclaration methodDeclaration) {
    return methodDeclaration.getBody().get().getStatements().stream()
        .filter(stmt -> stmt instanceof ExpressionStmt)
        .map(stmt -> (ExpressionStmt) stmt)
        .filter(stmt -> stmt.getExpression() instanceof MethodCallExpr)
        .filter(stmt -> {
          MethodCallExpr methodCallExpr = (MethodCallExpr) stmt.getExpression();
          return methodCallExpr.getNameAsString().equals("assertNotNull")
              || methodCallExpr.getNameAsString().equals("assertTrue")
              || methodCallExpr.getNameAsString().equals("assertFalse")
              || methodCallExpr.getNameAsString().equals("assertEquals")
              || methodCallExpr.getNameAsString().equals("assertNotEquals")
              // Used in JUnit 3
              || methodCallExpr.getNameAsString().equals("fail")
              || methodCallExpr.getNameAsString().equals("check");
        })
        .collect(Collectors.toList());
  }

  /**
   * Update the class name with a new name
   * @param prefix Prefix to update
   */
  private void updateClassName(Prefix prefix) {
    String currentClassName = prefix.getClassName();
    String newClassName = currentClassName + "withPreviousAssertion";
    prefix.setClassName(newClassName);
    CompilationUnit compilationUnit = prefix.getMethodCompilationUnit();
    compilationUnit.getClassByName(currentClassName).get().setName(newClassName);
    TransformationHelper.updateCompilationUnitNames(compilationUnit, currentClassName, newClassName);
  }
}
