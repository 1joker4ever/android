package mega.privacy.android.app.lollipop.megachat;


import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.Toast;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.TwoLineCheckPreference;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;

public class SettingsChatFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener
{
    Context context;
    private MegaApiAndroid megaApi;
    private MegaChatApiAndroid megaChatApi;
    DatabaseHandler dbH;
    MegaPreferences prefs;
    ChatSettings chatSettings;

    public static String KEY_CHAT_NOTIFICATIONS = "settings_chat_notifications";
    public static String KEY_CHAT_SOUND = "settings_chat_sound";
    public static String KEY_CHAT_VIBRATE = "settings_chat_vibrate";

    SwitchPreference chatNotificationsSwitch;
    Preference chatSoundPreference;
    SwitchPreference chatVibrateSwitch;

    TwoLineCheckPreference chatVibrateCheck;
    TwoLineCheckPreference chatNotificationsCheck;

    boolean chatNotifications;
    boolean chatVibration;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        if (megaApi == null) {
            megaApi = ((MegaApplication) getActivity().getApplication()).getMegaApi();
        }

        if (megaChatApi == null) {
            megaChatApi = ((MegaApplication) getActivity().getApplication()).getMegaChatApi();
        }

        dbH = DatabaseHandler.getDbHandler(getActivity());
        prefs = dbH.getPreferences();
        chatSettings = dbH.getChatSettings();

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_chat);

        chatSoundPreference = findPreference(KEY_CHAT_SOUND);
        chatSoundPreference.setOnPreferenceClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            chatNotificationsSwitch = (SwitchPreference) findPreference(KEY_CHAT_NOTIFICATIONS);
            chatNotificationsSwitch.setOnPreferenceClickListener(this);

            chatVibrateSwitch = (SwitchPreference) findPreference(KEY_CHAT_VIBRATE);
            chatVibrateSwitch.setOnPreferenceClickListener(this);

        }
        else{
            chatVibrateCheck = (TwoLineCheckPreference) findPreference(KEY_CHAT_VIBRATE);
            chatVibrateCheck.setOnPreferenceClickListener(this);

            chatNotificationsCheck = (TwoLineCheckPreference) findPreference(KEY_CHAT_NOTIFICATIONS);
            chatNotificationsCheck.setOnPreferenceClickListener(this);
        }

        if(chatSettings==null){
            dbH.setNotificationEnabledChat(true+"");
            dbH.setVibrationEnabledChat(true+"");
            dbH.setNotificationSoundChat("");
            chatNotifications = true;
            chatVibration = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                chatNotificationsSwitch.setChecked(chatNotifications);
                chatVibrateSwitch.setChecked(chatVibration);
            }
            else{
                chatNotificationsCheck.setChecked(chatNotifications);
                chatVibrateCheck.setChecked(chatVibration);
            }

            Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
            Ringtone defaultSound = RingtoneManager.getRingtone(context, defaultSoundUri);
            chatSoundPreference.setSummary(defaultSound.getTitle(context));
        }
        else{

            if (chatSettings.getNotificationsEnabled() == null){
                dbH.setNotificationEnabledChat(true+"");
                chatNotifications = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    chatNotificationsSwitch.setChecked(chatNotifications);
                }
                else{
                    chatNotificationsCheck.setChecked(chatNotifications);
                }
            }
            else{
                chatNotifications = Boolean.parseBoolean(chatSettings.getNotificationsEnabled());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    chatNotificationsSwitch.setChecked(chatNotifications);
                }
                else{
                    chatNotificationsCheck.setChecked(chatNotifications);
                }
            }

            if (chatSettings.getVibrationEnabled() == null){
                dbH.setVibrationEnabledChat(true+"");
                chatVibration = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    chatVibrateSwitch.setChecked(chatVibration);
                }
                else{
                    chatVibrateCheck.setChecked(chatVibration);
                }
            }
            else{
                chatVibration = Boolean.parseBoolean(chatSettings.getVibrationEnabled());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    chatVibrateSwitch.setChecked(chatVibration);
                }
                else{
                    chatVibrateCheck.setChecked(chatVibration);
                }
            }

            if (chatSettings.getNotificationsSound() == null){

                Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                Ringtone defaultSound = RingtoneManager.getRingtone(context, defaultSoundUri);
                chatSoundPreference.setSummary(defaultSound.getTitle(context));
            }
            else{
                if(chatSettings.getNotificationsSound().equals("")){

                    Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone defaultSound = RingtoneManager.getRingtone(context, defaultSoundUri);
                    chatSoundPreference.setSummary(defaultSound.getTitle(context));
                }
                else{
                    String soundString = chatSettings.getNotificationsSound();
                    Ringtone sound = RingtoneManager.getRingtone(context, Uri.parse(soundString));
                    String titleSound = sound.getTitle(context);
                    chatSoundPreference.setSummary(titleSound);
                }
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference.getKey().compareTo(KEY_CHAT_NOTIFICATIONS) == 0){
            log("KEY_CHAT_NOTIFICATIONS");
            chatNotifications = !chatNotifications;
            if (chatNotifications){
                Toast.makeText(context, "Not implemented yet: ENABLE NOTIFICATIONS", Toast.LENGTH_SHORT).show();
                dbH.setNotificationEnabledChat(true+"");
            }
            else{
                dbH.setNotificationEnabledChat(false+"");
                Toast.makeText(context, "Not implemented yet: DISABLE NOTIFICATIONS", Toast.LENGTH_SHORT).show();
            }
        }
        else if (preference.getKey().compareTo(KEY_CHAT_VIBRATE) == 0){
            log("KEY_CHAT_VIBRATE");
            chatVibration = !chatVibration;
            if (chatVibration){
                Toast.makeText(context, "Not implemented yet: ENABLE VIBRATION", Toast.LENGTH_SHORT).show();
                dbH.setVibrationEnabledChat(true+"");
            }
            else{
                dbH.setVibrationEnabledChat(false+"");
                Toast.makeText(context, "Not implemented yet: DISABLE VIBRATION", Toast.LENGTH_SHORT).show();
            }
        }
        else if (preference.getKey().compareTo(KEY_CHAT_SOUND) == 0){
            log("KEY_CHAT_SOUND");
//            chatVibration = !chatVibration;
//            if (chatVibration){
//                Toast.makeText(context, "Not implemented yet: ENABLE VIBRATION", Toast.LENGTH_SHORT).show();
//                dbH.setNotificationEnabledChat(true+"");
//            }
//            else{
//                dbH.setNotificationEnabledChat(false+"");
//                Toast.makeText(context, "Not implemented yet: DISABLE VIBRATION", Toast.LENGTH_SHORT).show();
//            }

            ((ChatPreferencesActivity) context).changeSound();
        }
        return true;
    }



    public void setNotificationSound (Uri uri){
        Ringtone sound = RingtoneManager.getRingtone(context, uri);

        String title = sound.getTitle(context);

        if(title!=null){
            log("Title sound notification: "+title);
            chatSoundPreference.setSummary(title);
        }

        String chosenSound = uri.toString();
        if(chatSettings==null){
            chatSettings = new ChatSettings(Boolean.toString(true), Boolean.toString(true), chosenSound, Boolean.toString(true), MegaChatApi.STATUS_ONLINE+"");
            dbH.setChatSettings(chatSettings);
        }
        else{
            chatSettings.setNotificationsSound(chosenSound);
            dbH.setNotificationSoundChat(chosenSound);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private static void log(String log) {
        Util.log("SettingsChatFragment", log);
    }
}
