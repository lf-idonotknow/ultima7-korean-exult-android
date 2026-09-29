package info.exult;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.*;
import org.libsdl.app.SDLActivity;
import java.util.*;

/** Translates physical controls before SDL distributes events to its modal loops. */
final class AndroidPad implements InputManager.InputDeviceListener {
    final ExultActivity activity;
    final Handler handler=new Handler(Looper.getMainLooper());
    final PadControls controls;
    final InputManager manager;
    final Map<String,Integer> keys=new HashMap<>();
    boolean active;
    int device=-1;
    final Set<Integer> announced=new HashSet<>();
    AndroidPad(ExultActivity a) {
        activity=a;
        controls=new PadControls(new PadControls.Sink() {
            public void mouse(int state,int action,float x,float y) {SDLActivity.onNativeMouse(state,action,x,y,false);}
            public void key(String key,boolean down) {
                int before=keys.getOrDefault(key,0), after=Math.max(0,before+(down?1:-1));keys.put(key,after);
                int code=key.equals("ESC")?KeyEvent.KEYCODE_ESCAPE:key.equals("SPACE")?KeyEvent.KEYCODE_SPACE:
                        KeyEvent.KEYCODE_A+key.charAt(0)-'A';
                if(before==0&&after>0)SDLActivity.onNativeKeyDown(code);
                if(before>0&&after==0)SDLActivity.onNativeKeyUp(code);
            }
            public void log(String message) {KoreanDiagnostics.record(message);}
        });
        manager=(InputManager)a.getSystemService(Context.INPUT_SERVICE);
        manager.registerInputDeviceListener(this,handler);
    }
    boolean ready() {return active && SDLActivity.mCurrentNativeState==SDLActivity.NativeState.RESUMED;}
    final Runnable frame=new Runnable() {public void run() {
        if(!active)return;
        if(ready()) {View v=activity.padSurface();if(v!=null)controls.size(v.getWidth(),v.getHeight());controls.tick(SystemClock.uptimeMillis());}
        handler.postDelayed(this,16);
    }};
    void resume() {if(active)return;active=true;handler.post(frame);}
    void pause(String why) {if(active)controls.reset(why);active=false;handler.removeCallbacks(frame);}
    void destroy() {pause("destroy");manager.unregisterInputDeviceListener(this);}
    static boolean isPad(InputDevice d) {
        return d!=null && ((d.getSources()&InputDevice.SOURCE_GAMEPAD)==InputDevice.SOURCE_GAMEPAD
                ||(d.getSources()&InputDevice.SOURCE_JOYSTICK)==InputDevice.SOURCE_JOYSTICK);
    }
    void useDevice(InputDevice d) {
        if(device!=d.getId()) {controls.reset("device switch");device=d.getId();}
        if(announced.add(device)) {
            StringBuilder b=new StringBuilder("DEVICE id="+device+" name="+d.getName()+" vendor="+d.getVendorId()+" product="+d.getProductId());
            for(InputDevice.MotionRange r:d.getMotionRanges())b.append(" axis=").append(r.getAxis()).append("[").append(r.getMin()).append(',').append(r.getMax()).append(']');
            KoreanDiagnostics.record(b.toString());
            KoreanDiagnostics.record("MAP right="+(has(d,MotionEvent.AXIS_Z)&&has(d,MotionEvent.AXIS_RZ)?"Z/RZ":"RX/RY")+" deadzone=.18 cursor=.85width/s precision=.16width/s");
        }
    }
    boolean key(KeyEvent event) {
        if(!ready() || !isPad(event.getDevice()))return false;
        useDevice(event.getDevice());
        String button=button(event.getKeyCode());
        if(button==null) {if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0)KoreanDiagnostics.record("UNMAPPED key="+event.getKeyCode());return false;}
        if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0)controls.button(button,"key:"+event.getKeyCode(),true);
        else if(event.getAction()==KeyEvent.ACTION_UP)controls.button(button,"key:"+event.getKeyCode(),false);
        return true;
    }
    static String button(int k) {
        switch(k) {
        case KeyEvent.KEYCODE_BUTTON_A:return "A";case KeyEvent.KEYCODE_BUTTON_B:return "B";
        case KeyEvent.KEYCODE_BUTTON_X:return "X";case KeyEvent.KEYCODE_BUTTON_Y:return "Y";
        case KeyEvent.KEYCODE_BUTTON_L1:return "L1";case KeyEvent.KEYCODE_BUTTON_R1:return "R1";
        case KeyEvent.KEYCODE_BUTTON_L2:return "L2";case KeyEvent.KEYCODE_BUTTON_R2:return "R2";
        case KeyEvent.KEYCODE_BUTTON_THUMBL:return "L3";case KeyEvent.KEYCODE_BUTTON_THUMBR:return "R3";
        case KeyEvent.KEYCODE_DPAD_UP:return "UP";case KeyEvent.KEYCODE_DPAD_DOWN:return "DOWN";
        case KeyEvent.KEYCODE_DPAD_LEFT:return "LEFT";case KeyEvent.KEYCODE_DPAD_RIGHT:return "RIGHT";
        case KeyEvent.KEYCODE_BUTTON_START:return "START";case KeyEvent.KEYCODE_BUTTON_SELECT:return "SELECT";
        default:return null;
        }
    }
    static boolean has(InputDevice d,int axis) {return d.getMotionRange(axis,InputDevice.SOURCE_JOYSTICK)!=null;}
    static float axis(MotionEvent e,int axis) {return has(e.getDevice(),axis)?e.getAxisValue(axis):0;}
    void motion(MotionEvent e) {
        if(!ready()||!isPad(e.getDevice())||(e.getSource()&InputDevice.SOURCE_JOYSTICK)!=InputDevice.SOURCE_JOYSTICK)return;
        useDevice(e.getDevice());
        boolean zr=has(e.getDevice(),MotionEvent.AXIS_Z)&&has(e.getDevice(),MotionEvent.AXIS_RZ);
        controls.axes(axis(e,MotionEvent.AXIS_X),axis(e,MotionEvent.AXIS_Y),axis(e,zr?MotionEvent.AXIS_Z:MotionEvent.AXIS_RX),axis(e,zr?MotionEvent.AXIS_RZ:MotionEvent.AXIS_RY));
        trigger("L2",Math.max(axis(e,MotionEvent.AXIS_LTRIGGER),axis(e,MotionEvent.AXIS_BRAKE)));
        trigger("R2",Math.max(axis(e,MotionEvent.AXIS_RTRIGGER),axis(e,MotionEvent.AXIS_GAS)));
        float hx=axis(e,MotionEvent.AXIS_HAT_X),hy=axis(e,MotionEvent.AXIS_HAT_Y);
        controls.button("LEFT","hat",hx<-.5);controls.button("RIGHT","hat",hx>.5);
        controls.button("UP","hat",hy<-.5);controls.button("DOWN","hat",hy>.5);
    }
    void trigger(String button,float value) {
        Set<String> sources=controls.held.get(button);
        boolean pressed=sources!=null&&sources.contains("axis");
        controls.button(button,"axis",value>(pressed?.35:.55));
    }
    void touch(MotionEvent event) {
        if(!ready()||event.getActionMasked()!=MotionEvent.ACTION_DOWN)return;
        if((event.getSource()&InputDevice.SOURCE_TOUCHSCREEN)!=InputDevice.SOURCE_TOUCHSCREEN)return;
        View v=activity.padSurface();if(v==null)return;
        int[] location=new int[2];v.getLocationOnScreen(location);
        controls.touch(event.getRawX()-location[0],event.getRawY()-location[1]);
    }
    public void onInputDeviceAdded(int id) {KoreanDiagnostics.record("DEVICE added="+id);}
    public void onInputDeviceChanged(int id) {announced.remove(id);}
    public void onInputDeviceRemoved(int id) {if(device==id)controls.reset("device removed");announced.remove(id);}
}
