
package it.polimi.deib.p2pchat.discovery.chatmessages;
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.polimi.deib.p2pchat.R;
import it.polimi.deib.p2pchat.discovery.DestinationDeviceTabList;
import it.polimi.deib.p2pchat.discovery.MainActivity;
import it.polimi.deib.p2pchat.discovery.TabFragment;
import it.polimi.deib.p2pchat.discovery.WordGenerator;
import it.polimi.deib.p2pchat.discovery.socketmanagers.ConnectionManager;
import it.polimi.deib.p2pchat.discovery.services.ServiceList;
import it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue;
import it.polimi.deib.p2pchat.discovery.services.WiFiP2pService;
import it.polimi.deib.p2pchat.discovery.utilities.DataContainer;
import it.polimi.deib.p2pchat.discovery.utilities.Enums;
import lombok.Getter;
import lombok.Setter;

/**
 * Class fragment that handles chat related UI which includes a list view for messages
 * and a message entry field with send button.
 * <p></p>
 * Created by Stefano Cappa on 04/02/15, based on google code samples.
 */
public class WiFiChatFragment extends Fragment {

    private static final String TAG = "WiFiChatFragment";

    @Getter @Setter private Integer tabNumber;
    @Getter @Setter private static boolean firstStartSendAddress;
    @Getter @Setter private boolean grayScale = true;
    @Getter private final List<String> items = new ArrayList<>();

    private TextView chatLine;

    @Getter @Setter private ConnectionManager connectionManager;
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
    public static WiFiChatFragment newInstance() {
        return new WiFiChatFragment();
    }

    /**
     * Default Fragment constructor.
     */
    public WiFiChatFragment() {}


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

        if (connectionManager != null) {
            if (!connectionManager.isDisable()) {
                connectionManager.write((combineMessages).getBytes());
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

    public boolean CheckWord(String answer){
        if (answer == null || ((MainActivity) getActivity()).wordToSolve == null)
            return false;
        if(((MainActivity) getActivity()).wordToSolve.toUpperCase().equals(answer.toUpperCase()))
            return true;
        return false;
    }

    public void reSendCustomMessage(String message)
    {
        if (connectionManager != null) {
            if (!connectionManager.isDisable()) {
                Log.d(TAG, "chatmanager state: enable");

                String deviceName = ((MainActivity)getActivity()).deviceName;
                for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                {
                    DataContainer dC = new DataContainer(deviceName, message, Enums.RequestTypes.CHAT_MESSAGE);
                    ((MainActivity)getActivity()).users.get(i).write(dC.toByteArray());
                }
                //connectionManager.write(chatLine.getText().toString().getBytes());

                String winnerName = message.substring(0, message.indexOf(":"));
                String answer = message.substring(message.indexOf(":") + 2, message.length());;
                //Check for answer
                if (CheckWord(answer))
                {
                    String systemMessage = "SYSTEM - Gracz '" + winnerName + "' odgadł hasło '" + answer + "'";
                    deviceName = ((MainActivity)getActivity()).deviceName;
                    for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                    {
                        DataContainer dC = new DataContainer(deviceName, systemMessage, Enums.RequestTypes.CHAT_MESSAGE);
                        ((MainActivity)getActivity()).users.get(i).write(dC.toByteArray());
                    }
                    Fragment f = ((MainActivity)getActivity()).tabFragment.getChatFragmentByTab(2);
                    ((GameFragment)f).AddMessageToChat(systemMessage);
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
                                // host
                                ((MainActivity)getActivity()).CreateGameRoom();
                                ((MainActivity)getActivity()).CreateRanking(((MainActivity)getActivity()).playerList);
                                String hostDeviceName = ((MainActivity)getActivity()).deviceName;
                                GameFragment hostGameFragment = (GameFragment) ((MainActivity) getActivity()).tabFragment.getChatFragmentByTab(2);

                                ((MainActivity) getActivity()).wordToSolve = ((MainActivity)getActivity()).wordGenerator.GetWord();
                                Random random = new Random();
                                String playerName = ((MainActivity)getActivity()).playerList.get(random.nextInt(((MainActivity)getActivity()).playerList.size())).playerName;
                                Log.d("PLAYER: ",playerName);
                                if(playerName.equals(hostDeviceName)){
                                    hostGameFragment.setWord(((MainActivity) getActivity()).wordToSolve);
                                    for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                                        ((MainActivity)getActivity()).users.get(i).write(new DataContainer(Enums.RequestTypes.START_GAME).toByteArray());
                                        Fragment f = ((MainActivity)getActivity()).tabFragment.getChatFragmentByTab(2);
                                        ((GameFragment)f).setDrawingPlayer(hostDeviceName);
                                } else {
                                    for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                                    {
                                        ((MainActivity)getActivity()).users.get(i).write(new DataContainer(Enums.RequestTypes.START_GAME).toByteArray());
                                        ((MainActivity)getActivity()).users.get(i).write((new DataContainer(Enums.RequestTypes.CHOOSE_PLAYER,((MainActivity) getActivity()).wordToSolve,playerName)).toByteArray());
                                    }
                                }
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

                                for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                                {
                                    DataContainer dC = new DataContainer(((MainActivity)getActivity()).deviceName, messageToSend, Enums.RequestTypes.CHAT_MESSAGE);
                                    ((MainActivity)getActivity()).users.get(i).write(dC.toByteArray());
                                }
                                //connectionManager.write(chatLine.getText().toString().getBytes());
                            } else {
                                Log.d(TAG, "chatmanager disabled, trying to send a message with tabNum= " + tabNumber);

                                addToWaitingToSendQueueAndTryReconnect();
                            }

                            //Jeśli wiadomość wysyła klient to doda ją do czatu dopiero po odesłaniu jej przez hosta
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
