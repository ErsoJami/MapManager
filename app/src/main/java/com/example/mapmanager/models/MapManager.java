package com.example.mapmanager.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.mapmanager.R;
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
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapManager implements UserLocationObjectListener, SearchListener, SuggestListener {
    private final MapView mapView;
    private final Context context;
    private UserLocationLayer userLocationLayer;
    private final MapObjectCollection mapObjectCollection;
    private final Bitmap userPinBitmap;
    private final Bitmap userArrowBitmap;
    private final Bitmap placemarkBitmap;
    private final SearchManager searchManager;
    private final SearchOptions searchOptions;
    private final SuggestOptions suggestOptions;
    private SuggestSession suggestSession;
    private Session searchSession;
    private boolean userGetLocation = false;


    public interface MapManagerSearchListener {
        void onSearchSuccess();
        void onSearchError(String error);
    }

    public interface MapManagerSuggestListener {
        void onSuggestResults(List<Map<String, String>> suggestions);
        void onSuggestError(String error);
    }
    private final MapManagerSearchListener searchListener;
    private final MapManagerSuggestListener suggestListener;
    public MapManager(Context context, MapView mapView, MapManagerSearchListener searchListener, MapManagerSuggestListener suggestListener) {
        this.context = context;
        this.mapView = mapView;
        this.searchListener = searchListener;
        this.suggestListener = suggestListener;
        this.mapObjectCollection = mapView.getMap().getMapObjects().addCollection();
        this.userPinBitmap = vectorToBitmap(context, R.drawable.pin);
        this.userArrowBitmap = vectorToBitmap(context, R.drawable.arrow);
        this.placemarkBitmap = vectorToBitmap(context, R.drawable.pin);
        this.searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        this.searchOptions = new SearchOptions()
                .setSearchTypes(SearchType.GEO.value | SearchType.BIZ.value)
                .setSearchTypes(32);
        this.suggestOptions = new SuggestOptions()
                .setSuggestTypes(SearchType.GEO.value | SearchType.BIZ.value);
    }
    public void createUserLayer() {
        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);

    }
    public void onStart() {
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }
    public void onStop() {
        MapKitFactory.getInstance().onStop();
        mapView.onStop();
    }
    public void search(String search) {
        if (search.trim().isEmpty()) {
            mapObjectCollection.clear();
            searchListener.onSearchError("empty");
            return;
        }
        searchSession = searchManager.submit(search, getVisibleRegionGeometry(), searchOptions, this);
    }
    public void suggest(String search) {
        if (search.trim().isEmpty()) {
            if (suggestSession != null) {
                suggestSession.reset();
            }
            suggestListener.onSuggestResults(new ArrayList<>());
            return;
        }
        if (suggestSession != null) {
            suggestSession.reset();
        }
        suggestSession = searchManager.createSuggestSession();
        Point userPoint = getCurrentUserLocation();
        if (userLocationLayer != null && userLocationLayer.cameraPosition() != null) {
            Point userLocation = userLocationLayer.cameraPosition().getTarget();
            suggestSession.suggest(search, new BoundingBox(new Point(userLocation.getLatitude() - 0.00899316, userLocation.getLongitude() - 0.01598718),
                    new Point(userLocation.getLatitude() + 0.00899316, userLocation.getLongitude() + 0.01598718)), suggestOptions, this);
        }
    }
    public Geometry getVisibleRegionGeometry() {
        if (mapView != null) {
            return VisibleRegionUtils.toPolygon(mapView.getMap().getVisibleRegion());
        }
        return null;
    }
    public Point getCurrentUserLocation() {
        if (userLocationLayer != null && userLocationLayer.cameraPosition() != null) {
            return userLocationLayer.cameraPosition().getTarget();
        }
        return null;
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
    public void moveCamera(@NonNull Point point, float zoom) {
        mapView.getMap().move(
                new CameraPosition(point, zoom, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0.5f),
                null
        );
    }
    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        userLocationView.getPin().setIcon(ImageProvider.fromBitmap(userPinBitmap));
        IconStyle iconStyle = new IconStyle();
        iconStyle.setRotationType(RotationType.ROTATE);
        userLocationView.getArrow().setIcon(ImageProvider.fromBitmap(userArrowBitmap), iconStyle);
        userLocationView.getAccuracyCircle().setFillColor(R.color.white2);
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {

    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {

    }
    @Override
    public void onSearchResponse(@NonNull Response response) {
        mapObjectCollection.clear();
        ImageProvider pinProvider = ImageProvider.fromBitmap(placemarkBitmap);
        for (GeoObjectCollection.Item geoObjectItem : response.getCollection().getChildren()) {
            Point point = geoObjectItem.getObj().getGeometry().get(0).getPoint();
            if (point != null) {
                PlacemarkMapObject mark = mapObjectCollection.addPlacemark(point);
                mark.setIcon(pinProvider);
                mark.setOpacity(0.7f);
                mark.setUserData(geoObjectItem.getObj());
                mark.setDraggable(false);
            }
        }
        if (!response.getCollection().getChildren().isEmpty()) {
            GeoObjectCollection.Item firstResult = response.getCollection().getChildren().get(0);
            GeoObject firstGeoObject = firstResult.getObj();
            if (firstGeoObject != null && !firstGeoObject.getGeometry().isEmpty()) {
                Point firstPoint = firstGeoObject.getGeometry().get(0).getPoint();
                if (firstPoint != null) {
                    moveCamera(firstPoint, 17.0f);
                }
            }
        }
        searchListener.onSearchSuccess();
    }

    @Override
    public void onSearchError(@NonNull Error error) {
        mapObjectCollection.clear();
        searchListener.onSearchError(error.toString());
    }

    @Override
    public void onResponse(@NonNull SuggestResponse suggestResponse) {
        List<SuggestItem> suggestItems = suggestResponse.getItems();
        List<Map<String, String>> suggestions = new ArrayList<>();
        for (SuggestItem item : suggestItems) {
            if (item.getTitle() != null && item.getSubtitle() != null) {
                HashMap<String, String> map = new HashMap<>();
                map.put("name", item.getTitle().getText());
                map.put("info", item.getSubtitle().getText());
                suggestions.add(map);
            }
        }
        suggestListener.onSuggestResults(suggestions);
    }

    @Override
    public void onError(@NonNull Error error) {
        suggestListener.onSuggestError(error.toString());
    }

    public boolean isUserGetLocation() {
        return userGetLocation;
    }

    public void setUserGetLocation(boolean userGetLocation) {
        this.userGetLocation = userGetLocation;
    }
}
