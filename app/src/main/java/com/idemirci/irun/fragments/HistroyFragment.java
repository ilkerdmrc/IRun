package com.idemirci.irun.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.idemirci.irun.R;
import com.idemirci.irun.adapters.RouteListAdapter;
import com.idemirci.irun.database.DBHelper;



public class HistroyFragment extends Fragment {

    private static final String TAG = "MyLog";
    private DBHelper dbHelper;
    private RouteListAdapter empListAdapter;
    private Fragment denemeFragment = new LastRunMapFragment();
    TextView txtDate;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.history_fragment_title);
        dbHelper = new DBHelper(getActivity());
        txtDate = (TextView) getActivity().findViewById(R.id.txtDate);
        Cursor cursor = dbHelper.getSummaryForHistory();
        ListView mListView = (ListView) getActivity().findViewById(R.id.routeList);
        mListView.setItemsCanFocus(true);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.history_relative_layout, denemeFragment);
                //Bundle bundleForRunIdPass = new Bundle();
               // bundleForRunIdPass.putString("runIdForThisRun", runId);
                //denemeFragment.setArguments(bundleForRunIdPass);
                ft.addToBackStack("Tag").commit();
            }
        });

        if(cursor!=null){
            /*cursor.moveToFirst();
            while (cursor.moveToNext()){
                //cursor.getString(cursor.getColumnIndex("_id"));

            }*/

            empListAdapter = new RouteListAdapter(getActivity().getApplicationContext(), cursor, 0);
            mListView.setAdapter(empListAdapter);
        }
        if(txtDate != null) {
            String runIdfromTextView = (String) txtDate.getText();
            Log.i(TAG,"txtDate**** : " + runIdfromTextView );
        }else{
            Log.i(TAG, "txtDate**** is null");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
}
