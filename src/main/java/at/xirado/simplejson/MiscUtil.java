package at.xirado.simplejson;

public class MiscUtil {
    public static long parseLong(String input) {
        if (input.startsWith("-"))
            return Long.parseLong(input);
        else
            return Long.parseUnsignedLong(input);
    }
}
