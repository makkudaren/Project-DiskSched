error id: file:///C:/Users/Mac%20Calimba/Documents/GitHub/Project-DiskSched/src/algorithm/CSCAN.java:java/lang/Math#
file:///C:/Users/Mac%20Calimba/Documents/GitHub/Project-DiskSched/src/algorithm/CSCAN.java
empty definition using pc, found symbol in pc: java/lang/Math#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 2110
uri: file:///C:/Users/Mac%20Calimba/Documents/GitHub/Project-DiskSched/src/algorithm/CSCAN.java
text:
```scala
package algorithm;

import graphics.ScrnSimulatorOutput.AlgoResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSCAN implements DiskSchedulingAlgorithm {

    @Override
    public AlgoResult run(String algorithmName, int[] requests, int headPosition, String direction) {
        final int MIN_CYLINDER = 0;
        final int MAX_CYLINDER = 199;

        int[] sorted = Arrays.copyOf(requests, requests.length);
        Arrays.sort(sorted);

        String dir = (direction != null && direction.equalsIgnoreCase("Left")) ? "Left" : "Right";
        List<Integer> order = new ArrayList<>();

        if ("Right".equals(dir)) {
            for (int req : sorted) {
                if (req >= headPosition) order.add(req);
            }

            // Go to MAX boundary if not already there
            if (headPosition != MAX_CYLINDER && (order.isEmpty() || order.get(order.size() - 1) != MAX_CYLINDER)) {
                order.add(MAX_CYLINDER);
            }

            order.add(MIN_CYLINDER);

            for (int req : sorted) {
                if (req < headPosition) order.add(req);
            }
        } else {

            for (int i = sorted.length - 1; i >= 0; i--) {
                if (sorted[i] <= headPosition) order.add(sorted[i]);
            }

            // Go to MIN boundary if not already there
            if (headPosition != MIN_CYLINDER && (order.isEmpty() || order.get(order.size() - 1) != MIN_CYLINDER)) {
                order.add(MIN_CYLINDER);
            }

            order.add(MAX_CYLINDER);
            for (int i = sorted.length - 1; i >= 0; i--) {
                if (sorted[i] > headPosition) order.add(sorted[i]);
            }
        }

        int[] seekSequence = new int[order.size() + 1];
        seekSequence[0] = headPosition;
        for (int i = 0; i < order.size(); i++) {
            seekSequence[i + 1] = order.get(i);
        }

        int totalSeekTime = 0;
        for (int i = 1; i < seekSequence.length; i++) {
            totalSeekTime += @@Math.abs(seekSequence[i] - seekSequence[i - 1]);
        }

        AlgoResult r = new AlgoResult();
        r.algorithmName = algorithmName;
        r.seekSequence = seekSequence;
        r.initialHead = headPosition;
        r.direction = dir;
        r.totalSeekTime = totalSeekTime;
        return r;
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: java/lang/Math#