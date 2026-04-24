package algorithm;

import graphics.ScrnSimulatorOutput.AlgoResult;

public class SSTF implements DiskSchedulingAlgorithm {

    @Override
    public AlgoResult run(String algorithmName, int[] requests, int headPosition, String direction) {
        int n = requests.length;
        boolean[] served = new boolean[n];

        int[] seekSequence = new int[n + 1];
        seekSequence[0] = headPosition;

        int current = headPosition;
        for (int step = 0; step < n; step++) {
            int bestIndex = -1;
            int bestDistance = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                if (served[i]) {
                    continue;
                }

                int distance = Math.abs(requests[i] - current);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = i;
                } else if (distance == bestDistance && bestIndex != -1 && requests[i] < requests[bestIndex]) {
                    // Tie-break toward lower cylinder for deterministic output.
                    bestIndex = i;
                }
            }

            served[bestIndex] = true;
            current = requests[bestIndex];
            seekSequence[step + 1] = current;
        }

        int totalSeekTime = 0;
        for (int i = 1; i < seekSequence.length; i++) {
            totalSeekTime += Math.abs(seekSequence[i] - seekSequence[i - 1]);
        }

        AlgoResult r = new AlgoResult();
        r.algorithmName = algorithmName;
        r.seekSequence = seekSequence;
        r.initialHead = headPosition;
        r.direction = "N/A";
        r.totalSeekTime = totalSeekTime;
        return r;
    }
}