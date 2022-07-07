package com.example.distributedsystemsapp.ui.logIn;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.distributedsystemsapp.R;
import com.example.distributedsystemsapp.domain.Message;
import com.example.distributedsystemsapp.ui.homepage.HomepageModel;
import com.example.distributedsystemsapp.ui.services.ConnectionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class LogInModel extends AppCompatActivity implements LogInView{

    Button buttonLogIn;

    private LogInPresenter presenter;
    ArrayList<String> users;
    EditText username;
    HashMap<String, Queue<Message>> conversations;
    String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.loginpage);


        users= initUsers();
        username=findViewById(R.id.textLogInName);

        presenter = new LogInPresenter(this);

        buttonLogIn = (Button) findViewById(R.id.logInBttn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        buttonLogIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String name= presenter.onLogIn();
                if(!name.equals(" ")){
                    Intent intent = new Intent(LogInModel.this, HomepageModel.class);
                    intent.putExtra("username",name);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Invalid username. Please input a valid username!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public ArrayList<String> initUsers(){

        ArrayList<String> users= new ArrayList<>();
        BufferedReader br = null;

        try {
            br= new BufferedReader(
                    new InputStreamReader(getAssets().open("data/usernode/userConf.txt"), "UTF-8"));
            String line;
            while( (line = br.readLine()) != null){
                String[] dataFromLine = line.split(",");
                users.add(dataFromLine[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return users;
    }

    @Override
    public ArrayList<String> getUsers(){
        return this.users;
    }

    @Override
    public EditText getUsername(){
        return this.username;
    }

    public void setConversations(HashMap<String, Queue<Message>> conv){
        this.conversations= conv;
    }

}
