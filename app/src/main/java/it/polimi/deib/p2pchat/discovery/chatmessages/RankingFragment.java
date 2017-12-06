package it.polimi.deib.p2pchat.discovery.chatmessages;

/**
 * Created by Krzysiek on 2017-12-05.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import it.polimi.deib.p2pchat.R;
import lombok.Getter;
import lombok.Setter;


public class RankingFragment extends Fragment {

    @Getter @Setter
    public static List<String> PlayersScore;
    private static final String TAG = "RankingFragment";
    private static TextView _rankingTextView;
    public static RankingFragment newInstance() {
        return new RankingFragment();
    }

    public RankingFragment() {

    }

    public static void Refresh(){
        _rankingTextView.setText(getFormattedPlayersScore());
    }

    private static String getFormattedPlayersScore(){
        String formattedResult = "";
        for (String score : PlayersScore ) {
                formattedResult += score + "\n";
        }
        return formattedResult;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ranking_window, container, false);
        _rankingTextView = (TextView)view.findViewById(R.id.textViewRanking);
        return view;
    }


}

