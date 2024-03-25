package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;
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
        variableTypes = new HANLinkedList<>();

        check(ast.root);
    }

    private void check(Stylesheet sheet) {
        for (ASTNode node : sheet.getChildren()) {
            if (node instanceof VariableAssignment)
                check((VariableAssignment) node);
            else if (node instanceof Stylerule)
                check((Stylerule) node);
            else
                node.setError("Unknown type as child of Stylesheet");
        }
    }

    private void check(VariableAssignment variableAssignment) {
        VariableReference variableReference = (VariableReference) variableAssignment.getChildren().get(0);
        Expression expression = (Expression) variableAssignment.getChildren().get(1);

        ExpressionType expressionType;
        if (expression instanceof PixelLiteral)
            expressionType = ExpressionType.PIXEL;
        else if (expression instanceof PercentageLiteral)
            expressionType = ExpressionType.PERCENTAGE;
        else if (expression instanceof ColorLiteral)
            expressionType = ExpressionType.COLOR;
        else if (expression instanceof BoolLiteral)
            expressionType = ExpressionType.BOOL;
        else
            variableAssignment.setError("Unable to determine expression type");
//        variableTypes.addFirst();
        // TODO: add variable and its type as hashmap to variableTypes

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

        if ((colorProperties.contains(propertyName.name) && !(expression instanceof ColorLiteral)) ||
                sizeProperties.contains(propertyName.name) && !(expression instanceof PixelLiteral || expression instanceof PercentageLiteral)) {
            declaration.setError("Property-Expression mismatch");
        } else {
            check(propertyName);
            check(expression);
        }

    }

    private void check(PropertyName propertyName) {
    }

    private void check(Expression expression) {
        if (expression instanceof PixelLiteral)
            check((PixelLiteral) expression);
        else if (expression instanceof PercentageLiteral)
            check((PercentageLiteral) expression);
        else if (expression instanceof ColorLiteral)
            check((ColorLiteral) expression);
        else
            expression.setError("Expression of unknown type");
    }

    private void check(PixelLiteral pixelLiteral) {

    }

    private void check(PercentageLiteral percentageLiteral) {

    }

    private void check(ColorLiteral colorLiteral) {

    }

}
