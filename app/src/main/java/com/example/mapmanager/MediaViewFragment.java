package com.example.mapmanager;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.adapters.MessageMediaAdapter;

import java.util.ArrayList;

public class MediaViewFragment extends Fragment implements MessageMediaAdapter.OnMediaListener {
    private RecyclerView recyclerView;
    private ImageView backImageView;
    private ArrayList<Uri> mediaList;
    private MessageMediaAdapter mediaLoaderAdapter;
    static MediaViewFragment updateMediaViewFragment(ArrayList<String> uriArrayList) {
        MediaViewFragment fragment = new MediaViewFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("uri", uriArrayList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mediaList = getArguments().getParcelableArrayList("uri", Uri.class);
            } else {
                mediaList = getArguments().getParcelableArrayList("uri");
            }
            if (mediaList == null) {
                mediaList = new ArrayList<>();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_view, container, false);
        recyclerView = view.findViewById(R.id.recycleView);
        backImageView = view.findViewById(R.id.imageView2);
        mediaLoaderAdapter = new MessageMediaAdapter(requireContext(), mediaList, this, 0);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(mediaLoaderAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backImageView.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    @Override
    public void onDeleteMedia(int position) {
    }
}
