package it.polimi.deib.p2pchat.discovery.utilities;

import android.content.Context;

import com.google.gson.GsonBuilder;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 *
 * Denotes that a parameter, field or method return value should be only private.
 * <p></p>
 * This is a marker annotation and it has no specific attributes.
 */
public class DataContainer {
    public String playerName;
    public String message;
    public byte[] image;
    public ArrayList<Player> playerList;
    public Enums.RequestTypes requestType;

    /**
     * Constructor.
     *
     * @param requestType (required) Type of container.
     */
    public DataContainer(Enums.RequestTypes requestType)
    {
        this.requestType = requestType;

    }

    /**
     * Constructor 2.
     *
     * @param playerName (required) Player name
     * @param message (required) TMessage
     * @param requestType (required) Type of container.
     */
    public DataContainer(String playerName, String message, Enums.RequestTypes requestType)
    {
        this.requestType = requestType;
        this.playerName = playerName;
        this.message = message;
    }

    public DataContainer(ArrayList<Player> players, Enums.RequestTypes requestType){
        this.requestType = requestType;
        this.playerList = players;
    }
    public DataContainer(String playerName, String message, ArrayList<Player> players, Enums.RequestTypes requestType)
    {
        this.requestType = requestType;
        this.playerName = playerName;
        this.message = message;
        this.playerList = players;
    }

    public DataContainer(String playerName, String message, byte[] image, Enums.RequestTypes requestType)
    {
        this.requestType = requestType;
        this.playerName = playerName;
        this.message = message;
        this.image = image;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, DataContainer.class);
    }

    public byte[] toByteArray(){
        return (this.toString()).getBytes();
    }


}
