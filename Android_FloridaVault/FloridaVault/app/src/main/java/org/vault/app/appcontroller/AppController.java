package org.vault.app.appcontroller;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.ncsavault.floridavault.R;

import net.hockeyapp.android.CrashManager;

import org.vault.app.dto.User;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.imagecache.BitmapLruCache;
import org.vault.app.imagecache.ImageCacheManager;
import org.vault.app.imagecache.RequestManager;
import org.vault.app.service.AndroidServiceContext;
import org.vault.app.service.ServiceManager;
import org.vault.app.serviceimpl.AbstractServiceManagerImpl;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class AppController extends Application {

	// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
	public static final String TAG = AppController.class.getSimpleName();

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;

	private static AppController mInstance;

	private static int DISK_IMAGECACHE_SIZE = 1024*1024*10;
	private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
	private static int DISK_IMAGECACHE_QUALITY = 100;  //PNG is lossless so quality is ignored but must be provided


	private AndroidServiceContext serviceContext;
	private ServiceManager serviceManager;
	/**
	 * set it as true only when all the screens are refreshed once app came to
	 * foreground after 2 hours.
	 */
	private boolean isAllScreenRefreshed = true;
	private boolean isDataRefreshed = false;
	private long cacheClearTime = 999999999999999L;

	private ArrayList<String> API_URLS = new ArrayList<>();

	public ArrayList<String> getAPI_URLS() {
		return API_URLS;
	}

	public void setAPI_URLS(ArrayList<String> API_URLS) {
		this.API_URLS = API_URLS;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		CrashManager.initialize(this, GlobalConstants.HOCKEY_APP_ID, null);

		TwitterAuthConfig authConfig = new TwitterAuthConfig(GlobalConstants.TWITTER_CONSUMER_KEY, GlobalConstants.TWITTER_CONSUMER_SECRET);
		Fabric.with(this, new Twitter(authConfig));
		mInstance = this;
		FlurryAgent.init(this, GlobalConstants.FLURRY_KEY);
		/*FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/Roboto-Medium.ttf");
		FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/Roboto-Medium.ttf");
		FontsOverride.setDefaultFont(this, "SERIF", "fonts/Roboto-Medium.ttf");
		FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/Roboto-Medium.ttf");*/

		FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/Roboto-Regular.ttf");
		FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/Roboto-Regular.ttf");
		FontsOverride.setDefaultFont(this, "SERIF", "fonts/Roboto-Regular.ttf");
		FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/Roboto-Regular.ttf");

		init();
	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}
		return mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(this.mRequestQueue,
					new BitmapLruCache());
		}
		return this.mImageLoader;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}


	private PowerManager.WakeLock wakeLock;

	@SuppressWarnings("deprecation")
	public void acquireWakeLock(Context context) {
		if (wakeLock != null)
			wakeLock.release();

		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);

		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, "WakeLock");

		wakeLock.acquire();
	}

	public void releaseWakeLock() {
		if (wakeLock != null)
			wakeLock.release();
		wakeLock = null;
	}




	/**
	 * Intialize the request manager and the image cache
	 */
	private void init() {
		RequestManager.init(this);
		createImageCache();

		new AsyncTask<Void, Void, Void>() {

			protected void onPreExecute() {
			};

			@Override
			protected Void doInBackground(Void... params) {
				synchronized (AppController.this) {
					try {
						serviceManager = new AbstractServiceManagerImpl(
								serviceContext);

					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					return null;
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
			}

		}.execute(null, null, null);
	}

	public String getDeviceId(){
        /**/
		String deviceID = Settings.Secure.getString(this.getContentResolver(),
				Settings.Secure.ANDROID_ID);
		return deviceID;
	}

	public long getUserId(){
		SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
		long userId = pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0);
		return userId;
	}

	public String getFirstName(){
		SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
		String firstName = pref.getString(GlobalConstants.PREF_VAULT_USER_FIRST_NAME, "");
		return firstName;
	}

	public String getLastName(){
		SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
		String lastName = pref.getString(GlobalConstants.PREF_VAULT_USER_LAST_NAME, "");
		return lastName;
	}

	public String getEmailAddress(){
		SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
		String email = pref.getString(GlobalConstants.PREF_VAULT_USER_EMAIL, "");
		return email;
	}

	public void storeUserDataInPreferences(User userDto){
		SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
		pref.edit().putLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, userDto.getUserID()).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_EMAIL, userDto.getEmailID()).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_NAME, userDto.getUsername()).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_FIRST_NAME, userDto.getFname()).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_LAST_NAME, userDto.getLname()).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_BIO_TEXT, userDto.getBiotext()).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_IMAGE_URL, userDto.getImageurl()).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_GENDER, userDto.getGender()).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_FLAG_STATUS, userDto.getFlagStatus()).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_PASSWORD, userDto.getPasswd()).commit();
		pref.edit().putInt(GlobalConstants.PREF_VAULT_USER_AGE, userDto.getAge()).commit();
	}

	public User getUserData(){
		User userDto = new User();
		SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
		userDto.setUserID(pref.getLong(GlobalConstants.PREF_VAULT_USER_ID_LONG, 0));
		userDto.setUsername(pref.getString(GlobalConstants.PREF_VAULT_USER_NAME, ""));
		userDto.setEmailID(pref.getString(GlobalConstants.PREF_VAULT_USER_EMAIL, ""));
		userDto.setFname(pref.getString(GlobalConstants.PREF_VAULT_USER_FIRST_NAME, ""));
		userDto.setLname(pref.getString(GlobalConstants.PREF_VAULT_USER_LAST_NAME, ""));
		userDto.setBiotext(pref.getString(GlobalConstants.PREF_VAULT_USER_BIO_TEXT, ""));
		userDto.setGender(pref.getString(GlobalConstants.PREF_VAULT_USER_GENDER, ""));
		userDto.setAge(pref.getInt(GlobalConstants.PREF_VAULT_USER_AGE, 0));
		userDto.setImageurl(pref.getString(GlobalConstants.PREF_VAULT_USER_IMAGE_URL, ""));
		userDto.setFlagStatus(pref.getString(GlobalConstants.PREF_VAULT_USER_FLAG_STATUS, ""));
		userDto.setPasswd(pref.getString(GlobalConstants.PREF_VAULT_USER_PASSWORD, ""));
		userDto.setAppID(GlobalConstants.APP_ID);
		return userDto;
	}

	public void updateUserData(String Username, String FName,String LName, String BioText, String ImageUrl){
		// update 5 fields in the preferences Username, FName, LName, BioText, ImageUrl
		SharedPreferences pref = getSharedPreferences(GlobalConstants.PREF_PACKAGE_NAME, Context.MODE_PRIVATE);
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_NAME, Username).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_FIRST_NAME, FName).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_LAST_NAME, LName).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_BIO_TEXT, BioText).commit();
		pref.edit().putString(GlobalConstants.PREF_VAULT_USER_IMAGE_URL, ImageUrl).commit();
	}

	public ServiceManager getServiceManager() {
		while (serviceManager == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		synchronized (this) {
			return serviceManager;
		}
	}

	/**
	 * Create the image cache.
	 */
	private void createImageCache(){
		ImageCacheManager.getInstance().init(this,
				this.getPackageCodePath()
				, DISK_IMAGECACHE_SIZE
				, DISK_IMAGECACHE_COMPRESS_FORMAT
				, DISK_IMAGECACHE_QUALITY);
	}


	public boolean isAllScreenRefreshed() {
		return isAllScreenRefreshed;
	}

	public void setAllScreenRefreshed(boolean isAllScreenRefreshed) {
		this.isAllScreenRefreshed = isAllScreenRefreshed;
	}

	public long getCacheClearTime() {
		return cacheClearTime;
	}

	public void setCacheClearTime(long cacheClearTime) {
		this.cacheClearTime = cacheClearTime;
	}

	public boolean isDataRefreshed() {
		return isDataRefreshed;
	}

	public void setDataRefreshed(boolean isDataRefreshed) {
		this.isDataRefreshed = isDataRefreshed;
	}

	public View showRelatedVideoLoader(Activity context, boolean showLogo) {
		View view = context.getLayoutInflater().inflate(R.layout.progress_bar, null);
		ProgressBar pBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			pBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar_lower));
		}else{
			pBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar));
		}
