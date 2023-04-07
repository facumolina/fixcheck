package org.imdea.fixcheck.transform.input;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.model.SymbolReference;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.PrefixTransformer;
import org.imdea.fixcheck.transform.common.TransformationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Input Transformer class: transform a prefix by changing its 'input'.
 * @author Facundo Molina
 */
public class InputTransformer extends PrefixTransformer {

  private static final String baseClassName = "SimilarPrefixInputTransformer"; // Base name for the new prefix class
  private static final String basePrefixName = "similarPrefix"; // Base name for the new prefix method

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
    // Replace the input
    replaceInput(newMethod);
    // Transformed prefix
    Prefix transformedPrefix = new Prefix(newMethod, newCompilationUnit, prefix);
    transformedPrefix.setClassName(className);
    return transformedPrefix;
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
    Expression inputExpr;
    Class<? extends Expression> classToSearch;
    if (InputHelper.isKnownClass(Properties.INPUTS_CLASS)) {
      classToSearch = getClassForInput();
      inputExpr = getRandomExpressionKnownInput(methodDecl, classToSearch);
    } else {
      inputExpr = getRandomExpressionUnknownInput(methodDecl);
      SymbolReference ref = getTypeInDeclaration(inputExpr);
      classToSearch = inputExpr.getClass();
    }

    String previousExpr = inputExpr.toString();
    // Get a value for the new input
    Object value = InputHelper.getValueForType(inputExpr.getClass());
    // Replace the value in the expression
    TransformationHelper.replace(inputExpr, value);
    lastTransformation = "[" + previousExpr + ":" + classToSearch.getSimpleName() +"] replaced by [" + inputExpr + ":" + value.getClass().getSimpleName()+"]";
    transformationsApplied++;
  }

  /**
   * Get a random Expression for a known input class
   * @param methodDecl method to search
   * @param classToSearch class to search
   * @return Random Expression for the input class
   */
  private Expression getRandomExpressionKnownInput(MethodDeclaration methodDecl, Class<? extends Expression> classToSearch) {
    // Find the expressions that inherit the class NodeWithType
    List<? extends Expression> allInputsOfType = methodDecl.findAll(classToSearch);
    if (allInputsOfType.isEmpty()) throw new IllegalArgumentException("No locals of type " + Properties.INPUTS_CLASS);
    Random random = new Random();
    int index = random.nextInt(allInputsOfType.size());
    return allInputsOfType.get(index);
  }

  /**
   * Get a random Expression for an unknown input class
   * @param methodDecl method to search
   * @return Random Expression for the input class
   */
  private Expression getRandomExpressionUnknownInput(MethodDeclaration methodDecl) {
    List<Expression> expressionsWithType = methodDecl.findAll(Expression.class).stream().filter(NodeWithType.class::isInstance).collect(Collectors.toList());
    if (expressionsWithType.isEmpty()) throw new IllegalArgumentException("No expressions with type");
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
    return expressionsWithInputType.get(index);
  }

  /**
   * Get the type in which the given expression is being used
   * @param expr Expression to get the type
   * @return Type in which the expression is being used
   */
  private SymbolReference getTypeInDeclaration(Expression expr) {
    Node parent = expr.getParentNode().get();
    if (parent instanceof ObjectCreationExpr) {
      ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) parent;
      TypeSolver typeSolver = new ReflectionTypeSolver();
      JavaParserFacade javaParserFacade = JavaParserFacade.get(typeSolver);
      SymbolReference resolution = javaParserFacade.solve(objectCreationExpr);
      System.out.println(resolution.getCorrespondingDeclaration());
      return resolution;
    }
    throw new IllegalArgumentException("Don't know how to get the actual type for expression: " + expr.getClass().getName());
  }

  /**
   * Get the class for the given type
   * @return Class for the current input type
   */
  private Class<? extends Expression> getClassForInput() {
    if (InputHelper.INPUTS_BY_TYPE.containsKey(Properties.INPUTS_CLASS)) {
      List<Class<? extends Expression>> possibleInputs = InputHelper.INPUTS_BY_TYPE.get(Properties.INPUTS_CLASS);
      Random random = new Random();
      int index = random.nextInt(possibleInputs.size());
      return possibleInputs.get(index);
    }
    throw new IllegalArgumentException("Input type not supported: " + Properties.INPUTS_CLASS);
  }

}