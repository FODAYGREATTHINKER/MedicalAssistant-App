package architect.jazzy.medicinereminder.MedicalAssistant.Activities;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import architect.jazzy.medicinereminder.BuildConfig;
import architect.jazzy.medicinereminder.HelperClasses.Constants;
import architect.jazzy.medicinereminder.HelperClasses.FirebaseConstants;
import architect.jazzy.medicinereminder.MedicalAssistant.CustomComponents.FragmentBackStack;
import architect.jazzy.medicinereminder.MedicalAssistant.CustomViews.ColorSelectorFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.CustomViews.DaySelectorFragmentDialog;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.AddDoctorFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.AddMedicineFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.BrowserFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.DashboardFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.DoctorListFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.EmojiSelectFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.MedicineListFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.NewsFragments.NewsListFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.SearchFragments.SearchFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Fragments.SettingsFragment;
import architect.jazzy.medicinereminder.MedicalAssistant.Models.Doctor;
import architect.jazzy.medicinereminder.MedicalAssistant.Models.FeedItem;
import architect.jazzy.medicinereminder.MedicalAssistant.Models.Medicine;
import architect.jazzy.medicinereminder.MedicalAssistant.Services.AlarmSetterService;
import architect.jazzy.medicinereminder.R;
import architect.jazzy.medicinereminder.RemedySharing.Fragments.UserDetailsFragment;
import architect.jazzy.medicinereminder.RemedySharing.OnlineActivity;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
    EmojiSelectFragment.OnFragmentInteractionListener, DaySelectorFragmentDialog.OnFragmentInteractionListener,
    MedicineListFragment.FragmentInteractionListener, AddMedicineFragment.FragmentInteractionListener,
    NewsListFragment.FeedClickListener, DoctorListFragment.OnMenuItemClickListener,
    DoctorListFragment.OnFragmentInteractionListenr, DashboardFragment.OnFragmentInteractionListener,
    AddDoctorFragment.OnFragmentInteractionListener, ColorSelectorFragment.OnColorChangeListener {

  public static final String TAG = "MainActivity";
  private static final int WRITE_PERMISSION_CODE = 8529;
  private static final int NAV_BEFORE_AD_SHOW = 6;
  public static boolean shouldShowInterstitial = true;
  final int SHOW_LIST_REQUEST_CODE = 9865;
  FragmentBackStack fragmentBackStack = new FragmentBackStack();
  FrameLayout frameLayout;
  DrawerLayout drawerLayout;
  ActivityClickListener activityClickListener;
  EditText searchQuery;
  NavigationView navigationView;
  Toolbar toolbar;
  FirebaseRemoteConfig mFirebaseRemoteConfig;
  SharedPreferences firebasePrefs;
  int toolbarColor = 0;
  int stepCount = 0;
  long cacheExpiration;
  int uiOptions;
  ActivityKeyClickListener activityKeyClickListener;
  ActivityResultListener activityResultListener;
  private InterstitialAd interstitialAd;
  private int moveToFragmentId = R.id.circularTest;
  private AdRequest adRequest;

  public static void setAlarm(final Context context) {
    Thread alarmStartThread = new Thread(new Runnable() {
      @Override
      public void run() {
        Intent startAlarmServiceIntent = new Intent(context, AlarmSetterService.class);
        startAlarmServiceIntent.setAction("CREATE");
        context.startService(startAlarmServiceIntent);
      }
    });
    alarmStartThread.start();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!getSharedPreferences("illustration", MODE_PRIVATE).getBoolean("shown1", false)) {
      startActivity(new Intent(this, Illustration.class));
      finish();
      return;
    }
    setContentView(R.layout.activity_main);

    toolbar = (Toolbar) findViewById(R.id.toolbar);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
    navigationView = (NavigationView) findViewById(R.id.navigationView);
    frameLayout = (FrameLayout) findViewById(R.id.frame);

    displayFragment(new DashboardFragment(), true);

    firebasePrefs = getSharedPreferences(FirebaseConstants.RemoteConfig.PREFERENCE_NAME, MODE_PRIVATE);

    setSupportActionBar(toolbar);
    toolbarColor = Constants.getThemeColor(this);
    toolbar.setBackgroundColor(toolbarColor);

    ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
    drawerLayout.setDrawerListener(drawerToggle);
    drawerToggle.syncState();


    //Refresh Alarms on different thread to speedup process
    setAlarm(this);

    //View setup

    Menu menu = navigationView.getMenu();
    menu.getItem(0).setVisible(firebasePrefs.getBoolean(FirebaseConstants.RemoteConfig.REMEDY_ENABLED, BuildConfig.DEBUG));
    menu.getItem(6).setVisible(firebasePrefs.getBoolean(FirebaseConstants.RemoteConfig.USER_LOGIN_ENABLED, BuildConfig.DEBUG));
    menu.getItem(5).setVisible(!firebasePrefs.getBoolean(FirebaseConstants.RemoteConfig.USER_LOGIN_ENABLED, BuildConfig.DEBUG));

    View headerLayout = navigationView.getHeaderView(0);
    searchQuery = (EditText) headerLayout.findViewById(R.id.searchQuery);
    headerLayout.findViewById(R.id.searchButton).setVisibility(View.GONE);
    navigationView.setNavigationItemSelectedListener(this);

    headerLayout.findViewById(R.id.back).setBackgroundColor(Constants.getThemeColor(this));

    //Search from nav bar handling
    searchQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          performSearch(v.getText().toString());
          searchQuery.setText("");
          hideKeyboard();
        }
        return false;
      }
    });

    dimNotificationBar();

    //Ask runtime permission if android version above lollipop
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
          ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
          AlertDialog.Builder builder = new AlertDialog.Builder(this, 0);
          builder.setTitle("Permission required")
              .setMessage("Permission is required for caching, reduce internet data usage and display images.")
              .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  ActivityCompat.requestPermissions(MainActivity.this,
                      new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                      WRITE_PERMISSION_CODE);
                }
              })
              .show();

        } else {
          ActivityCompat.requestPermissions(MainActivity.this,
              new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
              WRITE_PERMISSION_CODE);
        }
      }
    }

    shouldShowInterstitial = firebasePrefs.getBoolean(FirebaseConstants.RemoteConfig.AD_DASHBOARD_INTERSTITIAL_ENABLED, true);

    interstitialAd = new InterstitialAd(this);
    interstitialAd.setAdUnitId(FirebaseConstants.Ads.DASHBOARD_INTERSTITIAL);
    AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
    if (BuildConfig.DEBUG) {
      adRequestBuilder.addTestDevice("5C8BFD2BD4F4C415F7456E231E186EE5");
      adRequestBuilder.addTestDevice("2EDDA47AED66B1BF9537214AF158BBE2");
    }
    adRequest = adRequestBuilder.build();
