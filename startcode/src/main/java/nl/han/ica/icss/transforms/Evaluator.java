package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new LinkedList<>();
        applyStylesheet(ast.root);
    }

    private void applyStylesheet(Stylesheet stylesheet) {
        variableValues.add(new HashMap<>());
        ArrayList<ASTNode> nodesToRemove = new ArrayList<>();
        for (ASTNode node : stylesheet.getChildren()) {
            if (node instanceof VariableAssignment) {
                applyVariableAssignment((VariableAssignment) node);
                nodesToRemove.add(node);
            } else if (node instanceof Stylerule)
                applyStylerule((Stylerule) node);
        }
        for (ASTNode node : nodesToRemove) {
            stylesheet.removeChild(node);
        }
        variableValues.removeFirst();
    }

    private void applyVariableAssignment(VariableAssignment variableAssignment) {
        VariableReference variableReference = (VariableReference) variableAssignment.getChildren().get(0);
        Expression expression = (Expression) variableAssignment.getChildren().get(1);

        variableValues.getFirst().put(variableReference.name, (Literal) evaluateExpression(expression));
    }

    private void applyStylerule(Stylerule stylerule) {
        for (ASTNode node : stylerule.getChildren()) {
            if (node instanceof Declaration)
                applyDeclaration((Declaration) node);
        }
    }

    private void applyDeclaration(Declaration declaration) {
        declaration.expression = evaluateExpression(declaration.expression);
    }

    private Expression evaluateExpression(Expression expression) {
        // TODO: handle operations other than ADD
        if (expression instanceof Literal)
            return (Literal) expression;
        else if (expression instanceof VariableReference)
            return evaluateVariableReference((VariableReference) expression);
        else if (expression instanceof AddOperation)
            return evaluateAddOperation((AddOperation) expression);
        else {
            expression.setError("Could not evaluate expression");
            return null;
        }
    }

    private Literal evaluateVariableReference(VariableReference variableReference) {
        for (HashMap<String, Literal> map : variableValues) {
            if (map.containsKey(variableReference.name))
                return map.get(variableReference.name);
        }
        variableReference.setError("Could not evaluate variable");
        return null;
    }

    private Expression evaluateAddOperation(AddOperation expression) {
        // TODO: handle expressions other than PixelLiteral
        PixelLiteral left = (PixelLiteral) evaluateExpression(expression.lhs);
        PixelLiteral right = (PixelLiteral) evaluateExpression(expression.rhs);
        return new PixelLiteral(left.value + right.value);
    }

}
