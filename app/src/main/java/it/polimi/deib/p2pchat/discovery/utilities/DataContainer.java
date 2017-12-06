package it.polimi.deib.p2pchat.discovery.utilities;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
public DataContainer {
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
