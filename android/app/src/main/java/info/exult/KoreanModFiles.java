package info.exult;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

/** Checks the actual bundled files; never infers integrity from preferences. */
final class KoreanModFiles {
    static boolean check(InputStream archive, Path mods, String mod, boolean repair)
            throws IOException {
        boolean valid = true;
        boolean config = false, usecode = false;
        try (ZipInputStream zip = new ZipInputStream(archive)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!name.equals(mod + ".cfg") && !name.startsWith(mod + "/data/"))
                    throw new IOException("Unexpected mod entry: " + name);
                Path rel = Paths.get(name);
                String leaf = rel.getFileName().toString();
                if (!leaf.endsWith(".cfg")) leaf = leaf.toLowerCase(Locale.ROOT);
                rel = rel.getParent() == null ? Paths.get(leaf) : rel.getParent().resolve(leaf);
                Path destination = mods.resolve(rel).normalize();
                if (!destination.startsWith(mods.normalize())) throw new IOException("Invalid path");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int length;
                while ((length = zip.read(buffer)) != -1) bytes.write(buffer, 0, length);
                byte[] expected = bytes.toByteArray();
                boolean matches = Files.isRegularFile(destination)
                        && Arrays.equals(expected, Files.readAllBytes(destination));
                if (!matches && repair) {
                    Files.createDirectories(destination.getParent());
                    Path temporary = Files.createTempFile(destination.getParent(), ".korean-", ".tmp");
                    try {
                        Files.write(temporary, expected);
                        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
                    } finally { Files.deleteIfExists(temporary); }
                    matches = Arrays.equals(expected, Files.readAllBytes(destination));
                }
                valid &= matches;
                config |= name.equals(mod + ".cfg");
                usecode |= rel.toString().replace('\\', '/').equals(mod + "/data/usecode");
            }
        }
        if (!config || !usecode) throw new IOException("Incomplete Korean mod archive: " + mod);
        return valid;
    }
}
