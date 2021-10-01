package com.umu.zaim0001.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements  NewNote.DialogListener {
    private static final String TAG = "Main";
    private ListView listView;
    private ArrayAdapter arrayAdapter;
    private Button addNote, removeBtn, infoBtn;
    private ArrayList<Notes> noteList= new ArrayList<>();
    private FusedLocationProviderClient cl;
    volatile String cityName;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set and initialize listView and array adapter
        listView=(ListView)findViewById(R.id.listView);
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1);
        listView.setAdapter(arrayAdapter);

        /*
        * we use sharedPreferences to keep our notes persistent even when the user closes the application
        * */
        SharedPreferences sharedPreferences= getApplicationContext().getSharedPreferences("com.umu.zaim0001.notes", Context.MODE_PRIVATE);
        String mySavedNotes=sharedPreferences.getString("stringNoteSet",null);
        Log.d(TAG,"onCreate: mySavedNotes"+mySavedNotes);
        if (mySavedNotes!=null){
            Gson g = new Gson();
            Type type = new TypeToken<ArrayList<Notes>>(){}.getType();
            noteList = g.fromJson(mySavedNotes, type);
            synchronized(noteList){
                noteList.notify();
            }
            for(Notes n: noteList){
                arrayAdapter.add(n.getName());
            }
        }


        /*
        * request permission for accessing location
        * and accessing location if permission is granted.
        * */
        requestPermission();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_DENIED){
            getCityLoc();
        }


        //here we set our customized toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //adding an info-Button
        infoBtn = (Button)findViewById(R.id.infoBtn);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Notes n = new Notes("Info", "If location status changed, restart app \n"
                        +"Long-press a message to be able to delete it",null);
                ShowNote sNote = new ShowNote();
                Bundle args = new Bundle();
                Gson gson = new Gson();
                String gsonNote = gson.toJson(n);
                args.putString(ShowNote.tempNote,gsonNote);
                ShowNote showNote = new ShowNote();
                showNote.setArguments(args);
                showNote.show(getSupportFragmentManager(),"showNote");
            }
        });

        //initializing the button and list
        addNote=(Button)findViewById(R.id.addNote);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewNote newNote = new NewNote();
                newNote.show(getSupportFragmentManager(),"new note");
            }
        });


        //Make notes in the list clickable to be able to show a notes
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();
                Gson gson = new Gson();
                String jsonNotes = gson.toJson(noteList.get(position));
                args.putString(ShowNote.tempNote,jsonNotes);
                ShowNote showNote = new ShowNote();
                showNote.setArguments(args);
                showNote.show(getSupportFragmentManager(),"showNote");
            }
        });

        //handling the removal of a list item (a note)
        removeBtn=(Button)findViewById(R.id.removeItemBtn);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                removeBtn.setVisibility(View.VISIBLE);
                removeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //titles.remove(position);
                        //notesList.remove(position);
                        arrayAdapter.remove(noteList.get(position).getName());
                        noteList.remove(position);
                        arrayAdapter.notifyDataSetChanged();
                        removeBtn.setVisibility(View.INVISIBLE);
                    }
                });
                return true;
            }
        });
        //the DELETE button only gets viewed if an item on the list is long-clicked
        removeBtn.setVisibility(View.INVISIBLE);
    }

    //used to fetch title and note from the NewNote class
    @Override
    public void saveInputText(String title, String note) {
        Notes newNote = new Notes(title,note,cityName);
        noteList.add(newNote);
        arrayAdapter.add(title);
        arrayAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onPause() {

        //save shared preferences
        Gson gson = new Gson();
        String jsonNotes = gson.toJson(noteList);
        SharedPreferences sharedPreferences= getApplicationContext().getSharedPreferences("com.umu.zaim0001.notes",Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("stringNoteSet", jsonNotes).apply();
        super.onPause();

    }


    /*
    * a method to get the location of the user.
    * location will be updated once each time you enter main-activity/ restart app
    * */
    void getCityLoc(){
        cl = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        getCityName(latitude,longitude);
                        //Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_LONG).show();
                        Toast.makeText(MainActivity.this,"NOT",Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        cl.requestLocationUpdates(locationRequest,locationCallback,null);
        locationRequest.setNumUpdates(1);
    }

    /*
    * a method to transform latitude and longitude to an address
    * */
    void getCityName(double lat, double lng){
        Geocoder geo = new Geocoder(MainActivity.this);
        try {
            List<Address> addresses = geo.getFromLocation(lat, lng, 1);
            Address returnedAddress = addresses.get(0);
            cityName = returnedAddress.getAddressLine(0);
            //ensure cityName gets initialized
            cityName =returnedAddress.getAddressLine(0);

            //Toast.makeText(MainActivity.this,cityName,Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }


}
