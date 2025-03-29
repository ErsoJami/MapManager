package com.example.mapmanager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.example.mapmanager.models.MapManager;
import com.example.mapmanager.models.MapManager.MapManagerSearchListener;
import com.example.mapmanager.models.MapManager.MapManagerSuggestListener;
import com.example.mapmanager.models.MapManager;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.SearchType;
import com.yandex.mapkit.search.Session;
import com.yandex.mapkit.search.Session.SearchListener;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.mapkit.search.SuggestOptions;
import com.yandex.mapkit.search.SuggestResponse;
import com.yandex.mapkit.search.SuggestSession;
import com.yandex.mapkit.search.SuggestSession.SuggestListener;
import com.yandex.mapkit.search.SuggestType;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements MapManagerSearchListener, MapManagerSuggestListener  {
    private MapView mapView;
    private ActivityResultLauncher<String[]> getLocationPermission;
    private ListView listView;
    private SearchView searchView;
    private ArrayAdapter<Map<String, String>> adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MapManager mapManager;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLocationPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            Boolean fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted!= null && coarseLocationGranted) {
                mapManager.createUserLayer();
            } else {
                Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapview);
        mapManager = new MapManager(requireContext(), mapView, this, this);
        getLocationPermission();
        listView = view.findViewById(R.id.listView);
        searchView = view.findViewById(R.id.searchView);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                Map<String, String> item = getItem(position);
                if (item != null) {
                    text1.setText(item.getOrDefault("name", ""));
                    text2.setText(item.getOrDefault("info", ""));
                }
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setVisibility(View.GONE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> adress = adapter.getItem(position);
                String responce = adress.getOrDefault("name", "") + " " + adress.getOrDefault("info", "").trim();
                listView.setVisibility(View.GONE);
                searchView.setQuery(responce, true);
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mapManager.search(query);
                searchView.clearFocus();
                listView.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacksAndMessages(null);
                if (newText.isEmpty()) {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    mapManager.suggest("");
                    return true;
                }
                listView.setVisibility(View.VISIBLE);
                handler.postDelayed(() -> {
                    mapManager.suggest(newText);
                }, 500);
                return true;
            }
        });

        return view;
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapManager.createUserLayer();
        } else {
            getLocationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        mapManager.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapManager.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView = null;
        getLocationPermission = null;
        listView = null;
        searchView = null;
        mapManager = null;
    }

    @Override
    public void onSearchSuccess() {
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSearchError(String error) {
        listView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuggestResults(List<Map<String, String>> suggestions) {
        adapter.clear();
        if (!suggestions.isEmpty()) {
            adapter.addAll(suggestions);
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSuggestError(String error) {
        listView.setVisibility(View.GONE);
        adapter.clear();
        adapter.notifyDataSetChanged();
    }
}
