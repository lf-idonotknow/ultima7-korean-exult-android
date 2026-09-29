package info.exult;

import java.nio.charset.Charset;

/** Decode legacy Korean engine logs without altering any game text buffers. */
final class KoreanLogText {
    private static final Charset ENCODING = Charset.forName("MS949");
    static String decode(byte[] bytes) { return new String(bytes, ENCODING); }
}
