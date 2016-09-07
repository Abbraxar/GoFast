package com.i3cnam.gofast.views.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.views.Main;

public class GeneralForegroundNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final int NOTIFICATION_NUMBER = 1;

    public static void notify(final Service serviceContext, int smallIcon) {
        final Resources res = serviceContext.getResources();

        final String title = res.getString(R.string.app_name);
        final String text = res.getString(R.string.courseInProgress);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(serviceContext)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
//                .setDefaults(Notification.DEFAULT_ALL)
                .setDefaults(0)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(text)

                // All fields below this line are optional.

                // vibration
                .setVibrate(null)

                // sound
                .setSound(null)

                // Set the pending intent to be initiated when the user touches
                // the notification.
                .setContentIntent(
                        PendingIntent.getActivity(
                                serviceContext,
                                0,
                                new Intent(serviceContext, Main.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))

                // Make the notification staying.
                .setOngoing(true);

        serviceContext.startForeground(NOTIFICATION_NUMBER, builder.build());
    }
}
