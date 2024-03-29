package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;
    private final ArrayList<String> colorProperties = new ArrayList<>();
    private final ArrayList<String> sizeProperties = new ArrayList<>();
    private final ArrayList<ExpressionType> nonOperableExpTypes = new ArrayList<>();

    public Checker() {
        initColorProperties();
        initSizeProperties();
        initNonOperableExprTypes();
    }

    private void initNonOperableExprTypes() {
        nonOperableExpTypes.add(ExpressionType.BOOL);
        nonOperableExpTypes.add(ExpressionType.COLOR);
    }

    private void initColorProperties() {
        colorProperties.add("background-color");
        colorProperties.add("color");
    }

    private void initSizeProperties() {
        sizeProperties.add("height");
        sizeProperties.add("width");
    }

    public void check(AST ast) {
        variableTypes = new LinkedList<>();

        check(ast.root);
    }

    private void check(Stylesheet sheet) {
        variableTypes.addFirst(new HashMap<>());
        for (ASTNode node : sheet.getChildren()) {
            if (node instanceof VariableAssignment)
                check((VariableAssignment) node);
            else if (node instanceof Stylerule)
                check((Stylerule) node);
            else
                node.setError("Unknown type as child of Stylesheet");
        }
        variableTypes.removeFirst();
    }

    private void check(VariableAssignment variableAssignment) {
        VariableReference variableReference = (VariableReference) variableAssignment.getChildren().get(0);
        Expression expression = (Expression) variableAssignment.getChildren().get(1);

        if (variableExists(variableReference.name))
            variableAssignment.setError("Variable already exists in this scope");

        ExpressionType expressionType = checkTypeOfExpression(expression);

        variableTypes.getFirst().put(variableReference.name, expressionType);

        check(variableReference);
        check(expression);
    }

    private boolean variableExists(String name) {
        for (HashMap<String, ExpressionType> map : variableTypes) {
            if (map.containsKey(name))
                return true;
        }
        return false;
    }

    private void check(VariableReference variableReference) {
    }

    private void check(Stylerule rule) {
        for (ASTNode node : rule.getChildren()) {
            if (node instanceof Selector)
                check((Selector) node);
            else if (node instanceof Declaration)
                check((Declaration) node);
            else if (node instanceof IfClause)
                check((IfClause) node);
            else
                node.setError("Unknown type as child of Stylerule");
        }
    }

    private void check(IfClause ifClause) {
        for (ASTNode node : ifClause.getChildren()) {
            if (node instanceof VariableReference) {
                if (getTypeOfVariable((VariableReference) node) != ExpressionType.BOOL)
                    node.setError("If-clause must be a boolean");
            }
            else if (node instanceof Declaration)
                check((Declaration) node);
            else if (node instanceof ElseClause) {
                check ((ElseClause) node);
            } else if (node instanceof IfClause)
                check((IfClause) node);
            else
                node.setError("Unknown type as child of IfClause");
        }
    }

    private void check(ElseClause elseClause) {

    }

    private void check(Selector selector) {
        if (selector instanceof TagSelector)
            check((TagSelector) selector);
        else if (selector instanceof IdSelector)
            check((IdSelector) selector);
        else if (selector instanceof ClassSelector)
            check((ClassSelector) selector);
        else
            selector.setError("Selector of unknown type");
    }

    private void check(TagSelector tagSelector) {
    }

    private void check(IdSelector idSelector) {
    }

    private void check(ClassSelector classSelector) {
    }

    private void check(Declaration declaration) {
        PropertyName propertyName = (PropertyName) declaration.getChildren().get(0);
        Expression expression = (Expression) declaration.getChildren().get(1);
        ExpressionType type = checkTypeOfExpression(expression);

        if ((colorProperties.contains(propertyName.name) && !(type == ExpressionType.COLOR)) ||
                sizeProperties.contains(propertyName.name) && !(type == ExpressionType.PIXEL || type == ExpressionType.PERCENTAGE)) {
            declaration.setError("Property name " + propertyName.name + " with expressionType " + type + " is a mismatch");
        } else {
            check(propertyName);
            check(expression);
        }
    }

    private ExpressionType checkTypeOfExpression(Expression expression) {
        if (expression instanceof VariableReference) {
            return getTypeOfVariable((VariableReference) expression);
        } else if (expression instanceof PixelLiteral)
            return ExpressionType.PIXEL;
        else if (expression instanceof PercentageLiteral)
            return ExpressionType.PERCENTAGE;
        else if (expression instanceof ColorLiteral)
            return ExpressionType.COLOR;
        else if (expression instanceof BoolLiteral)
            return ExpressionType.BOOL;
        else if (expression instanceof ScalarLiteral)
            return ExpressionType.SCALAR;
        else if (expression instanceof Operation)
            return checkTypeOfOperation((Operation) expression);
        else
            expression.setError("Expression of unknown type");
        return null;
    }

    private ExpressionType checkTypeOfOperation(Operation operation) {
        ExpressionType lhsType = checkTypeOfExpression(operation.lhs);
        ExpressionType rhsType = checkTypeOfExpression(operation.rhs);

        if (nonOperableExpTypes.contains(lhsType) || nonOperableExpTypes.contains(rhsType)) {
            operation.setError("Cannot perform operations on a boolean or color");
        } else if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (lhsType != rhsType)
                operation.setError("Expressions on both sides of an add or subtract operation must be of same type");
            else
                return lhsType;
        } else if (operation instanceof MultiplyOperation) {
            if (lhsType != ExpressionType.SCALAR && rhsType != ExpressionType.SCALAR)
                operation.setError("Multiplication needs at least one scalar");
            else if (lhsType == ExpressionType.SCALAR)
                return rhsType;
            else
                return lhsType;
        }
        return null;
    }

    private ExpressionType getTypeOfVariable(VariableReference variableReference) {
        String name = variableReference.name;
        for (HashMap<String, ExpressionType> map : variableTypes) {
            if (map.containsKey(name))
                return map.get(name);
        }
        variableReference.setError("Could not find type of variable");
        return null;
    }

    private void check(PropertyName propertyName) {
    }

    private void check(Expression expression) {
        if (expression instanceof VariableReference)
            check((VariableReference) expression);
        else if (expression instanceof PixelLiteral)
            check((PixelLiteral) expression);
        else if (expression instanceof PercentageLiteral)
            check((PercentageLiteral) expression);
        else if (expression instanceof ColorLiteral)
            check((ColorLiteral) expression);
        else if (expression instanceof BoolLiteral)
            check((BoolLiteral) expression);
        else if (expression instanceof ScalarLiteral)
            check((ScalarLiteral) expression);
        else if (expression instanceof Operation)
            check((Operation) expression);
        else
            expression.setError("Expression of unknown instance");
    }

    private void check(PixelLiteral pixelLiteral) {
    }

    private void check(PercentageLiteral percentageLiteral) {
    }

    private void check(ColorLiteral colorLiteral) {
    }

    private void check(BoolLiteral boolLiteral) {
    }

    private void check(ScalarLiteral scalarLiteral) {
    }

    private void check(Operation operation) {
    }

}
