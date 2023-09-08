package com.example.cardview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseUser user;
    DatabaseReference databaseRef;
    FirebaseAuth database ;
    String height,gender, location, level, name,profileImg;
    ProfileFragment profileFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());

        }

        database = FirebaseAuth.getInstance();
        user = database.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference().child(user.getUid());

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the user's data
                    gender = dataSnapshot.child("gender").getValue(String.class);
                    height = dataSnapshot.child("height").getValue(String.class);
                    level = dataSnapshot.child("level").getValue(String.class);
                    location = dataSnapshot.child("location").getValue(String.class);
                    name = dataSnapshot.child("name").getValue(String.class);
                    profileImg = dataSnapshot.child("profileImg").getValue(String.class);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
            }
        });
                bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.video) {
                replaceFragment(new VideoFragment());
            } else if (itemId == R.id.about) {
                replaceFragment(new AboutFragment());
            } else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment(name , gender,  height,  level ,  location,profileImg ));
            }


            return true;
        });

    }
    private  void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}