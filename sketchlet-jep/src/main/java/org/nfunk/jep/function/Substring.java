/* @author rich
 * Created on 18-Nov-2003
 */
package org.nfunk.jep.function;

import org.nfunk.jep.*;

public class Substring extends PostfixMathCommand implements CallbackEvaluationI {

    public Substring() {
        super();
        numberOfParameters = -1;
    }

    public boolean checkNumberOfParameters(int n) {
        return (n == 2 || n == 3);
    }

    /**
     *
     */
    public Object evaluate(Node node, EvaluatorI pv) throws ParseException {
        int num = node.jjtGetNumChildren();
        if (!checkNumberOfParameters(num)) {
            throw new ParseException("Substring operator must have 2 or 3 arguments.");
        }

        Object objString = pv.eval(node.jjtGetChild(0));
        Object objBeginIndex = pv.eval(node.jjtGetChild(1));

        if (num == 3) {
            Object objEndIndex = pv.eval(node.jjtGetChild(2));
            if (objString instanceof String && objBeginIndex instanceof Number && objEndIndex instanceof Number) {
                String s = objString.toString();
                int start = ((Number) objBeginIndex).intValue();
                int end = ((Number) objEndIndex).intValue();

                if (start > end || start >= s.length()) {
                    return s;
                } else if (end >= s.length()) {
                    return s.substring(start);
                } else {
                    return s.substring(start, end);
                }
            }
        } else if (num == 2 && objString instanceof String && objBeginIndex instanceof Number) {
            String s = objString.toString();
            int start = ((Number) objBeginIndex).intValue();
            if (start >= s.length()) {
                return s;
            } else {
                return s.substring(start);
            }
        }
        throw new ParseException("Condition in substring operators are not good");
    }
}
