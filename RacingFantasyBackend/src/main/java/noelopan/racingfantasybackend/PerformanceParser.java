package noelopan.racingfantasybackend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerformanceParser {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+:\\d+(?:\\.\\d+)?)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    public static Double parseMarkToDecimal(String rawMark, String eventName) {
        if (rawMark == null || rawMark.trim().isEmpty()) {
            return null;
        }

        String cleanMark = rawMark.trim().toUpperCase().replace('\u00A0', ' ');

        // Handle non-performances
        if (cleanMark.equals("DNS") || cleanMark.equals("DNF") ||
                cleanMark.equals("DQ") || cleanMark.equals("FS") ||
                cleanMark.equals("NM") || cleanMark.equals("FOUL") ||
                cleanMark.equals("NH")) {
            return null;
        }

        try {
            // 1. Try to extract a time first (for track events)
            Matcher timeMatcher = TIME_PATTERN.matcher(cleanMark);
            if (timeMatcher.find()) {
                String timeText = timeMatcher.group(1);
                String[] parts = timeText.split(":");
                double minutes = Double.parseDouble(parts[0]);
                double seconds = Double.parseDouble(parts[1]);
                return (minutes * 60) + seconds;
            }

            // 2. Otherwise extract the first plain number
            Matcher numberMatcher = NUMBER_PATTERN.matcher(cleanMark);
            if (numberMatcher.find()) {
                double parsedValue = Double.parseDouble(numberMatcher.group(1));

                // Jumps and vaults use centimeters for your scoring constants
                if (eventName != null) {
                    String upperEvent = eventName.toUpperCase();
                    if (upperEvent.contains("JUMP") || upperEvent.contains("VAULT")) {
                        return parsedValue * 100.0;
                    }
                }

                return parsedValue;
            }

        } catch (NumberFormatException e) {
            System.err.println("Warning: Could not parse TFRRS mark into decimal: " + rawMark);
            return null;
        }

        System.err.println("Warning: Could not find a usable mark in: " + rawMark);
        return null;
    }
}