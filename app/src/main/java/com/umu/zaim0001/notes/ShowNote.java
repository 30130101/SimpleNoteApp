package com.umu.zaim0001.notes;

/*
* a dialog fragment used to show a note like a "pop-up" on the screen
* */

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


public class ShowNote extends AppCompatDialogFragment {
    private TextView noteTime,notePlace;
    public static final String tempNote="";
    private String noteAsString;

    public Dialog onCreateDialog(Bundle savedInstanceState){
        noteAsString = getArguments().getString(tempNote);
        Gson gson = new Gson();
        Type type = new TypeToken<Notes>(){}.getType();
        final Notes n = gson.fromJson(noteAsString, type);

        final AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.shownote,null);
        adb.setView(v).setTitle(n.getName()).setMessage(n.getNote());

         noteTime=(TextView)v.findViewById(R.id.noteTime);
         noteTime.setText(n.getDateTime());

         notePlace=(TextView)v.findViewById(R.id.notePlace);
         notePlace.setText(n.getLocation());

        return adb.create();
    }
}
