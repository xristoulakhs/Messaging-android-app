package com.example.distributedsystemsapp.ui.conversation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.distributedsystemsapp.ImageActivity;
import com.example.distributedsystemsapp.R;
import com.example.distributedsystemsapp.VideoActivity;
import com.example.distributedsystemsapp.domain.Message;
import com.example.distributedsystemsapp.domain.MultimediaFile;
import com.example.distributedsystemsapp.domain.Publisher;
import com.example.distributedsystemsapp.domain.UserNode;
import com.example.distributedsystemsapp.ui.homepage.HomepageModel;
import com.example.distributedsystemsapp.ui.services.ConnectionService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.Inflater;

public class ConversationModel extends AppCompatActivity implements ConversationView {


    private final int PICK_GALLERY = 1;
    private final int TAKE_PICTURE = 2;
    private final int PICK_VIDEO = 3;
    private final int TAKE_VIDEO = 4;
    private String dir = Environment.getExternalStorageDirectory().toString();

    private String topic;
    private UserNode usernode;
    private Queue<Message> conversation;
    ListView listView;
    Button sendMessageButton, sendMediaButton;
    EditText textField;
    ArrayList<String> messages;
    ArrayAdapter<String> adapter;

    private Bitmap media;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.conversation);
        Bundle bundle = getIntent().getExtras();
        topic = bundle.getString("topic");

        ((ConnectionService) this.getApplication()).getUserNode().checkBroker(topic);

        messages = ((ConnectionService) this.getApplication()).getTopicMessages(topic);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages);

        listView = (ListView) findViewById(R.id.conversationListView);


        sendMessageButton = (Button) findViewById(R.id.messageButton);
        sendMediaButton = (Button) findViewById(R.id.mediaButton);
        textField = (EditText) findViewById(R.id.newMessage);

        listView.setAdapter(adapter);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textField.getText().toString();
                if(message.trim().length()>0){
                    sendMessage(message);
                }
            }
        });


        sendMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageOptionsDialog();
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Object[] values = typeOfMessage(i);

                if(values[0].equals("v")){
                    showAlertDialog("v", (Message) values[1], (String) values[2]);
                }
                else if (values[0].equals("p")){
                   showAlertDialog("p", (Message) values[1], (String) values[2]);
                }

            }
        });


        loopInAnotherThread();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Object[] typeOfMessage(int i){
        Object[] obj = new Object[3];
        LinkedList<Message> conversation = (LinkedList<Message>) ((ConnectionService) this.getApplication()).getConversation(topic);
        Message temp = conversation.get(i);

        if (temp.getFiles() == null){
            obj[0] = "m";
        }
        else {
            String file = temp.getFiles().get(0).getMultimediaFileName();
            String[] fileNameParts = file.split("\\.");
            String fileType = fileNameParts[fileNameParts.length - 1];
            if(fileType.equals("mp4")){
                obj[0] = "v";
            }
            else{
                obj[0] = "p";
            }
            obj[1] = temp;
        }
        return obj;
    }

    private void showImageOptionsDialog() {
        String[] choices = {"Picture from gallery",
                "Take picture",
                "Video from gallery",
                "Take video"};

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int choice) {
                switch (choice) {
                    case 0:
                        choosePicture();
                        break;

                    case 1:
                        takePicture();
                        break;
                    case 2:
                        chooseVideo();
                        break;
                    case 3:
                        takeVideo();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialogInterface.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationModel.this);
        builder.setTitle("Choose source of media: ").setItems(choices, dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    private void choosePicture() {
        Intent choosePictureIntent = new Intent();
        choosePictureIntent.setType("image/*");
        choosePictureIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(choosePictureIntent, PICK_GALLERY);
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, TAKE_PICTURE);
    }

    private void chooseVideo() {
        Intent choosePictureIntent = new Intent();
        choosePictureIntent.setType("video/*");
        choosePictureIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(choosePictureIntent, PICK_VIDEO);
    }

    private void takeVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(takeVideoIntent,TAKE_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE) {
            media = null;
            Bundle extras = data.getExtras();

            media = (Bitmap) extras.get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            media.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytearray = stream.toByteArray();
            media.recycle();
            sendMediaToBroker(bytearray,".jpg");
            MultimediaFile multimediaFile = new MultimediaFile();
            multimediaFile.setMultimediaFileChunk(bytearray);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            multimediaFile.setMultimediaFileName(timeStamp + ".jpg");
            ArrayList<MultimediaFile> files = new ArrayList<>();


        }
        else if (requestCode == PICK_GALLERY) {

            media = null;
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getApplication().getContentResolver().openInputStream(imageUri);
                media = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                media.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytearray = stream.toByteArray();
                media.recycle();
                sendMediaToBroker(bytearray,".jpg");
                MultimediaFile multimediaFile = new MultimediaFile();
                multimediaFile.setMultimediaFileChunk(bytearray);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                multimediaFile.setMultimediaFileName(timeStamp + ".jpg");
                ArrayList<MultimediaFile> files = new ArrayList<>();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == PICK_VIDEO) {

            Uri videoUri= null;

            if (resultCode == RESULT_OK) {
                videoUri = data.getData();
            }

            InputStream fis;
            try {
                fis = getContentResolver().openInputStream(videoUri);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                int len = 0;
                while ((len = fis.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                byte[] bytearray = byteBuffer.toByteArray();
                sendMediaToBroker(bytearray,".mp4");
                MultimediaFile multimediaFile = new MultimediaFile();
                multimediaFile.setMultimediaFileChunk(bytearray);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                multimediaFile.setMultimediaFileName(timeStamp + ".mp4");
                ArrayList<MultimediaFile> files = new ArrayList<>();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if (requestCode == TAKE_VIDEO){

            Uri videoUri= null;

            if (resultCode == RESULT_OK) {
                videoUri = data.getData();
            }

            InputStream fis;
            try {
                fis = getContentResolver().openInputStream(videoUri);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                int len = 0;
                while ((len = fis.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                byte[] bytearray = byteBuffer.toByteArray();
                sendMediaToBroker(bytearray,".mp4");
                MultimediaFile multimediaFile = new MultimediaFile();
                multimediaFile.setMultimediaFileChunk(bytearray);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                multimediaFile.setMultimediaFileName(timeStamp + ".mp4");
                ArrayList<MultimediaFile> files = new ArrayList<>();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendMediaToBroker(byte[] media, String typeOfMedia){
        Publisher publisher = ((ConnectionService) this.getApplication()).getPublisher();
        publisher.push(topic, media, typeOfMedia);
    }

    private void sendMessage(String message) {
        ((ConnectionService) this.getApplication()).getPublisher().sendMessage(topic, message);
    }

    private void checkForMessages() {
        int difference = ((ConnectionService) this.getApplication()).showConversation(topic);

        if (difference > 0) {

            ArrayList<String> newMessages = ((ConnectionService) this.getApplication()).getLastMessages(topic, difference);
            for (String message : newMessages) {
                adapter.add(message);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void loopInAnotherThread() {
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkForMessages();
                            }
                        });
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void showAlertDialog(String type,Message message,String filepath) {

        if(type.equals("v")){
            Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
            String filename = message.getFiles().get(0).getMultimediaFileName();
            createFile(message.getFiles().get(0));

//            File file = new File(dir,filename);
            intent.putExtra("filename", filename);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
            String filename = message.getFiles().get(0).getMultimediaFileName();
            createFile(message.getFiles().get(0));

            intent.putExtra("filename", filename);
            startActivity(intent);
        }
    }

    private void createFile(MultimediaFile multimediaFile){

        File path = getApplicationContext().getFilesDir();

        String fileName = multimediaFile.getMultimediaFileName();

        try{
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            writer.write(multimediaFile.getMultimediaFileChunk());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
