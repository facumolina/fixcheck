package org.imdea.fixcheck.transform.input.provider;

import soot.Value;
import soot.jimple.IntConstant;

import java.util.Random;

/**
 * Integer Provider class: provides integers for the input transformer.
 * @author Facundo Molina
 */
public class IntegerProvider implements InputProvider {

    private final Random random = new Random();

    @Override
    public Value getInput() {
        return IntConstant.v(random.nextInt(100));
    }

    @Override
    public void addInput(Value value) {
        // Nothing to do
    }

    @Override
    public String toString() {
        return "values: [0 .. 100]";
    }
}
