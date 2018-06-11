package rs.com.servervrproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import rs.com.servervrproject.adapter.MyRecyclerViewAdapter;

public class ListOfVideosActivity extends AppCompatActivity {


    private List<String> mList;
    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_videos);

        mList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            mList.add("Test " + i);
        }

        mRecyclerAdapter = new MyRecyclerViewAdapter(this, mList);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mRecyclerAdapter);
        recyclerView.setHasFixedSize(true);
//        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }
}
