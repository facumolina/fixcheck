package org.imdea.fixcheck.transform.input;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.prefix.BasicInput;
import org.imdea.fixcheck.prefix.Input;
import org.imdea.fixcheck.prefix.ObjectInput;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.PrefixTransformer;
import org.imdea.fixcheck.transform.common.TransformationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * InputTransformer class: transform a prefix by changing its 'input'.
 * @author Facundo Molina
 */
public class InputTransformer extends PrefixTransformer {

  private static final String baseClassName = "SimilarPrefixInputTransformer"; // Base name for the new prefix class

  private int transformationsApplied; // Number of transformations applied

  private String lastTransformation; // Last transformation applied

  public InputTransformer() {
    transformationsApplied = 0;
    lastTransformation = "";
  }

  @Override
  public Prefix transform(Prefix prefix) {
    CompilationUnit prefixCompilationUnit = prefix.getMethodCompilationUnit();
    MethodDeclaration prefixMethod = prefix.getMethod();
    // Prepare the new class
    String className = baseClassName + transformationsApplied;
    CompilationUnit newCompilationUnit = TransformationHelper.initializeTransformedClass(className, prefixCompilationUnit);
    // Prepare the new method body
    MethodDeclaration newMethod = TransformationHelper.getMethodDeclFromCompilationUnit(newCompilationUnit, prefixMethod.getNameAsString());
    // Remove assert statements from the method containing org.junit.Assert or org.junit.TestCase*assert
    removeAssertionsFromMethod(newMethod);
    // Replace the input
    replaceInput(newMethod);
    // Transformed prefix
    Prefix transformedPrefix = new Prefix(newMethod, newCompilationUnit, prefix);
    transformedPrefix.setClassName(className);
    return transformedPrefix;
  }

  /**
   * Remove assert statements from the method containing org.junit.Assert
   */
  private static void removeAssertionsFromMethod(MethodDeclaration methodDecl) {
    // Remove the assertion statements from the method declaration
    methodDecl.getBody().get().getStatements().removeIf(stmt -> {
      if (stmt instanceof ExpressionStmt) {
        ExpressionStmt exprStmt = (ExpressionStmt) stmt;
        if (exprStmt.getExpression() instanceof MethodCallExpr) {
          MethodCallExpr methodCallExpr = (MethodCallExpr) exprStmt.getExpression();
          return methodCallExpr.getNameAsString().equals("assertNotNull")
              || methodCallExpr.getNameAsString().equals("assertTrue")
              || methodCallExpr.getNameAsString().equals("assertFalse")
              || methodCallExpr.getNameAsString().equals("assertEquals");
        }
      }
      return false;
    });
  }

  @Override
  public String getLastTransformation() {
    return lastTransformation;
  }

  /**
   * Replace a randomly selected input within the method.
   * @param methodDecl is the method declaration to be transformed.
   */
  private void replaceInput(MethodDeclaration methodDecl) {
    if (!methodDecl.getBody().isPresent()) throw new IllegalArgumentException("Method body is not present");
    // Get an input expression to be replaced
    Input input;
    Class<? extends Expression> classToSearch;
    if (InputHelper.isKnownClass(Properties.INPUTS_CLASS)) {
      classToSearch = getClassForNewInput(Properties.INPUTS_CLASS);
      input = getRandomInputKnownType(methodDecl, classToSearch);
    } else {
      ObjectInput objInput = getRandomInputUnknownType(methodDecl);
      String typeName = objInput.getTypeInDeclaration();
      classToSearch = getClassForNewInput(typeName);
      input = objInput;
    }
    // Save the previous expression
    String previousExpr = input.getExpression().toString();
    // Get a value for the new input
    Object value = InputHelper.getValueForType(classToSearch);
    // Replace the value in the expression
    TransformationHelper.replace(input, classToSearch, value);
    lastTransformation = "[" + previousExpr + ":" + Properties.INPUTS_CLASS +"] replaced by [" + value + ":" + value.getClass().getName() +"]";
    transformationsApplied++;
  }

  /**
   * Get a random input for a known type
   * @param methodDecl method to search
   * @param classToSearch class to search
   * @return Random input for the known class
   */
  private Input getRandomInputKnownType(MethodDeclaration methodDecl, Class<? extends Expression> classToSearch) {
    // Find the expressions that inherit the class NodeWithType
    List<? extends Expression> allInputsOfType = methodDecl.findAll(classToSearch);
    if (allInputsOfType.isEmpty()) throw new IllegalArgumentException("No locals of type " + Properties.INPUTS_CLASS);
    Random random = new Random();
    int index = random.nextInt(allInputsOfType.size());
    return new BasicInput(classToSearch,allInputsOfType.get(index));
  }

  /**
   * Get a random input for an unknown type
   * @param methodDecl method to search
   * @return Random input for the unknown class
   */
  private ObjectInput getRandomInputUnknownType(MethodDeclaration methodDecl) {
    List<Expression> expressionsWithType = methodDecl.findAll(Expression.class).stream().filter(NodeWithType.class::isInstance).collect(Collectors.toList());
    if (expressionsWithType.isEmpty()) throw new IllegalArgumentException("The method declaration has no typed expressions");
    List<Expression> expressionsWithInputType = new ArrayList<>();
    for (Expression expr : expressionsWithType) {
      if (expr instanceof NodeWithType) {
        NodeWithType nodeWithType = (NodeWithType) expr;
        if (nodeWithType.getType().toString().equals(Properties.INPUTS_CLASS)) {
          expressionsWithInputType.add(expr);
        }
      }
    }
    if (expressionsWithInputType.isEmpty()) throw new IllegalArgumentException("No expressions with type " + Properties.INPUTS_CLASS);
    Random random = new Random();
    int index = random.nextInt(expressionsWithInputType.size());
    Expression expr = expressionsWithInputType.get(index);
    return new ObjectInput(expr.getClass(),expr);
  }

  /**
   * Get the class for the given type
   * @param typeName Type to get the class for
   * @return Class for the given type
   */
  private Class<? extends Expression> getClassForNewInput(String typeName) {
    if (InputHelper.INPUTS_BY_TYPE.containsKey(typeName)) {
      List<Class<? extends Expression>> possibleInputs = InputHelper.INPUTS_BY_TYPE.get(typeName);
      Random random = new Random();
      int index = random.nextInt(possibleInputs.size());
      return possibleInputs.get(index);
    }
    throw new IllegalArgumentException("Type not supported: " + typeName);
  }

}