package ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.*;

import java.util.*;

public class CodeManager {

    Map<String, String> classMap;
    ArrayList<ClassManager> classList;
    ArrayList<MethodManager> methodList;
    Set<String> globalSet;
    String packageName = "";

    public CodeManager(FileAST ast, String ParentPath, CompilationUnit compilationUnit) {
        classMap = new TreeMap<>();
        classList = new ArrayList<>();
        methodList = new ArrayList<>();
        globalSet = new HashSet<>();

        if (compilationUnit.getPackageDeclaration().isPresent()) {
            packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        }

        for (ImportDeclaration immport : compilationUnit.getImports()) {
            String importName = immport.getNameAsString();
            if (ast.getClassNamesByImportTag(importName) != null) {
                for (String className : ast.getClassNamesByImportTag(importName)) {
                    if (immport.toString().contains("*")) {
                        classMap.put(className, getFullName(importName, className));
                    } else {
                        classMap.put(className, importName);
                    }
                }
            }
        }
        if (ast.getClassNamesBySource(ParentPath) != null) {
            for (String className : ast.getClassNamesBySource(ParentPath)) {
                classMap.put(className, getFullName(packageName, className));
            }
        }

        for (TypeDeclaration type : compilationUnit.getTypes()) {
            if (compilationUnit.getClassByName(type.getNameAsString()).isPresent()) {
                classList.add(new ClassManager(packageName, type.asClassOrInterfaceDeclaration(), classMap));
            }
            if (compilationUnit.getInterfaceByName(type.getNameAsString()).isPresent()) {
                classList.add(new ClassManager(packageName, type.asClassOrInterfaceDeclaration(), classMap));
            }
        }
    }

    private String getFullName(String packageName, String className) {
        return packageName + "." + className;
    }

    public ArrayList<ClassManager> getClassList() {
        return classList;
    }
}
