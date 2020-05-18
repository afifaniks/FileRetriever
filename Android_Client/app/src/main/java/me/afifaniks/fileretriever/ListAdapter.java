package me.afifaniks.fileretriever;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<FileHandler> {
    private Activity context;
    private ArrayList<FileHandler> fileList;
    private static DecimalFormat df = new DecimalFormat("0.00");

    public ListAdapter(Activity context, ArrayList<FileHandler> fileList) {
        super(context, R.layout.list_row, fileList);

        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.list_row, null, true);

        FileHandler file = fileList.get(position);

        TextView fileName = view.findViewById(R.id.fileName);
        TextView filePath = view.findViewById(R.id.filePath);
        TextView fileSize = view.findViewById(R.id.fileSize);
        ImageView fileIcon = view.findViewById(R.id.fileIcon);

        String name = file.getName();
        String path = file.getPath();
        String size;


        if (file.getType().equals("dir")) {
            fileIcon.setImageResource(R.drawable.folder);
            size = "-/-";
        } else if (file.getType().equals("drive")) {
            fileIcon.setImageResource(R.drawable.drive);
            name = "Logical Drive (" + name + ")";
            size = "-/-";
        }
        else {
            fileIcon.setImageResource(R.drawable.file);
            size = String.valueOf(df.format(file.getSize() * 0.000001)) + " MB";
        }

        fileName.setText(name);
        filePath.setText(path);
        fileSize.setText(size);

        return view;
    }
}
