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
import android.net.Uri;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.archivers.ArchiveException;

/** Installs the locally supplied games and Korean mods on first launch. */
final class BundledContentInstaller {
	private static final String ASSET_DIRECTORY = "bundled-content";

	interface ProgressListener {
		void report(String packageName, String stage, String file);
	}

	interface CompletionListener {
		void report(boolean successful, String details);
	}

	private static final class Item {
		final String       assetName;
		final ExultContent content;

		Item(String assetName, ExultContent content) {
			this.assetName = assetName;
			this.content   = content;
		}
	}

	private BundledContentInstaller() {}

	static boolean hasAsset(Context context, String assetName) {
		try (InputStream ignored = context.getAssets().open(
				 ASSET_DIRECTORY + "/" + assetName)) {
			return true;
		} catch (IOException exception) {
			return false;
		}
	}

	private static List<Item> buildItems(Context context) {
		List<Item> items = new ArrayList<>();
		addIfPresent(context, items, new Item(
						"blackgate-static-local.zip",
						new ExultGameContent(
								"forgeofvirtue", context, 0x33EC5F67L)));
		addIfPresent(context, items, new Item(
						"serpentisle-static-local.zip",
						new ExultGameContent(
								"silverseed", context, 0xB274BD4DL)));
		addIfPresent(context, items, new Item(
						"koreanbg.zip",
						new ExultModContent(
								"forgeofvirtue", "koreanbg", context)));
		addIfPresent(context, items, new Item(
						"koreansi.zip",
						new ExultModContent(
								"silverseed", "koreansi", context)));
		return items;
	}

	private static void addIfPresent(
			Context context, List<Item> items, Item item) {
		if (hasAsset(context, item.assetName)) {
			items.add(item);
		}
	}

	static boolean needsInstall(Context context) {
		for (Item item : buildItems(context)) {
			if (!item.content.isInstalled()) {
				return true;
			}
		}
		return false;
	}

	static void installAsync(
			Context context, ProgressListener progressListener,
			CompletionListener completionListener) {
		Context applicationContext = context.getApplicationContext();
		new Thread(() -> {
			try {
				for (Item item : buildItems(applicationContext)) {
					if (item.content.isInstalled()) {
						continue;
					}
					installItem(applicationContext, item, progressListener);
				}
				completionListener.report(true, null);
			} catch (Exception exception) {
				completionListener.report(false, exception.toString());
			}
		}, "Exult bundled content installer").start();
	}

	private static void installItem(
			Context context, Item item, ProgressListener progressListener)
			throws IOException, ArchiveException {
		File temporaryArchive = new File(context.getCacheDir(), item.assetName);
		try {
			progressListener.report(item.assetName, "Preparing...", item.assetName);
			try (InputStream inputStream = context.getAssets().open(
					 ASSET_DIRECTORY + "/" + item.assetName)) {
				Files.copy(
						inputStream, temporaryArchive.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			}

			final boolean[] installSucceeded = {false};
			final String[]  installDetails   = {null};
			item.content.install(
					Uri.fromFile(temporaryArchive),
					(stage, file) -> progressListener.report(
							item.assetName, stage, file),
					(successful, details) -> {
						installSucceeded[0] = successful;
						installDetails[0]   = details;
					});
			if (!installSucceeded[0]) {
				throw new IOException(
						"Unable to install " + item.assetName + ": "
						+ installDetails[0]);
			}
		} finally {
			Files.deleteIfExists(temporaryArchive.toPath());
		}
	}
}
