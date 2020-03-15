package com.task.app.taskapp.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.task.app.taskapp.common.Tags;
import com.task.app.taskapp.models.UserModel;

public class Preferences {

    private static Preferences instance = null;

    private Preferences() {
    }

    public static synchronized Preferences newInstance()
    {
        if (instance==null)
        {
            instance = new Preferences();
        }

        return instance;
    }

    // ميثود لانشاء وتحديث بيانات يوزر فى البرفرنس

    public void create_update_userData(Context context , UserModel userModel)
    {
        SharedPreferences preferences = context.getSharedPreferences("userPref",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String userDataGson = gson.toJson(userModel);
        editor.putString("user_data",userDataGson);
        editor.apply();
        createSession(context, Tags.session_login);
    }

    // ميثود للحصول على بيانات المستخدم
    public UserModel getUserData(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences("userPref",Context.MODE_PRIVATE);
        String userDataGson = preferences.getString("user_data","");
        return new Gson().fromJson(userDataGson,UserModel.class);
    }

    // ميثود لخلق سيشن حتى لا يتم عمل تسجيل خرووج فى كل مره نخرج من الابلكيشن
    public void createSession(Context context,String session)
    {
        SharedPreferences preferences = context.getSharedPreferences("sessionPref",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("session",session);
        editor.apply();
    }

    // ميثود للحصول على السيشن
    public String getSession(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences("sessionPref",Context.MODE_PRIVATE);
        return preferences.getString("session","");
    }

    // ميثود لمسح الداتا عند تسجيل الخروج

    public void clear(Context context)
    {
        SharedPreferences preferences1 = context.getSharedPreferences("userPref",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = preferences1.edit();
        editor1.clear();
        editor1.apply();

        SharedPreferences preferences2 = context.getSharedPreferences("sessionPref",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = preferences2.edit();
        editor2.clear();
        editor2.apply();

    }


}
