package ast;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class FileAST {

    private Map<String, ArrayList<String>> packageMap;
    private Map<String, ArrayList<String>> sourceMap;
    private Map<String, ArrayList<CompilationUnit>> cuMap;

    public FileAST(File rootFile) {
        packageMap = new TreeMap<>();
        sourceMap = new TreeMap<>();
        cuMap = new TreeMap<>();

        browseClasses(rootFile);
    }

    public Map<String, ArrayList<String>> getPackageMap() {
        return packageMap;
    }

    public Map<String, ArrayList<String>> getSourceMap() {
        return sourceMap;
    }

    public Map<String, ArrayList<CompilationUnit>> getCUMap() {
        return cuMap;
    }

    public void browseClasses(File rootFile) {
        if (rootFile.isDirectory()) {
            File[] files = rootFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".java")) {
                        parseJavaFile(file);
                    } else if (file.isDirectory()) {
                        browseClasses(file);
                    }
                }
            }
        }
    }

    private void parseJavaFile(File file) {
        try {
            JavaParser javaParser = new JavaParser();
            CompilationUnit cu = javaParser.parse(file).getResult().orElse(null);
            if (cu != null) {
                String src = file.getParent();

                String packName = null;
                if (cu.getPackageDeclaration().isPresent()) {
                    packName = cu.getPackageDeclaration().get().getNameAsString();
                    cuMap.computeIfAbsent(src, k -> new ArrayList<>()).add(cu);
                    includeToPackageMap(cu);
                }

                sourceMap.computeIfAbsent(src, k -> new ArrayList<>());
                for (TypeDeclaration<?> type : cu.getTypes()) {
                    sourceMap.get(src).add(type.getNameAsString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void includeToPackageMap(CompilationUnit cu) {
        String packName = cu.getPackageDeclaration().get().getNameAsString();
        packageMap.computeIfAbsent(packName, k -> new ArrayList<>());

        for (TypeDeclaration<?> type : cu.getTypes()) {
            packageMap.get(packName).add(type.getNameAsString());

            if (cu.getClassByName(type.getNameAsString()).isPresent()) {
                String className = cu.getClassByName(type.getNameAsString()).get().getNameAsString();
                packageMap.computeIfAbsent(packName + "." + className, k -> new ArrayList<>());
                packageMap.get(packName + "." + className).add(type.getNameAsString());
            } else if (cu.getInterfaceByName(type.getNameAsString()).isPresent()) {
                String className = cu.getInterfaceByName(type.getNameAsString()).get().getNameAsString();
                packageMap.computeIfAbsent(packName + "." + className, k -> new ArrayList<>());
                packageMap.get(packName + "." + className).add(type.getNameAsString());
            }
        }
    }

    public ArrayList<String> getClassNamesByImportTag(String packageName) {
        return packageMap.get(packageName);
    }

    public ArrayList<String> getClassNamesBySource(String path) {
        return sourceMap.get(path);
    }
}
