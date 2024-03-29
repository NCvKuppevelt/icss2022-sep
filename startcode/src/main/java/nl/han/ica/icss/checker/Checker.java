package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
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
    private final ArrayList<ExpressionType> nonArithmeticExpTypes = new ArrayList<>();

    public Checker() {
        initColorProperties();
        initSizeProperties();
        initNonOperableExprTypes();
    }

    private void initNonOperableExprTypes() {
        nonArithmeticExpTypes.add(ExpressionType.BOOL);
        nonArithmeticExpTypes.add(ExpressionType.COLOR);
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

        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet sheet) {
        variableTypes.addFirst(new HashMap<>());
        for (ASTNode node : sheet.getChildren()) {
            if (node instanceof VariableAssignment)
                checkVariableAssignment((VariableAssignment) node);
            else if (node instanceof Stylerule)
                checkStylerule((Stylerule) node);
            else
                node.setError("Unknown type as child of Stylesheet");
        }
        variableTypes.removeFirst();
    }

    private void checkVariableAssignment(VariableAssignment variableAssignment) {
        VariableReference variableReference = (VariableReference) variableAssignment.getChildren().get(0);
        Expression expression = (Expression) variableAssignment.getChildren().get(1);

        if (variableIsDefined(variableReference.name))
            variableAssignment.setError("Variable already exists in this scope");

        ExpressionType expressionType = checkTypeOfExpression(expression);

        variableTypes.getFirst().put(variableReference.name, expressionType);

        check(expression);
    }

    private boolean variableIsDefined(String name) {
        for (HashMap<String, ExpressionType> map : variableTypes) {
            if (map.containsKey(name))
                return true;
        }
        return false;
    }

    private void checkStylerule(Stylerule rule) {
        for (ASTNode node : rule.getChildren()) {
            if (node instanceof Selector)
                checkSelector((Selector) node);
            else if (node instanceof Declaration)
                checkDeclaration((Declaration) node);
            else if (node instanceof IfClause)
                checkIfClause((IfClause) node);
            else
                node.setError("Unknown type as child of Stylerule");
        }
    }

    private void checkIfClause(IfClause ifClause) {
        Expression expression = ifClause.getConditionalExpression();
        ExpressionType type = checkTypeOfExpression(expression);
        if (type != ExpressionType.BOOL)
            expression.setError("If-clause condition must be a boolean");
        checkBody(ifClause.body);
        if (ifClause.elseClause != null)
            checkBody(ifClause.elseClause.body);
    }

    private void checkBody(ArrayList<ASTNode> body) {
        for (ASTNode node : body) {
            if (node instanceof Declaration)
                checkDeclaration((Declaration) node);
            else if (node instanceof IfClause)
                checkIfClause((IfClause) node);
            else
                node.setError("Unknown type in body");
        }
    }

    private void checkSelector(Selector selector) {
        if (!(selector instanceof TagSelector
                || selector instanceof IdSelector
                || selector instanceof ClassSelector)) {
            selector.setError("Selector of unknown type");
        }
    }

    private void checkDeclaration(Declaration declaration) {
        PropertyName propertyName = (PropertyName) declaration.getChildren().get(0);
        Expression expression = (Expression) declaration.getChildren().get(1);
        ExpressionType type = checkTypeOfExpression(expression);

        if ((colorProperties.contains(propertyName.name) && !(type == ExpressionType.COLOR)) ||
                sizeProperties.contains(propertyName.name) && !(type == ExpressionType.PIXEL || type == ExpressionType.PERCENTAGE)) {
            declaration.setError("Property name " + propertyName.name + " with expressionType " + type + " is a mismatch");
        } else {
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

        if (isArithmeticOperation(operation)) {
            if (nonArithmeticExpTypes.contains(lhsType) || nonArithmeticExpTypes.contains(rhsType)) {
                operation.setError("Cannot perform arithmetic operations on a boolean or color");
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
        } else if (isBooleanOperation(operation)) {
            if (lhsType != ExpressionType.BOOL || rhsType != ExpressionType.BOOL)
                operation.setError("Boolean operations can only take boolean values");
            else
                return rhsType;
        }
        return null;
    }

    private boolean isBooleanOperation(Operation operation) {
        return operation instanceof NotOperation
                || operation instanceof AndOperation
                || operation instanceof OrOperation;
    }

    private boolean isArithmeticOperation(Operation operation) {
        return operation instanceof MultiplyOperation
                || operation instanceof AddOperation
                || operation instanceof SubtractOperation;
    }

    private ExpressionType getTypeOfVariable(VariableReference variableReference) {
        String name = variableReference.name;
        if (!variableIsDefined(name))
            variableReference.setError("Undefined variable");
        else {
            for (HashMap<String, ExpressionType> map : variableTypes) {
                if (map.containsKey(name))
                    return map.get(name);
            }
        }
        return null;
    }

    private void check(Expression expression) {
        if (!(expression instanceof VariableReference
                || expression instanceof Literal
                || expression instanceof Operation)) {
            expression.setError("Expression of unknown instance");
        }
    }
}
