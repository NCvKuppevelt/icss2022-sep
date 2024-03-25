package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
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

    public Checker() {
        initColorProperties();
        initSizeProperties();
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

        ExpressionType expressionType = ExpressionType.UNDEFINED;
        if (expression instanceof PixelLiteral)
            expressionType = ExpressionType.PIXEL;
        else if (expression instanceof PercentageLiteral)
            expressionType = ExpressionType.PERCENTAGE;
        else if (expression instanceof ColorLiteral)
            expressionType = ExpressionType.COLOR;
        else if (expression instanceof BoolLiteral)
            expressionType = ExpressionType.BOOL;
        else if (expression instanceof ScalarLiteral)
            expressionType = ExpressionType.SCALAR;
        else
            variableAssignment.setError("Unable to determine expression type");

        variableTypes.getFirst().put(variableReference.name, expressionType);

        check(variableReference);
        check(expression);
    }

    private void check(VariableReference variableReference) {

    }

    private void check(Stylerule rule) {
        for (ASTNode node : rule.getChildren()) {
            if (node instanceof Selector)
                check((Selector) node);
            else if (node instanceof Declaration)
                check((Declaration) node);
            else
                node.setError("Unknown type as child of Stylerule");
        }
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
        ExpressionType type = getType(expression);

        if ((colorProperties.contains(propertyName.name) && !(type == ExpressionType.COLOR)) ||
                sizeProperties.contains(propertyName.name) && !(type == ExpressionType.PIXEL || type == ExpressionType.PERCENTAGE)) {
            declaration.setError("Property-ExpressionType mismatch");
        } else {
            check(propertyName);
            check(expression);
        }

    }

    private ExpressionType getType(Expression expression) {
        if (expression instanceof VariableReference)
            return getTypeOfVariable(((VariableReference) expression).name);
        else if (expression instanceof PixelLiteral)
            return ExpressionType.PIXEL;
        else if (expression instanceof PercentageLiteral)
            return ExpressionType.PERCENTAGE;
        else if (expression instanceof ColorLiteral)
            return ExpressionType.COLOR;
        else if (expression instanceof BoolLiteral)
            return ExpressionType.BOOL;
        else if (expression instanceof ScalarLiteral)
            return ExpressionType.SCALAR;
        else
            expression.setError("Expression of unknown type");
        return null;
    }

    private ExpressionType getTypeOfVariable(String name) {
        for (HashMap<String, ExpressionType> map : variableTypes) {
            if (map.containsKey(name))
                return map.get(name);
        }
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
        else
            expression.setError("Expression of unknown type");
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

}
