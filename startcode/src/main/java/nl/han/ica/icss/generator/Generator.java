package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

public class Generator {

    public String generate(AST ast) {
        return generateStylesheet(ast.root);
    }

    private String generateStylesheet(Stylesheet stylesheet) {
        StringBuilder stylesheetString = new StringBuilder();
        for (ASTNode node : stylesheet.getChildren()) {
            if (node instanceof Stylerule) {
                stylesheetString
                        .append(generateStylerule((Stylerule) node))
                        .append("\n");
            }
        }
        return stylesheetString.toString();
    }

    private String generateStylerule(Stylerule rule) {
        StringBuilder styleruleString = new StringBuilder();
        for (ASTNode node : rule.getChildren()) {
            if (node instanceof Selector) {
                styleruleString
                        .append(generateSelector((Selector) node))
                        .append(" {\n");
            } else if (node instanceof Declaration) {
                styleruleString
                        .append("  ")
                        .append(generateDeclaration((Declaration) node))
                        .append("\n");
            }
        }
        styleruleString.append("}\n");
        return styleruleString.toString();
    }

    private String generateSelector(Selector selector) {
        if (selector instanceof TagSelector)
            return ((TagSelector) selector).tag;
        if (selector instanceof ClassSelector)
            return ((ClassSelector) selector).cls;
        if (selector instanceof IdSelector)
            return ((IdSelector) selector).id;
        return null;
    }

    private String generateDeclaration(Declaration declaration) {
        return declaration.property.name + ": " + generateExpression(declaration.expression) + ";";
    }

    private String generateExpression(Expression expression) {
        if (expression instanceof PixelLiteral)
            return ((PixelLiteral) expression).value + "px";
        else if (expression instanceof PercentageLiteral)
            return ((PercentageLiteral) expression).value + "%";
        else if (expression instanceof ColorLiteral)
            return ((ColorLiteral) expression).value;
        return null;
    }

}
