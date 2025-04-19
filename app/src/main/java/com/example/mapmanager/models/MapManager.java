package com.example.mapmanager.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import androidx.core.graphics.drawable.DrawableCompat;

import com.example.mapmanager.R;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Direction;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectDragListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.BusinessObjectMetadata;
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
import com.yandex.mapkit.search.ToponymObjectMetadata;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.MasstransitRouteSerializer;
import com.yandex.mapkit.transport.masstransit.PedestrianRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.SummarySession;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.transport.masstransit.internal.PedestrianRouterBinding;
import com.yandex.mapkit.uri.Uri;
import com.yandex.mapkit.uri.UriObjectMetadata;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.mapkit.transport.masstransit.RouteSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapManager implements UserLocationObjectListener, SearchListener, SuggestListener, InputListener, com.yandex.mapkit.transport.masstransit.Session.RouteListener, MapObjectDragListener {
    private final MapView mapView;
    private final Context context;
    private UserLocationLayer userLocationLayer;
    public final MapObjectCollection mapObjectCollection;
    public final MapObjectCollection mapRoutesObjectCollection;
    private final MapObjectCollection mapWaypointObjectCollection;
    private final Bitmap userPinBitmap;
    private final Bitmap userArrowBitmap;
    private final Bitmap placemarkBitmap;
    private final SearchManager searchManager;
    private final SearchOptions searchOptions, searchOptions1;
    private final SuggestOptions suggestOptions;
    private SuggestSession suggestSession;
    private Session searchSession;
    private com.yandex.mapkit.transport.masstransit.Session session;
    private final PedestrianRouter pedestrianRouter;
    private boolean userGetLocation = false;



    public interface MapManagerSearchListener {
        void onSearchSuccess();
        void onSearchError(String error);
    }
    public interface MapManagerSuggestListener {
        void onSuggestResults(List<Map<String, String>> suggestions);
        void onSuggestError(String error);
    }
    public interface MapLongTapListener {
        void onMapLongTap(PlacemarkMapObject placemarkMapObject);
        void OnDragWaypoint();
    }
    private final MapManagerSearchListener searchListener;
    private final MapLongTapListener mapLongTapListener;
    private final MapManagerSuggestListener suggestListener;
    public MapManager(Context context, MapView mapView, MapManagerSearchListener searchListener, MapManagerSuggestListener suggestListener, MapLongTapListener mapLongTapListener) {
        this.context = context;
        this.mapView = mapView;
        mapView.getMap().addInputListener(this);
        this.searchListener = searchListener;
        this.suggestListener = suggestListener;
        this.mapLongTapListener = mapLongTapListener;
        this.mapObjectCollection = mapView.getMap().getMapObjects().addCollection();
        this.mapRoutesObjectCollection = mapView.getMap().getMapObjects().addCollection();
        this.mapWaypointObjectCollection = mapView.getMap().getMapObjects().addCollection();
        this.userPinBitmap = vectorToBitmap(context, R.drawable.pin);
        this.userArrowBitmap = vectorToBitmap(context, R.drawable.arrow);
        this.placemarkBitmap = vectorToBitmap(context, R.drawable.pin);
        this.searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        this.searchOptions = new SearchOptions()
                .setSearchTypes(SearchType.GEO.value | SearchType.BIZ.value)
                .setSearchTypes(32);
        this.searchOptions1 = new SearchOptions()
                .setSearchTypes(SearchType.BIZ.value)
                .setResultPageSize(1);

        this.suggestOptions = new SuggestOptions()
                .setSuggestTypes(SearchType.GEO.value | SearchType.BIZ.value);
        this.pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter();
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
    public void getRoute(ArrayList<PlacemarkMapObject> placemarkMapObjects) {
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        for (PlacemarkMapObject placemarkMapObject : placemarkMapObjects) {
            Point point = placemarkMapObject.getGeometry();
            requestPoints.add(new RequestPoint(point, RequestPointType.WAYPOINT, null, null));
        }
        session = pedestrianRouter.requestRoutes(requestPoints, new TimeOptions(), this);
    }
    public void search(String search) {
        if (search.trim().isEmpty()) {
            mapObjectCollection.clear();
            searchListener.onSearchError("empty");
            return;
        }
        searchSession = searchManager.submit(search, getVisibleRegionGeometry(), searchOptions, this);
    }
    public void searchByPoint(Point search, SearchOptions searchOptions) {
        if (search == null) {
            mapObjectCollection.clear();
            searchListener.onSearchError("empty");
            return;
        }
        searchSession = searchManager.submit(search, 0, searchOptions, this);
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
                    Log.d("test", "--- First Search Result Details ---");
                    Log.d("test", "Name: " + (firstGeoObject.getName() != null ? firstGeoObject.getName() : "N/A"));
                    Log.d("test", "Description: " + (firstGeoObject.getDescriptionText() != null ? firstGeoObject.getDescriptionText() : "N/A"));
                    Log.d("test", "Coordinates: " + firstPoint.getLatitude() + ", " + firstPoint.getLongitude());
                    BusinessObjectMetadata businessData = firstGeoObject.getMetadataContainer().getItem(BusinessObjectMetadata.class);
                    if (businessData != null) {
                        Log.d("test", "[Business Metadata]");
                        Log.d("test", "  OID: " + (businessData.getOid() != null ? businessData.getOid() : "N/A"));
                        Log.d("test", "  Name: " + businessData.getName());
                        Log.d("test", "  Address: " + (businessData.getAddress() != null ? businessData.getAddress().getFormattedAddress() : "N/A"));
                        Log.d("test", "photo: ");
                        Log.d("test", "  Categories: ");
                        for (com.yandex.mapkit.search.Category category : businessData.getCategories()) Log.d("test", "    - " + category.getName());
                        for (com.yandex.mapkit.search.Feature feature : businessData.getFeatures()) Log.d("test", "    - " + feature.getName() + feature.getValue().toString());
                        for (com.yandex.mapkit.search.Phone phones : businessData.getPhones()) Log.d("test", "    - " + phones.getNumber());

                        Log.d("test", "  Working Hours: " + (businessData.getWorkingHours() != null ? businessData.getWorkingHours().getText() : "N/A"));
                    }
                    ToponymObjectMetadata toponymData = firstGeoObject.getMetadataContainer().getItem(ToponymObjectMetadata.class);
                    if (toponymData != null) {
                        Log.d("test", "[Toponym Metadata]");
                        Log.d("test", "  Address: " + (toponymData.getAddress() != null ? toponymData.getAddress().getFormattedAddress() : "N/A"));
                        Log.d("test", "  Former Names: " + toponymData.getNativeName());
                        Log.d("test", "  Point: " + (toponymData.getBalloonPoint() != null ? toponymData.getBalloonPoint().getLatitude()+","+toponymData.getBalloonPoint().getLongitude() : "N/A"));
                    }

                }
            }
        }
        searchListener.onSearchSuccess();
    }

    @Override
    public void onSearchError(@NonNull Error error) {
        if (error instanceof com.yandex.runtime.network.NetworkError) {
            Log.e("MapManager_SEARCH_ERROR", "Network error details: " + error.toString());
        }  else {
            Log.e("MapManager_SEARCH_ERROR", "Other search error: " + error.toString());
        }
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

    @Override
    public void onMapTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {

    }
    @Override
    public void onMasstransitRoutes(@NonNull List<Route> list) {
        Route route = list.get(0);
        mapWaypointObjectCollection.clear();
        PolylineMapObject routePolyline = mapWaypointObjectCollection.addPolyline(route.getGeometry());
//        CameraPosition cameraPosition = mapView.getMap().cameraPosition(
//                Geometry.fromPolyline(route.getGeometry())
//        );
//        mapView.getMap().move(cameraPosition);
    }

    @Override
    public void onMasstransitRoutesError(@NonNull Error error) {
        mapWaypointObjectCollection.clear();
    }

    @Override
    public void onMapObjectDragStart(@NonNull MapObject mapObject) {

    }

    @Override
    public void onMapObjectDrag(@NonNull MapObject mapObject, @NonNull Point point) {

    }

    @Override
    public void onMapObjectDragEnd(@NonNull MapObject mapObject) {
        mapLongTapListener.OnDragWaypoint();
    }
    @Override
    public void onMapLongTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {
        ImageProvider pinProvider = ImageProvider.fromBitmap(userArrowBitmap);
        PlacemarkMapObject mark = mapObjectCollection.addPlacemark(point);
        mark.setIcon(pinProvider);
        mark.setDraggable(true);
        mark.setUserData(new Waypoint("123", "123"));
        mapLongTapListener.onMapLongTap(mark);
    }
}
