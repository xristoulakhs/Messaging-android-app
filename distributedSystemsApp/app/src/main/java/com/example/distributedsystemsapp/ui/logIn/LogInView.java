package com.example.distributedsystemsapp.ui.logIn;

import android.widget.EditText;

import java.util.ArrayList;

public interface LogInView {
    public ArrayList<String> initUsers();

    public ArrayList<String> getUsers();
    public EditText getUsername();
}
