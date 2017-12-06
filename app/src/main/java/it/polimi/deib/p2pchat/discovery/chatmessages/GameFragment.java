package it.polimi.deib.p2pchat.discovery.chatmessages;



import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import it.polimi.deib.p2pchat.discovery.DestinationDeviceTabList;
import it.polimi.deib.p2pchat.discovery.MainActivity;
import it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue;
import it.polimi.deib.p2pchat.discovery.services.ServiceList;
import it.polimi.deib.p2pchat.discovery.services.WiFiP2pService;
import it.polimi.deib.p2pchat.discovery.socketmanagers.ConnectionManager;
import lombok.Getter;
import lombok.Setter;


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

import com.simplify.ink.InkView;

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

public class GameFragment extends Fragment {

    private static final String TAG = "GameFragment";

    @Getter
    @Setter
    private Integer tabNumber;
    @Getter @Setter private static boolean firstStartSendAddress;
    @Getter @Setter private boolean grayScale = true;
    @Getter private final List<String> items = new ArrayList<>();
    private InkView ink;

    private TextView chatLine;

    @Getter @Setter private ConnectionManager connectionManager;
    private WiFiChatMessageListAdapter adapter = null;

    public TextView chat;
    int roundTime=45;
    TextView textTimer;

    
    public interface AutomaticReconnectionListener {
        public void reconnectToService(WiFiP2pService wifiP2pService);
    }

    
    public static GameFragment newInstance() {
        return new GameFragment();
    }

    
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

    private void SetupGameChat(View view)
    {
        
        (getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        (getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        
        final EditText enterChatMessege = (EditText) view.findViewById(R.id.editText);
        enterChatMessege.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId== EditorInfo.IME_ACTION_DONE )) {

                    
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(enterChatMessege.getWindowToken(), 0);

                    String message = ((MainActivity)getActivity()).deviceName + ": " + enterChatMessege.getText().toString();
                    
                    for (int i = 0; i < ((MainActivity)getActivity()).users.size(); i++)
                    {
                        ((MainActivity)getActivity()).users.get(i).write(message.getBytes());
                    }

                    if (((MainActivity)getActivity()).isGroupOwner) {
                        AddMessageToChat(message);
                    }

                    enterChatMessege.setText("");
                    enterChatMessege.clearFocus();

                    return true;
                }
                return false;
            }
        });
        chat = (TextView) view.findViewById(R.id.chat);
        chat.setMovementMethod(new ScrollingMovementMethod());}

    public void AddMessageToChat(String message)
    {
        chat.append("\n" + message);
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    public void StartTimer()
    {
        new CountDownTimer(roundTime * 1000, 1000) {

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

        TextView labelWord = (TextView) view.findViewById(R.id.word);
        if (!((MainActivity)getActivity()).isGroupOwner) {
            labelWord.setVisibility(View.GONE);
        }
        else {
            labelWord.setText("Hasło: dupa");
        }

        ink = (InkView)view.findViewById(R.id.ink);
        ink.setColor(getResources().getColor(android.R.color.black));
        ink.setMinStrokeWidth(1.5f);
        ink.setMaxStrokeWidth(6f);

        SetupGameChat(view);

        StartTimer();

        return view;
    }


}

