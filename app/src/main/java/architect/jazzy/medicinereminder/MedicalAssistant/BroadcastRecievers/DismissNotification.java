package architect.jazzy.medicinereminder.MedicalAssistant.BroadcastRecievers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.util.Log;

import architect.jazzy.medicinereminder.MedicalAssistant.Activities.FullScreenLockScreen;

public class DismissNotification extends BroadcastReceiver {
  public static final String TAG = "DismissNotification";

  public DismissNotification() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {

    Log.e(TAG, "On receive");
    Ringtone r = FullScreenLockScreen.r;
    if (r != null) {
      r.stop();
    }

    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(AlarmReceiver.NOTIFICATION_TAG, AlarmReceiver.NOTIFICATION_ID);
  }
}
