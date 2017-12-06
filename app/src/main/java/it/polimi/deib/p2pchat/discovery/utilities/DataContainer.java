package it.polimi.deib.p2pchat.discovery.utilities;

import android.content.Context;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

public class DataContainer {
    public String playerName;
    public Enums.RequestTypes requestType;


    public DataContainer(Context c, Enums.RequestTypes requestType)
    {
        this.requestType = requestType;

    }

    public DataContainer(Context c, Enums.RequestTypes requestType, String playerName)
    {
        this.requestType = requestType;
        this.playerName = playerName;
    }

}
