package it.polimi.deib.p2pchat.discovery.chatmessages;

/**
 * Created by Krzysiek on 2017-12-05.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.polimi.deib.p2pchat.R;
import it.polimi.deib.p2pchat.discovery.BitmapConverters.BitmapToStringConverter;
import it.polimi.deib.p2pchat.discovery.DestinationDeviceTabList;
import it.polimi.deib.p2pchat.discovery.MainActivity;
import it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue;
import it.polimi.deib.p2pchat.discovery.services.ServiceList;
import it.polimi.deib.p2pchat.discovery.services.WiFiP2pService;
import it.polimi.deib.p2pchat.discovery.socketmanagers.ConnectionManager;
import it.polimi.deib.p2pchat.discovery.utilities.DataContainer;
import it.polimi.deib.p2pchat.discovery.utilities.Enums;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simplify.ink.InkView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import it.polimi.deib.p2pchat.R;
import it.polimi.deib.p2pchat.discovery.DestinationDeviceTabList;
import it.polimi.deib.p2pchat.discovery.MainActivity;
import it.polimi.deib.p2pchat.discovery.socketmanagers.ConnectionManager;
import it.polimi.deib.p2pchat.discovery.services.ServiceList;
import it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue;
import it.polimi.deib.p2pchat.discovery.services.WiFiP2pService;
import lombok.Getter;
import lombok.Setter;

public class GameFragment extends Fragment {

    private static final String TAG = "GameFragment";

    @Getter
    @Setter
    private Integer tabNumber;
    @Getter @Setter private static boolean firstStartSendAddress;
    @Getter @Setter private boolean grayScale = true;
    @Getter private final List<String> items = new ArrayList<>();
    private InkView ink;
    public String drawingPersonName = "";

    private TextView chatLine;

    @Getter @Setter private ConnectionManager connectionManager;
    private WiFiChatMessageListAdapter adapter = null;

    public TextView chat;
    int roundTime=45;
    TextView textTimer;
    TextView wordLabel;

    public CountDownTimer timer;
    private String word;

    public void setWord(String word){
        this.word = word.toUpperCase();
        wordLabel.setText("Hasło: "+word);
    }

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
    public static GameFragment newInstance() {
        return new GameFragment();
    }

    /**
     * Default Fragment constructor.
     */
    public GameFragment() {}


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

    public void setDrawingPlayer(String playerName)
    {
        drawingPersonName = playerName;

        if (((MainActivity)getActivity()).deviceName.contains(drawingPersonName) || drawingPersonName.contains(((MainActivity)getActivity()).deviceName)) {
            SendImageEvery();
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
        if (connectionManager != null) {
            if (!connectionManager.isDisable()) {
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

    public boolean CheckWord(String answer){
        if (answer == null || ((MainActivity) getActivity()).wordToSolve == null)
            return false;
        if(((MainActivity) getActivity()).wordToSolve.toUpperCase().equals(answer.toUpperCase()))
            return true;
        return false;
    }

    private void SetupGameChat(View view)
    {
        //To prevent keyboard popup on start
        (getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        (getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //Send chat message on keyboard enter click
        final EditText enterChatMessege = (EditText) view.findViewById(R.id.editText);
        enterChatMessege.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId== EditorInfo.IME_ACTION_DONE )) {

                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(enterChatMessege.getWindowToken(), 0);

                    //SendImage();

                    String message = ((MainActivity)getActivity()).deviceName + ": " + enterChatMessege.getText().toString();
                    String deviceName = ((MainActivity)getActivity()).deviceName;
                    //send message to all users (Client has only Host in 'users' table)
                    for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                    {
                        DataContainer dC = new DataContainer(deviceName, message, Enums.RequestTypes.CHAT_MESSAGE);
                        ((MainActivity)getActivity()).users.get(i).write(dC.toByteArray());
                    }

                    if (((MainActivity)getActivity()).isGroupOwner) {
                        AddMessageToChat(message);

                        String winnerName = message.substring(0, message.indexOf(":"));
                        String answer = message.substring(message.indexOf(":") + 2, message.length());
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
                            AddMessageToChat(systemMessage);

                            //Points update
                            ((MainActivity)getActivity()).AddPointsForPlayer(winnerName, 10);
                            ((MainActivity)getActivity()).AddPointsForPlayer(drawingPersonName, 5);
                            for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                            {
                                DataContainer dC = new DataContainer(((MainActivity)getActivity()).playerList, Enums.RequestTypes.UPDATE_PLAYERS_POINTS);
                                ((MainActivity)getActivity()).users.get(i).write(dC.toByteArray());
                            }

                            Fragment r = ((MainActivity)getActivity()).tabFragment.getChatFragmentByTab(3);
                            ((RankingFragment)r).playerList = ((MainActivity)getActivity()).playerList;
                            ((RankingFragment)r).Refresh();
                        }
                    }

                    enterChatMessege.setText("");
                    enterChatMessege.clearFocus();

                    return true;
                }
                return false;
            }
        });
        chat = (TextView) view.findViewById(R.id.chat);
        chat.setMovementMethod(new ScrollingMovementMethod());
    }

    private void SendImage()
    {
        String message = BitmapToStringConverter.Convert(ink.getBitmap());
        String deviceName = ((MainActivity)getActivity()).deviceName;
        for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
        {
            DataContainer dC = new DataContainer(deviceName, message, Enums.RequestTypes.REFRESH_IMAGE);
            byte[] test = dC.toByteArray();
            ((MainActivity)getActivity()).users.get(i).write(test);

        }
    }

    public int getInkWidth()
    {
        return ink.getWidth();
    }

    public int getInkHeight()
    {
        return ink.getHeight();
    }

    public void DrawImage(Bitmap bitmap)
    {
        ink.clear();
        ink.drawBitmap(bitmap, 0, 0, null);
        //ink.refreshDrawableState();
        //ink.forceLayout();
    }

    private void SendImageEvery()
    {
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        SendImage();
                        Thread.sleep(5000);
                    } catch (Exception v) {
                        System.out.println(v);
                    }
                }
            }
        };
        thread.start();
    }

    public void AddMessageToChat(String message)
    {
        chat.append("\n" + message);
        if (message.contains("SYSTEM -") || message.contains(word)) {
            timer.cancel();
        }
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    public CountDownTimer StartTimer()
    {
        return new CountDownTimer(roundTime * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                textTimer.setText("0:"+checkDigit(roundTime));
                roundTime--;
            }

            public void onFinish() {
                textTimer.setText("0:0");
            }

        }.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_window, container, false);

        textTimer = (TextView)view.findViewById(R.id.roundTime);

        wordLabel = (TextView) view.findViewById(R.id.word);

        /*
        TextView labelWord = (TextView) view.findViewById(R.id.word);
        if (((MainActivity)getActivity()).isGroupOwner) {
            labelWord.setVisibility(View.GONE);
        }
        else {

        }*/

        ink = (InkView)view.findViewById(R.id.ink);
        ink.setColor(getResources().getColor(android.R.color.black));
        ink.setMinStrokeWidth(1.5f);
        ink.setMaxStrokeWidth(6f);

        //if (((MainActivity)getActivity()).isGroupOwner)
        //    SendImageEvery();

        SetupGameChat(view);

        timer = StartTimer();

        return view;
    }


}

