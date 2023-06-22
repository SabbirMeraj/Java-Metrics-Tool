package main;

public class MetricsTool {
    public static void main(String [] args) throws Exception{
        MetricsManager metricsManager = new MetricsManager("C:\\Users\\sabbir919\\Desktop\\Test\\bond");
        
        metricsManager.calculateMetrics();
        metricsManager.printOutput();
    }
}
