package ast;

import java.util.*;
import metrics.LineCounter;
import metrics.CohesionGraph;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class ClassManager {

    ClassOrInterfaceDeclaration className;
    Map<String, String> classMap;
    String packageName;
    ArrayList<MethodManager> methodList;
    Set<String> globalSet;
    LineCounter lineCounter;
    double lackOfCohesion;
    double coupling;
    double numberOfChild;
    double levelOfInheritence = 0;

    public ClassManager(String myPackageName, ClassOrInterfaceDeclaration myClass, Map<String, String> classesMap) {
        this.packageName = myPackageName;
        this.className = myClass;
        this.classMap = classesMap;
        this.methodList = new ArrayList<>();
        this.globalSet = new HashSet<>();
        this.lineCounter = new LineCounter(myClass);

        prepareFields();
        generateCohesionGraph();
    }

    private void prepareFields() {
        String classNameWithPackage = getFullName(packageName, className.getNameAsString());
        Map<String, String> globalMap = new TreeMap<>();

        for (FieldDeclaration field : className.getFields()) {
            for (VariableDeclarator variable : field.getVariables()) {
                globalSet.add(variable.getNameAsString());

                for (String classVar : classMap.keySet()) {
                    if (classVar.equals(variable.getType().asString())) {
                        globalMap.put(variable.getNameAsString(), variable.getType().asString());
                    }
                }
            }
        }
        for (String string : classMap.keySet()) {
            globalMap.put(string, string);
        }
        convertAndAddConstructors(className, globalMap);

        for (MethodDeclaration method : className.getMethods()) {
            methodList.add(new MethodManager(method, classNameWithPackage, globalMap, classMap, globalSet));
        }
    }

    private void convertAndAddConstructors(ClassOrInterfaceDeclaration classVar, Map<String, String> globalMap) {
        for (ConstructorDeclaration constructorDeclaration : classVar.getConstructors()) {
            MethodDeclaration medthodDeclaration = new MethodDeclaration();
            medthodDeclaration.setName(constructorDeclaration.getName());
            medthodDeclaration.setParameters(constructorDeclaration.getParameters());
            medthodDeclaration.setBody(constructorDeclaration.getBody());

            methodList.add(new MethodManager(medthodDeclaration, getFullName(packageName, constructorDeclaration.getNameAsString()), globalMap, classMap, globalSet));
        }
    }

    public void generateCohesionGraph() {
        CohesionGraph cohesionGraph = new CohesionGraph();
        for (MethodManager methodManager : methodList) {
            cohesionGraph.addToMap(methodManager.getCohesionManager());
        }

        cohesionGraph.calculateDSU();
        lackOfCohesion = cohesionGraph.getDsu();
    }

    private String getFullName(String packageName, String className) {
        return packageName + "." + className;
    }

    public ArrayList<MethodManager> getMethodList() {
        return methodList;
    }

    public String getMyFullName() {
        return packageName + "." + className.getNameAsString();
    }

    public void setCoupling(double coupling) {
        this.coupling = coupling;
    }

    public void setNumberOfChild(double children) {
        this.numberOfChild = children;
    }

    public void setLevelOfInheritence(double levelOfInheritence) {
        this.levelOfInheritence = levelOfInheritence;
    }

    public ArrayList<String> getParents() {
        ArrayList<String> parents = new ArrayList<>();
        for (ClassOrInterfaceType type : className.getExtendedTypes()) {
            if (classMap.containsKey(type.getNameAsString())) {
                parents.add(classMap.get(type.getNameAsString()));
            }
        }

        for (ClassOrInterfaceType type : className.getImplementedTypes()) {
            if (classMap.containsKey(type.getNameAsString())) {
                parents.add(classMap.get(type.getNameAsString()));
            }
        }

        return parents;
    }

    public double getLackOfCohesion() {
        return lackOfCohesion;
    }

    public double getResponseOfClass() {
        int sum = methodList.size();
        for (MethodManager methodManager : methodList) {
            sum += methodManager.getCouplingManager().getNumberOfCalledMethods();
        }
        return sum;
    }

    public double getLineOfComments() {
        return lineCounter.getLineOfComments();
    }

    public double getNumberOfComments() {
        return lineCounter.getNumberOfComments();
    }

    public double getLineOfCodes() {
        return lineCounter.getLineOfCodes();
    }

    public double getNumberOfStatements() {
        return lineCounter.getNumberOfStatements();
    }

    public double getCoupling() {
        return coupling;
    }

    public double getWeightedMethodCount() {
        int WeightedMethodCount = 0;
        for (MethodManager methodManager : methodList) {
            WeightedMethodCount += methodManager.getCyclomaticComplexity();
        }

        return WeightedMethodCount;
    }

    public double getNumberOfChild() {
        return numberOfChild;
    }

    public double getLevelOfInheritance() {
        return levelOfInheritence;
    }
}
