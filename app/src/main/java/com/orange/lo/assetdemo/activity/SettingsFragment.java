package com.orange.lo.assetdemo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.orange.lo.assetdemo.BuildConfig;
import com.orange.lo.assetdemo.MyApplication;
import com.orange.lo.assetdemo.R;
import com.orange.lo.assetdemo.AppPreferences;
import com.orange.lo.assetdemo.utils.LinkedHashMapAdapter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = SettingsFragment.class.getName();

    private OnFragmentInteractionListener mListener;
    private ViewHolder mViewHolder;

    Map<Integer, String> servers = new LinkedHashMap<>();
    Map<Integer, String> protocols = new LinkedHashMap<>();

    public SettingsFragment() {
        // Required empty public constructor
        if (BuildConfig.FLAVOR.equals("localProxy")) {
            servers.put(0,"Local HaProxy");
        }
        else {
            servers.put(0, "Orange M2M Prod.");
            //servers.put(1,"Orange M2M Stag.");
            //servers.put(2,"Azure for IoT");
        }
        protocols.put(0,"MQTT");
        protocols.put(1,"MQTT w/ SSL");
     //   protocols.put(2,"WebSocket");
     //   protocols.put(3,"WebSocket w/ SSL");
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        //Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//        }
    }

    LinkedHashMapAdapter serversAdapter;
    LinkedHashMapAdapter protocolsAdapter;

    MyApplication myApplication = MyApplication.getInstance();
    AppPreferences appPreferences = myApplication.getAppPreferences();



    @Override
    public void onResume() {
        super.onResume();
        mViewHolder.serversSpinner.setOnItemSelectedListener(this);
        mViewHolder.protocolsSpinner.setOnItemSelectedListener(this);
        mViewHolder.resetAppSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.forceDisconnect();
                myApplication.clearData();
                reloadAppPreferences();
            }
        });

        mViewHolder.qrcodeApiKeylayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runQrScan();
            }
        });
        mViewHolder.manualApiKeyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlertDialogForApiKey();

            }
        });

        reloadAppPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewHolder.serversSpinner.setOnItemSelectedListener(null);
        mViewHolder.protocolsSpinner.setOnItemSelectedListener(null);
        mViewHolder.resetAppSettingsButton.setOnClickListener(null);
        mViewHolder.qrcodeApiKeylayout.setOnClickListener(null);
        mViewHolder.manualApiKeyLayout.setOnClickListener(null);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mViewHolder = new ViewHolder();

        mViewHolder.serversSpinner = (Spinner) view.findViewById(R.id.mqtt_server_sp);
        mViewHolder.protocolsSpinner  = (Spinner) view.findViewById(R.id.protocol_sp);

        mViewHolder.apiKeyTextView = (TextView) view.findViewById(R.id.apikey_tv);
        mViewHolder.usernameTextView = (TextView) view.findViewById(R.id.username_tv);
        mViewHolder.clientIdTextView = (TextView) view.findViewById(R.id.clientid_tv);
        mViewHolder.assetIdInfoTextView = (TextView) view.findViewById(R.id.assetId_info_tv);
        mViewHolder.modelInfoTextView = (TextView) view.findViewById(R.id.model_info_tv);
        mViewHolder.versionInfoTextView = (TextView) view.findViewById(R.id.version_info_tv);

        mViewHolder.resetAppSettingsButton = (Button) view.findViewById(R.id.app_settings_btn);

        mViewHolder.qrcodeApiKeylayout = (Button) view.findViewById(R.id.qrcode_apikey_button);
        mViewHolder.manualApiKeyLayout = (Button) view.findViewById(R.id.manual_apikey_button);

        serversAdapter = new LinkedHashMapAdapter<>(getContext(), android.R.layout.simple_spinner_item, (LinkedHashMap<Integer, String>)servers);
        serversAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        protocolsAdapter = new LinkedHashMapAdapter<>(getContext(), android.R.layout.simple_spinner_item, (LinkedHashMap<Integer, String>)protocols);
        protocolsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mViewHolder.serversSpinner.setAdapter(serversAdapter);
        mViewHolder.protocolsSpinner.setAdapter(protocolsAdapter);

        return view;

    }
    /**
     * Function that open AlertDialog with custom layout to allow displaying of an EditText
     *
     */
    private void openAlertDialogForApiKey() {
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View alertDialogView = factory.inflate(R.layout.alertdialog_edit_text, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(alertDialogView);

        final EditText et = (EditText) alertDialogView.findViewById(R.id.edit_alert_dialog);
        et.setText(appPreferences.getApiKey());

        builder.setTitle(this.getString(R.string.api_key))
                .setMessage(this.getString(R.string.please_insert_api_key))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                appPreferences.setApiKey(et.getText().toString().trim());
                                reloadAppPreferences();
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);

        builder.show();
    }

    /**
     * Manage the QR Scan using library com.journeyapps:zxing-android-embedded
     * This allows to use the camera in portrait and does not require to install third party
     * application com.google.zxing.client.android
     *
     */
    public void runQrScan(){
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.CAMERA}, MainActivity.PERMISSION_REQUEST_CAMERA);
        } else {
            Log.i(TAG, "Launching QR scan");
            //IntentIntegrator integrator= new IntentIntegrator(getActivity());
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            //integrator.setPrompt("Scan a barcode");
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            //Manage return from IntentIntegrator Scan activity
            if(result.getContents() == null) {
                Notify.toast(getContext(), this.getString(R.string.scan_cancelled), Toast.LENGTH_LONG);
            } else {
                Notify.toast(getContext(), this.getString(R.string.api_key_scanned) + result.getContents(), Toast.LENGTH_LONG);
                appPreferences.setApiKey(result.getContents());
                reloadAppPreferences();
            }
        } else {
            // Manage own activity results
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }


    private void reloadAppPreferences() {
        String apiKey = appPreferences.getApiKey();
        Log.d(TAG, "Api Key : "+apiKey);
        if (TextUtils.isEmpty(apiKey)) apiKey = getContext().getString(R.string.api_key_empty);
        appPreferences.initAssetId();
        // MQTT Login values
        mViewHolder.apiKeyTextView.setText(apiKey);
        mViewHolder.usernameTextView.setText(appPreferences.getUsernameDeviceMode());
        mViewHolder.clientIdTextView.setText(appPreferences.getShortClientId());
        mViewHolder.assetIdInfoTextView.setText(appPreferences.getAssetId());

        // MQTT Server / Protocol values
        mViewHolder.serversSpinner.setSelection(serversAdapter.getPosition(appPreferences.getMqttServerSettings()));
        mViewHolder.protocolsSpinner.setSelection(protocolsAdapter.getPosition(appPreferences.getMqttProtocolKey()));

        // Device Status
        mViewHolder.modelInfoTextView.setText(myApplication.getAsset().getDeviceStatus().getInfo().getModel());
        mViewHolder.versionInfoTextView.setText(myApplication.getAsset().getDeviceStatus().getInfo().getVersion());

    }
    

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
//        else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        LinkedHashMapAdapter<Integer, String> lhma;
        switch(adapterView.getId()) {
            case R.id.mqtt_server_sp:
                lhma =  (LinkedHashMapAdapter<Integer, String>) adapterView.getAdapter();
                appPreferences.setMqttServerKey(lhma.getItem(position).getKey());
                break;
            case R.id.protocol_sp:
                lhma =  (LinkedHashMapAdapter<Integer, String>) adapterView.getAdapter();
                appPreferences.setMqttProtocolKey(lhma.getItem(position).getKey());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton sw, boolean checked) {
        switch(sw.getId()) {
            default:
                break;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        public void forceDisconnect();
    }

    /**
     * Just an Holder to help manipulation the layout's view components
     */
    class ViewHolder {
        TextView apiKeyTextView;
        TextView usernameTextView;
        TextView clientIdTextView;
        TextView assetIdInfoTextView;
        TextView modelInfoTextView;
        TextView versionInfoTextView;
        Spinner serversSpinner;
        Spinner protocolsSpinner;
        Button resetAppSettingsButton;
        Button qrcodeApiKeylayout;
        Button manualApiKeyLayout;


    }
}
