package it.polimi.deib.p2pchat.discovery.chatmessages;

/**
 * Created by Krzysiek on 2017-12-05.
 */

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
import it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue;
import it.polimi.deib.p2pchat.discovery.services.ServiceList;
import it.polimi.deib.p2pchat.discovery.services.WiFiP2pService;
import it.polimi.deib.p2pchat.discovery.socketmanagers.ChatManager;
import lombok.Getter;
import lombok.Setter;

/*
 * Copyright (C) 2015-2016 Stefano Cappa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import it.polimi.deib.p2pchat.discovery.socketmanagers.ChatManager;
import it.polimi.deib.p2pchat.discovery.services.ServiceList;
import it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue;
import it.polimi.deib.p2pchat.discovery.services.WiFiP2pService;
import lombok.Getter;
import lombok.Setter;

public class RankingFragment extends Fragment {

    private static final String TAG = "RankingFragment";

    @Getter
    @Setter
    private Integer tabNumber;
    @Getter @Setter private static boolean firstStartSendAddress;
    @Getter @Setter private boolean grayScale = true;
    @Getter private final List<String> items = new ArrayList<>();

    private TextView chatLine;

    @Getter @Setter private ChatManager chatManager;
    private WiFiChatMessageListAdapter adapter = null;

    /**
     * Callback interface to call methods reconnectToService in {@link it.polimi.deib.p2pchat.discovery.MainActivity}.
     * MainActivity implements this interface.
     */
    public interface AutomaticReconnectionListener {
        public void reconnectToService(WiFiP2pService wifiP2pService);
    }

    /**
     * Method to obtain a new Fragment's instance.
     * @return This Fragment instance.
     */
    public static RankingFragment newInstance() {
        return new RankingFragment();
    }

    /**
     * Default Fragment constructor.
     */
    public RankingFragment() {}


    /**
     * Method that combines all the messages inside the
     * {@link it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue}
     * in one String and pass this one to the {@link it.polimi.deib.p2pchat.discovery.socketmanagers.ChatManager}
     * to send the message to other devices.
     */
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

        if (chatManager != null) {
            if (!chatManager.isDisable()) {
                chatManager.write((combineMessages).getBytes());
                WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber).clear();
            } else {
                Log.d(TAG, "Chatmanager disabled, impossible to send the queued combined message");
            }

        }
    }


    /**
     * Method to add a message to the Fragment's listView and notifies this update to
     * {@link it.polimi.deib.p2pchat.discovery.chatmessages.WiFiChatMessageListAdapter}.
     * @param readMessage String that represents the message to add.
     */
    public void pushMessage(String readMessage) {
        items.add(readMessage);
        adapter.notifyDataSetChanged();
    }

    /**
     * Method that updates the {@link it.polimi.deib.p2pchat.discovery.chatmessages.WiFiChatMessageListAdapter}.
     */
    public void updateChatMessageListAdapter() {
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Method that add the text in the chatLine EditText to the WaitingToSendQueue and try to reconnect
     * to the service associated to the device of this tab, with index tabNumber.
     */
    private void addToWaitingToSendQueueAndTryReconnect() {
        //add message to the waiting to send queue
        WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber).add(chatLine.getText().toString());

        //try to reconnect
        WifiP2pDevice device = DestinationDeviceTabList.getInstance().getDevice(tabNumber - 1);
        if(device!=null) {
            WiFiP2pService service = ServiceList.getInstance().getServiceByDevice(device);
            Log.d(TAG, "device address: " + device.deviceAddress + ", service: " + service);

            //call reconnectToService in MainActivity
            ((AutomaticReconnectionListener) getActivity()).reconnectToService(service);

        } else {
            Log.d(TAG,"addToWaitingToSendQueueAndTryReconnect device == null, i can't do anything");
        }
    }

    public void reSendCustomMessage(String message)
    {
        if (chatManager != null) {
            if (!chatManager.isDisable()) {
                Log.d(TAG, "chatmanager state: enable");

                //send message to the ChatManager's outputStream.
                for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                {
                    ((MainActivity)getActivity()).users.get(i).write(message.getBytes());
                }
                //chatManager.write(chatLine.getText().toString().getBytes());
            } else {
                Log.d(TAG, "chatmanager disabled, trying to send a message with tabNum= " + tabNumber);

                addToWaitingToSendQueueAndTryReconnect();
            }
        } else {
            Log.d(TAG, "chatmanager is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ranking_window, container, false);

        return view;
    }


}

