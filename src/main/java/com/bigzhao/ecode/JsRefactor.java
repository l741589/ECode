package com.bigzhao.ecode;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.*;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 * Created by yangzhao.lyz on 2015/9/15.
 */
public class JsRefactor {

    private LinkedList<NewExpression> newExpressions=new LinkedList<NewExpression>();
    public void refactor(String source,String name){
        Parser p=new Parser();
        AstRoot root=p.parse(source,name,1);
        root.visit(n -> {
            if (n instanceof NewExpression) {
                NewExpression e = (NewExpression) n;
                newExpressions.add(e);
            }
            for (int i = 0; i < n.depth(); ++i) System.out.print("\t");
            System.out.print(n.getClass().getSimpleName() + " " + Token.typeToName(n.getType()));
            System.out.print(" ");
            System.out.println(n.toSource().replaceAll("\\s+", " "));

            return true;
        });

        for (NewExpression e:newExpressions){
            e.setType(Token.FUNCTION);
        }

        System.out.println(root.toSource());
    }

    private Name createName(String name){
        Name n=new Name();
        n.setIdentifier(name);
        return n;
    }
    private StringLiteral createString(String s){
        StringLiteral sl=new StringLiteral();
        sl.setValue(s);
        return sl;
    }

    private void replace(AstNode oldNode,AstNode newNode){
        AstNode parent=oldNode.getParent();
        if (parent==null) return;
    }
}
