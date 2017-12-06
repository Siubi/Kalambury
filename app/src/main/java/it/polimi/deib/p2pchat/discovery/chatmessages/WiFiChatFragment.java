
package it.polimi.deib.p2pchat.discovery.chatmessages;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.polimi.deib.p2pchat.R;
import it.polimi.deib.p2pchat.discovery.DestinationDeviceTabList;
import it.polimi.deib.p2pchat.discovery.MainActivity;
import it.polimi.deib.p2pchat.discovery.socketmanagers.ConnectionManager;
import it.polimi.deib.p2pchat.discovery.services.ServiceList;
import it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue;
import it.polimi.deib.p2pchat.discovery.services.WiFiP2pService;
import lombok.Getter;
import lombok.Setter;


public class WiFiChatFragment extends Fragment {

    private static final String TAG = "WiFiChatFragment";

    @Getter @Setter private Integer tabNumber;
    @Getter @Setter private static boolean firstStartSendAddress;
    @Getter @Setter private boolean grayScale = true;
    @Getter private final List<String> items = new ArrayList<>();

    private TextView chatLine;

    @Getter @Setter private ConnectionManager connectionManager;
    private WiFiChatMessageListAdapter adapter = null;


    public interface AutomaticReconnectionListener {
        public void reconnectToService(WiFiP2pService wifiP2pService);
    }


    public static WiFiChatFragment newInstance() {
        return new WiFiChatFragment();
    }


    public WiFiChatFragment() {}



    public void sendForcedWaitingToSendQueue() {

        Log.d(TAG, "sendForcedWaitingToSendQueue() called");

        String combineMessages = "";
        List<String> listCopy = WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber);
        for (String message : listCopy) {
            if(!message.equals("") && !message.equals("\n")  ) {
                combineMessages = combineMessages + "\n" + message;
            }
        }
        combineMessages = combineMessages + "\n";

        Log.d(TAG, "Queued message to send: " + combineMessages);

        if (connectionManager != null) {
            if (!connectionManager.isDisable()) {
                connectionManager.write((combineMessages).getBytes());
                WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber).clear();
            } else {
                Log.d(TAG, "Chatmanager disabled, impossible to send the queued combined message");
            }

        }
    }



    public void pushMessage(String readMessage) {
        items.add(readMessage);
        adapter.notifyDataSetChanged();
    }


    public void updateChatMessageListAdapter() {
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }


    private void addToWaitingToSendQueueAndTryReconnect() {

        WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber).add(chatLine.getText().toString());


        WifiP2pDevice device = DestinationDeviceTabList.getInstance().getDevice(tabNumber - 1);
        if(device!=null) {
            WiFiP2pService service = ServiceList.getInstance().getServiceByDevice(device);
            Log.d(TAG, "device address: " + device.deviceAddress + ", service: " + service);


            ((AutomaticReconnectionListener) getActivity()).reconnectToService(service);

        } else {
            Log.d(TAG,"addToWaitingToSendQueueAndTryReconnect device == null, i can't do anything");
        }
    }

    public void reSendCustomMessage(String message)
    {
        if (connectionManager != null) {
            if (!connectionManager.isDisable()) {
                Log.d(TAG, "chatmanager state: enable");


                for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                {
                    ((MainActivity)getActivity()).users.get(i).write(message.getBytes());
                }

            } else {
                Log.d(TAG, "chatmanager disabled, trying to send a message with tabNum= " + tabNumber);

                addToWaitingToSendQueueAndTryReconnect();
            }
        } else {
            Log.d(TAG, "chatmanager is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatmessage_list, container, false);

        Button startButton = (Button) view.findViewById(R.id.startGame);
        if (!((MainActivity)getActivity()).isGroupOwner) {
            startButton.setVisibility(View.GONE);
        }
        else
        {
            startButton.setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (connectionManager != null) {

                                for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                                {
                                    ((MainActivity)getActivity()).users.get(i).write(("startGame").getBytes());
                                }
                                ((MainActivity)getActivity()).CreateGameRoom();
                                ((MainActivity)getActivity()).CreateRanking();
                            }
                        }
                    });
        }

        chatLine = (TextView) view.findViewById(R.id.txtChatLine);
        ListView listView = (ListView) view.findViewById(R.id.list);

        adapter = new WiFiChatMessageListAdapter(getActivity(),R.id.txtChatLine, this);
        listView.setAdapter(adapter);

        view.findViewById(R.id.sendMessage).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (connectionManager != null) {

                            String messageToSend = ((MainActivity)getActivity()).deviceName + ": " + chatLine.getText().toString();

                            if (!connectionManager.isDisable()) {
                                Log.d(TAG, "chatmanager state: enable");


                                for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                                {
                                    ((MainActivity)getActivity()).users.get(i).write(messageToSend.getBytes());
                                }

                            } else {
                                Log.d(TAG, "chatmanager disabled, trying to send a message with tabNum= " + tabNumber);

                                addToWaitingToSendQueueAndTryReconnect();
                            }


                            if (((MainActivity)getActivity()).isGroupOwner)
                            {
                                pushMessage(messageToSend);
                            }
                            chatLine.setText("");
                        } else {
                            Log.d(TAG, "chatmanager is null");
                        }
                    }
                });

        return view;
    }


}
