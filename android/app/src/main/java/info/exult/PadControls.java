package info.exult;

import java.util.*;

/** Platform-independent pad state. Sink receives real mouse/key transitions. */
final class PadControls {
    interface Sink {
        void mouse(int state, int action, float x, float y);
        void key(String key, boolean down);
        void log(String message);
    }
    final Sink sink;
    final Map<String, Set<String>> held = new HashMap<>();
    float x, y, width=1, height=1, rx, ry;
    int mouseState;
    boolean positioned, leftMoving, rightMoving;
    long lastTick;
    PadControls(Sink sink) { this.sink=sink; }
    void size(int w, int h) {
        if(w<=1 || h<=1) return;
        if(positioned && (width!=w || height!=h)) {x=x*w/width;y=y*h/height;}
        width=w; height=h;
        if(!positioned) {x=w/2f;y=h/2f;positioned=true;}
    }
    boolean isHeld(String button) {return held.containsKey(button) && !held.get(button).isEmpty();}
    void button(String button, String source, boolean down) {
        Set<String> sources=held.computeIfAbsent(button,k->new HashSet<>());
        boolean before=!sources.isEmpty();
        boolean changed=down?sources.add(source):sources.remove(source);
        if(!changed) return;
        boolean after=!sources.isEmpty();
        sink.log("INPUT "+button+" "+source+" "+(down?"down":"up")+(before==after?" duplicate-source":""));
        if(before==after) return;
        switch(button) {
        case "A": case "R2": case "L2": syncMouse(); break;
        case "R1":
            if(down) {
                if(mouseState!=0) {sink.log("ACTION double-click ignored: button held");break;}
                move();
                for(int i=0;i<2;i++) {sink.mouse(1,0,x,y);sink.mouse(0,1,x,y);}
                sink.log("ACTION double-left-click x="+(int)x+" y="+(int)y);
            }
            break;
        case "L1": sink.log("ACTION precision="+down); break;
        case "R3": if(down) {x=width/2;y=height/2;move();sink.log("ACTION cursor-center");} break;
        default:
            String key=keyFor(button);
            if(key!=null) {sink.key(key,down);sink.log("ACTION key="+key+" "+(down?"down":"up"));}
        }
    }
    static String keyFor(String b) {
        switch(b) {
        case "B": case "START":return "ESC";
        case "X":return "I";
        case "Y":return "C";
        case "UP":return "M";
        case "RIGHT":return "B";
        case "DOWN":return "Z";
        case "LEFT": case "SELECT":return "S";
        case "L3":return "SPACE";
        default:return null;
        }
    }
    void syncMouse() {
        int desired=(isHeld("A")||isHeld("R2")?1:0)|(isHeld("L2")?2:0);
        move();
        for(int bit:new int[]{1,2}) if((mouseState&bit)!=(desired&bit)) {
            boolean down=(desired&bit)!=0;
            mouseState=down?(mouseState|bit):(mouseState&~bit);
            sink.mouse(mouseState,down?0:1,x,y);
            sink.log("ACTION mouse="+(bit==1?"left":"right")+" "+(down?"down":"up")+" x="+(int)x+" y="+(int)y);
        }
    }
    void axes(float lx,float ly,float ax,float ay) {
        boolean lm=Math.hypot(lx,ly)>.25;
        boolean rm=Math.hypot(ax,ay)>.18;
        if(lm!=leftMoving) sink.log("STICK left "+(lm?"start -> SDL movement":"stop"));
        if(rm!=rightMoving) sink.log("STICK right "+(rm?"start -> cursor":"stop")+" x="+(int)x+" y="+(int)y);
        leftMoving=lm;rightMoving=rm;rx=ax;ry=ay;
    }
    void tick(long now) {
        float dt=lastTick==0?0:Math.min(50,now-lastTick)/1000f;lastTick=now;
        if(!rightMoving || !positioned) return;
        double length=Math.hypot(rx,ry);
        double amount=Math.min(1,(length-.18)/.82);
        double speed=width*(isHeld("L1")?.16:.85)*amount*amount*dt;
        x=Math.max(0,Math.min(width-1,x+(float)(rx/length*speed)));
        y=Math.max(0,Math.min(height-1,y+(float)(ry/length*speed)));
        move();
    }
    void move() {if(positioned)sink.mouse(mouseState,2,x,y);}
    void touch(float tx,float ty) {reset("touch takeover");x=Math.max(0,Math.min(width-1,tx));y=Math.max(0,Math.min(height-1,ty));}
    void reset(String why) {
        if(mouseState!=0 || leftMoving || rightMoving || held.values().stream().anyMatch(s->!s.isEmpty()))sink.log("RESET "+why);
        for(String b:new ArrayList<>(held.keySet()))for(String source:new ArrayList<>(held.get(b)))button(b,source,false);
        rx=ry=0;leftMoving=rightMoving=false;lastTick=0;
    }
}
