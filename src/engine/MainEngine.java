package engine;

import algorithm.*;
import graphics.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class MainEngine {
    private MainGUI gui;
    private HashMap<String, Boolean> chosenAlgorithms;
    private final Map<String, DiskSchedulingAlgorithm> algorithmRegistry = new HashMap<>();

    // Disk scheduling ranges
    public static final int MIN_TOKENS   = 1;
    public static final int MAX_TOKENS   = 40;
    public static final int MIN_CYL_VAL  = 0;
    public static final int MAX_CYL_VAL  = 199;
    public static final int MIN_HEAD     = 0;
    public static final int MAX_HEAD     = 199;

    public MainEngine() {
        chosenAlgorithms = new HashMap<>();
        initializeAlgorithms();
        initializeRegistry();
    }

    public void initializeAlgorithms() {
        String[] algorithms = {
            "First-Come, First-Served (FCFS)",
            "Shortest Seek Time First (SSTF)",
            "SCAN",
            "C-SCAN",
            "LOOK",
            "C-LOOK"
        };
        for (String algorithm : algorithms) {
            chosenAlgorithms.put(algorithm, false);
        }
    }

    private void initializeRegistry() {
        algorithmRegistry.put("First-Come, First-Served (FCFS)", new FCFS());
        algorithmRegistry.put("Shortest Seek Time First (SSTF)", new SSTF());
        algorithmRegistry.put("SCAN", new SCAN());
        algorithmRegistry.put("C-SCAN", new CSCAN());
        algorithmRegistry.put("LOOK", new LOOK());
        algorithmRegistry.put("C-LOOK", new CLOOK());
    }

    // ==================================================
    //               SIMULATION LOGIC
    // ==================================================

    // Runs the selected algorithm and returns results for ScrnSimulatorOutput
    public List<ScrnSimulatorOutput.AlgoResult> runSimulation(int[] requests, int headPosition, String direction) {
        return getSelectedAlgorithmNames().stream().map(algoName -> {
            DiskSchedulingAlgorithm algo = algorithmRegistry.get(algoName);
            if (algo != null)
                return algo.run(algoName, requests, headPosition, direction);

            // Fallback placeholder for unimplemented algorithms
            ScrnSimulatorOutput.AlgoResult r = new ScrnSimulatorOutput.AlgoResult();
            r.algorithmName = algoName + " (not yet implemented)";
            r.seekSequence  = new int[]{headPosition};
            r.initialHead   = headPosition;
            r.direction     = direction;
            r.totalSeekTime = 0;
            return r;
        }).collect(Collectors.toList());
    }

    // ==================================================
    //               VALIDATION LOGIC
    // ==================================================

    public static class ValidationResult {
        public final boolean valid;
        public final String errorMessage;
        public final int[] parsedTokens;

        private ValidationResult(boolean valid, String errorMessage, int[] parsedTokens) {
            this.valid        = valid;
            this.errorMessage = errorMessage;
            this.parsedTokens = parsedTokens;
        }

        public static ValidationResult ok(int[] tokens) {
            return new ValidationResult(true, null, tokens);
        }
        public static ValidationResult fail(String msg) {
            return new ValidationResult(false, msg, null);
        }
    }

    // Validates raw input string and head position for disk scheduling
    public ValidationResult validateSimulationInput(String rawInput, int headPosition) {
        if (rawInput == null || rawInput.trim().isEmpty())
            return ValidationResult.fail("Input sequence cannot be empty.");

        String[] tokens = rawInput.trim().split("\\s+");

        if (tokens.length < MIN_TOKENS || tokens.length > MAX_TOKENS)
            return ValidationResult.fail(
                "Input sequence must be between " + MIN_TOKENS + " and " + MAX_TOKENS + " values.");

        int[] values = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            if (!isInteger(tokens[i]))
                return ValidationResult.fail("Input sequence must contain only integers separated by spaces.");
            values[i] = Integer.parseInt(tokens[i]);
            if (values[i] < MIN_CYL_VAL || values[i] > MAX_CYL_VAL)
                return ValidationResult.fail(
                    "Cylinder values must be between " + MIN_CYL_VAL + " and " + MAX_CYL_VAL + ".");
        }

        if (headPosition < MIN_HEAD || headPosition > MAX_HEAD)
            return ValidationResult.fail(
                "Initial head position must be between " + MIN_HEAD + " and " + MAX_HEAD + ".");

        if (getSelectedAlgorithmNames().isEmpty())
            return ValidationResult.fail("Please select a disk scheduling algorithm.");

        return ValidationResult.ok(values);
    }

    // ==================================================
    //               INPUT GENERATION
    // ==================================================

    public static class RandomInput {
        public final String inputString;
        public final int    headPosition;
        public final String direction;

        public RandomInput(String inputString, int headPosition, String direction) {
            this.inputString  = inputString;
            this.headPosition = headPosition;
            this.direction    = direction;
        }
    }

    // Generates a random cylinder request sequence, head position, and direction
    public RandomInput generateRandomInput() {
        Random random     = new Random();
        int tokenCount    = MIN_TOKENS + random.nextInt(MAX_TOKENS - MIN_TOKENS + 1);
        int headPosition  = random.nextInt(MAX_HEAD + 1);
        String[] dirs     = {"Left", "Right"};
        String direction  = dirs[random.nextInt(2)];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokenCount; i++) {
            sb.append(random.nextInt(MAX_CYL_VAL + 1));
            if (i < tokenCount - 1) sb.append(" ");
        }
        return new RandomInput(sb.toString(), headPosition, direction);
    }

    // ==================================================
    //               FILE IMPORT
    // ==================================================

    public static class ImportResult {
        public final boolean valid;
        public final String  errorMessage;
        public final String  inputString;
        public final int     headPosition;
        public final String  direction;     // "Left" | "Right" | null if not in file

        private ImportResult(boolean valid, String errorMessage,
                             String inputString, int headPosition, String direction) {
            this.valid        = valid;
            this.errorMessage = errorMessage;
            this.inputString  = inputString;
            this.headPosition = headPosition;
            this.direction    = direction;
        }

        public static ImportResult ok(String inputString, int headPosition, String direction) {
            return new ImportResult(true, null, inputString, headPosition, direction);
        }
        public static ImportResult fail(String msg) {
            return new ImportResult(false, msg, null, -1, null);
        }
    }

    // Reads, parses, and validates a .txt import file
    // Expected format:
    //   Input String:
    //   50 100 75 ...
    //
    //   Number of frames:   <- parsed as head position for disk scheduling
    //   125
    //
    //   Direction:
    //   Right
    public ImportResult importFromFile(File file) {
        String inputString = null;
        int    headPosition = -1;
        String direction   = null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equalsIgnoreCase("Input String:")) {
                    String next = br.readLine();
                    if (next != null) inputString = next.trim();
                } else if (line.equalsIgnoreCase("Number of frames:")) {
                    // Reused as "head position" in the disk scheduling template
                    String next = br.readLine();
                    if (next != null) {
                        try { headPosition = Integer.parseInt(next.trim()); }
                        catch (NumberFormatException ignored) {}
                    }
                } else if (line.equalsIgnoreCase("Direction:")) {
                    String next = br.readLine();
                    if (next != null) direction = next.trim();
                }
            }
        } catch (IOException e) {
            return ImportResult.fail("Failed to read file: " + e.getMessage());
        }

        // --- Validate input string ---
        if (inputString == null || inputString.isEmpty())
            return ImportResult.fail("Could not find 'Input String:' in the file.");

        String[] tokens = inputString.split("\\s+");
        if (tokens.length < MIN_TOKENS || tokens.length > MAX_TOKENS)
            return ImportResult.fail("Input sequence must have between " +
                MIN_TOKENS + " and " + MAX_TOKENS + " values.");

        for (String token : tokens) {
            if (!isInteger(token))
                return ImportResult.fail("Input contains non-integer value: " + token);
            int val = Integer.parseInt(token);
            if (val < MIN_CYL_VAL || val > MAX_CYL_VAL)
                return ImportResult.fail(
                    "Cylinder values must be between " + MIN_CYL_VAL +
                    " and " + MAX_CYL_VAL + ". Found: " + val);
        }

        // --- Validate head position ---
        if (headPosition < MIN_HEAD || headPosition > MAX_HEAD)
            return ImportResult.fail(
                "Initial head position must be between " + MIN_HEAD +
                " and " + MAX_HEAD + ". Found: " + headPosition);

        // --- Validate direction (optional — default to "Right" if missing) ---
        if (direction == null || direction.isEmpty()) {
            direction = "Right";
        } else if (!direction.equalsIgnoreCase("Left") && !direction.equalsIgnoreCase("Right")) {
            return ImportResult.fail("Direction must be 'Left' or 'Right'. Found: " + direction);
        } else {
            // Normalise capitalisation
            direction = direction.substring(0, 1).toUpperCase() + direction.substring(1).toLowerCase();
        }

        return ImportResult.ok(inputString, headPosition, direction);
    }

    // ==================================================
    //               HELPERS
    // ==================================================

    public List<String> getSelectedAlgorithmNames() {
        return chosenAlgorithms.entrySet().stream()
            .filter(java.util.Map.Entry::getValue)
            .map(java.util.Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        try { Integer.parseInt(str); return true; }
        catch (NumberFormatException e) { return false; }
    }

    // ==================================================
    //               GETTERS AND SETTERS
    // ==================================================

    public void setGUI(MainGUI gui) { this.gui = gui; }
    public MainGUI getGUI() { return gui; }
    public HashMap<String, Boolean> getChosenAlgorithms() { return chosenAlgorithms; }

    public void setChosenAlgorithm(String algorithm, boolean isChosen) {
        if (chosenAlgorithms.containsKey(algorithm)) {
            chosenAlgorithms.put(algorithm, isChosen);
            System.out.println("Algorithm " + algorithm + " set to " + isChosen);
        }
    }
}