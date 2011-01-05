package android.discoveryRallye;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class POIList extends ListActivity implements IUIRefreshable,
		IOverlayRouteable {
	private POIContainer poic;

	/**
	 * Create this activity
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		poic = POIContainer.getInstance(getBaseContext());
		this.uiRefresh();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		/* Create the custom dialog */
		POIDialog dlg = new POIDialog(this, this, this, position);
		dlg.show();
	}

	public void addRouteOverlay(POI destination) {
		// GeoUtils.activateLocationListener(this);
		JSONRequest jsonRequest = new JSONRequest(this, destination);
		jsonRequest.calculateRoute();
	}

	/**
	 * Create the UI-List. After adding a POI or several POI to the POICOntainer
	 * call this method to refresh the UI
	 * 
	 * @see POI
	 * @see POIContainer
	 */
	public void uiRefresh() {
		setListAdapter((ListAdapter) new ArrayAdapter<String>(this,
				R.layout.poi_row, poic.getAllPOIsName()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, Menu.FIRST, "POIs zurücksetzen");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		boolean result = false;

		switch (item.getItemId()) {
		case 1:
			//Re-Init the Container
			POIContainer.getInstance(this).reInit();
			uiRefresh();
			result = true;
			break;
		}

		return result;
	}
}
