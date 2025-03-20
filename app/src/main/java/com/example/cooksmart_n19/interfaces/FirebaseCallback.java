package com.example.cooksmart_n19.interfaces;

import com.example.cooksmart_n19.models.User;

public interface FirebaseCallback {
    default void onSuccess(String message) {}
    default void onFailure(String errorMessage) {}
    default void onUserDataReceived(User user) {}
}