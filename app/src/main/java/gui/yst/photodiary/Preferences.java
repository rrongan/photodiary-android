package gui.yst.photodiary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;
import com.google.android.gms.common.api.GoogleApiClient;

//*Preferences Tutorial http://alvinalexander.com/android/android-tutorial-preferencescreen-preferenceactivity-preferencefragment
public class Preferences extends PreferenceActivity {

    private static Context mPreferences;
    private static final int REQUEST_CODE_ENABLE = 11;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = this;
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {

        private Intent intent;
        private PreferenceCategory securityCategory;
        private Preference password;
        private Preference changepass;
        private Preference disablepass;
        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            securityCategory = (PreferenceCategory) findPreference("security_category");
            password = findPreference("password");
            intent = new Intent(mPreferences, CustomPinActivity.class);
            password.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivityForResult(intent, REQUEST_CODE_ENABLE);
                    return false;
                }
            });

            changepass = findPreference("changepass");
            changepass.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN);
                    startActivity(intent);
                    return false;
                }
            });
            securityCategory.removePreference(changepass);

            disablepass = findPreference("disablepass");
            disablepass.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK);
                    startActivity(intent);
                    return false;
                }
            });
            securityCategory.removePreference(disablepass);

            if (lockManager.getAppLock().isPasscodeSet()) {
                securityCategory.removePreference(password);
                securityCategory.addPreference(changepass);
                securityCategory.addPreference(disablepass);
            }else{
                securityCategory.addPreference(password);
                securityCategory.removePreference(changepass);
                securityCategory.removePreference(disablepass);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (lockManager.getAppLock().isPasscodeSet()) {
                securityCategory.removePreference(password);
                securityCategory.addPreference(changepass);
                securityCategory.addPreference(disablepass);
            }else{
                securityCategory.addPreference(password);
                securityCategory.removePreference(changepass);
                securityCategory.removePreference(disablepass);
            }
        }
    }
}
