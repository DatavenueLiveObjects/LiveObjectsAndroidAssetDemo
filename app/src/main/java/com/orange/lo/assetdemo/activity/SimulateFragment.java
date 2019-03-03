package com.orange.lo.assetdemo.activity;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.orange.lo.assetdemo.MyApplication;
import com.orange.lo.assetdemo.R;
import com.orange.lo.assetdemo.AppPreferences;
import com.orange.lo.assetdemo.model.Asset;
import com.orange.lo.assetdemo.model.DeviceConfig;
import com.orange.lo.assetdemo.model.DeviceDataTelemetry;
import com.orange.lo.assetdemo.mqtt.Connection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SimulateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SimulateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SimulateFragment extends Fragment {

    private static final String TAG = SimulateFragment.class.getName();

    private OnFragmentInteractionListener mListener;

    private ViewHolder mViewHolder = null;

    MyApplication myApplication = MyApplication.getInstance();
    AppPreferences mAppPreferences = myApplication.getAppPreferences();
    Asset mAsset;

    PropertyChangeListener mConnectionStatusListener;

    PropertyChangeListener mAssetDataChangedListener;

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            //if (!fromUser) return;
            //if (!seekBar.isEnabled() || ! mAsset.isTelemetryModeAuto()) return; // Do not process when SeekBar is disabled ie auto mode
            switch (seekBar.getId()) {
                case R.id.temperature_sb:
                    int value = progress + ApplicationConstants.TEMPERATURE_MIN_PROGESS;
                    mAsset.getTelemetry().setTemperature(value);
                    mViewHolder.temperatureTextView.setText(String.valueOf(value).concat("°"));
                    break;
                case R.id.hydrometry_sb:
                    mAsset.getTelemetry().setHydrometry(progress);
                    mViewHolder.hydrometryTextView.setText(String.valueOf(progress).concat("%"));
                    break;
                case R.id.revmin_sb:
                    mAsset.getTelemetry().setRevmin(progress);
                    mViewHolder.revminTextView.setText(String.valueOf(progress).concat(" rpm"));
                    break;
                case R.id.CO2_sb:
                    mAsset.getTelemetry().setCO2(progress);
                    mViewHolder.CO2TextView.setText(String.valueOf(progress).concat(" ppm"));
                    break;
                case R.id.pressure_sb:
                    mAsset.getTelemetry().setPressure(progress);
                    mViewHolder.pressureTextView.setText(String.valueOf(progress).concat(" mBars"));
                    break;
            }

        }
    };

    public SimulateFragment() {
        // Required empty public constructor
        // Required empty public constructor
        mAssetDataChangedListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                switch (propertyChangeEvent.getPropertyName()) {
                    case ApplicationConstants.TelemetryProperty:
                        updateDeviceTelemetryDisplay((DeviceDataTelemetry) propertyChangeEvent.getNewValue());
                        updateSendingDataDisplay();
                        break;
                    case ApplicationConstants.LocationProperty:
                        updateDeviceLocationDisplay((Double[]) propertyChangeEvent.getNewValue());
                        break;
                    case ApplicationConstants.ConfigurationProperty:
                        updateConfigurationDisplay((DeviceConfig) propertyChangeEvent.getNewValue());
                        updateConfigLogLevelDisplay((DeviceConfig) propertyChangeEvent.getNewValue());
                        break;
                }
            }

        };

        mConnectionStatusListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                switch (propertyChangeEvent.getPropertyName()) {
                    case ApplicationConstants.ConnectionStatusProperty:
                        updateStatusDisplay((Connection.ConnectionStatus) propertyChangeEvent.getNewValue());
                        break;

                }
            }
        };


    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SimulateFragment.
     */
    public static SimulateFragment newInstance() {
        SimulateFragment fragment = new SimulateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//        }

    }

    boolean processingConnection = false;

    @Override
    public void onResume() {
        super.onResume();

        // Reload the Asset Current value (Reset Data makes the Asset to be recreated
        mAsset = myApplication.getAsset();


        mAsset.registerChangeListener(mAssetDataChangedListener);

        mViewHolder.connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!processingConnection){
                    processingConnection = true;
                    Log.d(TAG, "setOnClickListener Connection/Disconnection button");
                    mListener.mqttConnectOrDisconnect();
                    processingConnection = false;
                }
            }
        });


        mViewHolder.telemetryModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mAsset.setTelemetryModeAuto(checked);
                updateTelemetryMode();
            }
        });
        mViewHolder.doorStateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mAsset.SetTelemetryOpenDoor(b);
            }
        });
        mViewHolder.temperatureSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        mViewHolder.hydrometrySeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        mViewHolder.revminSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        mViewHolder.CO2SeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        mViewHolder.pressureSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        mViewHolder.telemetryModeSwitch.setChecked(mAsset.isTelemetryModeAuto());
        updateTelemetryMode();

        mViewHolder.doorStateSwitch.setChecked(mAsset.getTelemetry().isDoorOpen());

        updateDeviceTelemetryDisplay(mAsset.getTelemetry());

        mViewHolder.locationModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mAsset.setLocationModeAuto(checked);
                updateLocationMode();
            }
        });
        mViewHolder.locationModeSwitch.setChecked(mAsset.isLocationModeAuto());
        updateLocationMode();
        updateDeviceLocationDisplay(mAsset.getLocation());

        updateConfigurationDisplay(mAsset.getConfig());
        updateConfigLogLevelDisplay(mAsset.getConfig());

        myApplication.getMqttConnection().registerChangeListener(mConnectionStatusListener);
        updateStatusDisplay(myApplication.getMqttConnection().getStatus());

    }

    @Override
    public void onPause() {
        super.onPause();

        mAsset.removeChangeListener(mAssetDataChangedListener);

        mViewHolder.connectionButton.setOnClickListener(null);

        mViewHolder.telemetryModeSwitch.setOnCheckedChangeListener(null);
        mViewHolder.temperatureSeekBar.removeOnAttachStateChangeListener(null);
        mViewHolder.hydrometrySeekBar.setOnSeekBarChangeListener(null);
        mViewHolder.revminSeekBar.setOnSeekBarChangeListener(null);
        mViewHolder.CO2SeekBar.setOnSeekBarChangeListener(null);
        mViewHolder.pressureSeekBar.setOnSeekBarChangeListener(null);
        mViewHolder.locationModeSwitch.setOnCheckedChangeListener(null);
        mViewHolder.doorStateSwitch.setOnCheckedChangeListener(null);
        myApplication.getMqttConnection().removeChangeListener(mConnectionStatusListener);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_simulate, container, false);

        mViewHolder = new ViewHolder();
        mViewHolder.connectionButton = (Button) view.findViewById(R.id.connection_btn);
        mViewHolder.temperatureTextView = (TextView) view.findViewById(R.id.temperature_tv);
        mViewHolder.temperatureSeekBar = (SeekBar) view.findViewById(R.id.temperature_sb);
        mViewHolder.hydrometryTextView = (TextView) view.findViewById(R.id.hydrometry_tv);
        mViewHolder.hydrometrySeekBar = (SeekBar) view.findViewById(R.id.hydrometry_sb);
        mViewHolder.revminTextView = (TextView) view.findViewById(R.id.revmin_tv);
        mViewHolder.revminSeekBar = (SeekBar) view.findViewById(R.id.revmin_sb);
        mViewHolder.CO2TextView = (TextView) view.findViewById(R.id.CO2_tv);
        mViewHolder.CO2SeekBar = (SeekBar) view.findViewById(R.id.CO2_sb);
        mViewHolder.pressureTextView = (TextView) view.findViewById(R.id.pressure_tv);
        mViewHolder.pressureSeekBar = (SeekBar) view.findViewById(R.id.pressure_sb);
        mViewHolder.telemetryModeSwitch = (SwitchCompat) view.findViewById(R.id.telemetry_mode_sw);
        mViewHolder.doorStateSwitch = (SwitchCompat) view.findViewById(R.id.door_state_sw);
        mViewHolder.locationModeSwitch = (SwitchCompat) view.findViewById(R.id.location_mode_sw);
        mViewHolder.locationTextView = (TextView) view.findViewById(R.id.location_tv);
        mViewHolder.sendingDataImageView = (ImageView) view.findViewById(R.id.sending_iv);
        mViewHolder.configRefreshTextView = (TextView)  view.findViewById(R.id.config_refresh_rate);
        mViewHolder.configLogLevelText = (TextView) view.findViewById(R.id.config_log_level);

        return view;
    }

    private void updateTelemetryMode() {
        // Update the progress bar: runOnUiThread does not work ==> We use a simple Handler to update the data
        new Handler().post(new Runnable() {
            public void run() {

                boolean enabled = !mAsset.isTelemetryModeAuto();
                mViewHolder.temperatureSeekBar.setEnabled(enabled);
                mViewHolder.hydrometrySeekBar.setEnabled(enabled);
                mViewHolder.revminSeekBar.setEnabled(enabled);
                mViewHolder.pressureSeekBar.setEnabled(enabled);
                mViewHolder.CO2SeekBar.setEnabled(enabled);
            }
        });
    }

    private void updateLocationMode() {
        // Update the progress bar: runOnUiThread does not work ==> We use a simple Handler to update the data
        new Handler().post(new Runnable() {
            public void run() {

                mViewHolder.locationModeSwitch.setChecked(mAsset.isLocationModeAuto());
                //boolean enabled = !mAsset.isLocationModeAuto();
            }
        });
    }

    private void updateDeviceTelemetryDisplay(final DeviceDataTelemetry newValue) {
        // Update the progress bar: runOnUiThread does not work ==> We use a simple Handler to update the data
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mViewHolder.temperatureTextView.setText(String.valueOf(newValue.getTemperature()).concat("°"));
                mViewHolder.hydrometryTextView.setText(String.valueOf(newValue.getHydrometry()).concat("%"));
                mViewHolder.revminTextView.setText(String.valueOf(newValue.getRevmin()).concat(" rpm"));
                mViewHolder.CO2TextView.setText(String.valueOf(newValue.getCO2()).concat(" ppm"));
                mViewHolder.pressureTextView.setText(String.valueOf(newValue.getPressure()).concat(" mBars"));
                mViewHolder.temperatureSeekBar.setProgress(newValue.getTemperature() - ApplicationConstants.TEMPERATURE_MIN_PROGESS); // min temperature = -20
                mViewHolder.hydrometrySeekBar.setProgress(newValue.getHydrometry());
                mViewHolder.revminSeekBar.setProgress(newValue.getRevmin());
                mViewHolder.CO2SeekBar.setProgress(newValue.getCO2());
                mViewHolder.pressureSeekBar.setProgress(newValue.getPressure());
                mViewHolder.doorStateSwitch.setChecked(newValue.isDoorOpen());
            }
        });
    }

    private void updateConfigurationDisplay(DeviceConfig deviceConfig) {
        final DeviceConfig.CfgParameter cfgParameter = deviceConfig.current.get(Asset.ASSET_CONFIG_REFRESH);
        if (cfgParameter == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                Integer val = ((Number)cfgParameter.getValue()).intValue();
                String msg = getString(R.string.device_config_refresh, val);
                mViewHolder.configRefreshTextView.setText(msg);
            }
        });
    }

    private void updateConfigLogLevelDisplay(DeviceConfig deviceConfig){
        final DeviceConfig.CfgParameter cfgParameter = deviceConfig.current.get(Asset.ASSET_CONFIG_LOG);
        if (cfgParameter == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                String val = (cfgParameter.getValue()).toString();
                String msg = getString(R.string.device_config_log, val);
                mViewHolder.configLogLevelText.setText(msg);
            }
        });
    }

    private void updateSendingDataDisplay() {
        // Do not make the animation if we are not connected (problem at fragment creation
        //if (connectionStatus != Connection.ConnectionStatus.CONNECTED) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                mViewHolder.sendingDataImageView.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mViewHolder.sendingDataImageView.setVisibility(View.INVISIBLE);
                    }
                }, 800);
            }
        });

    }

    private void updateDeviceLocationDisplay(Double[] newValue) {
        final StringBuilder sb = new StringBuilder()
                .append(String.format("%.6f", newValue[0]))
                .append("° / ")
                .append(String.format("%.6f", newValue[1]))
                .append("°");
  //      Log.i(TAG, "New Location: " + sb.toString());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                mViewHolder.locationTextView.setText( sb.toString() );
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myApplication.getMqttConnection().removeChangeListener(mConnectionStatusListener);
        myApplication.getAsset().removeChangeListener(mAssetDataChangedListener);
    }

    Connection.ConnectionStatus connectionStatus;

    private void updateStatusDisplay(Connection.ConnectionStatus connectionStatus) {
        Button but = mViewHolder.connectionButton;
        int colorBg = -1;
        //int colorText = -1;
        but.setClickable(false);
        this.connectionStatus = connectionStatus;
        switch (connectionStatus) {
            case ERROR:
            case NONE:
            case DISCONNECTED:
                but.setEnabled(true);
                but.setSelected(false);
                but.setText(this.getString(R.string.connection));
          //      colorBg = R.color.colorPrimaryBlue;
                //colorText = R.color.white;
                break;
            case CONNECTED:
                but.setEnabled(true);
                but.setSelected(true);
                but.setText(this.getString(R.string.disconnection));
         //       colorBg = R.color.colorPrimaryDark;
                //colorText = R.color.white;
                break;
            case CONNECTING:
                but.setEnabled(false);
                but.setSelected(false);
                but.setText(this.getString(R.string.connecting));
                break;
            case DISCONNECTING:
                but.setEnabled(false);
                but.setSelected(true);
                but.setText(this.getString(R.string.disconnection));
                break;
        }
        but.setClickable(true);

        if (colorBg>=0) {
            but.getBackground().setColorFilter(ContextCompat.getColor(getContext(), colorBg), PorterDuff.Mode.MULTIPLY);
            //but.setTextColor(ContextCompat.getColor(getContext(), colorText));
        }
//        mViewHolder.connectionButton.setSelected(isConnected);
//        if (isConnected) mViewHolder.connectionButton.setText(this.getString(R.string.disconnection));

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void mqttConnectOrDisconnect();

    }

    /**
     * Just an Holder to help manipulation the layout's view components
     */
    class ViewHolder {
        Button connectionButton;
        TextView temperatureTextView;
        SeekBar temperatureSeekBar;
        TextView hydrometryTextView;
        SeekBar hydrometrySeekBar;
        TextView revminTextView;
        SeekBar revminSeekBar;
        TextView CO2TextView;
        SeekBar CO2SeekBar;
        TextView pressureTextView;
        SeekBar pressureSeekBar;
        SwitchCompat telemetryModeSwitch;
        SwitchCompat locationModeSwitch;
        SwitchCompat doorStateSwitch;
        TextView locationTextView;
        TextView configRefreshTextView;
        TextView configLogLevelText;
        ImageView sendingDataImageView;


    }

}
