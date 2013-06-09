package dlmbg.pckg.kamus.teknologi;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionStore;

public class MainActivity extends Activity {
	private Facebook mFacebook;
	private CheckBox mFacebookBtn;
	private ProgressDialog mProgress;
	
	private static final String[] PERMISSIONS = new String[] {"publish_stream", "read_stream", "offline_access"};
	
	private static final String APP_ID = "635684659793576";
	SessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
 		Button tmb_cari = (Button) findViewById(R.id.btn_cari_data);
 		tmb_cari.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
        		StartSearch();
 			}
 		});
        
 		Button tmb_browse = (Button) findViewById(R.id.btn_browse_data);
 		tmb_browse.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
        		StartBrowse();
 			}
 		});
        
 		Button tmb_dashboard = (Button) findViewById(R.id.btn_login);
 		
 		session = new SessionManager(getApplicationContext());

		if (session.isLoggedIn() == true) 
		{
			tmb_dashboard.setText("Log Out");
		}	
		
 		tmb_dashboard.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				StartAdmin();
 			}
 		});
        
 		Button tmb_about = (Button) findViewById(R.id.btn_about);
 		tmb_about.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				StartAbout();
 			}
 		});
 		

        
        mFacebookBtn	= (CheckBox) findViewById(R.id.cb_facebook);
        
        mProgress		= new ProgressDialog(this);
        mFacebook		= new Facebook(APP_ID);
        
        SessionStore.restore(mFacebook, this);
        
        if (mFacebook.isSessionValid()) {
			mFacebookBtn.setChecked(true);
			
			String name = SessionStore.getName(this);
			name		= (name.equals("")) ? "Unknown" : name;
			
			mFacebookBtn.setText("  Facebook (" + name + ")");
			mFacebookBtn.setTextColor(Color.BLACK);
		}
        
        mFacebookBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onFacebookClick();
			}
		});
	}
    
    private void onFacebookClick() {
 		if (mFacebook.isSessionValid()) {
 			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			
 			builder.setMessage("Delete current Facebook connection?")
 			       .setCancelable(false)
 			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	   fbLogout();
 			           }
 			       })
 			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			                
 			                mFacebookBtn.setChecked(true);
 			           }
 			       });
 			
 			final AlertDialog alert = builder.create();
 			
 			alert.show();
 		} else {
 			mFacebookBtn.setChecked(false);
 			
 			mFacebook.authorize(this, PERMISSIONS, -1, new FbLoginDialogListener());
 		}
 	}
     
     private final class FbLoginDialogListener implements DialogListener {
         public void onComplete(Bundle values) {
             SessionStore.save(mFacebook, MainActivity.this);
            
             mFacebookBtn.setText("  Facebook (No Name)");
             mFacebookBtn.setChecked(true);
 			mFacebookBtn.setTextColor(Color.BLACK);
 			 
             getFbName();
         }

         public void onFacebookError(FacebookError error) {
            Toast.makeText(MainActivity.this, "Facebook connection failed", Toast.LENGTH_SHORT).show();
            
            mFacebookBtn.setChecked(false);
         }
         
         public void onError(DialogError error) {
         	Toast.makeText(MainActivity.this, "Facebook connection failed", Toast.LENGTH_SHORT).show(); 
         	
         	mFacebookBtn.setChecked(false);
         }

         public void onCancel() {
         	mFacebookBtn.setChecked(false);
         }
     }
     
 	private void getFbName() {
 		mProgress.setMessage("Finalizing ...");
 		mProgress.show();
 		
 		new Thread() {
 			@Override
 			public void run() {
 		        String name = "";
 		        int what = 1;
 		        
 		        try {
 		        	String me = mFacebook.request("me");
 		        	
 		        	JSONObject jsonObj = (JSONObject) new JSONTokener(me).nextValue();
 		        	name = jsonObj.getString("name");
 		        	what = 0;
 		        } catch (Exception ex) {
 		        	ex.printStackTrace();
 		        }
 		        
 		        mFbHandler.sendMessage(mFbHandler.obtainMessage(what, name));
 			}
 		}.start();
 	}
 	
 	private void fbLogout() {
 		mProgress.setMessage("Disconnecting from Facebook");
 		mProgress.show();
 			
 		new Thread() {
 			@Override
 			public void run() {
 				SessionStore.clear(MainActivity.this);
 		        	   
 				int what = 1;
 					
 		        try {
 		        	mFacebook.logout(MainActivity.this);
 		        		 
 		        	what = 0;
 		        } catch (Exception ex) {
 		        	ex.printStackTrace();
 		        }
 		        	
 		        mHandler.sendMessage(mHandler.obtainMessage(what));
 			}
 		}.start();
 	}
 	
 	private Handler mFbHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			mProgress.dismiss();
 			
 			if (msg.what == 0) {
 				String username = (String) msg.obj;
 		        username = (username.equals("")) ? "No Name" : username;
 		            
 		        SessionStore.saveName(username, MainActivity.this);
 		        
 		        mFacebookBtn.setText("  Facebook (" + username + ")");
 		         
 		        Toast.makeText(MainActivity.this, "Connected to Facebook as " + username, Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(MainActivity.this, "Connected to Facebook", Toast.LENGTH_SHORT).show();
 			}
 		}
 	};
 	
 	private Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			mProgress.dismiss();
 			
 			if (msg.what == 1) {
 				Toast.makeText(MainActivity.this, "Facebook logout failed", Toast.LENGTH_SHORT).show();
 			} else {
 				mFacebookBtn.setChecked(false);
 	        	mFacebookBtn.setText("  Facebook (Not connected)");
 	        	mFacebookBtn.setTextColor(Color.GRAY);
 	        	   
 				Toast.makeText(MainActivity.this, "Disconnected from Facebook", Toast.LENGTH_SHORT).show();
 			}
 		}
 	};
    
    public void StartBrowse() {
		Intent intent = new Intent(this, BrowseData.class);
		startActivity(intent);
	}
    
    public void StartSearch() {
		Intent intent = new Intent(this, SearchData.class);
		startActivity(intent);
	}
    
    public void StartAbout() {
		Intent intent = new Intent(this, About.class);
		startActivity(intent);
	}
    
    public void StartAdmin() {
    	if(session.isLoggedIn() == true)
    	{
    		session.logoutUser();
    		finish();
    	}
    	else
    	{
    		Intent intent = new Intent(this, Admin.class);
    		startActivity(intent);
    		finish();
    	}
	}

}
