/*
 *  Copyright (C) 2021-2025  The Exult Team
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package info.exult;

import android.util.Log;
import android.view.View;

public class ModsFragment extends ContentInstallerFragment {
	@Override
	public void onViewCreated(View view, android.os.Bundle state) {
		super.onViewCreated(view, state);
		android.widget.LinearLayout layout = view.findViewById(R.id.contentLayout);
		int position = 0;
		if (BundledContentInstaller.hasAsset(requireContext(), "koreanbg.zip")) {
			addKoreanMod(
					layout, position, "forgeofvirtue", "koreanbg",
					"Black Gate 한글");
			position += 2;
		}
		if (BundledContentInstaller.hasAsset(requireContext(), "koreansi.zip")) {
			addKoreanMod(
					layout, position, "silverseed", "koreansi",
					"Serpent Isle 한글");
			position += 2;
		}
		if (position == 0) {
			return;
		}
		android.view.ViewGroup parent = (android.view.ViewGroup)layout.getParent();
		int index = parent.indexOfChild(layout);
		parent.removeView(layout);
		android.widget.ScrollView scroll = new android.widget.ScrollView(requireContext());
		scroll.addView(layout);
		parent.addView(scroll, index, new android.widget.LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
	}

	private void addKoreanMod(android.widget.LinearLayout layout, int position,
			String game, String mod, String title) {
		android.content.Context context = requireContext().getApplicationContext();
		android.widget.CheckBox status = new android.widget.CheckBox(requireContext());
		status.setText(title + " · 파일 확인 중");
		status.setClickable(false);
		android.widget.Button launch = new android.widget.Button(requireContext());
		launch.setText(title + " 실행 / 파일 복구");
		launch.setAllCaps(false);
		layout.addView(status, position);
		layout.addView(launch, position + 1);
		new Thread(() -> {
			boolean valid = false;
			String detail;
			try {
				valid = KoreanModFiles.check(context.getAssets().open("bundled-content/" + mod + ".zip"),
						context.getFilesDir().toPath().resolve(game).resolve("mods"), mod, false);
				detail = valid ? "파일 확인 완료" : "파일 복구 필요";
			} catch (Exception e) { detail = "확인 실패: " + e.getMessage(); }
			final boolean checked = valid;
			final String label = detail;
			status.post(() -> { status.setChecked(checked); status.setText(title + " · " + label); });
		}, "Korean mod check").start();
		launch.setOnClickListener(v -> {
			launch.setEnabled(false);
			status.setText(title + " · 검사 및 복구 중");
			new Thread(() -> {
				try {
					if (!KoreanModFiles.check(context.getAssets().open("bundled-content/" + mod + ".zip"),
							context.getFilesDir().toPath().resolve(game).resolve("mods"), mod, true))
						throw new java.io.IOException("파일 검증 실패");
					launch.post(() -> {
						launch.setEnabled(true);
						status.setChecked(true);
						status.setText(title + " · 파일 확인 완료");
						if (!isAdded()) return;
						android.content.Intent intent = new android.content.Intent(requireContext(), ExultActivity.class);
						intent.putExtra("koreanGame", game);
						intent.putExtra("koreanMod", mod);
						ExultActivity.clearConsole();
						startActivity(intent);
					});
				} catch (Exception e) {
					launch.post(() -> { launch.setEnabled(true); status.setChecked(false);
						status.setText(title + " · 실패: " + e.getMessage()); });
				}
			}, "Korean mod repair").start();
		});
	}
	ModsFragment() {
		super(R.layout.mods_card, R.string.mods_card_text);
	}

	@Override
	protected boolean shouldShowCustomModButton() {
		return true;    // Only the mods fragment should show this button
	}

	@Override
	protected ExultContent buildContentFromView(String name, View view) {
		if ("customMod".equals(name)) {
			// Create a placeholder that will be replaced later
			return new ExultModContent("placeholder", name, getContext());
		}

		String game = (String)view.getTag(R.id.game);
		return new ExultModContent(game, name, getContext());
	}
}
