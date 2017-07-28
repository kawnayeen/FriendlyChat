package com.google.firebase.udacity.friendlychat;

/**
 * Developed by : kawnayeen
 * Creation Date : 7/27/17
 */
public interface FriendlyChatView {
    void addMessage(FriendlyMessage friendlyMessage);
    void applyRetrievedLengthLimit(int messageLength);
}
