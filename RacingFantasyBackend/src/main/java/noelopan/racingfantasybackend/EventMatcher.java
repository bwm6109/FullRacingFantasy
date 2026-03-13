package noelopan.racingfantasybackend;

public class EventMatcher {

    public static TrackEventConstants matchEvent(String tfrrsEventName) {
        // 1. Convert to uppercase and remove special characters
        String cleanName = tfrrsEventName.toUpperCase()
                .replace("'", "")
                .trim();
        String[] eventParts = cleanName.split(" ");
        if (eventParts.length == 2) {
            cleanName = eventParts[0].toUpperCase() + "_" +  eventParts[1].toUpperCase();
        }else{
            cleanName = eventParts[0].toUpperCase() + "_" +  eventParts[1].toUpperCase() + "_" + eventParts[2].toUpperCase();
        }
        try {
            return TrackEventConstants.valueOf(cleanName);
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Enum match failed for: " + tfrrsEventName + " (Tried: " + cleanName + ")");
            return null;
        }
    }
}