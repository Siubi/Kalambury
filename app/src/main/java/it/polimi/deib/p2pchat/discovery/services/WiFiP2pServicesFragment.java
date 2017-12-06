
package it.polimi.deib.p2pchat.discovery.services;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.polimi.deib.p2pchat.R;
import it.polimi.deib.p2pchat.discovery.model.LocalP2PDevice;

import it.polimi.deib.p2pchat.discovery.MainActivity;
import it.polimi.deib.p2pchat.discovery.services.localdeviceguielement.LocalDeviceDialogFragment;
import lombok.Getter;


public class WiFiP2pServicesFragment extends Fragment implements


        WiFiServicesAdapter.ItemClickListener,


        LocalDeviceDialogFragment.DialogConfirmListener {

    private static final String TAG = "WiFiP2pServicesFragment";

    private RecyclerView mRecyclerView;
    @Getter private WiFiServicesAdapter mAdapter;

    private TextView localDeviceNameText;


    public interface DeviceClickListener {
        public void tryToConnectToAService(int position);
    }


    public static WiFiP2pServicesFragment newInstance() {
        return new WiFiP2pServicesFragment();
    }


    public WiFiP2pServicesFragment() {}



    @Override
    public void changeLocalDeviceName(String deviceName) {
        if(deviceName==null) {
            return;
        }

        String formattedDeviceName = deviceName;
        if (formattedDeviceName.length() > "[Phone] ".length() && formattedDeviceName.contains("[Phone] "))
        {
            formattedDeviceName = formattedDeviceName.substring("[Phone] ".length());
        }

        localDeviceNameText.setText(formattedDeviceName);
        ((MainActivity)getActivity()).setDeviceNameWithReflection(formattedDeviceName);
        ((MainActivity)getActivity()).deviceName = localDeviceNameText.getText().toString();
    }


    @Override
    public void itemClicked(View view) {
        int clickedPosition = mRecyclerView.getChildPosition(view);

        if(clickedPosition>=0) {
            ((DeviceClickListener) getActivity()).tryToConnectToAService(clickedPosition);
        }
    }


    public void showLocalDeviceGoIcon(){
        if(getView() !=null && getView().findViewById(R.id.go_logo)!=null && getView().findViewById(R.id.i_am_a_go_textview)!=null) {
            ImageView goLogoImageView = (ImageView) getView().findViewById(R.id.go_logo);
            TextView i_am_a_go_textView = (TextView) getView().findViewById(R.id.i_am_a_go_textview);

            goLogoImageView.setImageDrawable(getResources().getDrawable(R.drawable.go_logo));
            goLogoImageView.setVisibility(View.VISIBLE);
            i_am_a_go_textView.setVisibility(View.VISIBLE);
        }
    }


    public void resetLocalDeviceIpAddress() {
        if(getView()!=null && getView().findViewById(R.id.localDeviceIpAddress)!=null) {
            TextView ipAddress = (TextView) getView().findViewById(R.id.localDeviceIpAddress);
            ipAddress.setText(getResources().getString(R.string.ip_not_available));
        }
    }


    public void setLocalDeviceIpAddress(String ipAddress) {
        if(getView()!=null && getView().findViewById(R.id.localDeviceIpAddress)!=null) {
            TextView ipAddressTextView = (TextView) getView().findViewById(R.id.localDeviceIpAddress);
            ipAddressTextView.setText(ipAddress);
        }
    }


    public void hideLocalDeviceGoIcon() {
        if(getView()!=null && getView().findViewById(R.id.go_logo)!=null && getView().findViewById(R.id.i_am_a_go_textview)!=null) {
            ImageView goLogoImageView = (ImageView) getView().findViewById(R.id.go_logo);
            TextView i_am_a_go_textView = (TextView) getView().findViewById(R.id.i_am_a_go_textview);

            goLogoImageView.setVisibility(View.INVISIBLE);
            i_am_a_go_textView.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.services_list, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);


        mRecyclerView.setHasFixedSize(true);

        mAdapter = new WiFiServicesAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        String formattedDeviceName = LocalP2PDevice.getInstance().getLocalDevice().deviceName;
        if (formattedDeviceName.length() > "[Phone] ".length() && formattedDeviceName.contains("[Phone] "))
        {
            formattedDeviceName = formattedDeviceName.substring("[Phone] ".length());
        }
        LocalP2PDevice.getInstance().getLocalDevice().deviceName = formattedDeviceName;

        localDeviceNameText = (TextView) rootView.findViewById(R.id.localDeviceName);
        localDeviceNameText.setText(LocalP2PDevice.getInstance().getLocalDevice().deviceName);
        ((MainActivity)getActivity()).deviceName = localDeviceNameText.getText().toString();

        TextView localDeviceAddressText = (TextView) rootView.findViewById(R.id.localDeviceAddress);
        localDeviceAddressText.setText(LocalP2PDevice.getInstance().getLocalDevice().deviceAddress);

        CardView cardViewLocalDevice = (CardView) rootView.findViewById(R.id.cardviewLocalDevice);
        cardViewLocalDevice.setOnClickListener(new OnClickListenerLocalDevice(this));

        return rootView;
    }



    class OnClickListenerLocalDevice implements View.OnClickListener {

        private final Fragment fragment;

        public OnClickListenerLocalDevice(Fragment fragment1) {
            fragment = fragment1;
        }

        @Override
        public void onClick(View v) {
            LocalDeviceDialogFragment localDeviceDialogFragment = (LocalDeviceDialogFragment) getFragmentManager()
                    .findFragmentByTag("localDeviceDialogFragment");

            if (localDeviceDialogFragment == null) {
                localDeviceDialogFragment = LocalDeviceDialogFragment.newInstance();
                localDeviceDialogFragment.setTargetFragment(fragment, 0);

                localDeviceDialogFragment.show(getFragmentManager(), "localDeviceDialogFragment");
                getFragmentManager().executePendingTransactions();
            }
        }
    }
}


