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
        return generate(ast.root);
    }

    private String generate(Stylesheet stylesheet) {
        StringBuilder stylesheetString = new StringBuilder();
        for (ASTNode node : stylesheet.getChildren()) {
            if (node instanceof Stylerule) {
                stylesheetString
                        .append(generate((Stylerule) node))
                        .append("\n");
            }
            return null;
        }
        return stylesheetString.toString();
    }

    private String generate(Stylerule rule) {
        StringBuilder styleruleString = new StringBuilder();
        for (ASTNode node : rule.getChildren()) {
            if (node instanceof Selector) {
                styleruleString
                        .append(generate((Selector) node))
                        .append(" {\n");
            } else if (node instanceof Declaration) {
                styleruleString
                        .append("  ")
                        .append(generate((Declaration) node))
                        .append("\n");
            }
        }
        styleruleString.append("}\n");
        return styleruleString.toString();
    }

    private String generate(Selector selector) {
        if (selector instanceof TagSelector)
            return ((TagSelector) selector).tag;
        if (selector instanceof ClassSelector)
            return ((ClassSelector) selector).cls;
        if (selector instanceof IdSelector)
            return ((IdSelector) selector).id;
        return null;
    }

    private String generate(Declaration declaration) {
        return declaration.property.name + ": " + generate(declaration.expression) + ";";
    }

    private String generate(Expression expression) {
        if (expression instanceof PixelLiteral)
            return ((PixelLiteral) expression).value + "px";
        else if (expression instanceof PercentageLiteral)
            return ((PercentageLiteral) expression).value + "%";
        else if (expression instanceof ColorLiteral)
            return ((ColorLiteral) expression).value;
        return null;
    }

}
