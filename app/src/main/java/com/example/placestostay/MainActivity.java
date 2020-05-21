package com.example.placestostay;

import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import android.app.AlertDialog;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.ArrayList;

import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.content.Context;
import android.widget.Toast;

import java.io.*;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements LocationListener  {
    MapView mv;
    ItemizedIconOverlay<OverlayItem> places;
    ItemizedIconOverlay.OnItemGestureListener<OverlayItem> markerGestureListener;
    double currentlon;
    double currentlat;
    boolean autodownload,autoupload;

    ArrayList<Accommodation> placesObj=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setContentView(R.layout.activity_main);
        mv = findViewById(R.id.map1);
        mv.setMultiTouchControls(true);

        try {
            LocationManager mgr=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0, this);        }
        catch(SecurityException e){
            System.out.println("Security exception " +e);
        }


        markerGestureListener = new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>()
        {
            public boolean onItemLongPress(int i, OverlayItem item)
            {
                popupMessage("This place is called "+item.getTitle()+"\nThe place is:\n"+item.getSnippet());
                return true;
            }

            public boolean onItemSingleTapUp(int i, OverlayItem item)
            {
                Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        };

        places = new ItemizedIconOverlay<OverlayItem>(this, new ArrayList<OverlayItem>(), markerGestureListener);


    }

    public void onStart()
    {
        super.onStart();


    }

    public void onResume(){
            super.onResume();
       // mv.getController().setCenter(new GeoPoint(currentlat,currentlon));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            autodownload = prefs.getBoolean("autodownload", false);
            if(autodownload) {
                writeToFile();
                System.out.println("autodownload true");

            }
            else System.out.println("autodownload false");


        autoupload=prefs.getBoolean("autosaveweb",false);
            if(autoupload){
                System.out.println("autoupload true");
                MyTask2 t2 = new MyTask2();
                t2.execute();
            }
            if(!autoupload)System.out.println("autoupload false");
        mv.getController().setZoom(16.0);
        Location newLoc=new Location("service Provider");
        currentlat=newLoc.getLatitude();
        currentlon=newLoc.getLongitude();
        System.out.println("lon:"+currentlon+" lat:"+currentlat);
        mv.getController().setCenter(new GeoPoint(currentlat,currentlon));

        objectsToOverlayItems();
    }
    public void onLocationChanged(Location newLoc){
        //System.out.println("HEREEEEEEEEEEEEEEEEEEEEEEE (ONLOCCHANGED)");
        currentlat=newLoc.getLatitude();
        currentlon=newLoc.getLongitude();
        //System.out.println("Location changed: lat "+ currentlat+" lon "+currentlon);
        mv.getController().setCenter(new GeoPoint(currentlat,currentlon));

    }
    public void onStatusChanged(String provider, int status, Bundle extras){
        Toast.makeText(this, "Status changed: " + status,
                Toast.LENGTH_LONG).show();
    }
    public void onProviderEnabled(String provider)
    {
        Toast.makeText(this, "Provider " + provider +
                " enabled", Toast.LENGTH_LONG).show();
    }
    public void onProviderDisabled(String provider)
    {
        Toast.makeText(this, "Provider " + provider +
                " disabled", Toast.LENGTH_LONG).show();
    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    protected void onActivityResult(int requestCode,int resultCode,Intent intent)
    {

        if(requestCode==0) {
            try {
                Bundle extras = intent.getExtras();

                String name = extras.getString("com.placestostay.addplacename");

                String type = extras.getString("com.placestostay.addplacetype");
                double price = extras.getDouble("com.placestostay.addplaceprice");
                popupMessage("Place to stay added!\nName: " + name + " type: " + type + " price " + price);
                String snippet = "Type: " + type + "\nPrice: " + price;
               // OverlayItem temp = new OverlayItem(name, snippet, new GeoPoint(currentlat, currentlon));
                Accommodation tempobj = new Accommodation(name, type, price, currentlon, currentlat);
                placesObj.add(tempobj);
                objectsToOverlayItems();
            }
            catch (java.lang.NullPointerException e){
                Toast.makeText(MainActivity.this, "No data to be saved", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onDestroy()
    {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean ("autodownload", autodownload);
        editor.commit();
    }

    public void onSaveInstanceState (Bundle savedInstanceState)
    {
        super.onSaveInstanceState( savedInstanceState);
        savedInstanceState.putBoolean ("autodownload", autodownload);
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.addplace)
        {

            Intent intent = new Intent(this,addPlace.class);
            startActivityForResult(intent,0);
            return true;
        }
        else if(item.getItemId()==R.id.preferences){
            Intent intent = new Intent(this,MyPrefsActivity.class);
            //startActivityForResult(intent, 1);
            startActivity(intent);
            return true;
        }
        else if(item.getItemId()==R.id.loadfromfile){
            readFile();
        }
        else if(item.getItemId()==R.id.loadfromweb){
            MyTask t = new MyTask();
            t.execute();
        }
        else if(item.getItemId()==R.id.savetofile){
            writeToFile();

        }
        else if(item.getItemId()==R.id.savetoweb){
            MyTask2 t2 = new MyTask2();
            t2.execute();

        }

        return false;
    }



    private void writeToFile() {
            if(!placesObj.isEmpty()) {
                File mFolder = new File(getExternalStorageDirectory() + "/sample");
                File datafile = new File(mFolder.getAbsolutePath() + "/places.csv");
                if (!mFolder.exists()) {
                    mFolder.mkdir();
                }
                if (!datafile.exists()) {
                    try {

                        datafile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
           /* FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(datafile);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
*/

                try {
                    PrintWriter pw =
                            new PrintWriter(new FileWriter(mFolder.getAbsolutePath() + "/places.csv"));
                    for (int i = 0; i < placesObj.size(); i++) {
                        pw.println(placesObj.get(i).getName() + "," + placesObj.get(i).getType() + "," + placesObj.get(i).getPrice() + "," + placesObj.get(i).getLon() + "," + placesObj.get(i).getLat());
                    }

                    pw.close(); // close the file to ensure data is flushed to file
                } catch (IOException e) {
                    System.out.println("I/O Error: " + e);
                }

                //popupMessage(mFolder.getAbsolutePath() + "/places.csv");
                Toast.makeText(MainActivity.this, placesObj.size()+ " places written to file.", Toast.LENGTH_SHORT).show();

            }
            else Toast.makeText(MainActivity.this, "No objects to be saved to the file", Toast.LENGTH_SHORT).show();


    }
    void readFile() {
        File mFolder = new File(getExternalStorageDirectory() + "/sample");
        File datafile = new File(mFolder.getAbsolutePath() + "/places.csv");
        //popupMessage(mFolder.getAbsolutePath() + "/places.csv");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(datafile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] components = line.split(",");
                if (components.length == 5) {
                    Accommodation currentacc = new Accommodation(components[0], components[1], parseDouble(components[2]), parseDouble(components[3]), parseDouble(components[4]));
                    placesObj.add(currentacc);

                    System.out.println("read successful");
                    objectsToOverlayItems();
                }
            }
            Toast.makeText(MainActivity.this, "Sync successful. "+placesObj.size()+ " places added from file", Toast.LENGTH_SHORT).show();

        }
        catch (Exception e){
            System.out.println(e.toString());
            System.out.println("read unsuccessful");

        }
        return;
    }
    private void popupMessage(String message) {
        new AlertDialog.Builder(this).setPositiveButton("OK", null).setMessage(message).show();
    }
public void objectsToOverlayItems(){
        places.removeAllItems();
        for(int i=0;i<placesObj.size();i++){
            OverlayItem temp = new OverlayItem(placesObj.get(i).getName(), "Type: "+placesObj.get(i).getType()+"\nPrice: "+placesObj.get(i).getPrice(), new GeoPoint(placesObj.get(i).getLat(), placesObj.get(i).getLon()));
            places.addItem(temp);
        }

    mv.getOverlays().add(places);
}

void alltoString(){
        System.out.println("OBJECTS TO STING!!!");
        for(int i=0;i<placesObj.size();i++){

            System.out.println(" \n"+placesObj.get(i).toString());
            System.out.println(places.getItem(i).toString());

        }

    System.out.println("OVERLAYS TO STING!!!");

    for(int i=0;i<places.size();i++){

        System.out.println(places.getItem(i).getTitle()+places.getItem(i).getSnippet());

    }
}


    class MyTask extends AsyncTask<Void,Void,String>
    {
        public String doInBackground(Void... unused)
        {
            HttpURLConnection conn = null;
            try
            {
                URL url = new URL("https://www.website.com/get.php?format=CSV");//original web service hidden, hosted on private server

                conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                if(conn.getResponseCode() == 200)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String result = "", line;
                    while((line = br.readLine()) !=null)
                    {
                        result += line+",";

                    }

                    return result;

                }
                else
                {
                    return "HTTP ERROR: " + conn.getResponseCode();
                }
            }
            catch(IOException e)
            {
                return e.toString();
            }
            finally
            {
                if(conn!=null)
                {
                    conn.disconnect();
                }
            }
        }

        public void onPostExecute(String result)
        {
            placesObj.clear();

            int counter=0;
            String[] components = result.split(",");
            for(int i=0;i<components.length;i++) {
                if (i % 5 ==0&&i!=0) {
                    Accommodation currentacc = new Accommodation(components[i-5], components[i-4], parseDouble(components[i-3]), parseDouble(components[i-2]), parseDouble(components[i-1]));
                    placesObj.add(currentacc);
                    counter++;
                    //System.out.println(components[i-5]+ components[i-4]+components[i-3]+ components[i-2]+ components[i-1]);
                }
            }
            Toast.makeText(MainActivity.this, "Sync successful."+counter+" places added from the web", Toast.LENGTH_SHORT).show();
//            popupMessage(placesObj.get(components.length/5-1).toString());
            objectsToOverlayItems();
            alltoString();

        }
    }
    class MyTask2 extends AsyncTask<Void,Void,String>
    {
        public String doInBackground(Void... unused) {
            HttpURLConnection conn = null;

                try {
                    OutputStream out = null;
                    URL url = new URL("https://www.website.com/add.php");//original web service hidden, hosted on private server
                    conn = (HttpURLConnection) url.openConnection();
                    if (!placesObj.isEmpty()){
                        for (int i = 0; i < placesObj.size(); i++) {
                            String postData = "username=user007&name=" + placesObj.get(i).getName() + "&type=" + placesObj.get(i).getType() + "&price=" + placesObj.get(i).getPrice() + "&lat=" + placesObj.get(i).getLat() + "&lon=" + placesObj.get(i).getLon() + "&year=20";
                            // For POST
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setDoOutput(true);
                            conn.setFixedLengthStreamingMode(postData.length());


                            out = conn.getOutputStream();
                            out.write(postData.getBytes());
                        }
                        if (conn.getResponseCode() == 200) {
                            InputStream in = conn.getInputStream();
                            BufferedReader br = new BufferedReader(new InputStreamReader(in));
                            String all = "", line;
                            while ((line = br.readLine()) != null)
                                all += line;
//                            popupMessage(all);
                            return all;
                        } else {
                            System.out.println("HTTP ERROR: " + conn.getResponseCode());
                            return "No places uploaded";
                        }
                    }
                } catch (IOException e) {
                    return e.toString();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
                return "No places uploaded";
            }

            public void onPostExecute (String result)
            {
                if(!result.equals("No places uploaded"))  Toast.makeText(MainActivity.this, "Places uploaded successfully", Toast.LENGTH_SHORT).show();

                alltoString();

                System.out.println("Server sent back: " + result);

        }
    }


}

