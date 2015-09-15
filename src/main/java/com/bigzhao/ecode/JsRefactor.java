package com.bigzhao.ecode;

import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.StreamSupport;

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
            FunctionCall call=new FunctionCall();
            if (e.getTarget()!=null) call.setTarget(e.getTarget());
            if (e.getArguments()!=null) call.setArguments(e.getArguments());
            if (e.getInitializer()!=null) call.addArgument(e.getInitializer());
            replace(e,call);
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

    @SuppressWarnings("unchecked")
    private void replace(AstNode oldNode,AstNode newNode){
        AstNode parent=oldNode.getParent();
        if (parent==null) return;
        Method[] ms=parent.getClass().getMethods();
        HashMap<String,Method> getmap=new HashMap<>();
        HashMap<String,Method> setmap=new HashMap<>();
        for (Method m:ms) {
            if (m.getName().startsWith("get")&&m.getParameterCount()==0){
                getmap.put(m.getName().substring(3),m);
            }else if (m.getName().startsWith("set")&&m.getParameterCount()==1){
                setmap.put(m.getName().substring(3),m);
            }
        }
        for (Map.Entry<String,Method> e:getmap.entrySet()){
            try {
                Method get = e.getValue();
                Method set = setmap.get(e.getKey());
                AstNode n;
                if (get == null || set == null) continue;
                if (e.getKey().equals("Scope")) continue;
                Class<?> rtype=get.getReturnType();
                if (Node.class.isAssignableFrom(rtype)) {
                    Object val = get.invoke(parent);
                    if (val == oldNode) set.invoke(parent, newNode);
                }else if (List.class.isAssignableFrom(rtype)){
                    Collection<Object> val=(Collection<Object>)get.invoke(parent);
                    Collection<Object> c=new LinkedList<>();
                    for (Object o:val){
                        if (o==oldNode) c.add(newNode);
                        else c.add(oldNode);
                    }
                    set.invoke(parent,c);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
