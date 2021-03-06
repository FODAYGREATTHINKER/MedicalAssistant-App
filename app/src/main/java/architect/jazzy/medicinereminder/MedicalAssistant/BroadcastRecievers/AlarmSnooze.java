package architect.jazzy.medicinereminder.MedicalAssistant.BroadcastRecievers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.os.Bundle;
import android.util.Log;

import architect.jazzy.medicinereminder.HelperClasses.Constants;
import architect.jazzy.medicinereminder.MedicalAssistant.Activities.FullScreenLockScreen;

public class AlarmSnooze extends BroadcastReceiver {
  public static final String TAG = "AlarmSnooze";

  public AlarmSnooze() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {

    Log.e(TAG, "Snooze on received");
    Ringtone r = FullScreenLockScreen.r;
    if (r != null) {
      r.stop();
    }

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent i = new Intent(context, AlarmReceiver.class);
    Bundle bundle = new Bundle();
    bundle.putParcelableArrayList(Constants.MEDICINE_NAME_LIST, intent.getParcelableArrayListExtra(Constants.MEDICINE_NAME_LIST));
    i.putExtras(bundle);
    PendingIntent alarmservice = PendingIntent.getBroadcast(context, 12531, i, PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10 * 60 * 1000, alarmservice);

    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(AlarmReceiver.NOTIFICATION_TAG, AlarmReceiver.NOTIFICATION_ID);

  }
}
