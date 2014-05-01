package org.opendatakit.sync.activities;

import org.opendatakit.common.android.data.ConflictTable;
import org.opendatakit.common.android.data.DbTable;
import org.opendatakit.common.android.data.TableProperties;
import org.opendatakit.common.android.provider.DataTableColumns;
import org.opendatakit.sync.TableFileUtils;

import android.app.ListActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * An activity for presenting a list of all the rows in conflict.
 * @author sudar.sam@gmail.com
 *
 */
public class ConflictResolutionListActivity extends ListActivity {

  private static final String TAG =
      ConflictResolutionListActivity.class.getSimpleName();

  private ConflictTable mConflictTable;
  private ArrayAdapter<String> mAdapter;

  @Override
  protected void onResume() {
    super.onResume();
    // Do this in on resume so that if we resolve a row it will be refreshed
    // when we come back.
    String appName = getIntent().getStringExtra(Aggregate.INTENT_KEY_APP_NAME);
    if ( appName == null ) {
      appName = TableFileUtils.getDefaultAppName();
    }
    String tableId =
        getIntent().getStringExtra(Aggregate.INTENT_KEY_TABLE_ID);
    TableProperties tableProperties =
        TableProperties.getTablePropertiesForTable(this, appName, tableId);
    DbTable dbTable = DbTable.getDbTable(tableProperties);
    this.mConflictTable = dbTable.getConflictTable();
    this.mAdapter = new ArrayAdapter<String>(
        getActionBar().getThemedContext(),
        android.R.layout.simple_list_item_1);
    for (int i = 0; i < this.mConflictTable.getLocalTable().getNumberOfRows(); i++) {
      String localRowId = this.mConflictTable.getLocalTable()
          .getMetadataByElementKey(i, DataTableColumns.ID);
      String serverRowId = this.mConflictTable.getServerTable()
          .getMetadataByElementKey(i, DataTableColumns.ID);
      if (!localRowId.equals(serverRowId)) {
        Log.e(TAG, "row ids at same index are not the same! this is an " +
            "error.");
      }
      this.mAdapter.add(localRowId);
    }
    this.setListAdapter(mAdapter);
  }


  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    Log.e(TAG, "[onListItemClick] clicked position: " + position);
    Intent i = new Intent(this, ConflictResolutionRowActivity.class);
    i.putExtra(Aggregate.INTENT_KEY_APP_NAME,
        mConflictTable.getLocalTable().getTableProperties().getAppName());
    i.putExtra(Aggregate.INTENT_KEY_TABLE_ID,
        mConflictTable.getLocalTable().getTableProperties().getTableId());
    String rowId =
        this.mConflictTable.getLocalTable().getRowAtIndex(position).getRowId();
    i.putExtra(ConflictResolutionRowActivity.INTENT_KEY_ROW_ID, rowId);
    this.startActivity(i);
  }

}
