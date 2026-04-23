package algorithm;

import graphics.ScrnSimulatorOutput.AlgoResult;

public interface DiskSchedulingAlgorithm {
    //Run the algorithm and return a fully-populated AlgoResult.
    AlgoResult run(String algorithmName, int[] requests, int headPosition, String direction);
}