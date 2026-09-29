package info.exult;

import android.content.*;
import android.os.*;
import android.provider.MediaStore;
import android.net.Uri;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Three 256 KiB pad trace slots plus two 1 MiB crash slots. No glyph/console dump. */
final class KoreanDiagnostics {
    static final int LIMIT=256*1024;
    private static Context context;
    private static OutputStream output;
    private static int bytes;
    private static boolean attempted;
    private static final long run=System.currentTimeMillis();
    private static SharedPreferences prefs() {return context.getSharedPreferences("pad-diagnostics-v10",0);}
    private static OutputStream slot(String key,String name,String mime) throws IOException {
        String saved=prefs().getString(key,null);
        if(saved!=null)try {
            OutputStream s=context.getContentResolver().openOutputStream(Uri.parse(saved),"rwt");
            if(s!=null)return s;
        } catch(Exception ignored) {}
        ContentValues v=new ContentValues();v.put(MediaStore.MediaColumns.DISPLAY_NAME,name);
        v.put(MediaStore.MediaColumns.MIME_TYPE,mime);
        v.put(MediaStore.MediaColumns.RELATIVE_PATH,"Download/Exult-Korean/diagnostics");
        Uri uri=context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);
        if(uri==null)throw new IOException("Cannot create diagnostics");
        prefs().edit().putString(key,uri.toString()).apply();
        OutputStream s=context.getContentResolver().openOutputStream(uri,"rwt");
        if(s==null)throw new IOException("Cannot open diagnostics");return s;
    }
    private static void rotate() throws IOException {
        if(output!=null)output.close();output=null;
        int sequence=prefs().getInt("sequence",0),index=Math.floorMod(sequence,3);
        prefs().edit().putInt("sequence",sequence+1).apply();
        output=slot("trace"+index,"pad-v10-"+index+".txt","text/plain");bytes=0;
        byte[] header=("Exult Korean v10 run="+run+" segment="+sequence+" Android="+Build.VERSION.RELEASE+"\n"+
            "INPUT=physical transition ACTION=emitted control ENGINE=consumer dispatch/result; no visual-success claim\n").getBytes(StandardCharsets.UTF_8);
        output.write(header);bytes=header.length;
    }
    static synchronized void record(String message) {
        if(output==null)return;
        try {
            if(message.length()>600)message=message.substring(0,600)+"...";
            byte[] line=(System.currentTimeMillis()+" "+message.replace('\n',' ').replace('\r',' ')+"\n").getBytes(StandardCharsets.UTF_8);
            if(bytes+line.length>LIMIT)rotate();
            output.write(line);output.flush();bytes+=line.length;
        } catch(Exception e) {try {if(output!=null)output.close();}catch(Exception ignored){}output=null;}
    }
    static synchronized int open(Context c) {
        // Return no native descriptor: old per-glyph and console tracing is disabled.
        if(attempted)return -1;attempted=true;
        if(Build.VERSION.SDK_INT<29)return -1;
        context=c.getApplicationContext();
        try {
            rotate();record("START pad mapping v10");
            if(Build.VERSION.SDK_INT>=30) {
                android.app.ActivityManager am=c.getSystemService(android.app.ActivityManager.class);
                long previous=prefs().getLong("exitTime",0),latest=previous;int exported=0;
                for(android.app.ApplicationExitInfo info:am.getHistoricalProcessExitReasons(c.getPackageName(),0,6)) {
                    if(info.getTimestamp()<=previous)continue;
                    latest=Math.max(latest,info.getTimestamp());
                    record("PREVIOUS_EXIT time="+info.getTimestamp()+" reason="+info.getReason()+" status="+info.getStatus()+" description="+info.getDescription());
                    if(exported>=2)continue;
                    try(InputStream in=info.getTraceInputStream()) {
                        if(in!=null) {
                            int nslot=Math.floorMod(prefs().getInt("exitSequence",0),2);
                            prefs().edit().putInt("exitSequence",prefs().getInt("exitSequence",0)+1).apply();
                            try(OutputStream save=slot("exit"+nslot,"pad-v10-exit-"+nslot+".bin","application/octet-stream")) {
                                byte[] b=new byte[8192];int n,total=0;
                                while(total<1024*1024 && (n=in.read(b,0,Math.min(b.length,1024*1024-total)))>0){save.write(b,0,n);total+=n;}
                                record("EXIT_TRACE slot="+nslot+" time="+info.getTimestamp()+" bytes="+total);
                            }exported++;
                        }
                    }catch(Exception e){record("EXIT_TRACE_ERROR "+e);}
                }
                prefs().edit().putLong("exitTime",latest).apply();
            }
        }catch(Exception e){android.util.Log.e("ExultDiagnostics","Cannot open trace",e);}
        return -1;
    }
}
