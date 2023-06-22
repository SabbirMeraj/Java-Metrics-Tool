package metrics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CouplingManager {

    String methodName;
    String className;
    Map<String, Set<String>> couplingMap;
    Set<String> calledMethodList;

    public CouplingManager(String myMethodName, String myClassName) {
        this.methodName = myMethodName;
        this.className = myClassName;

        calledMethodList = new HashSet<>();
        couplingMap = new TreeMap<>();
    }

    public void addCalledMethod(String className, String methodName) {
        if (!couplingMap.containsKey(className)) {
            couplingMap.put(className, new HashSet<>());
        }
        couplingMap.get(className).add(methodName);

        calledMethodList.add(generateFullName(className, methodName));
    }

    private String generateFullName(String className, String methodName) {
        return className + "::" + methodName;
    }

    public int getNumberOfCalledMethods() {
        return calledMethodList.size();
    }

    public Set<String> getCalledMethodList() {
        return calledMethodList;
    }

    public String getClassName() {
        return className;
    }

    public String getFullName() {
        return className + "::" + methodName;
    }

    public Set<String> getUsedClasses() {
        return couplingMap.keySet();
    }
}
