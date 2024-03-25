package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;

import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        //variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        //variableValues = new HANLinkedList<>();
        applyStylesheet(ast.root);
    }

    private void applyStylesheet(Stylesheet stylesheet) {
        applyStylerule((Stylerule) stylesheet.getChildren().get(0)); // TODO: loop?
    }

    private void applyStylerule(Stylerule stylerule) {
        for (ASTNode node : stylerule.getChildren()) {
            if (node instanceof Declaration) {
                applyDeclaration((Declaration) node);
            }
        }
    }

    private void applyDeclaration(Declaration declaration) {
        declaration.expression = evaluateExpression(declaration.expression);
    }

    private Expression evaluateExpression(Expression expression) {
        // TODO: handle operations other than ADD
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else {
            return evaluateAddOperation((AddOperation) expression);
        }
    }

    private Expression evaluateAddOperation(AddOperation expression) {
        // TODO: handle literals other than PixelLiteral
        PixelLiteral left  = (PixelLiteral) evaluateExpression(expression.lhs);
        PixelLiteral right = (PixelLiteral) evaluateExpression(expression.rhs);
        return new PixelLiteral(left.value + right.value);
    }


}
