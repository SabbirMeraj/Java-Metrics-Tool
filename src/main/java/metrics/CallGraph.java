package metrics;

import java.util.*;

public class CallGraph {

    private ArrayList<CouplingManager> couplingManagers;
    Map<String, Set<String>> classCallMap;
    Map<String, Integer> couplingValues;
    Set<String> coupleClasses;
    Map<String, Integer> methodHash;
    Integer[][] callGraph;

    public CallGraph() {
        classCallMap = new TreeMap<>();
        couplingManagers = new ArrayList<>();
        coupleClasses = new HashSet<>();
        methodHash = new HashMap<>();
        couplingValues = new TreeMap<>();
    }

    public void addCouplingManager(CouplingManager manager) {
        couplingManagers.add(manager);
    }

    public void generateCouplingClass() {
        for (CouplingManager manager : couplingManagers) {
            if (!classCallMap.containsKey(manager.getClassName())) {
                classCallMap.put(manager.getClassName(), new HashSet<>());
            }
            classCallMap.get(manager.getClassName()).addAll(manager.getUsedClasses());

            for (String calledClass : manager.getUsedClasses()) {
                if (!classCallMap.containsKey(calledClass)) {
                    classCallMap.put(calledClass, new HashSet<>());
                }
                classCallMap.get(calledClass).add(manager.getFullName());
            }
        }
        for (String value : classCallMap.keySet()) {
            couplingValues.put(value, classCallMap.get(value).size());
        }
    }

    public void generateMethodCallGraph() {
        int counter = 0;
        for (CouplingManager manager : couplingManagers) {
            if (!methodHash.containsKey(manager.getFullName())) {
                methodHash.put(manager.getFullName(), counter++);
            }
            for (String methodName : manager.getCalledMethodList()) {
                if (!methodHash.containsKey(methodName)) {
                    methodHash.put(methodName, counter++);
                }
            }
        }

        callGraph = new Integer[counter + 1][counter + 1];
        for (int i = 0; i < callGraph.length; i++) {
            for (int j = 0; j < callGraph[i].length; j++) {
                callGraph[i][j] = 0;
            }
        }

        for (CouplingManager src : couplingManagers) {
            for (String target : src.getCalledMethodList()) {
                int a = methodHash.get(src.getFullName());
                int b = methodHash.get(target);
                callGraph[a][b] = 1;
            }
        }
    }

    public Map<String, Integer> getCouplingValues() {
        return couplingValues;
    }

    public Map<String, Integer> getMethodHash() {
        return methodHash;
    }

    public Integer[][] getCallGraph() {
        return callGraph;
    }
}
