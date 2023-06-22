package ast;

import java.util.*;

public class InheritanceManager {

    ArrayList<ClassManager> classManagers;
    Map<String, HashSet<String>> childrenMap = new TreeMap<>();
    Map<String, ArrayList<String>> parentsMap = new TreeMap<>();
    Map<String, Integer> levelMap = new TreeMap<>();

    public InheritanceManager(ArrayList<ClassManager> classManagers) {
        this.classManagers = classManagers;
        prepareInheritanceMap();
        calculateLevelsOfInheritance();
    }

    private void calculateLevelsOfInheritance() {
        for (ClassManager classManager : classManagers) {
            levelMap.put(classManager.getMyFullName(), calculateLevel(classManager.getMyFullName()));
        }
    }

    private int calculateLevel(String className) {
        if (!parentsMap.containsKey(className)) {
            return 0;
        }
        int max = 0;
        for (String parent : parentsMap.get(className)) {
            max = Math.max(max, 1 + calculateLevel(parent));
        }
        levelMap.put(className, max);
        return max;
    }

    private void prepareInheritanceMap() {
        for (ClassManager classManager : classManagers) {
            parentsMap.put(classManager.getMyFullName(), classManager.getParents());
            for (String parent : classManager.getParents()) {
                childrenMap.computeIfAbsent(parent, k -> new HashSet<>()).add(classManager.getMyFullName());
            }
        }
    }

    public double getNumberOfChildrenOfClass(String className) {
        if (!childrenMap.containsKey(className)) {
            return 0;
        }
        return childrenMap.get(className).size();
    }

    public double getLevelOfInheritanceOfClass(String className) {
        if (!levelMap.containsKey(className)) {
            return 0;
        }
        return levelMap.get(className);
    }
}
