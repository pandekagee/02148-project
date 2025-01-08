package dk.spilstuff.engine;

public class Mathf {
    public static double pointDirection(double x1, double y1, double x2, double y2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;

        // Calculate angle in radians
        double radians = Math.atan2(deltaY, deltaX);

        // Convert radians to degrees
        double degrees = Math.toDegrees(radians);

        // Ensure the result is positive (between 0 and 360 degrees)
        degrees = (degrees + 360) % 360;

        return degrees;
    }

    public static double pointDistance(double x1, double y1, double x2, double y2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;

        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }

    public static double dcos(double deg) {
        return Math.cos(Math.toRadians(deg));
    }

    public static double dsin(double deg) {
        return Math.sin(Math.toRadians(deg));
    }

    public static double lerp(double val1, double val2, double percentage) {
        return val1 + (val2 - val1) * percentage;
    }

    public static double softClamp(double value, double min, double max, double percentage)
    {
        if(value < min) {
            return lerp(value,min,percentage);
        }
        if(value > max) {
            return lerp(value,max,percentage);
        }
        return value;
    }

    public static boolean bclamp(double value, double min, double max) {
        return value < min || value > max;
    }

    public static int hashString(String str) {
        int hash = 5381; // Start with a prime number

        for (var i = 0; i < str.length(); i++) {
            hash = (((hash * 33) ^ str.charAt(i)) << 1) % (Integer.MAX_VALUE + 1);
        }

        return hash;
    }

    public static int intRandomRange(int min, int max) {
        return (int)(Math.random() * (max - min) + min);
    }

    public static double randomRange(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static int sign(double value) {
        return (int)Math.signum(value);
    }

    public static int sign(int value) {
        return (int)Math.signum(value);
    }

    public static int floor(double value) {
        return (int)Math.floor(value);
    }

    public static int ceil(double value) {
        return (int)Math.ceil(value);
    }

    public static double lengthDirectionX(double length, double direction) {
        return length * dcos(direction);
    }

    public static double lengthDirectionY(double length, double direction) {
        return length * dsin(direction);
    }
}
