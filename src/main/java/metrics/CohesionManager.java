package metrics;

import java.util.HashSet;
import java.util.Set;

public class CohesionManager {

    String methodName;
    String className;
    Set<String> globalVariableList;
    Set<String> calledMethodList;

    public CohesionManager(String methodName, String className) {
        this.methodName = methodName;
        this.className = className;

        globalVariableList = new HashSet<>();
        calledMethodList = new HashSet<>();
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClassName() {
        return className;
    }

    public Set<String> getGlobalVariableList() {
        return globalVariableList;
    }

    public Set<String> getCalledMethodList() {
        return calledMethodList;
    }

    public void addGlobalVariable(String v) {
        globalVariableList.add(v);
    }

    public void addCalledMethod(String calledMethod) {
        calledMethodList.add(calledMethod);
    }

    public void addToGraph(CohesionGraphAdapter adapter) {
        for (String globalVariable : globalVariableList) {
            adapter.graphCallBack(methodName, globalVariable);
            adapter.graphCallBack(globalVariable, methodName);
        }

        for (String calledMethod : calledMethodList) {
            adapter.graphCallBack(methodName, calledMethod);
            adapter.graphCallBack(calledMethod, methodName);
        }
    }
}
