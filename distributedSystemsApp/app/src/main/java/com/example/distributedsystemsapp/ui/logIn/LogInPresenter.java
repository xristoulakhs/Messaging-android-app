package com.example.distributedsystemsapp.ui.logIn;

import android.widget.EditText;

import java.util.ArrayList;

public class LogInPresenter {

    private LogInView logInView;

    public LogInPresenter(LogInView view) {
        this.logInView = view;
    }

    public String onLogIn(){
        EditText inputUsername = logInView.getUsername();
        ArrayList<String> users= logInView.getUsers();
        String result=" ";
        for(String user: users){
            if(user.equals(inputUsername.getText().toString())) {
                result= user;
                break;
            }
        }
        return result;
    }


}
