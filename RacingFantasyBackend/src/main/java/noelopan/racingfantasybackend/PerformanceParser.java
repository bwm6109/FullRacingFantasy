package noelopan.racingfantasybackend;

public class PerformanceParser {

    public static Double parseMarkToDecimal(String rawMark, String eventName) {
        if (rawMark == null || rawMark.trim().isEmpty()) {
            return null;
        }

        String cleanMark = rawMark.trim().toUpperCase();

        // 1. Handle non-performances (Returns null so we don't calculate points for them)
        if (cleanMark.equals("DNS") || cleanMark.equals("DNF") ||
                cleanMark.equals("DQ") || cleanMark.equals("FS") ||
                cleanMark.equals("NM") || cleanMark.equals("FOUL") || cleanMark.equals("NH")) {
            return null;
        }

        // 2. Strip out any "m" or "meters" text if TFRRS includes it for field events
        cleanMark = cleanMark.replace("M", "").trim();

        try {
            // 3. Handle Distance/Mid-Distance Times (e.g., "14:35.24" or "4:05.00")
            if (cleanMark.contains(":")) {
                String[] parts = cleanMark.split(":");
                double minutes = Double.parseDouble(parts[0]);
                double seconds = Double.parseDouble(parts[1]);
                return (minutes * 60) + seconds;
            }
            // 4. Handle Sprints and Field Marks (e.g., "10.55" or "7.24")
            else {
                if(eventName.contains("JUMP") || eventName.contains("VAULT")) {
                    return 100 * Double.parseDouble(cleanMark);
                }
                return Double.parseDouble(cleanMark);
            }
        } catch (NumberFormatException e) {
            System.err.println("Warning: Could not parse TFRRS mark into decimal: " + rawMark);
            return null; // Return null if it's a completely unrecognized string
        }
    }
}
