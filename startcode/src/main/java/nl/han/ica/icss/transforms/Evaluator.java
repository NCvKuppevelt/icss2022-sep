package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

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
        ArrayList<ASTNode> body = stylerule.body;
        stylerule.body = evaluateBody(body);
    }

    private ArrayList<ASTNode> evaluateBody(ArrayList<ASTNode> body) {
        ArrayList<ASTNode> nodesToKeep = new ArrayList<>();
        for (ASTNode node : body) {
            if (node instanceof Declaration) {
                applyDeclaration((Declaration) node);
                nodesToKeep.add(node);
            } else if (node instanceof IfClause)
                nodesToKeep.addAll(evaluateIfClause((IfClause) node));
        }
        return nodesToKeep;
    }

    private ArrayList<ASTNode> evaluateIfClause(IfClause ifClause) {
        BoolLiteral boolLiteral = (BoolLiteral) evaluateExpression(ifClause.getConditionalExpression());
        if (boolLiteral != null && boolLiteral.value)
            return evaluateBody(ifClause.body);
        else if (ifClause.elseClause != null)
            return evaluateBody(ifClause.elseClause.body);
        return new ArrayList<>();
    }

    private void applyDeclaration(Declaration declaration) {
        Expression expression = declaration.expression;
        declaration.expression = evaluateExpression(expression);
    }

    private Expression evaluateExpression(Expression expression) {
        if (expression instanceof Literal)
            return (Literal) expression;
        else if (expression instanceof VariableReference)
            return evaluateVariableReference((VariableReference) expression);
        else if (expression instanceof Operation)
            return evaluateOperation((Operation) expression);
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

    private Expression evaluateOperation(Operation operation) {
        if (operation instanceof MultiplyOperation)
            return evaluateMultiplyOperation((MultiplyOperation) operation);
        else if (operation instanceof AddOperation)
            return evaluateAddOperation((AddOperation) operation);
        else if (operation instanceof SubtractOperation) {
            return evaluateSubtractOperation((SubtractOperation) operation);
        }
        return null;
    }

    private Expression evaluateMultiplyOperation(MultiplyOperation operation) {
        Expression lhs = evaluateExpression(operation.lhs);
        Expression rhs = evaluateExpression(operation.rhs);
        if (lhs instanceof PixelLiteral && rhs instanceof ScalarLiteral)
            return new PixelLiteral(((PixelLiteral) lhs).value * ((ScalarLiteral) rhs).value);
        else if (lhs instanceof ScalarLiteral && rhs instanceof PixelLiteral)
            return new PixelLiteral(((ScalarLiteral) lhs).value * ((PixelLiteral) rhs).value);
        else if (lhs instanceof PercentageLiteral && rhs instanceof ScalarLiteral)
            return new PercentageLiteral(((PercentageLiteral) lhs).value * ((ScalarLiteral) rhs).value);
        else if (lhs instanceof ScalarLiteral && rhs instanceof PercentageLiteral)
            return new PercentageLiteral(((ScalarLiteral) lhs).value * ((PercentageLiteral) rhs).value);
        else if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral)
            return new ScalarLiteral(((ScalarLiteral) lhs).value * ((ScalarLiteral) rhs).value);
        return null;
    }

    private Expression evaluateAddOperation(AddOperation operation) {
        Expression lhs = evaluateExpression(operation.lhs);
        Expression rhs = evaluateExpression(operation.rhs);
        if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral)
            return new ScalarLiteral(((ScalarLiteral) lhs).value + ((ScalarLiteral) rhs).value);
        else if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral)
            return new PixelLiteral(((PixelLiteral) lhs).value + ((PixelLiteral) rhs).value);
        else if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral)
            return new PercentageLiteral(((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value);
        return null;
    }

    private Expression evaluateSubtractOperation(SubtractOperation operation) {
        Expression lhs = evaluateExpression(operation.lhs);
        Expression rhs = evaluateExpression(operation.rhs);
        if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral)
            return new ScalarLiteral(((ScalarLiteral) lhs).value - ((ScalarLiteral) rhs).value);
        else if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral)
            return new PixelLiteral(((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value);
        else if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral)
            return new PercentageLiteral(((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value);
        return null;
    }
}
