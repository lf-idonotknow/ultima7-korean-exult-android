/*
 * Copyright (C) 2026
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package info.exult;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class PadControlsTest {
	private static final class Sink implements PadControls.Sink {
		int mouseState;
		int downs;
		int ups;
		int moves;
		final List<String> keys = new ArrayList<>();
		final List<String> logs = new ArrayList<>();

		@Override
		public void mouse(int state, int action, float x, float y) {
			if (action == 0) {
				downs++;
			} else if (action == 1) {
				ups++;
			} else if (action == 2) {
				moves++;
			}
			mouseState = state;
		}

		@Override
		public void key(String key, boolean down) {
			keys.add(key + ":" + down);
		}

		@Override
		public void log(String message) {
			logs.add(message);
		}
	}

	@Test
	public void rightStickMovesContinuouslyWithoutFloodingLog() {
		Sink sink = new Sink();
		PadControls pad = new PadControls(sink);
		pad.size(1920, 1080);
		pad.axes(0, 0, .5f, 0);
		float start = pad.x;
		int logCount = sink.logs.size();
		for (int frame = 1; frame <= 100; frame++) {
			pad.tick(frame * 16L);
		}
		assertTrue(pad.x > start);
		assertEquals(logCount, sink.logs.size());
	}

	@Test
	public void overlappingLeftClickSourcesDoNotReleaseDragEarly() {
		Sink sink = new Sink();
		PadControls pad = new PadControls(sink);
		pad.size(1920, 1080);
		pad.button("R2", "axis", true);
		pad.button("A", "key", true);
		pad.button("R2", "axis", false);
		assertEquals(1, sink.mouseState);
		pad.button("A", "key", false);
		assertEquals(0, sink.mouseState);
		assertEquals(1, sink.downs);
		assertEquals(1, sink.ups);
	}

	@Test
	public void focusLossReleasesMouseAndKeyboardState() {
		Sink sink = new Sink();
		PadControls pad = new PadControls(sink);
		pad.size(1920, 1080);
		pad.button("L2", "axis", true);
		pad.button("X", "key", true);
		pad.axes(0, 0, .8f, .2f);
		pad.reset("focus lost");
		assertEquals(0, sink.mouseState);
		assertFalse(pad.rightMoving);
		assertTrue(sink.keys.contains("I:false"));
	}

	@Test
	public void faceAndDpadButtonsMatchDocumentedKeys() {
		Sink sink = new Sink();
		PadControls pad = new PadControls(sink);
		String[] buttons = {"B", "X", "Y", "UP", "RIGHT", "DOWN", "LEFT", "L3"};
		String[] expected = {"ESC", "I", "C", "M", "B", "Z", "S", "SPACE"};
		for (int index = 0; index < buttons.length; index++) {
			pad.button(buttons[index], "test", true);
			pad.button(buttons[index], "test", false);
			assertEquals(expected[index] + ":true", sink.keys.get(index * 2));
			assertEquals(expected[index] + ":false", sink.keys.get(index * 2 + 1));
		}
	}
}
