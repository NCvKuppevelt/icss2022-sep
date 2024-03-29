package nl.han.ica.icss.ast.operations;

import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Operation;

public class NotOperation extends Operation {

    @Override
    public String getNodeLabel() {
        return "Not";
    }

    @Override
    public ASTNode addChild(ASTNode child) {
        // The NOT operation has only one operand, so I'm using both sides to store the single expression
        if (lhs == null) {
            lhs = (Expression) child;
        }
        if (rhs == null) {
            rhs = (Expression) child;
        }
        return this;
    }
}
