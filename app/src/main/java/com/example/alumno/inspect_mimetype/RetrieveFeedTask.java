package com.example.alumno.inspect_mimetype;

import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.NameValuePair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Alumno on 10/11/2017.
 */

public class RetrieveFeedTask extends AsyncTask<String, Void, String>{

    private String [] params;
    private String url = "";
    private List<NameValuePair> params_web;

    public AsyncResponse delegate = null;

    // Define interfaz.
    public interface AsyncResponse {
        void processFinish(String... params);
    }

    public RetrieveFeedTask(AsyncResponse delegate, String... params){
        this.delegate = delegate;
        this.params = params;
    }

    @Override
    protected String doInBackground(String... paramss) {
        if ( params[0].equals("content-type") ){
            this.url = params[1];
            try {
                java.net.URL url = new java.net.URL(this.url);
                URLConnection u = url.openConnection();
                //long length = Long.parseLong(u.getHeaderField("Content-Length"));
                params[1] = u.getHeaderField("Content-Type");
            } catch (Exception e) {
                params[1] = "Error" + e.toString();
            }
        }
        else if ( params[0].equals("webm-mp3") ){
            this.url = params[1];
            try {
                String[] prueba = new String[]{"ffmpeg", "-i", url, "-acodec", "libmp3lame", "-aq", "4", "/storage/sdcard0/" + params[2].substring(0, params[2].indexOf(" - YouTube") ) + ".mp3"};
                Process p = Runtime.getRuntime().exec( prueba, new String[]{"LD_LIBRARY_PATH=/data/data/com.example.alumno.inspect_mimetype/files/lib"}, null);
                p.waitFor();
            } catch (IOException e) {
                params[1] = "Error" + e.toString();
            } catch (InterruptedException e) {
                params[1] = "Error" + e.toString();
            }
        }
        else{
            try{
                //Descomprimir
                System.out.println("unzip");
                unzip( params[0] + "lib.zip", params[0] );
                //POr si acaso
                Process p = Runtime.getRuntime().exec( "chmod -R 777 /data/data/com.example.alumno.inspect_mimetype/files/" );
            }
            catch (IOException e) {
                params[0] = "Error primera ejecucion descomprimir " + e.toString();
            }
        }
        return null;
    }

    public void unzip(String zipFile, String location){
        System.out.println("unzip");
        try {
            File f = new File(location);
            if(!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if(!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    }
                    else {
                        FileOutputStream fout = new FileOutputStream(path, false);
                        try {
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);
                            }
                            zin.closeEntry();
                        }
                        finally {
                            fout.close();
                        }
                    }
                }
            }
            finally {
                zin.close();
            }
        }
        catch (Exception e) {
            System.out.println("error" + e.toString());
            params[0] = "Error unzip " + e.toString();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if ( params[0].equals("content-type") ) delegate.processFinish( new String [] { "content-type", params[1], url } );
        else if ( params[0].equals("webm-mp3") ) delegate.processFinish( new String [] { "webm-mp3", params[2].substring(0, params[2].indexOf(" - YouTube") )/*Result ;)*/ } );
        else delegate.processFinish( new String [] { params[0] } );
    }
}