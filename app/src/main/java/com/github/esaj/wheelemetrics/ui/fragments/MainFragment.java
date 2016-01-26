package com.github.esaj.wheelemetrics.ui.fragments;


import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothchat.DeviceListActivity;
import com.github.esaj.wheelemetrics.Preferences;
import com.github.esaj.wheelemetrics.bluetooth.BluetoothService;
import com.github.esaj.wheelemetrics.bluetooth.Constants;
import com.github.esaj.wheelemetrics.bluetooth.UIBackgroundColorWarningBluetoothObserver;
import com.github.esaj.wheelemetrics.data.LoggableData;
import com.github.esaj.wheelemetrics.ui.graph.GraphDataControl;
import com.github.esaj.wheelemetrics.utils.ConversionUtils;
import com.github.esaj.wheelemetrics.utils.StringUtils;
import com.jjoe64.graphview.GraphView;

import java.lang.ref.WeakReference;

import test.ej.wheelemetricsproto.R;

/**
 * @author esaj
 */
public class MainFragment extends Fragment
{
    public static final String TAG = "MainFragment";

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;


    private View mainView;
    private EditText outEditText;
    private Button sendButton;

    private Button clearGraphButton;
    private Spinner graphSpinner;

    private long graphIndex = 0;
    private GraphDataControl graphControl;
    private int graphItemIndex = 0;

    /**
     * Name of the connected device
     */
    private String connectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer outStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter bluetoothAdapter = null;

    private BluetoothService bluetoothService;

    private Context context;

    private final CallbackHandler handler;

    public MainFragment()
    {
        super();
        handler = new CallbackHandler(this);
    }

