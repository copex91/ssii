package com.example.cope.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;

import java.lang.reflect.Field;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private BubblesManager bubblesManager;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeBubbleManager();
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    addNewNotification();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeBubbleManager() {

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
//            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
//                if (!shouldShowRequestPermissionRationale(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
//                    showMessageOKCancel("You need to allow access to system windows",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                        requestPermissions(new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
//                                                REQUEST_CODE_ASK_PERMISSIONS);
//                                    }
//                                }
//                            });
//                    return;
//                }
//                requestPermissions(new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW},
//                        REQUEST_CODE_ASK_PERMISSIONS);
//                return;
//            }
//            bubblesManager = new BubblesManager.Builder(this)
//                    .setTrashLayout(R.layout.notification_trash_layout)
//                    .build();
//            bubblesManager.initialize();
//        }

        bubblesManager = new BubblesManager.Builder(this)
            .setTrashLayout(R.layout.notification_trash_layout)
            .build();
            bubblesManager.initialize();
    }

    private void addNewNotification() {
            BubbleLayout bubbleView = (BubbleLayout) LayoutInflater.from(MainActivity.this)
                                   .inflate(R.layout.notification_layout, null);
                // this method call when user remove notification layout
                bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
                    @Override
                    public void onBubbleRemoved(BubbleLayout bubble) {
                            Toast.makeText(getApplicationContext(), "Bubble removed!",
                            Toast.LENGTH_SHORT).show();
                    }
                });
                // this methoid call when cuser click on the notification layout( bubble layout)
                bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {
                    @Override
                    public void onBubbleClick(BubbleLayout bubble) {
//                            Toast.makeText(getApplicationContext(), "Clicked!",
//                            Toast.LENGTH_SHORT).show();
                        clickEnBurbuja();
                    }
                });
                bubblesManager.addBubble(bubbleView, 60, 20);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        bubblesManager.recycle();
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    bubblesManager = new BubblesManager.Builder(this)
                            .setTrashLayout(R.layout.notification_trash_layout)
                            .build();
                    bubblesManager.initialize();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "WRITE_CONTACTS Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void clickEnBurbuja(){
        Activity activity;

        activity = getActivity();


    }

    public static Activity getActivity() {
        try{
        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
        activitiesField.setAccessible(true);

        Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
        if (activities == null)
            return null;

        for (Object activityRecord : activities.values()) {
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = activityRecordClass.getDeclaredField("paused");
            pausedField.setAccessible(true);
            if (!pausedField.getBoolean(activityRecord)) {
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                return activity;
            }
        }}
        catch (Exception e){
            return null;
        }

        return null;
    }

}
