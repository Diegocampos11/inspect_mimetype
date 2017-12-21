package com.example.alumno.inspect_mimetype;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements RetrieveFeedTask.AsyncResponse, View.OnClickListener, View.OnKeyListener{

    private EditText URL;
    private WebView web;
    private Button btncargar;
    //private TabHost th;
    //private final ArrayList<String> list = new ArrayList<String>();
    //private ArrayAdapter adapter = null;
    private String video_actual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btncargar = ( Button ) findViewById( R.id.btncargar );
        URL = ( EditText ) findViewById( R.id.txturl );
        /*lista*/
        //ListView listview = (ListView) findViewById(R.id.list);//List
        //adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);//Adaptader for list
        //listview.setAdapter( adapter );//Set adaptader, lo coloco aqui por que la lista forma parte del diseño

        /*lista*/

        /*Listeners*/
        URL.setOnKeyListener( this );
        btncargar.setOnClickListener( this );

        //Web--and settings
        web = ( WebView ) findViewById( R.id.web );
        //Detecto mime type pero por extension de archivo, por lo tanto mejor llamo a asynctask :D
        web.getSettings().setSupportMultipleWindows(true);
        web.getSettings().setJavaScriptEnabled(true);
        web.setWebViewClient( new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                URL.setText( url );
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest url) {
                execute( new String [] { "content-type" , url.getUrl().toString() } );
                return null;
            }
        } );
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg)
            {
                try{
                    WebView.HitTestResult result = view.getHitTestResult();
                    String data = result.getExtra();
                    //Context context = view.getContext();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                    view.getContext().startActivity(browserIntent);
                }
                catch( Exception e){
                    Toast.makeText( getBaseContext(), "error " + e, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        //primera ejecucion :D
        primera_ejecucion();
        //Color barra de estado :D
        Window window = this.getWindow();
        window.setStatusBarColor( this.getResources().getColor( R.color.colorAccent ) );
        web.loadUrl("https://m.youtube.com");
        URL.setText("https://m.youtube.com");
    }

    private void execute( String... params ){
        new RetrieveFeedTask(this, params).execute();
    }
    @Override
    public void onClick(View v) {
        if ( v.getId() == btncargar.getId() ){
        	web.loadUrl( URL.getText().toString().trim() );
        	InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(URL.getWindowToken(), 0);
        }
    }

    /*public void  primera_ejecucion(){
        try {
            //if ( ! new File("/data/data/com.example.alumno.inspect_mimetype/files/ffmpeg").exists() ){

                Toast.makeText( getBaseContext(), "Inicializando la aplicación, esto puede tardar unos minutos", Toast.LENGTH_SHORT ).show();
                //Saco ffmpeg
                InputStream inputStream = getAssets().open("ffmpeg");
                FileOutputStream outputStream = openFileOutput( "ffmpeg", Context.MODE_PRIVATE );
                outputStream.write( inputStream.read() );
                outputStream.close();
                //Saco lib.zip
                inputStream = getAssets().open("lib.zip");
                outputStream = openFileOutput( "lib.zip", Context.MODE_PRIVATE );
                outputStream.write( inputStream.read() );
                outputStream.close();
                //Descomprimir lib.zip
                Process p = Runtime.getRuntime().exec( "chmod -R 777 /data/data/com.example.alumno.inspect_mimetype/files/" );
                p.waitFor();
                execute( "/data/data/com.example.alumno.inspect_mimetype/files/" );
                Toast.makeText( getBaseContext(), "Listo Raquel! puedes comenzar a utilizarla!!", Toast.LENGTH_LONG ).show();
            //}
        } catch (IOException e) {
            Toast.makeText( getBaseContext(), "Error primera ejecucion " + e.toString(), Toast.LENGTH_SHORT ).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    private void primera_ejecucion(){
        try {
            //if ( ! new File("/data/data/com.example.alumno.inspect_mimetype/files/ffmpeg").exists() ){
            Toast.makeText( getBaseContext(), "Inicializando la aplicación, esto puede tardar unos minutos", Toast.LENGTH_SHORT ).show();
            //Saco files_dir
            files_dir( "lib" );
            Process p = Runtime.getRuntime().exec( "ls -l /data/data/com.example.alumno.inspect_mimetype/files/lib" );
            BufferedReader reader = new BufferedReader( new InputStreamReader( p.getInputStream() ));
            String line = "";
            StringBuffer output = new StringBuffer();
            while( ( line = reader.readLine() ) != null ) {
                output.append( line + "\n" );
                p.waitFor();
            }
            System.out.println( output.toString() );
            Toast.makeText( getBaseContext(), output.toString(), Toast.LENGTH_LONG ).show();
            //Process p = Runtime.getRuntime().exec( "chmod -R 777 /data/data/com.example.alumno.inspect_mimetype/files/" );
            //p.waitFor();
            //execute( "/data/data/com.example.alumno.inspect_mimetype/files/" );
            //Toast.makeText( getBaseContext(), "Listo Raquel! puedes comenzar a utilizarla!!", Toast.LENGTH_LONG ).show();
            //}
        } catch (Exception e) {
            Toast.makeText( getBaseContext(), "Error primera ejecucion " + e.toString(), Toast.LENGTH_SHORT ).show();
        } /*catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText( getBaseContext(), "Error primera ejecucion " + e.toString(), Toast.LENGTH_SHORT ).show();
        }*/
    }

    private void files_dir( String path ){
        try {
            String[] files_dir = getAssets().list( path );
            if ( getAssets().list( path ).length > 0 ){
                Process p = Runtime.getRuntime().exec( "mkdir /data/data/com.example.alumno.inspect_mimetype/files/" + path  );
            }
            for( int i = 0; i < files_dir.length; i++ ) {
                //Toast.makeText( getBaseContext(), files_dir[i]+"/", Toast.LENGTH_SHORT ).show();
                if ( getAssets().list( path + File.separator + files_dir[i] ).length > 0 ) {
                    //Toast.makeText( getBaseContext(), getAssets().list( files_dir[i]+"/" )[0], Toast.LENGTH_SHORT ).show();
                    files_dir( path + File.separator + files_dir[i] );
                }
                else {
                    InputStream inputStream = getAssets().open( path + File.separator + files_dir[i] );
                    FileOutputStream outputStream = openFileOutput( files_dir[i], Context.MODE_PRIVATE );
                    int size = inputStream.available();
                    byte[] buffer = new byte[size];
                    inputStream.read(buffer);
                    //while ( ctr != -1 ) {
                        outputStream.write( buffer );
                        //ctr = inputStream.read( buffer );
                    //}
                    outputStream.close();
                    System.out.println( path+ "/" + files_dir[i] );
                    Process p = Runtime.getRuntime().exec( "chmod -R 777 /data/data/com.example.alumno.inspect_mimetype/files/" );
                    p = Runtime.getRuntime().exec( "mv /data/data/com.example.alumno.inspect_mimetype/files/" + files_dir[i] + " /data/data/com.example.alumno.inspect_mimetype/files/" + path + "/" );
                }
            }
        }
        catch ( Exception e ){
            System.out.println( e.toString() );
            Toast.makeText( getBaseContext(), "Error primera ejecucion cd" + e.toString(), Toast.LENGTH_SHORT ).show();
        }
    }

    @Override
    public void processFinish(final String... params) {
        /*list.add( type );
        adapter.notifyDataSetChanged();*/
        if ( params[0].equals( "content-type" ) ) {
            if ( params[1] != null && ( params[1].indexOf("audio/webm") != -1 ) && ! video_actual.equals( web.getUrl() ) ) {
                this.video_actual = web.getUrl();
                android.app.AlertDialog.Builder alertdialog = new android.app.AlertDialog.Builder( MainActivity.this );
                alertdialog.setTitle("¿Descargar Raquel ;)?");
                //final CharSequence[] items = {"Añadir", "Añadir y eliminar"};
                alertdialog.setItems( new CharSequence[]{"Sí :P", "No"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            String new_url = params[2];
                            int indice_qparam = new_url.indexOf( "&range=" );//Encuentro indice o posicion en la que se encuentra
                            String url = new_url.substring( 0, indice_qparam ) + new_url.substring( new_url.indexOf( "&", indice_qparam + 1 )  , new_url.length() );
                            Toast.makeText( getBaseContext(), "Descargada comenzada", Toast.LENGTH_SHORT ).show();
                            execute( new String [] { "webm-mp3", url, web.getTitle() } );
                        }
                    }
                });
                alertdialog.show();
            }
        }
        else if ( params[0].equals( "webm-mp3" ) ){
            /*ClipData clip = ClipData.newPlainText( "text", params[1].toString() );
            ClipboardManager clipboard = ( ClipboardManager ) this.getSystemService( CLIPBOARD_SERVICE );
            clipboard.setPrimaryClip( clip );*/
            //Toast.makeText( getBaseContext(), "Descargada completada " + params[1], Toast.LENGTH_SHORT ).show();
            this.showNotification( params[1] );
        }
        else{
            System.out.println( params[0] );
        }
    }

    public void showNotification( String nombre_song ) {
        Notification.Builder mBuilder;
        NotificationManager mNotifyMgr =(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        int icono = R.drawable.icon_;
        Intent i=new Intent(MainActivity.this, Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, i, 0);

        mBuilder =new Notification.Builder( getApplicationContext() )
                .setContentIntent(pendingIntent)
                .setSmallIcon(icono)
                .setContentTitle("Descarga completa")
                .setContentText( nombre_song )
                .setVibrate(new long[] {100, 250, 100, 500})
                .setAutoCancel(true);
        mNotifyMgr.notify(1, mBuilder.build());
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        //if ( keyEvent.getAction() == KeyEvent.ACTION_DOWN ) {
            if ( i == KeyEvent.KEYCODE_ENTER ) {
                web.loadUrl( URL.getText().toString().trim() );
                InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(URL.getWindowToken(), 0);
            }
            return false;
        /*}
        return false;*/
    }
}