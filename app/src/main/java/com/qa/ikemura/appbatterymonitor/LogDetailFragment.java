
package com.qa.ikemura.appbatterymonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.qa.ikemura.appbatterymonitor.dummy.DummyContent;

/**
 * A fragment representing a single log detail screen. This fragment is either
 * contained in a {@link LogListActivity} in two-pane mode (on tablets) or a
 * {@link LogDetailActivity} on handsets.
 */
public class LogDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    final File file = Environment.getExternalStorageDirectory();

    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LogDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = (DummyContent.DummyItem) getArguments().getSerializable(ARG_ITEM_ID);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.fileName);
            }
            else {
                getActivity().setTitle(mItem.fileName);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log_detail, container, false);

        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.log_detail)).setText(readFileAsString());
        }

        return rootView;
    }

    private String readFileAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(file, mItem.fileName)));
            while ((line = in.readLine()) != null)
                stringBuilder.append(line).append("\n");

        } catch (IOException e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("LogListFragment", "readFileAsString:" + e.getMessage());
        }

        return stringBuilder.toString();
    }
}
