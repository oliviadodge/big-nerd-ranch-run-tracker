package com.bignerdranch.android.runtracker;


import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by oliviadodge on 4/30/2015.
 */
public class RunListFragment extends ListFragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_NEW_RUN = 0;
    private static final String TAG = "RunListFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //initialize the loader to load the list of runs
        getLoaderManager().initLoader(0, null, this);
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        ListView lv = (ListView)v.findViewById(android.R.id.list);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
            //Use floating context menus on Froyo and GingerBread
            registerForContextMenu(lv);
        } else {
            //Use contextual action bar on Hone
            lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    //required, but not used in this implementation
                }

                //ActionMode.Callback methods
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.run_list_item_context, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                    //required but not used in this implementation
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.menu_item_delete_run:
                            RunCursorAdapter adapter = (RunCursorAdapter)getListAdapter();
                            RunManager runManager = RunManager.get(getActivity());
                            for (int i = adapter.getCount() -1; i >= 0; i--){
                                if (getListView().isItemChecked(i)){
                                    boolean deleted = runManager.deleteRun(adapter.getItemId(i));
                                    Log.i(TAG, "run deleted? " + deleted);
                                }
                            }
                            mode.finish();
                            updateUI();
                            return true;
                        default:
                            return false;

                    }
                }
                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    //required but not used in this implementation
                }
            });
        }
        return v;
    }

    public void updateUI(){
        getLoaderManager().restartLoader(0, null, this);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //the id argument will be the run id; CursorAdapter gives us this for free
        Intent i = new Intent(getActivity(), RunActivity.class);
        i.putExtra(RunActivity.EXTRA_RUN_ID, id);
        startActivity(i);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.run_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_run:
                Intent i = new Intent(getActivity(), RunActivity.class);
                startActivityForResult(i, REQUEST_NEW_RUN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_NEW_RUN == requestCode) {
            //restart the loader to get any new run available
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //you only ever load the runs, so assume this is the case
        return new RunListCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor == null) {
            setListAdapter(null);
        } else {
            //create an adapter to point at this cursor
            RunCursorAdapter adapter =
                    new RunCursorAdapter(getActivity(), (RunDatabaseHelper.RunCursor) cursor);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        //stop using the cursor (via the adapter)
        setListAdapter(null);
    }

    private static class RunListCursorLoader extends SQLiteCursorLoader {

        public RunListCursorLoader(Context context) {
            super(context);
        }

        @Override
        protected Cursor loadCursor() {
            //query the list of runs
            return RunManager.get(getContext()).queryRuns();
        }
    }

    private static class RunCursorAdapter extends CursorAdapter {
        private RunDatabaseHelper.RunCursor mRunCursor;

        public RunCursorAdapter(Context context, RunDatabaseHelper.RunCursor cursor) {
            super(context, cursor, 0);
            mRunCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            //use a layout inflater to get a row view
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            //get the run for the current row
             Run run = mRunCursor.getRun();

            //set up the start date text view
            TextView startDateTextView = (TextView)view;
            String cellText =
                    context.getString(R.string.cell_text, run.getStartDate());
            startDateTextView.setText(cellText);

        }
    }
}
