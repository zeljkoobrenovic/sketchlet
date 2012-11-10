/*****************************************************************************

JEP 2.4.1, Extensions 1.1.1
April 30 2007
(c) Copyright 2007, Nathan Funk and Richard Morris
See LICENSE-*.txt for license information.

 *****************************************************************************/
package org.nfunk.jep.function;

import java.lang.Math;
import java.util.*;
import org.nfunk.jep.*;

/**
 * A PostfixMathCommandI which rounds a number
 * round(pi) finds the closest integer to the argument
 * round(pi,3) rounds the argument to 3 decimal places
 * @author Richard Morris
 *
 */
public class Min extends PostfixMathCommand {

    public Min() {
        numberOfParameters = -1;
    }

    public void run(Stack inStack) throws ParseException {
        checkStack(inStack);// check the stack
        if (this.curNumberOfParameters == 1) {
            Object param = inStack.pop();
            inStack.push(min(param));//push the result on the inStack
        } else {
            Object r = inStack.pop();
            Object l = inStack.pop();
            inStack.push(min(l, r));//push the result on the inStack
        }
        return;
    }

    private Object min(Object num1, Object num2) throws ParseException {
        if (num1 instanceof Number && num2 instanceof Number) {
            double val1 = ((Number) num1).doubleValue();
            double val2 = ((Number) num2).doubleValue();
            return new Double(Math.min(val1, val2));
        }
        throw new ParseException("Invalid parameter type");
    }

    public Object min(Object param) throws ParseException {
        if (param instanceof Number) {
            return new Double(((Number) param).doubleValue());
        }

        throw new ParseException("Invalid parameter type");
    }
}
