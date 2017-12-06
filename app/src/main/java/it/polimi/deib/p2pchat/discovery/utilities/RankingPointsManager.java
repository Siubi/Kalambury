package it.polimi.deib.p2pchat.discovery.utilities;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Created by Krzysiek on 2017-12-06.
 */
public class RankingPointsManager {


    private List<Player> players = new ArrayList<>();

    public RankingPointsManager(List<Player> players) {
        this.players = players;
    }

    public void ResetAllPlayersPoints(){
        for(int x = 0 ; x < players.size(); x++){
            players.get(x).points = 0;
        }
    }

    public void AddPointsToPlayer(String playerName){
            for(int x = 0 ; x < players.size(); x++){
                if(players.get(x).playerName.equals(playerName)){
                    players.get(x).points += 2;
                    break;
                }
            }
    }

    public List<Player> GetPlayers(){
        return players;
    }
}
