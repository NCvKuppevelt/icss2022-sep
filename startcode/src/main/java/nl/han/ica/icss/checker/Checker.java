package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
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
        colorProperties.add("color");
        colorProperties.add("background-color");
    }

    private void initSizeProperties() {
        sizeProperties.add("width");
        sizeProperties.add("height");
    }

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();

        check(ast.root);
    }

    private void check(Stylesheet sheet) {
        for (ASTNode node : sheet.getChildren()) {
            check((Stylerule) node);
        }
    }

    private void check(Stylerule rule) {
        for (ASTNode node : rule.getChildren()) {
            if (node instanceof Selector) check((Selector) node);
            else if (node instanceof Declaration) check((Declaration) node);
            else rule.setError("Stylerule can only have Selector and Declaration children");
        }
    }

    private void check(Selector selector) {
        if (selector instanceof TagSelector) check((TagSelector) selector);
        else if (selector instanceof IdSelector) check((IdSelector) selector);
        else if (selector instanceof ClassSelector) check((ClassSelector) selector);
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
                sizeProperties.contains(propertyName.name) && !(expression instanceof PixelLiteral) ||
                sizeProperties.contains(propertyName.name) && !(expression instanceof PercentageLiteral)) {
            declaration.setError("Property-Expression mismatch");
        }

        check(propertyName);
        check(expression);
    }

    private void check(PropertyName propertyName) {
    }

    private void check(Expression expression) {
        if (expression instanceof PixelLiteral) check((PixelLiteral) expression);
        else if (expression instanceof PercentageLiteral) check((PercentageLiteral) expression);
        else if (expression instanceof ColorLiteral) check((ColorLiteral) expression);
    }

    private void check(PixelLiteral pixelLiteral) {

    }

    private void check(PercentageLiteral percentageLiteral) {

    }

    private void check(ColorLiteral colorLiteral) {

    }

}
