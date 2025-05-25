package com.example.mapmanager;

import static android.view.View.VISIBLE;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;

public class PlayerViewFragment extends Fragment {

    private Uri uri;
    private String url;
    private ExoPlayer player;
    private PlayerView playerView;
    private ImageView backImageView;
    private SubsamplingScaleImageView subsamplingScaleImageView;
    private int mediaType = -1; // 0 - image, 1 - video;
    static PlayerViewFragment updatePlayerViewFragment(Object url) {
        PlayerViewFragment fragment = new PlayerViewFragment();
        Bundle args = new Bundle();
        if (url instanceof Uri){
            args.putString("url", String.valueOf(url));
        } else if (url instanceof String) {
            args.putString("url", (String) url);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String url = getArguments().getString("url");
            try {
                uri = Uri.parse(url);
            } catch (Exception e) {
                uri = null;
            }
            if (uri != null) {
                if (isVideoFile(requireContext(), uri)) {
                    mediaType = 1;
                } else if (isImageFile(requireContext(), uri)) {
                    mediaType = 0;
                }
            } else if (url != null) {
                if (isVideoFile(requireContext(), url)) {
                    mediaType = 1;
                } else if (isImageFile(requireContext(), url)) {
                    mediaType = 0;
                }
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
            CustomTarget<File> glideFileTarget = new CustomTarget<File>() {
                @Override
                public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                    MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(resource));
                    if (mediaItem != null) {
                        player.setMediaItem(mediaItem);
                        player.prepare();
                        player.setPlayWhenReady(true);
                    }
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    MediaItem mediaItem = MediaItem.fromUri(uri);
                    if (mediaItem != null) {
                        player.setMediaItem(mediaItem);
                        player.prepare();
                        player.setPlayWhenReady(true);
                    }
                }
            };
            playerView.setVisibility(VISIBLE);
            player = new ExoPlayer.Builder(requireContext()).build();
            playerView.setPlayer(player);
            if (uri != null) {
                if (getContext() != null) {
                    Glide.with(getContext().getApplicationContext())
                            .asFile()
                            .load(uri)
                            .into(glideFileTarget);
                }
            } else if (url != null) {
                if (getContext() != null) {
                    Glide.with(getContext().getApplicationContext())
                            .asFile()
                            .load(url)
                            .into(glideFileTarget);
                }
            }
        } else if (mediaType == 0) {
            CustomTarget<File> glideFileTarget = new CustomTarget<File>() {
                @Override
                public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                    subsamplingScaleImageView.setVisibility(VISIBLE);
                    subsamplingScaleImageView.setMaxScale(8f);
                    subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                    subsamplingScaleImageView.setImage(ImageSource.uri(Uri.fromFile(resource)));
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    subsamplingScaleImageView.setVisibility(VISIBLE);
                    subsamplingScaleImageView.setMaxScale(8f);
                    subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                    subsamplingScaleImageView.setImage(ImageSource.uri(uri));
                }
            };
            if (url != null) {
                if (getContext() != null) {
                    Glide.with(getContext().getApplicationContext())
                            .asFile()
                            .load(url)
                            .into(glideFileTarget);
                }
            } else if (uri != null) {
                if (getContext() != null) {
                    Glide.with(getContext().getApplicationContext())
                            .asFile()
                            .load(uri)
                            .into(glideFileTarget);
                }
            }
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
    public static String getMimeType(Context context, Object uriOrUrl) {
        if (uriOrUrl == null) {
            return null;
        }
        Uri uri = null;
        String url = null;
        if (uriOrUrl instanceof Uri) {
            uri = (Uri) uriOrUrl;
            url = uri.toString();
        } else if (uriOrUrl instanceof String) {
            url = (String) uriOrUrl;
            try {
                uri = Uri.parse(url);
            } catch (Exception e) {
                uri = null;
            }
        } else {
            return null;
        }
        String mimeType = null;
        String scheme = (uri != null) ? uri.getScheme() : null;

        if (uri != null && ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        }
        if (mimeType == null && url != null) {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(url);
            if (!TextUtils.isEmpty(fileExtension)) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }
        }
        return mimeType;
    }
    public static boolean isVideoFile(Context context, Object url) {
        if (url == null) {
            return false;
        }
        String mimeType = getMimeType(context, url);
        return mimeType != null && mimeType.startsWith("video/");
    }
    public static boolean isImageFile(Context context, Object url) {
        if (url == null) {
            return false;
        }
        String mimeType = getMimeType(context, url);
        return mimeType != null && mimeType.startsWith("image/");
    }
}
