package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

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
            if (node instanceof Selector) check ((Selector) node);
            else if (node instanceof Declaration) check ((Declaration) node);
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

    }

}