    private ServiceConnection bluetoothServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder binder)
        {
            BluetoothService.BluetoothBinder btBinder = (BluetoothService.BluetoothBinder) binder;
            bluetoothService = btBinder.getService();
            bluetoothService.setHandler(handler);

            try
            {
                bluetoothService.registerObserver(new UIBackgroundColorWarningBluetoothObserver(MainFragment.this.getActivity(), mainView));
            }
            catch(Exception e)
            {
                Log.e(TAG, "Error occurred while registering GotwayBluetoothObserver", e);
            }
        }

        public void onServiceDisconnected(ComponentName className)
        {
            bluetoothService = null;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        context = getActivity().getApplicationContext();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if(bluetoothAdapter == null)
        {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

    }

    @Override
    public void onStart()
    {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            setupUI();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        detachBluetooth();
    }

    public void detachBluetooth()
    {
        if(bluetoothService != null)
        {
            bluetoothService.stop();
            bluetoothService = null;
        }

        if(bluetoothServiceConnection != null)
        {
            context.unbindService(bluetoothServiceConnection);
            bluetoothServiceConnection = null;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if(bluetoothService != null)
        {
            // Only if the state is STATE_NOT_CONNECTED, do we know that we haven't started already
            if(bluetoothService.getState() == BluetoothService.State.NOT_CONNECTED)
            {
                // Start the BluetoothService
                bluetoothService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.activity_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        mainView = view.getRootView().findViewById(R.id.mainLayout);
        outEditText = (EditText)view.findViewById(R.id.edit_text_out);
        sendButton = (Button)view.findViewById(R.id.button_send);
        clearGraphButton = (Button)view.findViewById(R.id.button_cleargraph);
        graphSpinner = (Spinner)view.findViewById(R.id.graph_spinner);

        GraphView graphView = (GraphView)view.findViewById(R.id.graph);
        graphView.getViewport().setBackgroundColor(Color.argb(255, 0, 0, 0));
        graphControl = new GraphDataControl(graphView);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupUI()
    {
        Log.d(TAG, "setupUI()");

        // Initialize the compose field with a listener for the return key
        outEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Send a message using content of the edit text widget
                View view = getView();
                if(null != view)
                {
                    TextView textView = (TextView)view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        clearGraphButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                graphIndex = 0;
                graphControl.clearGraph();
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.chart_spinner_values, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        graphSpinner.setAdapter(adapter);
        graphSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                graphIndex = 0;
                graphItemIndex = (int)id;
                clearGraphButton.callOnClick();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                //?
            }
        });

        // Bind to the BluetoothService to perform bluetooth connections
        context.bindService(new Intent(context, BluetoothService.class), bluetoothServiceConnection, Context.BIND_AUTO_CREATE);

        // Initialize the buffer for outgoing messages
        outStringBuffer = new StringBuffer("");
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message)
    {
        // Check that we're actually connected before trying anything
        if(bluetoothService.getState() != BluetoothService.State.CONNECTED)
        {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if(message.length() > 0)
        {
            //Add cr + lf, if not found
            if(!message.endsWith("\r\n"))
            {
                message = message + "\r\n";
            }

            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bluetoothService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            outStringBuffer.setLength(0);
            outEditText.setText(outStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener()
    {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
        {
            // If the action is a key-up event on the return key, send the message
            if(actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP)
            {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId)
    {
        FragmentActivity activity = getActivity();
        if(null == activity)
        {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if(null == actionBar)
        {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle)
    {
        FragmentActivity activity = getActivity();
        if(null == activity)
        {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if(null == actionBar)
        {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private static class CallbackHandler extends Handler
    {
        private final WeakReference<MainFragment> ref;
        public CallbackHandler(MainFragment ref)
        {
            this.ref = new WeakReference<MainFragment>(ref);
        }

        @Override
        public void handleMessage(Message msg)
        {
            MainFragment main = ref.get();
            if(main == null)
            {
                Log.w(TAG, "Handler has lost reference to MainFragment");
                return;
            }

            if(main.isDetached() || !main.isAdded())
            {
                return;
            }

            switch(msg.what)
            {
                case Constants.MESSAGE_STATE_CHANGE:
                    BluetoothService.State state = BluetoothService.State.getByOrdinal(msg.arg1);
                    switch(state)
                    {
                        case CONNECTED:
                            main.setStatus(main.getString(R.string.title_connected_to, main.connectedDeviceName));
                            break;
                        case CONNECTING:
                            main.setStatus(R.string.title_connecting);
                            break;
                        case NOT_CONNECTED:
                            main.setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;

                case Constants.MESSAGE_WRITE:
                    break;

                case Constants.MESSAGE_DATA_READ:
                    LoggableData data = (LoggableData)msg.obj;

                    View ownView = main.getView();
                    if(ownView == null)
                    {
                        break;
                    }
                    View rootView = ownView.getRootView();
                    if(rootView != null)
                    {
                        TextView view = (TextView)rootView.findViewById(R.id.speed);

                        if(Preferences.isImperialUnits())
                        {
                            String text = StringUtils.roundDoubleDecimals(ConversionUtils.kilometersToMiles(data.getSpeed() * Preferences.getSpeedCorrectionFactor()), 2);
                            view.setText(text + "mph");
                        }
                        else
                        {
                            view.setText(StringUtils.roundDoubleDecimals(data.getSpeed() * Preferences.getSpeedCorrectionFactor(), 2) + "km/h");
                        }

                        view = (TextView)rootView.findViewById(R.id.voltage);
                        view.setText(data.getVoltageString() + "V");

                        view = (TextView)rootView.findViewById(R.id.current);
                        view.setText(StringUtils.roundDoubleDecimals(data.getCurrent() * Preferences.getCurrentCorrectionFactor(), 2) + "A");

                        view = (TextView)rootView.findViewById(R.id.mbtemp);
                        if(Preferences.isImperialUnits())
                        {
                            String text = StringUtils.roundDoubleDecimals(ConversionUtils.celsiusToFahrenheit(data.getTemperature()), 2);
                            view.setText(text + "° F");
                        }
                        else
                        {
                            view.setText(data.getTemperatureString() + "\u00B0 C");
                        }

                        view = (TextView)rootView.findViewById(R.id.power);
                        view.setText((int)(data.getVoltage() * data.getCurrent() * Preferences.getCurrentCorrectionFactor()) + "W");

                        view = (TextView)rootView.findViewById(R.id.trip);
                        if(Preferences.isImperialUnits())
                        {
                            String text = StringUtils.roundDoubleDecimals(ConversionUtils.kilometersToMiles(data.getTrip()), 3);
                            view.setText(text + "mi");
                        }
                        else
                        {
                            view.setText(data.getTripString() + "km");
                        }

                        view = (TextView)rootView.findViewById(R.id.odo);
                        if(Preferences.isImperialUnits())
                        {
                            String text = StringUtils.roundDoubleDecimals(ConversionUtils.kilometersToMiles(data.getOdo()), 2);
                            view.setText(text + "mi");
                        }
                        else
                        {
                            view.setText(data.getOdoString() + "km");
                        }
                    }

                    //Update graph, if possible
                    try
                    {
                        //Yes this is ugly... ;)
                        if(((CheckBox)main.getView().findViewById(R.id.checkbox_chart)).isChecked())
                        {
                            double value = 0;
                            switch(main.graphItemIndex)
                            {
                                case 0:
                                    value = data.getVoltage();
                                    break;
                                case 1:
                                    value = data.getSpeed() * Preferences.getSpeedCorrectionFactor();
                                    if(Preferences.isImperialUnits())
                                    {
                                        value = ConversionUtils.kilometersToMiles(value);
                                    }
                                    break;
                                case 2:
                                    value = data.getTrip();
                                    if(Preferences.isImperialUnits())
                                    {
                                        value = ConversionUtils.kilometersToMiles(value);
                                    }
                                    break;
                                case 3:
                                    value = data.getCurrent() * Preferences.getCurrentCorrectionFactor();
                                    break;
                                case 4:
                                    value = data.getTemperature();
                                    if(Preferences.isImperialUnits())
                                    {
                                        value = ConversionUtils.celsiusToFahrenheit(value);
                                    }
                                    break;
                                case 5:
                                    value = data.getOdo();
                                    if(Preferences.isImperialUnits())
                                    {
                                        value = ConversionUtils.kilometersToMiles(value);
                                    }
                                    break;
                                case 6:
                                    value = data.getVoltage() * data.getCurrent();
                            }

                            main.graphControl.addDataPoint(main.graphIndex++, value, ((CheckBox)main.getView().findViewById(R.id.checkbox_scroll)).isChecked());
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG, "Exception updating graph", e);
                    }

                    break;

                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    main.connectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(main.context, "Connected to " + main.connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(main.context, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if(resultCode == Activity.RESULT_OK)
                {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if(resultCode == Activity.RESULT_OK)
                {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if(resultCode == Activity.RESULT_OK)
                {
                    // Bluetooth is now enabled, so set up a chat session
                    setupUI();
                }
                else
                {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure)
    {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        bluetoothService.connect(device, secure);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        switch(item.getItemId())
//        {
//            case R.id.secure_connect_scan:
//            {
//                // Launch the DeviceListActivity to see devices and do scan
//                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
//                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
//                return true;
//            }
//            case R.id.insecure_connect_scan:
//            {
//                // Launch the DeviceListActivity to see devices and do scan
//                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
//                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
//                return true;
//            }
//        }
//        return false;
//    }
}
