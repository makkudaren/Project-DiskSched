package algorithm;

import graphics.ScrnSimulatorOutput.AlgoResult;

public class FCFS implements DiskSchedulingAlgorithm {

    @Override
    public AlgoResult run(String algorithmName, int[] requests, int headPosition, String direction) {
        // Build visit order: head first, then requests in the order they arrived
        int[] seekSequence = new int[requests.length + 1];
        seekSequence[0] = headPosition;
        System.arraycopy(requests, 0, seekSequence, 1, requests.length);

        // Calculate total seek time
        int totalSeekTime = 0;
        for (int i = 1; i < seekSequence.length; i++)
            totalSeekTime += Math.abs(seekSequence[i] - seekSequence[i - 1]);

        AlgoResult r = new AlgoResult();
        r.algorithmName = algorithmName;
        r.seekSequence  = seekSequence;
        r.initialHead   = headPosition;
        r.direction     = "N/A";       // FCFS does not use direction
        r.totalSeekTime = totalSeekTime;
        return r;
    }
}