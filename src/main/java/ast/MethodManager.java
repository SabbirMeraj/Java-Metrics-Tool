package ast;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import metrics.CohesionManager;
import metrics.CouplingManager;
import metrics.CyclomaticComplexityCalculator;

public class MethodManager {

    Map<String, String> localMap;
    CohesionManager cohesionManager;
    CouplingManager couplingManager;
    Map<String, String> classMap;
    Map<String, String> globalMap;
    Set<String> variables;
    String className;
    String methodName = "";
    double cyclomaticComplexity;

    public MethodManager(MethodDeclaration method, String myClassName, Map<String, String> globalMap, Map<String, String> classesMap, Set<String> globalSet) {
        this.globalMap = globalMap;
        this.classMap = classesMap;
        this.methodName = method.getNameAsString();
        this.className = myClassName;
        this.variables = new HashSet<>();

        localMap = new TreeMap<>();
        couplingManager = new CouplingManager(methodName, myClassName);
        cohesionManager = new CohesionManager(methodName, myClassName);

        for (Parameter parameter : method.getParameters()) {
            for (String flazz : classesMap.keySet()) {
                if (flazz.equals(parameter.getTypeAsString())) {
                    localMap.put(parameter.getNameAsString(), parameter.getTypeAsString());
                }
            }
        }

        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(VariableDeclarator variable, Void arg) {
                variables.add(variable.getNameAsString());
                addVariableToLocalMap(variable);
                super.visit(variable, arg);
            }

            @Override
            public void visit(ForEachStmt forEach, Void arg) {
                if (globalSet.contains(forEach.getIterable().toString()) & !variables.contains(forEach.getIterable().toString())) {
                    cohesionManager.addGlobalVariable(forEach.getIterable().toString());
                }
                forEach.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(VariableDeclarator variable, Void arg) {
                        addVariableToLocalMap(variable);
                        super.visit(variable, arg);
                    }
                }, null);
                super.visit(forEach, arg);
            }

            @Override
            public void visit(MethodCallExpr methodCall, Void arg) {
                inspectMethodCall(methodCall);
                super.visit(methodCall, arg);
            }

            @Override
            public void visit(AssignExpr expr, Void arg) {
                if (globalSet.contains(expr.getTarget().toString()) & !variables.contains(expr.getTarget().toString())) {
                    cohesionManager.addGlobalVariable(expr.getTarget().toString());
                }
                if (expr.isObjectCreationExpr()) {
                    addConstructorAsMethodCall(expr.asObjectCreationExpr());
                }
                super.visit(expr, arg);
            }
        }, null);

        CyclomaticComplexityCalculator calculator = new CyclomaticComplexityCalculator(method);
        cyclomaticComplexity = calculator.calculateComplexity();
    }

    private void addConstructorAsMethodCall(ObjectCreationExpr expr) {
        String className = expr.getType().asString();
        if (classMap.containsKey(className)) {
            String fullClassName = classMap.get(className);
            String methodName = className;

            couplingManager.addCalledMethod(fullClassName, methodName);
        }
    }

    private void inspectMethodCall(MethodCallExpr methodCall) {
        for (Expression expression : methodCall.getArguments()) {
            if (expression.isObjectCreationExpr()) {
                addConstructorAsMethodCall(expression.asObjectCreationExpr());
            }
            if (expression.isMethodCallExpr()) {
                inspectMethodCall(expression.asMethodCallExpr());
            }
        }
        if (methodCall.getScope().isPresent()) {
            String objectName = methodCall.getScope().get().toString();
            String className = null;
            if (globalMap.containsKey(objectName)) {
                className = globalMap.get(objectName);
            }
            if (localMap.containsKey(objectName)) {
                className = localMap.get(objectName);
            }

            if (className != null) {
                String fullClassName = classMap.get(className);
                String methodName = methodCall.getNameAsString();

                couplingManager.addCalledMethod(fullClassName, methodName);
            }
        } else {
            cohesionManager.addCalledMethod(methodCall.getNameAsString());
        }
    }

    private void addVariableToLocalMap(VariableDeclarator variable) {
        boolean isAForeignClass = false;
        for (String flazz : classMap.keySet()) {
            if (flazz.equals(variable.getType().asString())) {
                localMap.put(variable.getNameAsString(), variable.getTypeAsString());
                isAForeignClass = true;
            }
        }
        if (!isAForeignClass) {
            localMap.put(variable.getNameAsString(), null);
        }
    }

    public CohesionManager getCohesionManager() {
        return cohesionManager;
    }

    public CouplingManager getCouplingManager() {
        return couplingManager;
    }

    public Set<String> getCalledMethodsSet() {
        return couplingManager.getCalledMethodList();
    }

    public String getFullName() {
        return className + "::" + methodName;
    }

    @Override
    public String toString() {
        return className + "::" + methodName;
    }

    public double getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }
}
