package com.example.mapmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.adapters.RouteDisplayAdapter;
import com.example.mapmanager.adapters.RouteSelectAdapter;
import com.example.mapmanager.models.Chat;
import com.example.mapmanager.models.FilterSettings;
import com.example.mapmanager.models.Route;
import com.example.mapmanager.models.RouteCard;
import com.example.mapmanager.models.RouteCardSettings;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;


public class HomeFragment extends Fragment implements RouteSelectAdapter.PostListener, RouteDisplayAdapter.RouteDisplayListener  {
    private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\s-]+");
    private static final Pattern MULTIPLE_HYPHENS_PATTERN = Pattern.compile("-+");
    private static final Pattern MULTIPLE_WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern HYPHEN_WITH_SPACES_PATTERN = Pattern.compile("\\s+-\\s+");
    private RecyclerView routeListView, selectingRouteListView;
    private RouteDisplayAdapter adapter;
    private RouteSelectAdapter selectAdapter;
    private List<RouteCard> routeList;
    private List<RouteCard> displayedRouteList;
    private List<Route> localRouteList;
    private ImageView routeCardBuildingCloseImage;
    private TextView addRouteCardView, routeCardBuildingDateEditText, routeCardBuildingStartTimeEditText, routeCardBuildingEndTimeEditText;
    private EditText routeCardBuildingNameEnter, routeCardBuildingDescriptionEnter, routeCardBuildingCityEditText, routeCardBuildingAgeEditText, filterMinLenEnter, filterMaxLenEnter, filterCityEnter;
    private RangeSlider ageRangeSlider;
    private View plusView, filterView, topView, applyView;
    private AlertDialog dialog;
    private FirebaseAuth mAuth;
    private Calendar startTime, endTime;
    private HomeFragmentListener homeFragmentListener;
    private SearchView searchView;
    private DatabaseReference routeCardReference;
    private FilterSettings settings = new FilterSettings();
    private Query query;
    private boolean isSearching = false;
    private MessengerFragment.OnChatSelectChat selectChatListner;
    private interface SearchResultListener {
        void onResultsFound(List<RouteCard> results);
        void onError();
    }

    interface HomeFragmentListener {
        void showInMap(Route route);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        routeList = new ArrayList<RouteCard>();
        displayedRouteList = new ArrayList<RouteCard>();
        localRouteList = new ArrayList<Route>();
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("routeCards");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {

                    routeList.add(snapshot.getValue(RouteCard.class));
                    if (!isSearching) {
                        if (settings.passFilter(snapshot.getValue(RouteCard.class).getRouteCardSettings())) {
                            displayedRouteList.add(snapshot.getValue(RouteCard.class));
                            adapter.notifyItemInserted(displayedRouteList.size() - 1);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    RouteCard routeCard = snapshot.getValue(RouteCard.class);
                    int position = Collections.binarySearch(routeList, routeCard, new Comparator<RouteCard>() {
                        @Override
                        public int compare(RouteCard routeCard, RouteCard routeCard1) {
                            return routeCard.getId().compareTo(routeCard1.getId());
                        }
                    });
                    if (position != -1) {
                        routeList.set(position, routeCard);
                    }
                    if (!isSearching) {
                        int position1 = Collections.binarySearch(displayedRouteList, routeCard, new Comparator<RouteCard>() {
                            @Override
                            public int compare(RouteCard routeCard, RouteCard routeCard1) {
                                return routeCard.getId().compareTo(routeCard1.getId());
                            }
                        });
                        if (position != -1) {
                            displayedRouteList.set(position1, routeCard);
                            adapter.notifyItemChanged(position1);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                RouteCard routeCard = snapshot.getValue(RouteCard.class);
                int position = Collections.binarySearch(routeList, routeCard, new Comparator<RouteCard>() {
                    @Override
                    public int compare(RouteCard routeCard, RouteCard routeCard1) {
                        return routeCard.getId().compareTo(routeCard1.getId());
                    }
                });
                if (position != -1) {
                    routeList.remove(position);
                }
                if (!isSearching) {
                    int position1 = Collections.binarySearch(displayedRouteList, routeCard, new Comparator<RouteCard>() {
                        @Override
                        public int compare(RouteCard routeCard, RouteCard routeCard1) {
                            return routeCard.getId().compareTo(routeCard1.getId());
                        }
                    });
                    if (position != -1) {
                        displayedRouteList.remove(position1);
                        adapter.notifyItemRemoved(position);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference routeReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("routeList");
        routeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                localRouteList.clear();
                GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                ArrayList<String> routeId = snapshot.getValue(t);
                if (routeId != null) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("routes");
                    for (String id : routeId) {
                        DatabaseReference routesRef = databaseReference.child(id);
                        if (routesRef != null) {
                            databaseReference.child(id).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot dataSnapshot) {
                                    Route route = dataSnapshot.getValue(Route.class);
                                    if (route != null) {
                                        localRouteList.add(route);
                                        selectAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        selectChatListner = (MessengerFragment.OnChatSelectChat) context;
        homeFragmentListener = (HomeFragmentListener) context;
        routeCardReference = FirebaseDatabase.getInstance().getReference().child("routeCards");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        routeListView = view.findViewById(R.id.routeListView);
        plusView = view.findViewById(R.id.plusView);
        filterView = view.findViewById(R.id.filterView);
        topView = view.findViewById(R.id.topView);
        searchView = view.findViewById(R.id.routeSearch);
        adapter = new RouteDisplayAdapter(getContext(), displayedRouteList, this);
        selectAdapter = new RouteSelectAdapter(localRouteList, this);
        routeListView.setLayoutManager(new LinearLayoutManager(getContext()));
        routeListView.setAdapter(adapter);
        routeListView.setVisibility(View.VISIBLE);
        if (displayedRouteList.isEmpty() && !routeList.isEmpty()) {
            displayedRouteList.addAll(routeList);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        plusView.setOnClickListener(v->{
            LayoutInflater inflater = getLayoutInflater();
            View changeDialogView = inflater.inflate(R.layout.select_route_dialog, null);
            dialog = new AlertDialog.Builder(requireActivity()).setView(changeDialogView).create();
            selectingRouteListView = changeDialogView.findViewById(R.id.selectingRouteListView);
            selectingRouteListView.setLayoutManager(new LinearLayoutManager(getContext()));
            selectingRouteListView.setAdapter(selectAdapter);
            selectingRouteListView.setVisibility(View.VISIBLE);
            dialog.show();
        });
        topView.setOnClickListener(v->{
            LayoutInflater inflater = getLayoutInflater();
            View topRouteView = inflater.inflate(R.layout.route_top_fragment, null);

        });
        filterView.setOnClickListener(v->{
            LayoutInflater inflater = getLayoutInflater();
            View filterDialogView = inflater.inflate(R.layout.filter_dialog, null);
            applyView = filterDialogView.findViewById(R.id.acceptButton);
            ageRangeSlider = filterDialogView.findViewById(R.id.ageSlider);
            filterMinLenEnter = filterDialogView.findViewById(R.id.lenStartTextInput);
            filterMaxLenEnter = filterDialogView.findViewById(R.id.lenEndTextInput);
            filterCityEnter = filterDialogView.findViewById(R.id.cityTextInput);

            ageRangeSlider.setValues(List.of((float) settings.getMinAge(), (float) settings.getMaxAge()));
            filterCityEnter.setText(settings.getCity());
            filterMinLenEnter.setText(Double.valueOf(settings.getMinLen()).equals(FilterSettings.DOWN_BORDER_LEN)? "" : String.valueOf(settings.getMinLen()));
            filterMaxLenEnter.setText(Double.valueOf(settings.getMaxLen()).equals(FilterSettings.UP_BORDER_LEN)? "" : String.valueOf(settings.getMaxLen()));

            applyView.setOnClickListener(list->{
                settings.setMinAge(ageRangeSlider.getValues().get(0).shortValue());
                settings.setMaxAge(ageRangeSlider.getValues().get(1).shortValue());
                settings.setCity(cityUniform(filterCityEnter.getText().toString()));
                settings.setMaxLen(filterMaxLenEnter.getText().toString().isEmpty()? FilterSettings.UP_BORDER_LEN : Float.parseFloat(filterMaxLenEnter.getText().toString()));
                settings.setMinLen(filterMinLenEnter.getText().toString().isEmpty()? FilterSettings.DOWN_BORDER_LEN : Float.parseFloat(filterMinLenEnter.getText().toString()));
                if (!isSearching) showAllRoutes();
                else {
                    searchView.setQuery(searchView.getQuery().toString(), true);
                }
            });
            final PopupWindow filterDialog = new PopupWindow(filterDialogView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            filterDialog.setFocusable(true);
            filterDialog.setOutsideTouchable(true);
            filterDialog.showAsDropDown(filterView, 0, 14);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                isSearching = true;
                searchRoutesByNamePrefix(s, new SearchResultListener() {
                    @Override
                    public void onResultsFound(List<RouteCard> results) {
                        displayedRouteList.clear();
                        displayedRouteList.addAll(results);
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onError() {
                        showAllRoutes();
                    }
                });
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                String text = s != null ? s.trim() : "";
                if (text.isEmpty()) {
                    showAllRoutes();
                    isSearching = false;
                } else {
                    isSearching = true;
                }
                return true;
            }
        });
    }

    private void searchRoutesByNamePrefix(String prefix, @NonNull SearchResultListener listener) {
        if (prefix == null || prefix.trim().isEmpty()) {
            listener.onError();
            return;
        }
        query = routeCardReference.orderByChild("name").startAt(prefix).endAt(prefix + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<RouteCard> results = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot routeShot : snapshot.getChildren()) {
                        RouteCard routeCard = routeShot.getValue(RouteCard.class);
                        if (settings.passFilter(routeCard.getRouteCardSettings())) {
                            results.add(routeCard);
                        }
                    }
                }
                listener.onResultsFound(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showAllRoutes() {
        displayedRouteList.clear();
        for (RouteCard route : routeList) {
            if (settings.passFilter(route.getRouteCardSettings())) {
                displayedRouteList.add(route);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        isSearching = false;
    }
    @Override
    public void postRouteCard(Route curRoute) {
        dialog.dismiss();
        LayoutInflater inflater = getLayoutInflater();
        View postRouteCardDialogView = inflater.inflate(R.layout.route_card_build_dialog, null);
        AlertDialog buildDialog = new AlertDialog.Builder(requireActivity()).setView(postRouteCardDialogView).setCancelable(false).create();
        buildDialogDeclaration(postRouteCardDialogView);
        routeCardBuildingCloseImage.setOnClickListener(v->{
            buildDialog.dismiss();
        });
        addRouteCardView.setOnClickListener(v->{
            final long initialStartTimeMillis = startTime.getTimeInMillis();
            final long initialEndTimeMillis = endTime.getTimeInMillis();
            startTime = Calendar.getInstance();
            endTime = Calendar.getInstance();
            startTime.add(Calendar.SECOND, 1);
            endTime.add(Calendar.SECOND, 1);
            if (routeCardBuildingNameEnter.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), getResources().getString(R.string.need_name), Toast.LENGTH_SHORT).show();
            } else if (initialStartTimeMillis <= startTime.getTimeInMillis() && initialEndTimeMillis <= endTime.getTimeInMillis()) {
                Toast.makeText(requireContext(), getResources().getString(R.string.time_error), Toast.LENGTH_SHORT).show();
            } else {
                RouteCardSettings settings = new RouteCardSettings(Integer.parseInt(routeCardBuildingAgeEditText.getText().toString()), routeCardBuildingCityEditText.getText().toString(), 100);
                settings.normalize();
                RouteCard routeCard = new RouteCard(curRoute.getId(), routeCardBuildingNameEnter.getText().toString(), routeCardBuildingDescriptionEnter.getText().toString(),
                        settings, initialStartTimeMillis, initialEndTimeMillis);

                ArrayList<String> memberList = new ArrayList<>();
                Chat chat = new Chat(startTime.getTimeInMillis(), memberList, routeCardBuildingNameEnter.getText().toString(), "", mAuth.getUid());
                chat.addNewMember(mAuth.getUid());
                routeCard.setChatId(chat.getId());
                routeCard.createRoutCard();
                selectChatListner.onSelectChat(chat.getId(), chat.getLastReadMessageId());
                buildDialog.dismiss();
                startTime = Calendar.getInstance();
                endTime = Calendar.getInstance();
            }
        });
        routeCardBuildingDateEditText.setOnClickListener(v->{
            new DatePickerDialog(requireActivity(), d, startTime.get(Calendar.YEAR), startTime.get(Calendar.MONTH), startTime.get(Calendar.DAY_OF_MONTH)).show();
        });
        routeCardBuildingStartTimeEditText.setOnClickListener(v->{
            new TimePickerDialog(requireActivity(), st, startTime.get(Calendar.HOUR), startTime.get(Calendar.MINUTE), true).show();
        });
        routeCardBuildingEndTimeEditText.setOnClickListener(v->{
            new TimePickerDialog(requireActivity(), et, endTime.get(Calendar.HOUR), endTime.get(Calendar.MINUTE), true).show();
        });
        buildDialog.show();
    }
    public void buildDialogDeclaration(View view) {
        routeCardBuildingCloseImage = view.findViewById(R.id.routeCardBuildingCloseImage);
        addRouteCardView = view.findViewById(R.id.addRouteCardView);
        routeCardBuildingAgeEditText = view.findViewById(R.id.routeCardBuildingAgeEditText);
        routeCardBuildingCityEditText = view.findViewById(R.id.routeCardBuildingCityEditText);
        routeCardBuildingNameEnter = view.findViewById(R.id.routeCardBuildingNameEnter);
        routeCardBuildingDescriptionEnter = view.findViewById(R.id.routeCardBuildingDescriptionEnter);
        routeCardBuildingDateEditText = view.findViewById(R.id.routeCardBuildingDateEditText);
        routeCardBuildingStartTimeEditText = view.findViewById(R.id.routeCardBuildingStartTimeEditText);
        routeCardBuildingEndTimeEditText = view.findViewById(R.id.routeCardBuildingEndTimeEditText);
    }
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            startTime.set(Calendar.YEAR, year);
            startTime.set(Calendar.MONTH, monthOfYear);
            startTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            endTime.set(Calendar.YEAR, year);
            endTime.set(Calendar.MONTH, monthOfYear);
            endTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (startTime.getTimeInMillis() >= endTime.getTimeInMillis()) {
                endTime.add(Calendar.DAY_OF_MONTH, 1);
            }
            setInitialDate();
        }
    };

    TimePickerDialog.OnTimeSetListener st = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            startTime.set(Calendar.HOUR_OF_DAY, hour);
            startTime.set(Calendar.MINUTE, minute);
            setInitialTime(true);
            setInitialDate();
        }
    };

    TimePickerDialog.OnTimeSetListener et = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            endTime.set(Calendar.HOUR_OF_DAY, hour);
            endTime.set(Calendar.MINUTE, minute);
            setInitialTime(false);
            setInitialDate();
        }
    };

    void setInitialTime(boolean isStart) {
        if (isStart) {
            routeCardBuildingStartTimeEditText.setText(DateUtils.formatDateTime(requireContext(),
                    startTime.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_TIME));
        } else {
            routeCardBuildingEndTimeEditText.setText(DateUtils.formatDateTime(requireContext(),
                    endTime.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_TIME));
        }
    }
    void setInitialDate() {
        routeCardBuildingDateEditText.setText(DateUtils.formatDateTime(requireContext(),
                startTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }
    @Override
    public void acceptRouteButton(int position) {
        RouteCard routeCard = routeList.get(position);
        FirebaseDatabase.getInstance().getReference().child("chats").child(routeCard.getChatId()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    chat.setId(dataSnapshot.getKey());
                    chat.addNewMember(mAuth.getUid());
                    selectChatListner.onSelectChat(chat.getId(), chat.getLastReadMessageId());
                }
            }
        });
    }

    @Override
    public void showInMapButton(int position) {
        FirebaseDatabase.getInstance().getReference().child("routes").child(routeList.get(position).getRoute()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    homeFragmentListener.showInMap(dataSnapshot.getValue(Route.class));
                }
            }
        });

    }
    String cityUniform(String s) {
        s = FORBIDDEN_CHARS_PATTERN.matcher(s).replaceAll("");
        s = MULTIPLE_HYPHENS_PATTERN.matcher(s).replaceAll("-");
        s = MULTIPLE_WHITESPACE_PATTERN.matcher(s).replaceAll(" ");
        s = HYPHEN_WITH_SPACES_PATTERN.matcher(s).replaceAll("-");
        s = s.trim();
        return s;
    }
}