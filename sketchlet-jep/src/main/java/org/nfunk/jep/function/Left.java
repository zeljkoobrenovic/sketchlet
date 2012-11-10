/* @author rich
 * Created on 18-Nov-2003
 */
package org.nfunk.jep.function;

import org.nfunk.jep.*;
import org.nfunk.jep.type.*;

public class Left extends PostfixMathCommand implements CallbackEvaluationI {

    public Left() {
        super();
        numberOfParameters = -1;
    }

    public boolean checkNumberOfParameters(int n) {
        return (n == 2);
    }

    /**
     *
     */
    public Object evaluate(Node node, EvaluatorI pv) throws ParseException {
        int num = node.jjtGetNumChildren();
        if (!checkNumberOfParameters(num)) {
            throw new ParseException("Left operator must have 2");
        }

        Object objString = pv.eval(node.jjtGetChild(0));
        Object objSize = pv.eval(node.jjtGetChild(1));

        if (num == 2 && objString instanceof String && objSize instanceof Number) {
            String s = objString.toString();
            int start = ((Number) objSize).intValue();
            if (start >= s.length()) {
                return s;
            } else {
                return s.substring(0, start);
            }
        }
        throw new ParseException("Condition in if operators are not good");
    }
}
