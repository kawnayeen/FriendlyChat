package com.google.firebase.udacity.friendlychat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

/**
 * Developed by : kawnayeen
 * Creation Date : 7/27/17
 */
public interface MessageEventListener extends ChildEventListener {
    void onChildAdded(DataSnapshot var1, String var2);

    default void onChildChanged(DataSnapshot var1, String var2) {}
    default void onChildRemoved(DataSnapshot var1){}
    default void onChildMoved(DataSnapshot var1, String var2){}
    default void onCancelled(DatabaseError var1){}
}
