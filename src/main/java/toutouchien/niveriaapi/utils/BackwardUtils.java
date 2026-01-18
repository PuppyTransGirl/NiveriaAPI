package toutouchien.niveriaapi.utils;

import net.kyori.adventure.sound.Sound;

public class BackwardUtils {
    private BackwardUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final Sound.Source UI_SOUND_SOURCE = VersionUtils.isHigherThanOrEquals(VersionUtils.v1_12_6) ? Sound.Source.UI : Sound.Source.MASTER;
}
