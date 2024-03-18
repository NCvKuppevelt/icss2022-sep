package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.Stylesheet;

import javax.swing.text.Style;
import java.util.Stack;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    //Accumulator attributes:
    private AST ast;

    //Use this to keep track of the parent nodes when recursively traversing the ast
//    private IHANStack<ASTNode> currentContainer;
    private Stack<ASTNode> currentContainer; //later te vervangen

    public ASTListener() {
        ast = new AST();
        //currentContainer = new HANStack<>();
        currentContainer = new Stack<>();
    }

    public AST getAST() {
        return ast;
    }

    @Override
    public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
        Stylesheet sheet = new Stylesheet();
        currentContainer.add(sheet);
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        Stylesheet sheet = (Stylesheet) currentContainer.pop();
        ast.root = sheet;
    }

    @Override
    public void enterStylerule(ICSSParser.StyleruleContext ctx) {
        currentContainer.add(new Stylerule());
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        Stylerule rule = (Stylerule) currentContainer.pop();
        currentContainer.peek().addChild(rule);
    }
}