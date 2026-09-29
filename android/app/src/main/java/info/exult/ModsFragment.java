/*
 *  Copyright (C) 2021-2026  The Exult Team
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 */

package info.exult;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ModsFragment extends ContentInstallerFragment {
	@Override
	public void onViewCreated(View view, android.os.Bundle state) {
		super.onViewCreated(view, state);
		LinearLayout layout = view.findViewById(R.id.contentLayout);
		addKoreanLaunch(
				layout, 0, "forgeofvirtue", "koreanbg", "Black Gate 한글");
		addKoreanLaunch(
				layout, 2, "silverseed", "koreansi", "Serpent Isle 한글");

		ViewGroup parent = (ViewGroup)layout.getParent();
		int index = parent.indexOfChild(layout);
		parent.removeView(layout);
		ScrollView scroll = new ScrollView(requireContext());
		scroll.addView(layout);
		parent.addView(
				scroll, index,
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
	}

	private void addKoreanLaunch(
			LinearLayout layout, int position, String game, String bundledMod,
			String title) {
		Context context = requireContext().getApplicationContext();
		CheckBox status = new CheckBox(requireContext());
		status.setClickable(false);
		Button launch = new Button(requireContext());
		launch.setText(title + " 실행");
		launch.setAllCaps(false);
		layout.addView(status, position);
		layout.addView(launch, position + 1);
		refreshStatus(context, status, game, bundledMod, title);

		launch.setOnClickListener(v -> {
			launch.setEnabled(false);
			status.setText(title + " · 파일 검사 중");
			new Thread(() -> {
				try {
					boolean bundled = BundledContentInstaller.hasAsset(
							context, bundledMod + ".zip");
					if (bundled) {
						if (!KoreanModFiles.check(
								context.getAssets().open(
										"bundled-content/" + bundledMod + ".zip"),
								context.getFilesDir().toPath().resolve(game).resolve(
										"mods"),
								bundledMod, true)) {
							throw new java.io.IOException("한글 모드 파일 검증 실패");
						}
					} else if (!KoreanPatchContent.isReady(context, game)) {
						throw new java.io.IOException(
								"아래에서 공통 한글 글꼴과 이 게임의 한글 패치를 먼저 설치하십시오");
					}
					launch.post(() -> {
						launch.setEnabled(true);
						status.setChecked(true);
						status.setText(title + " · 파일 확인 완료");
						if (!isAdded()) {
							return;
						}
						Intent intent = new Intent(requireContext(), ExultActivity.class);
						intent.putExtra("koreanGame", game);
						if (bundled) {
							intent.putExtra("koreanMod", bundledMod);
						}
						ExultActivity.clearConsole();
						startActivity(intent);
					});
				} catch (Exception exception) {
					launch.post(() -> {
						launch.setEnabled(true);
						status.setChecked(false);
						status.setText(title + " · " + exception.getMessage());
					});
				}
			}, "Korean patch launch").start();
		});
	}

	private void refreshStatus(
			Context context, CheckBox status, String game, String bundledMod,
			String title) {
		new Thread(() -> {
			boolean valid = KoreanPatchContent.isReady(context, game);
			if (!valid && BundledContentInstaller.hasAsset(
							 context, bundledMod + ".zip")) {
				try {
					valid = KoreanModFiles.check(
							context.getAssets().open(
									"bundled-content/" + bundledMod + ".zip"),
							context.getFilesDir().toPath().resolve(game).resolve(
									"mods"),
							bundledMod, false);
				} catch (Exception ignored) {
					valid = false;
				}
			}
			final boolean ready = valid;
			status.post(() -> {
				status.setChecked(ready);
				status.setText(
						title + (ready ? " · 파일 확인 완료" : " · 한글 패치 설치 필요"));
			});
		}, "Korean patch check").start();
	}

	ModsFragment() {
		super(R.layout.mods_card, R.string.mods_card_text);
	}

	@Override
	protected boolean shouldShowCustomModButton() {
		return true;
	}

	@Override
	protected ExultContent buildContentFromView(String name, View view) {
		if (KoreanPatchContent.COMMON.equals(name)
			|| KoreanPatchContent.BG.equals(name)
			|| KoreanPatchContent.SI.equals(name)) {
			return new KoreanPatchContent(name, view.getContext());
		}
		if ("customMod".equals(name)) {
			return new ExultModContent("placeholder", name, getContext());
		}
		String game = (String)view.getTag(R.id.game);
		return new ExultModContent(game, name, getContext());
	}
}
