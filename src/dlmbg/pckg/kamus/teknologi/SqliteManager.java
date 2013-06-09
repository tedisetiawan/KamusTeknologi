package dlmbg.pckg.kamus.teknologi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Gede Lumbung - 2013
 * http://gedelumbung.com
 */

public class SqliteManager {
	public static final int VERSI_DATABASE= 1;
	public static final String NAMA_DATABASE = "DBaseKamusTeknologi";
	public static final int POSISI_ID = 0;

	public static final String[] FIELD_TABEL_KAMUS ={"_id","nama","keterangan","gambar"};

	private Context crudContext;
	private SQLiteDatabase crudDatabase;
	private SqliteManagerHelper crudHelper;

	private static class SqliteManagerHelper extends SQLiteOpenHelper {

		private static final String TABEL_KAMUS =
			"CREATE TABLE IF NOT EXISTS tbl_kamus (" +
			"_id integer primary key autoincrement," +
			"nama text NOT NULL," +
			"keterangan text NOT NULL," +
			"gambar text NOT NULL)";

		public SqliteManagerHelper(Context context) {
			super(context, NAMA_DATABASE, null, VERSI_DATABASE);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL(TABEL_KAMUS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {}
	}

	public SqliteManager(Context context) {
		crudContext = context;
	}

	public void bukaKoneksi() throws SQLException {
		crudHelper = new SqliteManagerHelper(crudContext);
		crudDatabase = crudHelper.getWritableDatabase();
	}

	public void tutupKoneksi() {
		crudHelper.close();
		crudHelper = null;
		crudDatabase = null;
	}

	public long insertData(ContentValues values, String nama_tabel) {
		return crudDatabase.insert(nama_tabel, null, values);
	}

	public boolean updateData(long rowId, ContentValues values, String nama_tabel, String id_param) {
		return crudDatabase.update(nama_tabel, values,
				id_param + "=" + rowId, null) > 0;
	}

	public boolean hapusData(long rowId, String nama_tabel, String id_param) {
		return crudDatabase.delete(nama_tabel,
				id_param + "=" + rowId, null) > 0;
	}

	public Cursor bacaData() {
		return crudDatabase.query("tbl_kamus",FIELD_TABEL_KAMUS,null, null,null, null,"nama DESC");
	}

	public Cursor bacaDataPencarian(String cari) {
		
		return crudDatabase.query("tbl_kamus", FIELD_TABEL_KAMUS, "nama like '%"+cari+"%'", null, null, null, null);
	}

	public Cursor bacaDataTerseleksi(long rowId) throws SQLException {
		Cursor cursor = crudDatabase.query(true, "tbl_kamus",FIELD_TABEL_KAMUS,"_id =" + rowId,null, null, null, null, null);
		cursor.moveToFirst();
		return cursor;
	}

	public ContentValues ambilData(String nama, String keterangan, String gambar) {
		ContentValues values = new ContentValues();
		values.put("nama", nama);
		values.put("keterangan", keterangan);
		values.put("gambar", gambar);
		return values;
	}
}
