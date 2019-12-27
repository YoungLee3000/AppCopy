package com.nlscan.android.appcopy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "AppCopy";
    private int flag = 0x00000020;
    private Object mUserInfo;
    private Button btn_create;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_create = (Button) findViewById(R.id.btn_create);
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProfile(MainActivity.this, "shadow",flag);
            }
        });
    }


    private int getUserIdFromUserInfo(Object userInfo) {
        int userId = -1;
        try {
            Field field_id = userInfo.getClass().getDeclaredField("id");
            field_id.setAccessible(true);
            userId = (Integer)field_id.get(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userId;
    }

    private boolean startUser(int userId){
        Object iActivityManager = null;
        try {
            iActivityManager = Class.forName("android.app.ActivityManagerNative").getMethod("getDefault").invoke(null);

            boolean isOk=(boolean)iActivityManager.getClass().getMethod("startUserInBackground",int.class)
                    .invoke(iActivityManager,userId);
            return isOk;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private String createProfile(Context context, String userName, int flag) {
        UserManager mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);

        UserHandle userHandle = UserHandle.getUserHandleForUid(0);

        Log.d(TAG,"userHandle = "+userHandle.toString());
        try {
            int getIdentifier=(int)userHandle.getClass().getMethod("getIdentifier").invoke(userHandle);
            Log.d(TAG,"Identifier = "+getIdentifier);
            mUserInfo = mUserManager.getClass().getMethod("createProfileForUser",String.class, int.class, int.class)
                    .invoke(mUserManager
                            ,userName
                            , flag
                            ,getIdentifier);
            if(mUserInfo == null){
                Log.d(TAG, "mUserInfo is null!");
                return null;
            }
            int userId = getUserIdFromUserInfo(mUserInfo);
            boolean isOk=startUser(userId);
            Log.d(TAG, "startUserInBackground() userId = " + userId + " | isOk = " + isOk);
            return isOk ? ""+userId : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




}
