package com.orange.lo.assetdemo.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ZLGP6287 on 27/10/2016.
 */

public class AbstractListenable {

    /** Collection of {@link java.beans.PropertyChangeListener} **/
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private ArrayList<PropertyChangeListener> listeners = new ArrayList<>();

    /**
     * Register a {@link PropertyChangeListener} to this object
     * @param listener the listener to register
     */
    public void registerChangeListener(PropertyChangeListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Remove a registered {@link PropertyChangeListener}
     * @param listener A reference to the listener to remove
     */
    public void removeChangeListener(PropertyChangeListener listener)
    {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Notify {@link PropertyChangeListener} objects that the object has been updated
     * @param propertyChangeEvent
     */
    protected void notifyListeners(PropertyChangeEvent propertyChangeEvent)
    {
        for (PropertyChangeListener listener : listeners)
        {
            listener.propertyChange(propertyChangeEvent);
        }
    }
}
