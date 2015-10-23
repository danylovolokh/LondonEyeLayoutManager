package com.volokh.danylo;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.volokh.danylo.adapter.LondonEyeListAdapter;
import com.volokh.danylo.layoutmanager.LondonEyeLayoutManager;
import com.volokh.danylo.q.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        List<String> mList = new ArrayList<>(Arrays.asList(
                "Passenger Cabin 1",
                "Passenger Cabin 2",
                "Passenger Cabin 3",
                "Passenger Cabin 4",
                "Passenger Cabin 5",
                "Passenger Cabin 6",
                "Passenger Cabin 7",
                "Passenger Cabin 8",
                "Passenger Cabin 9",
                "Passenger Cabin 10",
                "Passenger Cabin 11"));

        private RecyclerView mRecyclerView;

//        private LinearLayoutManager mLayoutManager;
        private LondonEyeLayoutManager mLondonEyeLayoutManager;

        private LondonEyeListAdapter mVideoRecyclerViewAdapter;
        private RecyclerView mRecyclerView2;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
//            mLayoutManager = new LinearLayoutManager(getActivity());
            mLondonEyeLayoutManager = new LondonEyeLayoutManager(getActivity(), getActivity().getResources().getDisplayMetrics().widthPixels);
            mRecyclerView.setLayoutManager(mLondonEyeLayoutManager);//new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

            mVideoRecyclerViewAdapter = new LondonEyeListAdapter(getActivity(), mList);

            mRecyclerView.setAdapter(mVideoRecyclerViewAdapter);

//            mRecyclerView2 = (RecyclerView)rootView.findViewById(R.id.recycler_view2);
//            mRecyclerView2.setHasFixedSize(true);
//
//            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
//            mRecyclerView2.setAdapter(new LondonEyeListAdapter(getActivity(), mList));

            return rootView;
        }
    }
}
