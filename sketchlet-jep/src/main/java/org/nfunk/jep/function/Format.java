/* @author rich
 * Created on 18-Nov-2003
 */
package org.nfunk.jep.function;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.nfunk.jep.*;

public class Format extends PostfixMathCommand implements CallbackEvaluationI {

    public Format() {
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
            throw new ParseException("Format operator must have 2 arguments.");
        }

        Object objNumber = pv.eval(node.jjtGetChild(0));
        Object objFormat = pv.eval(node.jjtGetChild(1));

        if (num == 2 && objNumber instanceof Number && objFormat instanceof String) {
            double val = ((Number) objNumber).doubleValue();
            String format = objFormat.toString();

            try {
                DecimalFormat df = new DecimalFormat(format, new DecimalFormatSymbols(Locale.US));
                return df.format(val);
            } catch (Exception e) {
            }
        }
        
        return objNumber;
    }
}
