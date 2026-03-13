package noelopan.racingfantasybackend;

public class WorldAthleticsCalculator {

    // Calculate points using Quadratic Regression (a*x^2 + b*x + c)
    public static int calculateTrackPoints(double timeInSeconds, double a, double b, double c) {
        double rawPoints = (a * Math.pow(timeInSeconds, 2)) + (b * timeInSeconds) + c;

        if (rawPoints < 0) {
            return 0; // Prevent negative points for very slow times
        }
        return (int) Math.floor(rawPoints);
    }

    // Calculate points using Quadratic Regression (a*x^2 + b*x + c)
    public static int calculateFieldPoints(double markInMeters, double a, double b, double c) {
        double rawPoints = (a * Math.pow(markInMeters, 2)) + (b * markInMeters) + c;

        if (rawPoints < 0) {
            return 0; // Prevent negative points for very short throws/jumps
        }
        return (int) Math.floor(rawPoints);
    }
}