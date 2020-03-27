package com.nlscan.android.appcopy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "AppCopy";
    private int flag = 0x00000020;
    private Object mUserInfo;


    //进度条相关事件
    protected boolean mPaused = false;
    private MyHandler myHandler= new MyHandler(this);
    private ProgressDialog mDialog;

    //private RelativeLayout relativeLayout;
    private static final  int CASE_INIT = 1;
    private static final int CASE_INSTALL = 2;
    private static final int CASE_INSTALL_FAILED = 3;
    private static final int CASE_UNINSTALL = 4;
    private static final int CASE_UNINSTALL_FAILED =5;
    private static final int CASE_FINISH = 6;
    private static final int CASE_FAILED = 7;

    //影子用户相关
    private String shadowName = "shadow";
    private int shadowId = -1;


    //应用列表相关
    private TitleInfo infoMaster, infoCustomer;
    private ExpandableListView mElistview;

    private MyAdapter mAdapter;
    private List<TitleInfo> mTitleInfoList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mElistview = (ExpandableListView) findViewById(R.id.app_list);
        //relativeLayout = (RelativeLayout) findViewById(R.id.init_uhf_rl);

//        new Thread(){
//            @Override
//            public void run() {
//
//                new Timer().schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        Looper.prepare();
//                        showLoadingWindow(getResources().getString(R.string.init));
//                        Looper.loop();
//                    }
//
//                },50);
//
//            }
//        }.start();

        new Thread(){
            @Override
            public void run() {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        showLoadingWindow(getResources().getString(R.string.init),0);

                        Looper.loop();
                    }
                },50);
            }
        }.start();


        systemInit();


        //cancelDialog();

    }

    private void systemInit(){


        initData();
        initList();
        initAdapter();
        initListenter();


    }




    //初始化数据
    private void initData(){
        List<String> userList = runCommand("pm list users");

        Log.d(TAG,"the user size is " + userList.size());
        if (userList.size() < 3){
            shadowId = createProfile(MainActivity.this, shadowName,flag);
        }
        else {
            shadowId = getShadow(userList);
        }

        Log.d(TAG,"the shadow id is " + shadowId);
        if (shadowId == -1){
            myHandler.sendEmptyMessage(CASE_FAILED);
        }
//        else {
//            myHandler.sendEmptyMessage(CASE_FINISH);
//        }

    }


    //获取影子用户的id,删除指定名称的影子用户之外的所有子用户，如果指定影子用户未运行，则将其唤醒
    private int getShadow(List<String> userList){
        int resultId = -1;
        for (int i=2; i<userList.size(); i++){
            int tempId = Integer.parseInt(userList.get(i).split("\\{")[1].split(":")[0]);
            String tempName = userList.get(i).split("\\{")[1].split(":")[1];
            if (shadowName.equals(tempName)){
                resultId = tempId;
                if ( !(userList.get(i).split("\\}").length == 2  &&
                        userList.get(i).split("\\}")[1].equals("running")) ) {
                    runCommand("am start-user " + tempId);
                }
            }
            else {
                runCommand("pm remove-user " + tempId);
            }
        }
        return resultId;
    }

    //显示应用列表
    private void initList(){

        List<ContentInfo> masterList = getAppList(0);
        List<ContentInfo> customerList = getAppList(shadowId);
        infoMaster = new TitleInfo(getResources().getString(R.string.title_master),masterList);
        infoCustomer = new TitleInfo(getResources().getString(R.string.title_customer),customerList);

        Log.d(TAG,"the size of master is " + masterList.size());
        Log.d(TAG,"the size of customer is " + customerList.size());

        mTitleInfoList.add(infoMaster);
        mTitleInfoList.add(infoCustomer);
    }



    /**
     * 初始化适配器
     */
    private void initAdapter() {
        mAdapter = new MyAdapter(mTitleInfoList, this);
        mElistview.setAdapter(mAdapter);
    }


    /**
     * ExpandableListView条目点击事件
     */
    private void initListenter() {
        //子对象点击监听事件
        mElistview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, final int childPosition, long id) {

                ContentInfo contentInfo = mTitleInfoList.get(groupPosition).getContentList().get(childPosition);
                final String packageName = contentInfo.getPackageName();
                final String path = contentInfo.getPath();
                String appName = contentInfo.getAppName();
                switch (groupPosition){
                    case 0:


                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        builder.setPositiveButton(getResources().getString(R.string.confirm),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.cancel();

                                        new Thread(){
                                            @Override
                                            public void run() {

                                                new Timer().schedule(new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        Looper.prepare();
                                                        showLoadingWindow(getResources().getString(R.string.process_install),1);
                                                        Looper.loop();
                                                    }

                                                },50);

                                            }
                                        }.start();



                                        int result = copyApp(shadowId,path,packageName,childPosition);
                                        if (result == 1){
                                            myHandler.sendEmptyMessage(CASE_INSTALL);
                                        }
                                        else {
                                            myHandler.sendEmptyMessage(CASE_INSTALL_FAILED);
                                        }
                                        cancelDialog();
                                    }
                                });
                        builder.setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        builder.setTitle(getResources().getString(R.string.dialog_install_title));
                        builder.setMessage(getResources().getString(R.string.dialog_install_content_pre) + " " + appName  + " "+
                                           getResources().getString(R.string.dialog_install_content_back) );
                        builder.create().show();
                        break;
                    case 1:

