
package android.discoveryRallye;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 *   Class to access the SQLite database
 *  
 *   This class provides all necessary methods 
 *   to access the SQLite database. This includes
 *   create, update, insert, remove, etc.
 */
public class DiscoveryDbAdapter implements IDBPOI {

	/** 
	 * The default POIs
	 * 
	 * Some static POIs around the campus do 
	 */
	public static POI[] staticPois = {
			new POI(51.493396, 7.416286, "Sonnendeck"),
			new POI(51.493670, 7.420191, "FH FB Informatik"),
			new POI(51.494467, 7.420663, "FH Mensa"),
			new POI(51.494073, 7.421479, "FH FB Architektur"),
			new POI(51.493652, 7.418754, "FH FB Wirtschaft"),
			new POI(51.492559, 7.417670, "S-Bahn (Uni)"),
			new POI(51.492748, 7.416855, "Uni Bibliothek"),
			new POI(51.493009, 7.414805, "Uni Mensa"), };

	/* Database statements since version 1 */
	
	/**
	 *  Create table statement
	 *  
	 *    This statement will create the table for the POIs
	 */
	private static final String STMT_CREATE = "create table if not exists "
			+ DB_TABLE_POIS + " (" + ATTR_ID
			+ " integer primary key autoincrement, " + ATTR_NAME
			+ " text   unique  , " + ATTR_LON + " double not null, " + ATTR_LAT
			+ " double not null  " + ");";
	
	/**
	 * Statement to drop the pois table
	 */
	private static final String STMT_DROP_POIS = " DROP TABLE IF EXISTS "
			+ DB_TABLE_POIS;

	/* Attributes for database access */
	/** Reference to the inner db handling class */
	private DatabaseHelper dbHelper;
	
	/** To manage the SQLite database */
	private SQLiteDatabase sldb;

	/**  
	 * Android context
	 * 
	 * The context in which is class will be execute.
	 */
	private final Context ctx;

	/**
	 * Inner class to access the SQLite database
	 * 
	 * Provides methods to create and upgrade the DB
	 * 
	 * @author axel
	 *
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		/**
		 * The default constructor. 
		 * 
		 * @param context The Android context
		 */
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		/**
		 * To create the  pois table
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i("DiscoveryRallye", "DatabaseHelper::onCreate()");
			db.execSQL(STMT_CREATE);
		}

		/**
		 * To delete the pois table.
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("DiscoveryRallye", "DatabaseHelper::onUpgrade()");
			db.execSQL(STMT_DROP_POIS);
		}
	}

	/**
	 * Default Constructor
	 * 
	 * The Android context is necessary to open and close the SQLite database.
	 * 
	 * @param ctx The Android context
	 */
	public DiscoveryDbAdapter(Context ctx) {
		this.ctx = ctx;

	}

	/**
	 * Open a writable DB connection
	 * 
	 * @return itself
	 * @throws SQLiteException is just pass through from SQLiteOpenHelper
	 */
	public DiscoveryDbAdapter open() throws SQLiteException {

		dbHelper = new DatabaseHelper(ctx);
		sldb = dbHelper.getWritableDatabase();

		checkDB();

		return this;
	}

	/**
	 * Write the default POIs into the DB
	 * 
	 * Write all defaults POIs to the Database, if the
	 * DB empty; otherwise it do nothing
	 * 
	 */
	private void checkDB() {
		if (getPOIs() != null) {
			if (!(getPOIs().getCount() > 0)) {
				setDefaultPOIs();
			}
		}
	}

	/**
	 * Close the DB Connection
	 */
	public void close() {
		if (dbHelper != null) {
			dbHelper.close();
		}
	}

	/**
	 * Insert a POI object into the SQLite database
	 * 
	 * @param poi A POI obejct
	 * @see POI
	 *            the POI object
	 * @return -1 if a POI with the same name already in the database; otherwise
	 *         the row id (primary key)
	 */
	public long insertPoi(POI poi) {
		ContentValues val = new ContentValues();
		val.put(ATTR_NAME, poi.getDescription());
		val.put(ATTR_LON, poi.getLon());
		val.put(ATTR_LAT, poi.getLat());

		return sldb.insert(DB_TABLE_POIS, null, val);
	}

	/**
	 * Delete a record from the table DB_TABLE_POIs
	 * 
	 * @param name
	 *            the name of the POI have to be equal ATTR_NAME
	 * @see DB_TABLE_POIS
	 * @see ATTR_NAME
	 * @return true if the record was deleted; otherwise false
	 */
	public boolean deletePOI(String name) {
		return sldb.delete(DB_TABLE_POIS, ATTR_NAME + "=" + "'" + name + "'",
				null) > 0;
	}

	/**
	 * Rename a POI in the SQLite database
	 * 
	 * @param oldName
	 *            The current name
	 * @param newName
	 *            The new name
	 * @return The number of affected rows, should be one; otherwise a error is
	 *         occurred.
	 */
	public int renamePOI(String oldName, String newName)
			throws SQLiteConstraintException {
		ContentValues val = new ContentValues();
		val.put(ATTR_NAME, newName);
		int res = -4;
		try {
			res = sldb.update(DB_TABLE_POIS, val, ATTR_NAME + "=" + "'"
					+ oldName + "'", null);
		} catch (SQLiteConstraintException e) {
			throw new SQLiteConstraintException("POI-Name vergeben");
		}
		Log.i("Rename", "Affected Rows:" + res);

		return res;
	}

	/**
	 * Fetch all POIs from the DB
	 * 
	 * @return The Cursor with contains the result set
	 */
	public Cursor getPOIs() {
		return sldb.query(DB_TABLE_POIS, new String[] { ATTR_ID, ATTR_NAME,
				ATTR_LON, ATTR_LAT }, null, null, null, null, null);
	}

	/**
	 * Restore the default records in the DB. <b>Warning:</b> already created
	 * POIs will be deleted
	 */
	public void resetDB() {
		// Drop table and create it again
		sldb.execSQL(STMT_DROP_POIS);
		sldb.execSQL(STMT_CREATE);

		setDefaultPOIs();
	}

	/**
	 * Set the default POIs to the database.
	 * 
	 * To ensure that there will be no duplicates, you should call
	 * resetDB(), before you call this method.
	 */
	private void setDefaultPOIs() {
		// Set all defaults POIs
		for (int i = 0; i < staticPois.length; i++) {
			insertPoi(staticPois[i]);
		}

	}

}
