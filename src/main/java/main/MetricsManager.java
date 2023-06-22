package main;

import ast.InheritanceManager;
import ast.ClassManager;
import ast.CodeManager;
import ast.FileAST;
import ast.MethodManager;
import com.github.javaparser.ast.CompilationUnit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import metrics.CallGraph;

public class MetricsManager {

    String projectPath;
    String outputPath;
    ArrayList<ClassManager> classManagerList;
    CallGraph callGraph;
    InheritanceManager inheritanceManager;

    public MetricsManager(String projectPath) {
        this.projectPath = projectPath;
        this.outputPath = projectPath;
        classManagerList = new ArrayList<>();
        callGraph = new CallGraph();
    }

    public void calculateMetrics() {
        FileAST fileAST = new FileAST(new File(projectPath));

        try {
            for (String key : fileAST.getCUMap().keySet()) {
                List<CompilationUnit> compilationUnits = fileAST.getCUMap().get(key);

                for (CompilationUnit cu : compilationUnits) {
                    CodeManager codeManager = new CodeManager(fileAST, key, cu);

                    for (ClassManager classManager : codeManager.getClassList()) {
                        classManagerList.add(classManager);
                    }
                }
            }

            generateCallGraph();
            setCouplingValues();

            inheritanceManager = new InheritanceManager(classManagerList);
            setInheritanceValues();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void generateCallGraph() {
        for (ClassManager classManager : classManagerList) {
            for (MethodManager methodManager : classManager.getMethodList()) {
                callGraph.addCouplingManager(methodManager.getCouplingManager());
            }
        }
        callGraph.generateCouplingClass();
        callGraph.generateMethodCallGraph();
    }

    private void setCouplingValues() {
        Map<String, Integer> couplingValues = callGraph.getCouplingValues();
        for (ClassManager classManager : classManagerList) {
            String fullName = classManager.getMyFullName();
            Integer value = couplingValues.getOrDefault(fullName, 0);
            classManager.setCoupling(value);
        }
    }

    private void setInheritanceValues() {
        for (ClassManager classManager : classManagerList) {
            String className = classManager.getMyFullName();
            double numberOfChildren = inheritanceManager.getNumberOfChildrenOfClass(className);
            double levelOfInheritance = inheritanceManager.getLevelOfInheritanceOfClass(className);

            classManager.setNumberOfChild(numberOfChildren);
            classManager.setLevelOfInheritence(levelOfInheritance);
        }
    }

    public void printOutput() {
        printClassMetrics();
        printMethodsMetrics();
    }

    private void printClassMetrics() {
        String fullClassPath, className, packageName;
        StringBuilder sb = new StringBuilder();
        sb.append("Package,");
        sb.append("Class,");
        sb.append("Number of Statements,");
        sb.append("Line Of Comments,");
        sb.append("Coupling,");
        sb.append("Lack of Cohesion,");
        sb.append("Response of Class,");
        sb.append("Weighted Method Count,");
        sb.append("Number of Children,");
        sb.append("Level of Inheritance,");
        sb.append("\n");

        for (ClassManager classManager : classManagerList) {
            fullClassPath = classManager.getMyFullName();
            packageName = fullClassPath.substring(0, fullClassPath.lastIndexOf("."));
            className = fullClassPath.substring(fullClassPath.lastIndexOf(".") + 1);
            sb.append(packageName).append(",");
            sb.append(className).append(",");
            sb.append(classManager.getNumberOfStatements()).append(",");
            sb.append(classManager.getLineOfComments()).append(",");
            sb.append(classManager.getCoupling()).append(",");
            sb.append(classManager.getLackOfCohesion()).append(",");
            sb.append(classManager.getResponseOfClass()).append(",");
            sb.append(classManager.getWeightedMethodCount()).append(",");
            sb.append(classManager.getNumberOfChild()).append(",");
            sb.append(classManager.getLevelOfInheritance()).append(",");
            sb.append("\n");
        }

        String filePath = outputPath + "\\class_metrics.csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(sb.toString());
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    private void printMethodsMetrics() {
        String fullClassPath, className, packageName, methodName;
        StringBuilder sb = new StringBuilder();
        sb.append("Package,");
        sb.append("Class,");
        sb.append("Method,");
        sb.append("Cyclomatic Complexity,");
        sb.append("\n");
        
        for (ClassManager classManager : classManagerList) {
            for (MethodManager methodManager : classManager.getMethodList()) {
                fullClassPath = methodManager.getFullName();
                packageName = fullClassPath.substring(0, fullClassPath.lastIndexOf("."));
                className = fullClassPath.substring(fullClassPath.lastIndexOf(".") + 1 , fullClassPath.indexOf(":"));
                methodName = fullClassPath.substring(fullClassPath.lastIndexOf(":") + 1);
                sb.append(packageName).append(",");
                sb.append(className).append(",");
                sb.append(methodName).append(",");
                sb.append(methodManager.getCyclomaticComplexity()).append("\n");
            }
        }

        String filePath = outputPath + "\\method_metrics.csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(sb.toString());
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

}
