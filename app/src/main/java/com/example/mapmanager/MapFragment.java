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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
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
import java.util.List;

public class MapFragment extends Fragment implements UserLocationObjectListener, SearchListener, SuggestListener  {
    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private ActivityResultLauncher<String[]> getLocationPermission;
    private ListView listView;
    private SearchView searchView;
    private SearchManager searchManager;
    private SuggestSession suggestSession;
    private ArrayAdapter<String> adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLocationPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            Boolean fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted!= null && coarseLocationGranted) {
                createUserLayer();
            } else {
                Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        });
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapview);
        getLocationPermission();
        listView = view.findViewById(R.id.listView);
        searchView = view.findViewById(R.id.searchView);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacksAndMessages(null);
                if (newText.isEmpty()) {
                    if (suggestSession != null) {
                        suggestSession.reset();
                    }
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    return true;
                }
                handler.postDelayed(() -> {
                    requestSuggest(newText);
                }, 500);
                return true;
            }
        });
        return view;
    }
    private void requestSuggest(String query) {
        if (suggestSession != null) {
            suggestSession.reset();
        }
        suggestSession = searchManager.createSuggestSession();
        SuggestOptions suggestOptions = new SuggestOptions();
        suggestOptions.setSuggestTypes(SuggestType.GEO.value | SuggestType.BIZ.value);
        if (userLocationLayer != null && userLocationLayer.cameraPosition() != null) {
            Point userLocation = userLocationLayer.cameraPosition().getTarget();
            suggestSession.suggest(query, new BoundingBox(new Point(userLocation.getLatitude() - 0.00899316, userLocation.getLongitude() - 0.01598718),
                    new Point(userLocation.getLatitude() + 0.00899316, userLocation.getLongitude() + 0.01598718)), suggestOptions, this);
        } else {
            Toast.makeText(requireContext(), "Местоположение недоступно", Toast.LENGTH_SHORT).show();
        }
    }
    private void createUserLayer() {
        if (mapView == null) {
            return;
        }
        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);
    }
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            createUserLayer();
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
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        Bitmap bitmapPin = vectorToBitmap(requireContext(), R.drawable.pin);
        Bitmap bitmapArrow = vectorToBitmap(requireContext(), R.drawable.arrow);
        userLocationView.getPin().setIcon(ImageProvider.fromBitmap(bitmapPin));
        IconStyle iconStyle = new IconStyle();
        iconStyle.setRotationType(RotationType.ROTATE);
        userLocationView.getArrow().setIcon(ImageProvider.fromBitmap(bitmapArrow), iconStyle);
        userLocationView.getAccuracyCircle().setFillColor(R.color.white2);
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {

    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {

    }
    private Bitmap vectorToBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) {
            return null;
        }
        drawable = DrawableCompat.wrap(drawable).mutate();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onSearchResponse(@NonNull Response response) {

    }

    @Override
    public void onSearchError(@NonNull Error error) {
        String errorMessage = "Unknown search error";
        if (error instanceof com.yandex.runtime.network.RemoteError) {
            errorMessage = "Remote error";
        } else if (error instanceof com.yandex.runtime.network.NetworkError) {
            errorMessage = "Network error";
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(@NonNull SuggestResponse suggestResponse) {
        List<SuggestItem> suggestItems = suggestResponse.getItems();
        List<String> suggestions = new ArrayList<>();
        for (SuggestItem item : suggestItems) {
            if (item.getTitle() != null) {
                suggestions.add(item.getTitle().getText());
            }
        }

        // Обновляем адаптер ListView
        adapter.clear();
        adapter.addAll(suggestions);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onError(@NonNull Error error) {
        String errorMessage = "Unknown suggest error";
        if (error instanceof com.yandex.runtime.network.RemoteError) {
            errorMessage = "Remote error";
        } else if (error instanceof com.yandex.runtime.network.NetworkError) {
            errorMessage = "Network error";
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }
}
