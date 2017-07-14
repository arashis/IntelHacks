package com.example.arashis.iokey;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TextView  t;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t = (TextView)findViewById(R.id.status);

        ListView myListView = (ListView) findViewById(R.id.myListView);

        // ListViewに表示
        final KeyAdapter ka = new KeyAdapter(this);
        myListView.setAdapter(ka);


        mDatabase = FirebaseDatabase.getInstance().getReference("Status");

        // Attach a listener to read the data at our posts reference
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                status = dataSnapshot.getValue(String.class);
                t.setText(status);

                Date date = new Date();
                SimpleDateFormat sdf1 = new SimpleDateFormat("M/d  H:m:s");
                ka.additem(status,sdf1.format(date));
                ka.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
            });

        // 確認用、後で消す
        TextView s = (TextView) findViewById(R.id.status);
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date();
                SimpleDateFormat sdf1 = new SimpleDateFormat("M/d  H:m:s");
                ka.additem(status,sdf1.format(date));
                ka.notifyDataSetChanged();
            }
        });

    }
}