package com.umu.zaim0001.notes;
/*
* a dialog fragment to show a pop-up where a user can enter their title and note
* */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

public class NewNote extends AppCompatDialogFragment {
    private EditText title,note;
    private DialogListener dialogListener;
    @SuppressLint("ResourceType")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.addnote_layout,null);
        builder.setView(v).setTitle("Add New Note").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //closes dialog
                //sends text to mainActivity by using saveInputText.
                String inTitle= title.getText().toString();
                String inNote = note.getText().toString();
                if(!inTitle.equals(""))
                    dialogListener.saveInputText(inTitle,inNote);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //closes dialog
            }
        });
        title=v.findViewById(R.id.nTitle);
        note=v.findViewById(R.id.note);

        return builder.create();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            dialogListener=(DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+"Implement DialogListener");
        }
    }

    public interface DialogListener{
        void saveInputText(String title, String note);
    }
}
