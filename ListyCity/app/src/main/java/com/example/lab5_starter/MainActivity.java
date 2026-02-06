package com.example.lab5_starter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private CityArrayAdapter cityArrayAdapter;

    final String Tag = "System";
    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("Cities");
//        seedCities();

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore Error", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");
                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        // set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList, (city, position) -> {
            deleteCityFromSwipe(city, position);
        });
        cityListView.setAdapter(cityArrayAdapter);

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });
    }

    private void deleteCityFromSwipe(City city, int position) {
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();

        citiesRef.document(city.getName())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(Tag, "City deleted via swipe!"))
                .addOnFailureListener(e -> {
                    Log.e(Tag, "Failed to delete city", e);
                    cityArrayList.add(city);
                    cityArrayAdapter.notifyDataSetChanged();
                });
    }

    private void seedCities() {
        String[] names = {
                "Edmonton", "Calgary", "Toronto", "Vancouver", "Montreal", "Ottawa", "Winnipeg", "Quebec City", "Hamilton", "Halifax", "Beijing"
        };
        String[] provinces = {
                "AB", "AB", "ON", "BC", "QC", "ON", "MB", "QC", "ON", "NS", "China"
        };

        for (int i = 0; i < names.length; i++) {
            City newCity = new City(names[i], provinces[i]);

            citiesRef.document(newCity.getName())
                    .set(newCity)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(Tag, "Seeded: " + newCity.getName());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(Tag, "Failed to seed: " + newCity.getName(), e);
                    });
        }
    }

    @Override
    public void updateCity(City city, String title, String year) {
        String oldName = city.getName();

        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        if (oldName.equals(title)) {
            citiesRef.document(oldName)
                    .update("province", year)
                    .addOnSuccessListener(aVoid -> Log.d(Tag, "Province updated successfully!"))
                    .addOnFailureListener(e -> Log.e(Tag, "Error updating province", e));
        } else {
            citiesRef.document(oldName).delete();
            citiesRef.document(title).set(city)
                    .addOnSuccessListener(aVoid -> Log.d(Tag, "City renamed successfully!"))
                    .addOnFailureListener(e -> Log.e(Tag, "Error renaming city", e));
        }
    }


    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        String cityName = city.getName();
        citiesRef
                .document(cityName)
                .set(city)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(Tag, "Data added successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(Tag, "Data add failure" + e.toString());
                    }
                });
    }
}