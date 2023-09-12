package com.example.cardview;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.cardview.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {

    private Geocoder geocoder;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Marker currentMarker;
    private TextView text_selected_location;
    private SearchView searchView;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private static final String TAG = "MapsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        searchView = findViewById(R.id.searchView);

        if (geocoder == null) {
            geocoder = new Geocoder(this, Locale.getDefault());
        }
        Button confirmButton = findViewById(R.id.button_confirm);
        text_selected_location = findViewById(R.id.text_selected_location);
        confirmButton.setOnClickListener(v -> confirmSelection());

        ImageView closeButton = findViewById(R.id.button_close);
        closeButton.setOnClickListener(v -> finish());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Intent intent = getIntent();
        String selectedLocation = intent.getStringExtra("selected_location");
        Log.d("selected_location","fuck my ass"+selectedLocation);
        if (selectedLocation != null) {
            performSearch(selectedLocation);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMapListeners();
        setupSearchView();
        setupMapSettings();

    }
    private interface GeocodeCallback {
        void onGeocodeResult(@Nullable LatLng latLng, @Nullable String addressText);
    }


    @Override
    public boolean onMyLocationButtonClick() {
        updateLocation();
        return false;
    }
    private void setupMapListeners() {
        mMap.setOnMapClickListener(this::onMapClick);
        mMap.setOnMapLongClickListener(this::onMapLongClick);
    }
    private void onMapLongClick(LatLng latLng) {
        if (currentMarker != null) {
            currentMarker.remove();
            currentMarker = null;
        }
    }
    private void onMapClick(LatLng latLng) {
        geocodeLocation(latLng, this::updateMarker);
    }



    private void setupMapSettings() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }


    private void updateLocation() {
        boolean hasLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!hasLocationPermission) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        updateMarker(latLng, "Your location");


                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1, new Geocoder.GeocodeListener() {
                            @Override
                            public void onGeocode(@NonNull List<Address> addresses) {
                                if (!addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    String addressText = address.getAddressLine(0);
                                    runOnUiThread(() -> text_selected_location.setText(addressText));
                                }
                            }

                            @Override
                            public void onError(@Nullable String errorMessage) {
                                runOnUiThread(() -> Toast.makeText(MapsActivity.this, "Geocode error", Toast.LENGTH_SHORT).show());
                            }

                        });
                        geocodeLocation(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), this::updateMarker);
                    }
                });
    }


    private void performSearch(String query) {
        geocodeLocationName(query, 1, (latLng, addressText) -> {
            if (latLng != null && addressText != null) {
                updateMarker(latLng, addressText);
            } else {
                runOnUiThread(() -> Toast.makeText(MapsActivity.this, "No location found for the query", Toast.LENGTH_SHORT).show());
            }
        });
    }



    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }


    private void updateMarker(LatLng latLng, String addressText) {
        if (currentMarker != null) {
            currentMarker.remove();
        }
        text_selected_location.setText(addressText);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Selected Location")
                .snippet(addressText)
                .anchor(0.5f, 0.5f)
                .zIndex(1.0f)
                .draggable(true)
                .flat(true);
        currentMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
    }


    private void geocodeLocationName(String locationName, int maxResults, GeocodeCallback callback) {
        geocoder.getFromLocationName(locationName, maxResults, new Geocoder.GeocodeListener() {
            @Override
            public void onGeocode(@NonNull List<Address> addresses) {
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = address.getAddressLine(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    runOnUiThread(() -> callback.onGeocodeResult(latLng, addressText));
                } else {
                    runOnUiThread(() -> callback.onGeocodeResult(null, null));
                }
            }

            @Override
            public void onError(@Nullable String errorMessage) {
                runOnUiThread(() -> callback.onGeocodeResult(null, null));
                Log.e(TAG, "Geocode error: " + errorMessage);
            }
        });
    }

    private void geocodeLocation(LatLng latLng, GeocodeCallback callback) {
        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1, new Geocoder.GeocodeListener() {
            @Override
            public void onGeocode(@NonNull List<Address> addresses) {
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = address.getAddressLine(0);
                    runOnUiThread(() -> callback.onGeocodeResult(latLng, addressText));
                } else {
                    runOnUiThread(() -> callback.onGeocodeResult(null, null));
                }
            }

            @Override
            public void onError(@Nullable String errorMessage) {
                runOnUiThread(() -> callback.onGeocodeResult(null, null));
                Log.e(TAG, "Geocode error: " + errorMessage);
            }
        });
    }


    private void confirmSelection() {
        String selectedLocationText = text_selected_location.getText().toString();

        if (!selectedLocationText.isEmpty()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_location", selectedLocationText);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "No location selected", Toast.LENGTH_SHORT).show();
        }
    }

}
