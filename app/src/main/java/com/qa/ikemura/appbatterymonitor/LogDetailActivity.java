
package com.qa.ikemura.appbatterymonitor;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.qa.ikemura.appbatterymonitor.dummy.DummyContent;

/**
 * An activity representing a single log detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link LogListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link LogDetailFragment}.
 */
public class LogDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareLogFile();
                Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Show the Up button in the action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putSerializable(LogDetailFragment.ARG_ITEM_ID,
                    getIntent().getSerializableExtra(LogDetailFragment.ARG_ITEM_ID));
            LogDetailFragment fragment = new LogDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.log_detail_container, fragment)
                    .commit();
        }
    }

    private void ShareLogFile() {

        DummyContent.DummyItem item = (DummyContent.DummyItem) getIntent().getSerializableExtra(LogDetailFragment.ARG_ITEM_ID);
        File file = getFileStreamPath(item.content);
        Uri internal = Uri.fromFile(file);

        // Intent shareIntent = new Intent();
        // shareIntent.setAction(Intent.ACTION_SEND);
        // shareIntent.putExtra(Intent.EXTRA_STREAM, file.getAbsolutePath());
        // shareIntent.setType("text/plain");
        // startActivity(Intent.createChooser(shareIntent, "ログファイル"));

        // IntentBuilder をインスタンス化
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this);
        builder.setChooserTitle("Choose Send App");
        builder.setStream(internal);
        // String[] toList = new String[]{address.getText().toString()};
        // builder.setEmailTo(toList);
        // builder.setSubject(item.content);
        // builder.setText(item.details);
        builder.setType("application/log");
        builder.startChooser();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, LogListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
