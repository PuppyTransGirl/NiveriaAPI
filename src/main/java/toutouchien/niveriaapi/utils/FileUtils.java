package toutouchien.niveriaapi.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileUtils {
    private static String[] invalidCharacters;
    private static String[] invalidWords;
    private static String os;

    private FileUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String[] invalidCharacters() {
        if (invalidCharacters != null)
            return invalidCharacters;

        if (os == null)
            os = operatingSystem();

        Set<String> temp = new HashSet<>(Arrays.asList(".", "*", "?", ":", ">", "\"", "|", "-"));

        switch (os) {
            case "Linux", "Mac" -> temp.add("/");
            case "Windows" -> temp.addAll(Arrays.asList("<", "/", "\\"));
        }

        return invalidCharacters = temp.toArray(new String[0]);
    }

    public static String[] invalidWords() {
        if (invalidWords != null)
            return invalidWords;

        if (os == null)
            os = operatingSystem();

        if (!os.equals("Windows"))
            return invalidWords = new String[0];

        return invalidWords = new String[]{"con", "prn", "aux", "nul", "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "com0", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "lpt0"};
    }

    private static String operatingSystem() {
        String os = System.getProperty("os.name");

        if (os.contains("Linux"))
            return "Linux";

        if (os.contains("Mac"))
            return "Mac";

        if (os.contains("Windows"))
            return "Windows";

        return "";
    }
}