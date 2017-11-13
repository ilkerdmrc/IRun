package com.idemirci.irun.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.idemirci.irun.R;
import com.idemirci.irun.adapters.RouteListAdapter;
import com.idemirci.irun.database.DBHelper;



public class HistroyFragment extends Fragment {

    private DBHelper dbHelper;
    private RouteListAdapter empListAdapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.history_fragment_title);
        dbHelper = new DBHelper(getActivity());

        Cursor cursor = dbHelper.getSummaryForHistory();
        ListView mListView = (ListView) getActivity().findViewById(R.id.routeList);

        if (cursor!=null){

        }

        if(cursor!=null){
            /*cursor.moveToFirst();
            while (cursor.moveToNext()){
                //cursor.getString(cursor.getColumnIndex("_id"));

            }*/

            empListAdapter = new RouteListAdapter(getActivity().getApplicationContext(), cursor, 0);
            mListView.setAdapter(empListAdapter);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
}