//        pBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#CC0000"), android.graphics.PorterDuff.Mode.MULTIPLY);
		ImageView imgView = (ImageView) view.findViewById(R.id.img_circular);

		Point size = new Point();
		WindowManager w = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			w.getDefaultDisplay().getSize(size);
			width = size.x;
			// Measuredheight = size.y;
		} else {
			Display d = w.getDefaultDisplay();
			// Measuredheight = d.getHeight();
			width = d.getWidth();
		}

		int dimension = (int) (width * 0.10);

		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			dimension = (int) (width * 0.15);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dimension, dimension);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		imgView.setLayoutParams(lp);
		imgView.setAdjustViewBounds(true);
		imgView.setImageBitmap(setCircularBitmap());

		lp = new RelativeLayout.LayoutParams(dimension + 35, dimension + 35);
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			lp = new RelativeLayout.LayoutParams(dimension + 45, dimension + 45);
		}

		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		pBar.setLayoutParams(lp);

		if(!showLogo)
			imgView.setVisibility(View.GONE);
		return view;
	}

	public View setViewToProgressDialog(Activity context) {
		View view = context.getLayoutInflater().inflate(R.layout.progress_bar, null);
		ProgressBar pBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			pBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar_lower));
		}else{
			pBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circle_progress_bar));
		}
//        pBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#CC0000"), android.graphics.PorterDuff.Mode.MULTIPLY);
		ImageView imgView = (ImageView) view.findViewById(R.id.img_circular);

		Point size = new Point();
		WindowManager w = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			w.getDefaultDisplay().getSize(size);
			width = size.x;
			// Measuredheight = size.y;
		} else {
			Display d = w.getDefaultDisplay();
			// Measuredheight = d.getHeight();
			width = d.getWidth();
		}

		int dimension = (int) (width * 0.10);

		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			dimension = (int) (width * 0.15);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dimension, dimension);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		imgView.setLayoutParams(lp);
		imgView.setAdjustViewBounds(true);
		imgView.setImageBitmap(setCircularBitmap());

		lp = new RelativeLayout.LayoutParams(dimension + 35, dimension + 35);
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			lp = new RelativeLayout.LayoutParams(dimension + 45, dimension + 45);
		}

		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		pBar.setLayoutParams(lp);

		return view;
	}

	public Bitmap setCircularBitmap() {
		Point size = new Point();
		WindowManager w = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			w.getDefaultDisplay().getSize(size);
			width = size.x;
			// Measuredheight = size.y;
		} else {
			Display d = w.getDefaultDisplay();
			// Measuredheight = d.getHeight();
			width = d.getWidth();
		}

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.vault_logo_glare);
		final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(output);

		final int color = Color.RED;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawOval(rectF, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		bitmap.recycle();
		return output;
	}
}
