package com.example.placestostay;


import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.app.AlertDialog;

import android.app.Activity;
import android.os.Bundle;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.content.Context;
import android.widget.Toast;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;



public class addPlace  extends AppCompatActivity implements   OnClickListener {

    String name, type;
    double price;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent=getIntent();
        ItemizedIconOverlay<OverlayItem> places;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.addplace);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        Button btn = (Button) findViewById(R.id.btn1);
        btn.setOnClickListener(this);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.addplace)
        {

            Intent intent = new Intent(this,addPlace.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
    public void onClick(View view) {
        EditText et = (EditText) findViewById(R.id.et1);
        EditText et2 = (EditText) findViewById(R.id.et2);
        EditText et3 = (EditText) findViewById(R.id.et3);

        try {
            name = et.getText().toString();
            type = et2.getText().toString();
            price = Double.parseDouble(et3.getText().toString());


        }
        catch (NumberFormatException nfe){

            popupMessage("Please enter data in all text fields");
        }
        popupMessage("name "+name+" type "+type+" price "+price);
        Intent intent = new Intent();
        Bundle bundle=new Bundle();
        bundle.putString("com.placestostay.addplacename",name);

        bundle.putString("com.placestostay.addplacetype",type);
        bundle.putDouble("com.placestostay.addplaceprice",price);
        intent.putExtras(bundle);
        setResult(RESULT_OK,intent);
        finish();
    }
    private void popupMessage(String message) {
        new AlertDialog.Builder(this).setPositiveButton("OK", null).setMessage(message).show();

    }
}