//                        final AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
//
//                        builder2.setPositiveButton(getResources().getString(R.string.confirm),
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.cancel();
//                                        showLoadingWindow(getResources().getString(R.string.process_uninstall));
//                                        int resultCode = unInstallApp(shadowId, packageName,childPosition);
//                                        if (resultCode == 1){
//                                            myHandler.sendEmptyMessage(CASE_UNINSTALL);
//                                        }
//                                        else {
//                                            myHandler.sendEmptyMessage(CASE_UNINSTALL_FAILED);
//                                        }
//                                        cancelDialog();
//                                    }
//                                });
//                        builder2.setNegativeButton(getResources().getString(R.string.cancel),
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.cancel();
//                                    }
//                                });
//                        builder2.setTitle(getResources().getString(R.string.dialog_uninstall_title));
//                        builder2.setMessage(getResources().getString(R.string.dialog_uninstall_content_pre) + " " + appName +  " "+
//                                            getResources().getString(R.string.dialog_uninstall_content_back));
//                        builder2.create().show();

                        break;
                }

                return false;
            }
        });
        //组对象点击监听事件
        mElistview.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                return false;//请务必返回false，否则分组不会展开
            }
        });

        myHandler.sendEmptyMessage(CASE_FINISH);
    }


    //获取应用列表
    private List<ContentInfo> getAppList(int userId){
        List<ContentInfo> contentInfoList = new ArrayList<>();

        List<String> resultList = runCommand("pm list packages --user " + userId + " -f");
        Log.d(TAG,"the size of result is " + resultList.size());
        for (int i=0; i<resultList.size(); i++){
            String resultItem = resultList.get(i);
            //只获取用户安装的应用，即data目录下的应用
            if (resultItem.split(":")[1].substring(1,5).equals("data")){

                ContentInfo  contentInfo = getContentInfo(resultItem);
                if (contentInfo != null){
                    contentInfoList.add(contentInfo);
                }

            }
        }
        return contentInfoList;
    }


    private ContentInfo getContentInfo (String resultItem){

        PackageManager pm = getPackageManager();
        String [] strArray = resultItem.split("=");
        String packageName = strArray[strArray.length-1];

        //跳过自身应用
        if (packageName.equals("com.nlscan.android.appcopy")) return  null;

        ApplicationInfo packageInfo = null;
        try {
            packageInfo = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null){
            String appName = (String)  packageInfo.loadLabel(pm);
            String path = resultItem.split(":")[1].split("apk=")[0]+ "apk";
            Drawable icon = packageInfo.loadIcon(pm);
            return new ContentInfo(appName,icon,path,packageName);
        }else {
            return null;
        }


    }


    //安装应用至指定用户
     private int copyApp (int userId, String path , String packageName, int childPosition){

        List<String> resultList =  runCommand("pm install -t -r -i " + packageName +
                                                " --user " + userId + " " + path);
        if (resultList.size()>0 && resultList.get(0).equals("Success") &&
                childPosition < mTitleInfoList.get(0).getContentList().size()){

            ContentInfo contentInfo = mTitleInfoList.get(0).getContentList().get(childPosition);
            if ( ! mTitleInfoList.get(1).getContentList().
                    contains(contentInfo)){
                mTitleInfoList.get(1).getContentList().add(contentInfo);
                mAdapter.notifyDataSetChanged();
            }
            return 1;
            //Toast.makeText(MainActivity.this,getResources().getString(R.string.install_success),Toast.LENGTH_SHORT).show();
        }
        else {
            return 0;
            //Toast.makeText(MainActivity.this,getResources().getString(R.string.install_fail),Toast.LENGTH_SHORT).show();
        }
    }

    //卸载指定用户的应用
    private int unInstallApp ( int userId, String packageName,int childPosition){


//        Intent intent = new Intent(MainActivity.this, getClass());
//        PendingIntent sender = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
//
//        PackageInstaller mPackageInstaller = getPackageManager().getPackageInstaller();
//        mPackageInstaller.uninstall(packageName, sender.getIntentSender());

        int uninstallResult = 1;




        List<String> resultList  = runCommand("pm uninstall --user " + userId + " " +packageName);
        if ( resultList.size()>0 && resultList.get(0).equals("Success") &&
                childPosition < mTitleInfoList.get(1).getContentList().size()){

            mTitleInfoList.get(1).getContentList().remove(childPosition);
            //更新应用地址
            List<String> newPathResult = runCommand("pm path " + packageName);
            if (newPathResult.size() > 0){
                String newPath = newPathResult.get(0).split(":")[1];
                for (int i=0; i<mTitleInfoList.get(0).getContentList().size(); i++){
                    if (mTitleInfoList.get(0).getContentList().get(i).getPackageName().equals(packageName))
                        mTitleInfoList.get(0).getContentList().get(i).setPath(newPath);
                }
            }
            mAdapter.notifyDataSetChanged();
            //Toast.makeText(MainActivity.this,getResources().getString(R.string.uninstall_success),Toast.LENGTH_SHORT).show();
        }
        else {
            uninstallResult = 0;
            //Toast.makeText(MainActivity.this,getResources().getString(R.string.uninstall_fail),Toast.LENGTH_SHORT).show();
        }

        return uninstallResult;


    }

    public  void deleteUser(Context context,int userId){
        UserManager userManager=(UserManager) context.getSystemService(Context.USER_SERVICE);
        try {
            userManager.getClass().getMethod("removeUser",int.class).invoke(userManager,userId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




//    private boolean uninstall(String packageName, int userId) {
//        Process process = null;
//        BufferedReader successResult = null;
//        BufferedReader errorResult = null;
//        StringBuilder successMsg = new StringBuilder();
//        StringBuilder errorMsg = new StringBuilder();
//        try {
//            process = new ProcessBuilder("pm", "uninstall --user "," " + userId, packageName).start();
//            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            String s;
//            while ((s = successResult.readLine()) != null) {
//                successMsg.append(s);
//            }
//            while ((s = errorResult.readLine()) != null) {
//                errorMsg.append(s);
//            }
//        } catch (Exception e) {
//            Log.d(TAG,"e = " + e.toString());
//        } finally {
//            try {
//                if (successResult != null) {
//                    successResult.close();
//                }
//                if (errorResult != null) {
//                    errorResult.close();
//                }
//            } catch (Exception e) {
//                Log.d(TAG,"Exception : " + e.toString());
//            }
//            if (process != null) {
//                process.destroy();
//            }
//        }
//        //如果含有"success"单词则认为卸载成功
//        return successMsg.toString().equalsIgnoreCase("success");
//    }


    @Override
    protected void onResume() {
        super.onResume();
        mTitleInfoList.clear();
        initList();
        mAdapter.notifyDataSetChanged();
        mPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }


    protected void  cancelDialog(){
        if (mDialog != null){
            mDialog.dismiss();
        }
    }



    //显示进度框
    protected void showLoadingWindow(String message, int type)
    {
        if(mPaused)
            return ;

        if(mDialog != null && mDialog.isShowing())
            return ;


        mDialog = type == 1 ? new ProgressDialog(MainActivity.this) :
                              new LoadingProgressDialog(MainActivity.this)  ;
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        mDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        mDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
        mDialog.setMessage(message);
        mDialog.show();
    }


    //获取用户信息
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


    //唤醒用户
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

    //创建用户
    private int createProfile(Context context, String userName, int flag) {
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

                return -1;
            }
            int userId = getUserIdFromUserInfo(mUserInfo);
            boolean isOk=startUser(userId);
            Log.d(TAG, "startUserInBackground() userId = " + userId + " | isOk = " + isOk);

            return isOk ? userId : -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG,"create user failed !");
        return -1;
    }



    //执行adb命令
    private List<String> runCommand(String commandStr) {
        List<String> resultStr = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(commandStr);
            BufferedReader bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                if (!"".equals(line)){
                    //Toast.makeText(MainActivity.this,line,Toast.LENGTH_SHORT).show();
                    resultStr.add(line);
                    Log.d(TAG,line);
                }else {
                    Log.d(TAG,"empty result!");
                }
            }
        } catch (IOException e) {
            System.out.println("MainActivity.runCommand,e=" + e);
            Log.d(TAG,"run command error" + e);
        }
        return resultStr;
    }






    static class MyHandler extends Handler {
        private SoftReference<MainActivity> mainActivitySoftReference;

        public MyHandler(MainActivity mainActivity) {
            this.mainActivitySoftReference = new SoftReference<>(mainActivity);
        }

        @Override
        public void handleMessage( Message msg) {
            MainActivity mainActivity = mainActivitySoftReference.get();
            if (mainActivity == null) return;

            switch (msg.what){
                case CASE_INIT:
                    mainActivity.showLoadingWindow(mainActivity.getResources().getString(R.string.init),0);
                    break;
                case CASE_INSTALL:
                    Toast.makeText(mainActivity,mainActivity.getResources().getString(R.string.install_success),Toast.LENGTH_SHORT).show();
                    break;
                case CASE_INSTALL_FAILED:
                    Toast.makeText(mainActivity,mainActivity.getResources().getString(R.string.install_fail),Toast.LENGTH_SHORT).show();
                    break;
                case CASE_UNINSTALL:
                    Toast.makeText(mainActivity,mainActivity.getResources().getString(R.string.uninstall_success),Toast.LENGTH_SHORT).show();
                    break;
                case CASE_UNINSTALL_FAILED:
                    Toast.makeText(mainActivity,mainActivity.getResources().getString(R.string.uninstall_fail),Toast.LENGTH_SHORT).show();
                    break;
                case CASE_FINISH:
                    //if (mainActivity.myProgress !=null)
                    //mainActivity.relativeLayout.setVisibility(View.GONE);
                    mainActivity.cancelDialog();
                    Toast.makeText(mainActivity,mainActivity.getResources().getString(R.string.init_finish),Toast.LENGTH_SHORT).show();
                    break;
                case CASE_FAILED:
                    Toast.makeText(mainActivity,mainActivity.getResources().getString(R.string.init_failed),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }




}
