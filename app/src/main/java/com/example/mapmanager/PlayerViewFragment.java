package com.example.mapmanager;

import static android.view.View.VISIBLE;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class PlayerViewFragment extends Fragment {

    private Uri uri;
    private String url;
    private ExoPlayer player;
    private PlayerView playerView;
    private ImageView backImageView;
    private SubsamplingScaleImageView subsamplingScaleImageView;
    private int mediaType = -1; // 0 - image, 1 - video;
    static PlayerViewFragment updatePlayerViewFragment(String url, Uri uri) {
        PlayerViewFragment fragment = new PlayerViewFragment();
        Bundle args = new Bundle();
        if (url != null) {
            args.putString("url", url);
        } else {
            args.putString("uri", String.valueOf(uri));
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String s = getArguments().getString("uri");
            if (s != null) {
                uri = Uri.parse(s);
            }
            url = getArguments().getString("url");
            if (uri != null) {
                if (isVideoFile(requireContext(), uri)) {
                    mediaType = 1;
                } else if (isImageFile(requireContext(), uri)) {
                    mediaType = 0;
                }
            } else if (url != null) {

            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_view, container, false);
        playerView = view.findViewById(R.id.player_view);
        backImageView = view.findViewById(R.id.imageView2);
        subsamplingScaleImageView = view.findViewById(R.id.subsampling_image);
        if (mediaType == 1) {
            playerView.setVisibility(VISIBLE);
            player = new ExoPlayer.Builder(requireContext()).build();
            playerView.setPlayer(player);
            MediaItem mediaItem = MediaItem.fromUri(uri);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);
        } else if (mediaType == 0) {
            subsamplingScaleImageView.setVisibility(VISIBLE);
            subsamplingScaleImageView.setMaxScale(8f);
            subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
            subsamplingScaleImageView.setImage(ImageSource.uri(uri));
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backImageView.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }
    public static String getMimeType(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String mimeType = null;
        String scheme = uri.getScheme();

        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (!TextUtils.isEmpty(fileExtension)) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }
        }
        return mimeType;
    }
    public static boolean isVideoFile(Context context, Uri uri) {
        if (uri == null) {
            return false;
        }
        String mimeType = getMimeType(context, uri);
        return mimeType != null && mimeType.startsWith("video/");
    }
    public static boolean isImageFile(Context context, Uri uri) {
        if (uri == null) {
            return false;
        }
        String mimeType = getMimeType(context, uri);
        return mimeType != null && mimeType.startsWith("image/");
    }
}
