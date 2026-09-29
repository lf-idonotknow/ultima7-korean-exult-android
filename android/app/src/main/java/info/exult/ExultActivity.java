/*
 *  Copyright (C) 2021-2022  The Exult Team
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.view.WindowCompat;
import org.libsdl.app.SDLActivity;

/**
 * This class implements the Activity that will launch the Exult game engine on
 * Android.
 */
public class ExultActivity extends SDLActivity {
    private AndroidPad pad;
    public android.view.View padSurface() {return mSurface;}
    public static void logPadEffect(String message) {KoreanDiagnostics.record("ENGINE "+message);}
    @Override public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        return pad!=null && pad.key(event) || super.dispatchKeyEvent(event);
    }
    @Override public boolean dispatchGenericMotionEvent(android.view.MotionEvent event) {
        if(pad!=null)pad.motion(event);
        return super.dispatchGenericMotionEvent(event); // Keep native left-stick movement.
    }
    @Override public boolean dispatchTouchEvent(android.view.MotionEvent event) {
        if(pad!=null)pad.touch(event);
        return super.dispatchTouchEvent(event);
    }
    @Override public void onWindowFocusChanged(boolean focus) {
        if(!focus && pad!=null)pad.pause("focus lost");
        super.onWindowFocusChanged(focus);
        if(focus && pad!=null)pad.resume();
    }
    @Override protected void onPause() {
        if(pad!=null)pad.pause("activity paused");
        super.onPause();
    }
    @Override protected void onResume() {
        super.onResume();if(pad!=null && hasWindowFocus())pad.resume();
    }
    @Override protected void onDestroy() {
        if(pad!=null)pad.destroy();super.onDestroy();
    }
    public int getDiagnosticFd() { return KoreanDiagnostics.open(this); }
	@Override
	protected String[] getArguments() {
		String game = getIntent().getStringExtra("koreanGame");
		String mod = getIntent().getStringExtra("koreanMod");
		if (("forgeofvirtue".equals(game) && "koreanbg".equals(mod))
				|| ("silverseed".equals(game) && "koreansi".equals(mod))) {
			return new String[] {"--game", game};
		}
		return super.getArguments();
	}
	// Making this static for easy access from UI fragments, but it feels a bit
	// hacky.
	private static String m_consoleLog;

	private static ExultActivity m_instance;
	public static String         consoleLog;

	/**
	 * This method clears the console buffer.
	 */
	public static void clearConsole() {
		m_consoleLog = "";
	}

	/**
	 * This method appends a new line to the console buffer.
	 *
	 * @param message The message to append to the console buffer.
	 */
	public void writeToConsole(String message) {
		if (message != null) {
            if (m_consoleLog == null) m_consoleLog = "";
            m_consoleLog += message + "\n";
            if (m_consoleLog.length() > 262144)
                m_consoleLog = m_consoleLog.substring(m_consoleLog.length() - 131072);
		}
	}

    public void writeToConsoleBytes(byte[] bytes) {
        if (bytes != null) writeToConsole(KoreanLogText.decode(bytes));
    }

	public static ExultActivity instance() {
		return m_instance;
	}

	public void showGameControls(String dpadLocation) {
		runOnUiThread(() -> {
			RelativeLayout.LayoutParams dpadLayoutParams
					= (RelativeLayout.LayoutParams)
							  m_dpadImageView.getLayoutParams();
			dpadLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			dpadLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);

			switch (dpadLocation) {
			case "left":
				dpadLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				dpadLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				break;
			case "right":
				dpadLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				dpadLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
				break;
			default:
				m_dpadImageView.setVisibility(View.GONE);
				return;
			}
			m_dpadImageView.setLayoutParams(dpadLayoutParams);
			m_dpadImageView.setVisibility(View.VISIBLE);
		});
	}

	public void hideGameControls() {
		runOnUiThread(() -> { m_dpadImageView.setVisibility(View.GONE); });
	}

	public void showButtonControls(String dpadLocation) {
		runOnUiThread(() -> {
			RelativeLayout.LayoutParams escLayoutParams
					= (RelativeLayout.LayoutParams)
							  m_escTextView.getLayoutParams();
			escLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			escLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);

			switch (dpadLocation) {
			case "left":
				escLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				escLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
				break;
			case "right":
				escLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				escLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				break;
			default:
				m_escTextView.setVisibility(View.GONE);
				return;
			}
			m_escTextView.setLayoutParams(escLayoutParams);
			m_escTextView.setVisibility(View.VISIBLE);
		});
	}

	public void hideButtonControls() {
		runOnUiThread(() -> { m_escTextView.setVisibility(View.GONE); });
	}

	private TextView m_escTextView;

	private ImageView m_dpadImageView;
	private float     m_dpadImageViewActionDownX;
	private float     m_dpadImageViewActionDownY;

	public native void setVirtualJoystick(float x, float y);

	public native void sendEscapeKeypress();

    public native void setName(byte[] name);

    public void promptForNameBytes(byte[] name) {
        final String current = KoreanLogText.decode(name);
        runOnUiThread(() -> {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setSingleLine(true);
            input.setText(current);
            input.setSelection(input.length());
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("캐릭터 이름")
                    .setMessage("한글 최대 8자 / 영문 최대 16자 (혼합 가능)")
                    .setView(input)
                    .setPositiveButton("확인", null)
                    .setNegativeButton("취소", (d, which) -> d.cancel())
                    .create();
            dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        try {
                            byte[] encoded = KoreanNameText.encode(input.getText().toString());
                            setName(encoded);
                            dialog.dismiss();
                        } catch (IllegalArgumentException error) {
                            input.setError(error.getMessage());
                        }
                    }));
            dialog.show();
        });
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// stretch Exult to all edges and don't make room for the camera cutout
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		getWindow().getAttributes().layoutInDisplayCutoutMode
				= WindowManager.LayoutParams
						  .LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;

		m_instance = this;
        KoreanDiagnostics.open(this);
        pad = new AndroidPad(this);
        if(hasWindowFocus())pad.resume();

		m_escTextView = new TextView(this);
		m_escTextView.setBackground(getResources().getDrawable(R.drawable.btn));
		m_escTextView.setPadding(20, 20, 20, 20);
		m_escTextView.setAutoSizeTextTypeWithDefaults(
				TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
		m_escTextView.setGravity(Gravity.CENTER);
		m_escTextView.setText("ESC");

		m_escTextView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				boolean inside
						= !(event.getX() < 0 || event.getY() < 0
							|| event.getX() > v.getMeasuredWidth()
							|| event.getY() > v.getMeasuredHeight());
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					m_escTextView.setBackground(
							getResources().getDrawable(R.drawable.btnpressed));
					return true;
				case MotionEvent.ACTION_MOVE:
					m_escTextView.setBackground(getResources().getDrawable(
							inside ? R.drawable.btnpressed : R.drawable.btn));
					return true;
				case MotionEvent.ACTION_UP:
					m_escTextView.setBackground(
							getResources().getDrawable(R.drawable.btn));
					if (inside) {
						sendEscapeKeypress();
					}
					return true;
				default:
					// Appease CodeFactor with a default case
					break;
				}
				return false;
			}
		});

		m_dpadImageView = new ImageView(this);
		m_dpadImageView.setImageResource(R.drawable.dpad_center);
		m_dpadImageView.setPadding(140, 0, 140, 170);
		m_dpadImageView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					m_dpadImageViewActionDownX = event.getRawX();
					m_dpadImageViewActionDownY = event.getRawY();
					return true;
				case MotionEvent.ACTION_MOVE:
					float deltaX = event.getRawX() - m_dpadImageViewActionDownX;
					float deltaY = event.getRawY() - m_dpadImageViewActionDownY;

					setVirtualJoystick(
							deltaX / (m_dpadImageView.getWidth() / 2),
							deltaY / (m_dpadImageView.getHeight() / 2));

					double radius
							= Math.sqrt(deltaX * deltaX + deltaY * deltaY);
					if (radius < m_dpadImageView.getWidth() / 8) {
						m_dpadImageView.setImageResource(
								R.drawable.dpad_center);
						return true;
					}
					int[] directionDrawables = {
							R.drawable.dpad_east,  R.drawable.dpad_southeast,
							R.drawable.dpad_south, R.drawable.dpad_southwest,
							R.drawable.dpad_west,  R.drawable.dpad_northwest,
							R.drawable.dpad_north, R.drawable.dpad_northeast};
					double angle
							= Math.toDegrees(Math.atan2(deltaY, deltaX)) + 360;
					int    directionCount = directionDrawables.length;
					double directionWidth = 360.0 / directionCount;
					int    direction = (int)Math.round(angle / directionWidth)
									% directionCount;
					m_dpadImageView.setImageResource(
							directionDrawables[direction]);
					return true;
				case MotionEvent.ACTION_UP:
					setVirtualJoystick(0, 0);
					m_dpadImageView.setImageResource(R.drawable.dpad_center);
					return true;
				default:
					// Appease CodeFactor with a default case
					break;
				}
				return false;
			}
		});
		hideGameControls();
		hideButtonControls();
		mLayout.addView(m_escTextView);
		mLayout.addView(m_dpadImageView);
	}

	/**
	 * This method retrieves the current console buffer.
	 */
	public static String getConsole() {
		return m_consoleLog;
	}

	/**
	 * This method returns the name of the application entry point It can be
	 * overridden by derived classes.
	 */
	protected String getMainFunction() {
		return "ExultAndroid_main";
	}

	/**
	 * This method is called by SDL before loading the native shared libraries.
	 * It can be overridden to provide names of shared libraries to be loaded.
	 * The default implementation returns the defaults. It never returns null.
	 * An array returned by a new implementation must at least contain "SDL2".
	 * Also keep in mind that the order the libraries are loaded may matter.
	 *
	 * @return names of shared libraries to be loaded (e.g. "SDL2", "main").
	 */
	protected String[] getLibraries() {
		return new String[] {"SDL2", "exult-android-wrapper"};
	}
}
