package algorithm;

import graphics.ScrnSimulatorOutput.AlgoResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CLOOK implements DiskSchedulingAlgorithm {

	@Override
	public AlgoResult run(String algorithmName, int[] requests, int headPosition, String direction) {
		int[] sorted = Arrays.copyOf(requests, requests.length);
		Arrays.sort(sorted);

		String dir = (direction != null && direction.equalsIgnoreCase("Left")) ? "Left" : "Right";
		List<Integer> order = new ArrayList<>();

		if ("Right".equals(dir)) {
			for (int req : sorted) {
				if (req >= headPosition) order.add(req);
			}
			for (int req : sorted) {
				if (req < headPosition) order.add(req);
			}
		} else {
			for (int i = sorted.length - 1; i >= 0; i--) {
				if (sorted[i] <= headPosition) order.add(sorted[i]);
			}
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
			totalSeekTime += Math.abs(seekSequence[i] - seekSequence[i - 1]);
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
