package org.imdea.fixcheck.transform.sequence;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.PrefixTransformer;

/**
 * SequenceTransformer class: transform a prefix by performing a change in the sequence of method calls.
 * @author Facundo Molina
 */
public class SequenceTransformer extends PrefixTransformer {

  @Override
  public Prefix transform(Prefix prefix) {
    MethodDeclaration methodDeclaration = prefix.getMethod();
    //ethodDeclaration.accept();
    return null;
  }

  @Override
  public String getLastTransformation() {
    return null;
  }
}
