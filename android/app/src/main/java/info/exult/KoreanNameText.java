package info.exult;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;

/** The customized engine and actor save records use MS949, up to 16 bytes. */
final class KoreanNameText {
    static byte[] encode(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            // The bundled fonts contain modern syllables and compatibility jamo.
            if (!((c >= 0x20 && c <= 0x7e) || (c >= 0xac00 && c <= 0xd7a3)
                    || (c >= 0x3131 && c <= 0x3163)))
                throw new IllegalArgumentException("한글, 영문, 숫자, 기본 기호만 입력해 주세요.");
        }
        try {
            ByteBuffer buffer = Charset.forName("MS949").newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(name));
            if (buffer.remaining() > 16)
                throw new IllegalArgumentException("이름이 너무 깁니다. 한글 8자 또는 영문 16자 이내로 입력해 주세요.");
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        } catch (CharacterCodingException error) {
            throw new IllegalArgumentException("이 이름은 게임의 한글 글꼴로 표시할 수 없습니다.", error);
        }
    }
}