//    if (shouldShowInterstitial) {
      interstitialAd.loadAd(adRequest);
//    }
    interstitialAd.setAdListener(new AdListener() {
      @Override
      public void onAdClosed() {
        Log.e(TAG, "onAdClosed");
        moveToFragment();
      }

      @Override
      public void onAdLoaded() {
        super.onAdLoaded();
        Log.e(TAG, "onAdLoaded");
      }

      @Override
      public void onAdOpened() {
        super.onAdOpened();
        Log.e(TAG, "onAdOpened");
      }

      @Override
      public void onAdLeftApplication() {
        super.onAdLeftApplication();
        Log.e(TAG, "onAdLefApplication");
      }

      @Override
      public void onAdFailedToLoad(int i) {
        super.onAdFailedToLoad(i);
        Log.e(TAG, "onAdFailedToLoad: "+i);
      }
    });

    mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
        .setDeveloperModeEnabled(BuildConfig.DEBUG)
        .build();
    mFirebaseRemoteConfig.setConfigSettings(configSettings);
    cacheExpiration = BuildConfig.DEBUG ? 0 : 12 * 60 * 60;
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    mFirebaseRemoteConfig.fetch(cacheExpiration)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            if (task.isComplete()) {
              Log.e(TAG, "Remote config task complete");
              mFirebaseRemoteConfig.activateFetched();
              setupFirebaseConfig();
              return;
            }
            Log.e(TAG, "Remote config task failed");
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Log.e(TAG, "Remote config on failure: " + e.getMessage());
            e.printStackTrace();
          }
        });

  }

  @Override
  public void onThemeColorChange(int color) {
    toolbar.setBackgroundColor(color);
    getSharedPreferences(Constants.SETTING_PREF, MODE_PRIVATE)
        .edit()
        .putInt(Constants.THEME_COLOR, color)
        .apply();
    findViewById(R.id.back).setBackgroundColor(Constants.getThemeColor(this));

    setSupportActionBar(toolbar);
    ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
    drawerLayout.setDrawerListener(drawerToggle);
    drawerToggle.syncState();
  }

  private void setupFirebaseConfig() {
    firebasePrefs.edit()
        .putBoolean(FirebaseConstants.RemoteConfig.USER_LOGIN_ENABLED, mFirebaseRemoteConfig.getBoolean(FirebaseConstants.RemoteConfig.ServerKeys.USER_ENABLED))
        .putBoolean(FirebaseConstants.RemoteConfig.REMEDY_VOTE_ENABLED, mFirebaseRemoteConfig.getBoolean(FirebaseConstants.RemoteConfig.ServerKeys.VOTE_ENABLED))
        .putBoolean(FirebaseConstants.RemoteConfig.REMEDY_COMMENT_ENABLED, mFirebaseRemoteConfig.getBoolean(FirebaseConstants.RemoteConfig.ServerKeys.COMMENT_ENABLED))
        .putBoolean(FirebaseConstants.RemoteConfig.REMEDY_ENABLED, mFirebaseRemoteConfig.getBoolean(FirebaseConstants.RemoteConfig.ServerKeys.REMEDY_ENABLED))
        .putBoolean(FirebaseConstants.RemoteConfig.AD_DASHBOARD_INTERSTITIAL_ENABLED, mFirebaseRemoteConfig.getBoolean(FirebaseConstants.RemoteConfig.ServerKeys.DASHBOARD_INTERSTITIAL_ENABLED))
        .apply();

    shouldShowInterstitial = firebasePrefs.getBoolean(FirebaseConstants.RemoteConfig.AD_DASHBOARD_INTERSTITIAL_ENABLED, true);
    if (shouldShowInterstitial) {
      interstitialAd.loadAd(adRequest);
    }

    Menu menu = navigationView.getMenu();
    menu.getItem(0).setVisible(firebasePrefs.getBoolean(FirebaseConstants.RemoteConfig.REMEDY_ENABLED, BuildConfig.DEBUG));
    menu.getItem(6).setVisible(firebasePrefs.getBoolean(FirebaseConstants.RemoteConfig.USER_LOGIN_ENABLED, BuildConfig.DEBUG));
    menu.getItem(5).setVisible(!firebasePrefs.getBoolean(FirebaseConstants.RemoteConfig.USER_LOGIN_ENABLED, BuildConfig.DEBUG));
  }

  public Toolbar getToolbar() {
    return toolbar;
  }

  private void dimNotificationBar() {
    final View decorView = getWindow().getDecorView();
    uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
    decorView.setSystemUiVisibility(uiOptions);
    decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
      @Override
      public void onSystemUiVisibilityChange(int visibility) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
          @Override
          public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                decorView.setSystemUiVisibility(uiOptions);
              }
            });
          }
        }, 5000);
      }
    });
  }

  private void performSearch(String s) {
    displayFragment(SearchFragment.initiate(s), true);
    drawerLayout.closeDrawers();
  }

  @Override
  public void onBackPressed() {
    if (activityKeyClickListener != null) {
      if (activityKeyClickListener.onBackKeyPressed()) {
        return;
      }
    }
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawers();
    } else if (!fragmentBackStack.empty()) {
      toolbar.setBackgroundColor(toolbarColor);
      Fragment fragment = fragmentBackStack.pop();
      if (fragment == null) {
        android.support.v4.app.Fragment fragment1 = fragmentBackStack.popSupport();
        if (fragment1 == null) {
          super.onBackPressed();
          return;
        }
        displaySupportFragment(fragment1, false);
        return;
      }
      displayFragment(fragment, false);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    AddMedicineFragment.setAlarm(MainActivity.this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    getSharedPreferences(Constants.INTERNAL_PREF, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(Constants.SEARCH_RESULT, false)
        .apply();

    //Delete Search Cache
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmpMR");
//                File file = new File(folder, Constants.SEARCH_FILE_NAME);
//                if (file.exists()) {
//                    try {
//                        file.delete();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        thread.start();

  }

  @Override
  public boolean onNavigationItemSelected(MenuItem menuItem) {
    int id = menuItem.getItemId();

    menuItem.setChecked(true);
    drawerLayout.closeDrawers();
    moveToFragmentId = id;

    if (shouldShowInterstitial && interstitialAd.isLoaded()) {
      interstitialAd.show();
      return false;
    }
    moveToFragment();

    return false;
  }

  void moveToFragment() {
    stepCount++;
    shouldShowInterstitial = (stepCount % NAV_BEFORE_AD_SHOW == 1) && firebasePrefs.getBoolean(FirebaseConstants.RemoteConfig.AD_DASHBOARD_INTERSTITIAL_ENABLED, true);
    Fragment fragment = null;
    android.support.v4.app.Fragment supportFragment = null;
    try {
      fragment = getFragmentManager().findFragmentById(R.id.frame);
    } catch (Exception e) {
      supportFragment = getSupportFragmentManager().findFragmentById(R.id.frame);
    }
    toolbar.setBackgroundColor(toolbarColor);
    switch (moveToFragmentId) {
      case R.id.showMedicineList:
        if (!(fragment instanceof MedicineListFragment)) {
          FirebaseConstants.Analytics.logCurrentScreen(this, "MedicineList");
          displayFragment(new MedicineListFragment(), true);
        }
        break;
      case R.id.myAccount:
        FirebaseConstants.Analytics.logCurrentScreen(this, "OnlineActivity");
        Intent i = new Intent(this, OnlineActivity.class);
        i.putExtra("fragment", UserDetailsFragment.TAG);
        startActivity(i);
        break;
      case R.id.action_settings_dev:
      case R.id.action_settings:
        FirebaseConstants.Analytics.logCurrentScreen(this, "Settings");
        showSettings();
        break;
      case R.id.remedies:
        FirebaseConstants.Analytics.logCurrentScreen(this, "RemedyList");
        startActivity(new Intent(this, OnlineActivity.class));
        break;
      case R.id.news:
        if (!(fragment instanceof NewsListFragment)) {
          FirebaseConstants.Analytics.logCurrentScreen(this, "NewsList");
          displayFragment(new NewsListFragment(), true);
        }
        break;
      case R.id.addDoctor:
        if (!(fragment instanceof DoctorListFragment)) {
          FirebaseConstants.Analytics.logCurrentScreen(this, "DoctorList");
          displayFragment(new DoctorListFragment(), true);
        }
        break;
      case R.id.circularTest:
        if (!(fragment instanceof DashboardFragment)) {
          FirebaseConstants.Analytics.logCurrentScreen(this, "Dashboard");
          displayFragment(new DashboardFragment(), true);
        }
        break;
      case DashboardFragment.ADD_DOCTOR_ID:
        if (!(fragment instanceof AddDoctorFragment)) {
          FirebaseConstants.Analytics.logCurrentScreen(this, "Add Doctor");
          displayFragment(new AddDoctorFragment(), true);
        }
        break;
      case DashboardFragment.ADD_MEDICINE_ID:
        if (!(fragment instanceof AddMedicineFragment)) {
          FirebaseConstants.Analytics.logCurrentScreen(this, "Add Medicine");
          displayFragment(new AddMedicineFragment(), true);
        }
        break;
      case DashboardFragment.SEARCH_ID:
        if (!(fragment instanceof SearchFragment)) {
          FirebaseConstants.Analytics.logCurrentScreen(this, "Search");
          displayFragment(new SearchFragment(), true);
        }
      default:
        break;
    }
  }

  @Override
  public void performNavAction(int menuId) {
    moveToFragmentId = menuId;
    if (moveToFragmentId != R.id.action_settings) {
//            Log.e(TAG, "Performing nav action: "+shouldShowInterstitial);
      if (shouldShowInterstitial && interstitialAd.isLoaded()) {
        interstitialAd.show();
        return;
      }
    }
    moveToFragment();
  }

  void showSettings() {
    displayFragment(new SettingsFragment(), true);
  }

  void addMedicineToView(boolean add) {
    Fragment fragment = new AddMedicineFragment();
    displayFragment(fragment, add);
  }

  public void displayFragment(Fragment fragment) {
    displayFragment(fragment, false);
  }

  public void displayFragment(Fragment fragment, boolean add) {
    displayFragment(fragment, add, R.anim.fragment_in_from_left, R.anim.fragment_out_from_right);
  }

  public void displayFragment(Fragment fragment, boolean add, @AnimRes int enterAnim, @AnimRes int exitAnim) {
    Log.e(TAG, "display fragment");
    if (add) {
      addToBackStack();
    }
    toolbar.getMenu().clear();
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.setCustomAnimations(enterAnim,exitAnim);
    transaction.replace(R.id.frame, fragment);
    transaction.commit();
  }

  void addToBackStack() {
    try {
      Fragment fragment = getFragmentManager().findFragmentById(R.id.frame);
      if (fragment == null) {
        throw new Exception();
      }
//            if (!(fragment instanceof SearchFragment))
      fragmentBackStack.push(fragment);
      toolbar.getMenu().clear();
    } catch (Exception e) {
      android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame);
      if (fragment == null) {
        return;
      }
      fragmentBackStack.push(fragment);
    }
  }

  public void displaySupportFragment(android.support.v4.app.Fragment fragment, boolean add) {
    displaySupportFragment(fragment, add, R.anim.fragment_in_from_left, R.anim.fragment_out_from_left);
  }

  public void displaySupportFragment(android.support.v4.app.Fragment fragment, boolean add, @AnimRes int enterAnim, @AnimRes int exitAnim) {
    if (fragment == null) {
      return;
    }
    toolbar.getMenu().clear();
    if (add) {
      addToBackStack();
    }

    try {
      getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.frame)).commit();
    } catch (Exception e) {
      e.printStackTrace();
    }

    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
    android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.setCustomAnimations(enterAnim,exitAnim);
    transaction.replace(R.id.frame, fragment).commit();
  }

  void showCredits() {
    startActivity(new Intent(this, AboutUs.class));
  }

  public void showDaySelection(View v) {
    activityClickListener.daySelectionClick();
  }

  public void showFeed(String url) {
    displayFragment(BrowserFragment.getInstance(url), true);
  }

  public void showFeed(String url, boolean isNews) {
    displayFragment(BrowserFragment.getInstance(url, isNews), true);
  }

  @Override
  public void onEmojiSelected(int position) {
    activityClickListener.emojiClick(position);
  }

  @Override
  public void onDaySelectionChanged(int position, Boolean isChecked) {
    activityClickListener.daySelectionChanged(position, isChecked);
  }

  @Override
  public void addMedicine() {
    addMedicineToView(true);
  }

  @Override
  public void onAddDoctorClicked() {
    displayFragment(new AddDoctorFragment(), true);
  }

  @Override
  public void onDoctorSelected(Doctor doctor) {
//        displaySupportFragment(DoctorDetailFragment.newInstance(doctor), true);
    Bundle bundle = new Bundle();
    bundle.putParcelable("doctor", doctor);
    Intent intent = new Intent(this, DoctorDetail.class);
    intent.putExtras(bundle);
    startActivity(intent);


  }

  @Override
  public void addDoctor() {
    AddDoctorFragment fragment = new AddDoctorFragment();
    displayFragment(fragment, true);
  }

  @Override
  public void addMedicine(boolean addToBackStack) {
    addMedicineToView(addToBackStack);
  }

  @Override
  public void showDoctors() {
    displayFragment(new DoctorListFragment(), true);
  }

  @Override
  public void showDetails(int position, ArrayList<Medicine> medicines) {
    Intent i = new Intent(this, MedicineDetails.class);
    Bundle bundle = new Bundle();
    bundle.putParcelableArrayList(Constants.MEDICINE_NAME_LIST, medicines);
    i.putExtra(Constants.MEDICINE_POSITION, position);
    i.putExtras(bundle);
    startActivityForResult(i, SHOW_LIST_REQUEST_CODE);
  }

  @Override
  public void onFeedClick(FeedItem item) {
    showFeed(item.getUrl());
  }

  public void hideKeyboard() {
    // Check if no view has focus:
    View view = this.getCurrentFocus();
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public void setActivityKeyClickListener(ActivityKeyClickListener activityKeyClickListener) {
    this.activityKeyClickListener = activityKeyClickListener;
  }

  public void setActivityClickListener(ActivityClickListener activityClickListener) {
    this.activityClickListener = activityClickListener;
  }

  public void setActivityResultListener(ActivityResultListener activityResultListener) {
    this.activityResultListener = activityResultListener;
  }

  public interface ActivityClickListener {
    void daySelectionChanged(int position, boolean isCheck);

    void daySelectionClick();

    void emojiClick(int position);
  }

  public interface ActivityKeyClickListener {
    boolean onBackKeyPressed();
  }

  public interface ActivityResultListener {
    void medicineListActivityResult(int requestCode, int resultCode, Intent data);
  }
}

