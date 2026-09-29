/*
 *  Copyright (C) 2026
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 */

package info.exult;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.apache.commons.compress.archivers.ArchiveEntry;

/** Imports user-supplied Korean patch files without bundling them in the APK. */
final class KoreanPatchContent extends ExultContent {
	static final String COMMON = "korean-common";
	static final String BG     = "korean-bg";
	static final String SI     = "korean-si";

	private static final String[] COMMON_FILES = {
			"exultmsg.txt", "font0bg.fnt", "font0si.fnt", "font1bg.fnt",
			"font1si.fnt", "font2bg.fnt", "font2si.fnt", "font2ex.fnt",
			"font4bg.fnt", "font4si.fnt", "font7bg.fnt", "font7si.fnt",
			"font13bg.fnt", "font13si.fnt", "font19bg.fnt", "font19si.fnt",
			"font24bg.fnt", "font25bg.fnt", "font34si.fnt", "font90bg.fnt",
			"font90si.fnt", "font95ex.fnt", "font96ex.fnt", "font97ex.fnt",
			"font98ex.fnt"};
	private static final String[] BG_FILES = {
			"endshape.flx", "faces.vga", "fonts.vga", "gumps.vga",
			"initgame.dat", "mainshp.flx", "pointers.shp", "text.flx",
			"usecode"};
	private static final String[] SI_FILES = {
			"faces.vga", "gumps.vga", "initgame.dat", "mainshp.flx",
			"pointers.shp", "schedule.dat", "sprites.vga", "text.flx",
			"usecode"};

	private final Path   installPath;
	private final String sentinel;
	private Path         rootInArchive;

	KoreanPatchContent(String name, Context context) {
		super("korean-patch", name, context);
		Path files = context.getFilesDir().toPath();
		switch (name) {
		case COMMON:
			installPath = files.resolve("patch");
			sentinel    = "font0bg.fnt";
			break;
		case BG:
			installPath = files.resolve("forgeofvirtue").resolve("patch");
			sentinel    = "endshape.flx";
			break;
		case SI:
			installPath = files.resolve("silverseed").resolve("patch");
			sentinel    = "schedule.dat";
			break;
		default:
			throw new IllegalArgumentException("Unknown Korean patch: " + name);
		}
	}

	@Override
	protected boolean identify(
			Path location, ArchiveEntry entry, InputStream inputStream)
			throws IOException {
		if (entry == null || entry.isDirectory()) {
			return false;
		}
		Path full = getFullArchivePath(location, entry);
		if (!full.getFileName().toString().toLowerCase(Locale.ROOT).equals(
				sentinel)) {
			return false;
		}
		rootInArchive = full.getParent();
		if (rootInArchive == null) {
			rootInArchive = Paths.get("");
		}
		return true;
	}

	@Override
	protected Path getContentRootInArchive() {
		return rootInArchive;
	}

	@Override
	protected Path getContentInstallRoot() {
		return installPath;
	}

	@Override
	protected Path getInstallDestination(Path location, ArchiveEntry entry) {
		Path destination = super.getInstallDestination(location, entry);
		if (destination == null) {
			return null;
		}
		Path normalizedRoot = installPath.normalize();
		Path normalized     = destination.normalize();
		return normalized.startsWith(normalizedRoot) ? normalized : null;
	}

	static boolean isReady(Context context, String game) {
		Path files = context.getFilesDir().toPath();
		if (!contains(files.resolve("patch"), COMMON_FILES)) {
			return false;
		}
		if ("forgeofvirtue".equals(game)) {
			return contains(files.resolve(game).resolve("patch"), BG_FILES);
		}
		if ("silverseed".equals(game)) {
			return contains(files.resolve(game).resolve("patch"), SI_FILES);
		}
		return false;
	}

	private static boolean contains(Path root, String[] files) {
		for (String file : files) {
			if (!Files.isRegularFile(root.resolve(file))) {
				return false;
			}
		}
		return true;
	}
}
