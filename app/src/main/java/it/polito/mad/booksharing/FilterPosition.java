package it.polito.mad.booksharing;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.util.NumberUtils;

import java.util.List;

/**
 * Created by sergiogentile on 14/05/18.
 */

public class FilterPosition extends AppCompatActivity {

    private boolean enableCurrentLocation;
    private TextInputEditText tvPosition, tvRadius;
    private Button buttonOk;
    private LinearLayout llCurrentPosition;
    private double latPhone, longPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_position);

        enableCurrentLocation = false;

        tvPosition = (TextInputEditText) findViewById(R.id.changeCurrentPosition);
        buttonOk = (Button) findViewById(R.id.confirmFilterPosition);
        tvRadius = (TextInputEditText) findViewById(R.id.changeRadius);
        llCurrentPosition = (LinearLayout) findViewById(R.id.llCurrentPosition);

        String street = getIntent().getStringExtra("currentStreet");
        String city = getIntent().getStringExtra("currentCity");
        tvRadius.setText(Integer.toString(getIntent().getIntExtra("radius", 20)));

        if(street!=null && !street.isEmpty() && city!=null && !city.isEmpty()){
            tvPosition.setText(street + ", " + city);
        }
        else{
            tvPosition.setText("");
        }

        latPhone = getIntent().getDoubleExtra("currentLat", -1);
        longPhone = getIntent().getDoubleExtra("currentLong", -1);


        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(NumberUtils.isNumeric(tvRadius.getText().toString())){
                    Geocoder geocoder = new Geocoder(FilterPosition.this);
                    List<Address> addresses;
                    String location = tvPosition.getText().toString();
                    try {
                        addresses = geocoder.getFromLocationName(location, 1);
                        if (addresses.size() > 0) {
                            double latPhone = addresses.get(0).getLatitude();
                            double longPhone = addresses.get(0).getLongitude();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("radius", Integer.valueOf(tvRadius.getText().toString()));
                            resultIntent.putExtra("latPhone", latPhone);
                            resultIntent.putExtra("longPhone", longPhone);
                            resultIntent.putExtra("enableCurrentLocation", enableCurrentLocation);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(FilterPosition.this, getString(R.string.own_pos_not_found), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(FilterPosition.this, getString(R.string.own_pos_not_found), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(FilterPosition.this, getString(R.string.valid_radius), Toast.LENGTH_SHORT).show();
                }
            }
        });

        llCurrentPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(latPhone!=-1 && longPhone!=-1){
                    System.out.println("Lat and long different from one");
                    Geocoder geocoder = new Geocoder(FilterPosition.this);
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(latPhone, longPhone, 1);
                        if (addresses.size() > 0) {
                            String currentLocationCity = addresses.get(0).getLocality();
                            String[] currentAddresses = addresses.get(0).getAddressLine(0).split(",");
                            String currentLocationStreet = currentAddresses[0] != null ? currentAddresses[0] : "";
                            currentLocationStreet += currentAddresses[1] != null ? " " + currentAddresses[1] : "";

                            tvPosition.setText(currentLocationStreet + ", " + currentLocationCity);
                            enableCurrentLocation = true;


                        } else {
                            Toast.makeText(FilterPosition.this, getString(R.string.own_pos_not_found), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(FilterPosition.this, getString(R.string.own_pos_not_found), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(FilterPosition.this, getString(R.string.own_pos_not_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}
