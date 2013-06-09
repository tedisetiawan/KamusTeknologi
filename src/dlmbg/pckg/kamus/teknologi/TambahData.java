package dlmbg.pckg.kamus.teknologi;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/*
 * Gede Lumbung - 2013
 * http://gedelumbung.com
 */

public class TambahData extends Activity {

	private SqliteManager sqliteDB;
	private Long id;
	public static final String SIMPAN_DATA = "simpan";
	
	private ImageView mImageView;	
	private Uri mImageCaptureUri;	
	
	private static final int PICK_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 2;
	
    private EditText EdNama,EdKeterangan,EdGambar;
	String var_nama,var_keterangan,var_gambar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_data);

        EdNama = (EditText) findViewById(R.id.ed_nama);
        EdKeterangan = (EditText) findViewById(R.id.ed_keterangan);
        EdGambar = (EditText) findViewById(R.id.ed_gambar);
        
		mImageView = (ImageView) findViewById(R.id.iv_pic);
        
        id = null;

		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null && extras.containsKey(BrowseData.EXTRA_ROWID)) {
				id = extras.getLong(BrowseData.EXTRA_ROWID);
			}
			else
			{
				var_nama = extras.getString("nama");
				var_keterangan = extras.getString("keterangan");
				var_gambar = extras.getString("gambar");
			}
		}
		
		sqliteDB = new SqliteManager(this);
		sqliteDB.bukaKoneksi();
		
        pindahData();

		Button button = (Button) findViewById(R.id.btn_simpan);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				simpan();
				finish();
			}
		});
		

		final String [] items			= new String [] {"From Camera", "From SD Card"};				
		ArrayAdapter<String> adapter	= new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
		AlertDialog.Builder builder		= new AlertDialog.Builder(this);
		
		builder.setTitle("Select Image");
		builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int item ) {
				if (item == 0) {
					Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File file		 = new File(Environment.getExternalStorageDirectory().toString(),
							   			"app_wisata_kuliner/tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
					mImageCaptureUri = Uri.fromFile(file);

					try {			
						intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
						intent.putExtra("return-data", true);
						
						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (Exception e) {
						e.printStackTrace();
					}			
					
					dialog.cancel();
				} else {
					Intent intent = new Intent();
					
	                intent.setType("image/*");
	                intent.setAction(Intent.ACTION_GET_CONTENT);
	                
	                startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
				}
			}
		} );
		
		final AlertDialog dialog = builder.create();
		
		((Button) findViewById(R.id.btn_choose)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});
    }
	
    @Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    if (resultCode != RESULT_OK) return;
 	   
 		Bitmap bitmap 	= null;
 		String path		= "";
 		
 		if (requestCode == PICK_FROM_FILE) {
 			mImageCaptureUri = data.getData(); 
 			path = getRealPathFromURI(mImageCaptureUri); //from Gallery 
 		
 			if (path == null)
 				path = mImageCaptureUri.getPath(); //from File Manager
 			
 			if (path != null) 
 				bitmap 	= BitmapFactory.decodeFile(path);
 		} else {
 			path	= mImageCaptureUri.getPath();
 			bitmap  = BitmapFactory.decodeFile(path);
 		}
 		EdGambar.setText(path);
 		mImageView.setImageBitmap(bitmap);		
 	}
 	
 	public String getRealPathFromURI(Uri contentUri) {
         String [] proj 		= {MediaStore.Images.Media.DATA};
         @SuppressWarnings("deprecation")
 		Cursor cursor 		= managedQuery( contentUri, proj, null, null,null);
         
         if (cursor == null) return null;
         
         int column_index 	= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         
         cursor.moveToFirst();

         return cursor.getString(column_index);
 	}
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		sqliteDB.tutupKoneksi();
	}

	private void pindahData() {
		if (id != null) {
			Cursor cursor = sqliteDB.bacaDataTerseleksi(id);
			EdNama.setText(cursor.getString(1));
			EdKeterangan.setText(cursor.getString(2));
			EdGambar.setText(cursor.getString(3));
			Bitmap bitmap = BitmapFactory.decodeFile(cursor.getString(3));
			mImageView.setImageBitmap(bitmap);
			cursor.close();
		}
	}

	private void simpan() {
		String nama_in = EdNama.getText().toString();
		String keterangan_in = EdKeterangan.getText().toString();
		String gambar_in = EdGambar.getText().toString();

		if (id != null) {
			sqliteDB.updateData(id, sqliteDB.ambilData(nama_in, keterangan_in, gambar_in),"tbl_kamus","_id");
		}
		else {
			id = sqliteDB.insertData(sqliteDB.ambilData(nama_in, keterangan_in, gambar_in),"tbl_kamus");
		}
	}
}
