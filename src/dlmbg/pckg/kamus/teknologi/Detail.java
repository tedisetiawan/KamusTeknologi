package dlmbg.pckg.kamus.teknologi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.SessionStore;

public class Detail extends Activity{

	private TextView nama_et, keterangan_et;
	private ImageView gambar_iv;
	
	String var_nama, var_keterangan, var_gambar;

	Button btnShare;
	
	private Facebook mFacebook;
	private CheckBox mFacebookCb;
	private ProgressDialog mProgress;
	
	private Handler mRunOnUi = new Handler();
	
	private static final String APP_ID = "635684659793576";
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.detail);

		nama_et = (TextView) findViewById(R.id.nama_detail);
		keterangan_et = (TextView) findViewById(R.id.keterangan_detail);
		gambar_iv = (ImageView) findViewById(R.id.gambar_detail);
		
		Bundle extras = getIntent().getExtras();
		var_nama = extras.getString("nama");
		var_keterangan = extras.getString("keterangan");
		var_gambar = extras.getString("gambar");

		nama_et.setText(var_nama);
		keterangan_et.setText(var_keterangan);
		
		Bitmap bmImg = BitmapFactory.decodeFile(var_gambar);
		gambar_iv.setImageBitmap(bmImg);
		
		String imageInSD = var_gambar;      
		Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
		gambar_iv.setImageBitmap(bitmap);
		
		mFacebookCb				  = (CheckBox) findViewById(R.id.cb_facebook);
		
		mProgress	= new ProgressDialog(this);
		
		mFacebook 	= new Facebook(APP_ID);
		
		SessionStore.restore(mFacebook, this);

		if (mFacebook.isSessionValid()) {
			mFacebookCb.setChecked(true);
				
			String name = SessionStore.getName(this);
			name		= (name.equals("")) ? "Unknown" : name;
				
			mFacebookCb.setText("  Facebook  (" + name + ")");
		}
		
		 btnShare = (Button) findViewById(R.id.btn_share);
			btnShare.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					String review = var_nama+"\n\n"+var_keterangan;
					
					if (review.equals("")) return;
				
					if (mFacebookCb.isChecked()) postToFacebook(review);
				}
			});
	}
	
	private void postToFacebook(String review) {	
		mProgress.setMessage("Posting ...");
		mProgress.show();
		
		AsyncFacebookRunner mAsyncFbRunner = new AsyncFacebookRunner(mFacebook);
		
		Bundle params = new Bundle();
    		
		params.putString("message", review);
		
		mAsyncFbRunner.request("me/feed", params, "POST", new WallPostListener());
	}

	private final class WallPostListener extends BaseRequestListener {
        public void onComplete(final String response) {
        	mRunOnUi.post(new Runnable() {
        		@Override
        		public void run() {
        			mProgress.cancel();
        			
        			Toast.makeText(Detail.this, "Posted to Facebook", Toast.LENGTH_SHORT).show();
        		}
        	});
        }
    }
}
