package com.orange.lo.assetdemo.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orange.lo.assetdemo.MyApplication;
import com.orange.lo.assetdemo.R;
import com.orange.lo.assetdemo.model.Asset;
import com.orange.lo.assetdemo.model.DeviceResourceVersion;
import com.orange.lo.assetdemo.mqtt.Connection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResourcesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ResourcesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResourcesFragment extends Fragment {

    private static final String TAG = ResourcesFragment.class.getName();

    private OnFragmentInteractionListener mListener;

    PropertyChangeListener mConnectionStatusListener;

    PropertyChangeListener mAssetDataChangedListener;

    private Asset mAsset;

    private boolean mHasNewSplashScreenVersion;

    private ViewHolder viewHolder;
    public ResourcesFragment() {
        // Required empty public constructor
        viewHolder = new ViewHolder();
        mHasNewSplashScreenVersion = false;

        mAssetDataChangedListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                switch (propertyChangeEvent.getPropertyName()) {
                    case ApplicationConstants.ResourceNewVersionProperty:
                        final DeviceResourceVersion drv = (DeviceResourceVersion)propertyChangeEvent.getNewValue();
                        refreshNewVersionView(drv);
                        break;
                }
            }

        };

        mConnectionStatusListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                switch (propertyChangeEvent.getPropertyName()) {
                    case ApplicationConstants.ConnectionStatusProperty:
                        Connection.ConnectionStatus status = (Connection.ConnectionStatus) propertyChangeEvent.getNewValue();
                        switch (status) {
                            case CONNECTED:
                                viewHolder.resourceDownloadButton.setEnabled(mHasNewSplashScreenVersion?true: false);
                                break;
                            default:
                                viewHolder.resourceDownloadButton.setEnabled(false);
                                break;
                        }
                        break;

                }
            }
        };

    }

    private void refreshNewVersionView(final DeviceResourceVersion drv) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewHolder.splashScreenNewVersionTextView.setText(getString(R.string.new_version_found, drv.getNewVersion()));
                viewHolder.resourceDownloadButton.setEnabled(true);
                mHasNewSplashScreenVersion = true;
            }
        });

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ResourcesFragment.
     */
    public static ResourcesFragment newInstance() {
        ResourcesFragment fragment = new ResourcesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_resources, container, false);

        viewHolder.splashScreenVersionTextView = (TextView) view.findViewById(R.id.splash_screen_version_tv);
        viewHolder.splashScreenImageView = (ImageView) view.findViewById(R.id.splash_screen_iv);
        viewHolder.splashScreenNewVersionTextView = (TextView) view.findViewById(R.id.splash_screen_new_version_tv);
        viewHolder.resourceDownloadButton = (Button) view.findViewById(R.id.resource_download_bt);


        // Refresh Asset Value from singleton ==> Asset can be reset when reset app settings
        mAsset = MyApplication.getInstance().getAsset();

        reloadSplashScreenData();


        return view;
    }

    private void reloadSplashScreenData() {
        try {
            final File file = mAsset.getResource(Asset.ASSET_RESOURCE_SPLASH_ID);
            final DeviceResourceVersion drv = mAsset.getResources().getRsc().get(Asset.ASSET_RESOURCE_SPLASH_ID);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            final float ratio = displaymetrics.widthPixels / bitmap.getWidth();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    refreshNewVersionView(drv);
                    if (TextUtils.isEmpty(drv.getNewVersion()) ||
                            !MyApplication.getInstance().getMqttConnection().isConnected()) {
                        viewHolder.resourceDownloadButton.setEnabled(false);
                        if (TextUtils.isEmpty(drv.getNewVersion())) {
                            viewHolder.splashScreenNewVersionTextView.setText("");
                        }
                    }

                    viewHolder.splashScreenImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth()*ratio), Math.round(bitmap.getHeight()*ratio), false));
                    viewHolder.splashScreenImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    Map<String, DeviceResourceVersion> rsc = mAsset.getResources().getRsc();
                    viewHolder.splashScreenVersionTextView.setText(rsc.get(Asset.ASSET_RESOURCE_SPLASH_ID).getVersion());
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "Error when reload the Splash Screen image: " + Log.getStackTraceString(e));
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewHolder.resourceDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewHolder.resourceDownloadButton.setEnabled(false);
                viewHolder.splashScreenNewVersionTextView.setText(getString(R.string.downloading));

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mAsset.downloadResource(Asset.ASSET_RESOURCE_SPLASH_ID);
                            reloadSplashScreenData();
                            mHasNewSplashScreenVersion = false; // No new version available
                            mListener.onPublishResourcesNewVersion(); // Notify Main Activity to publish the new resources versions
                        } catch (final IOException e) {
                            Log.e(TAG, "Error when downloading the new Asset's Resource: " + Log.getStackTraceString(e));
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Notify.toast(getContext(), getContext().getString(R.string.download_resource_error, e.getMessage()), Toast.LENGTH_LONG);
                                }
                            });
                        }
                    }
                });
                thread.start();
            }
        });

        mAsset = MyApplication.getInstance().getAsset();


        mAsset.registerChangeListener(mAssetDataChangedListener);

    }

    @Override
    public void onPause() {
        super.onPause();
        viewHolder.resourceDownloadButton.setOnClickListener(null);
        mAsset.removeChangeListener(mAssetDataChangedListener);

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        public void onPublishResourcesNewVersion();
    }

    public class ViewHolder {
        public TextView splashScreenVersionTextView;
        public TextView splashScreenNewVersionTextView;
        public ImageView splashScreenImageView;
        public Button resourceDownloadButton;

    }
}
