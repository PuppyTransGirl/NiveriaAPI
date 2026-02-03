package toutouchien.niveriaapi.lang;

import java.time.Duration;
import java.util.regex.Pattern;

public class LangUtils {
    public static final Pattern NEWLINE_PATTERN = Pattern.compile("\\R");
    public static final String SOUND_SUFFIX = "_sound";
    public static final int DEFAULT_MAX_CACHE = 2048;
    public static final Duration DEFAULT_CACHE_EXPIRE = Duration.ofMinutes(30);
    public static final String DEFAULT_LANG_CODE = "en_US";

    private LangUtils() {
        throw new IllegalStateException("Utility class");
    }
}
