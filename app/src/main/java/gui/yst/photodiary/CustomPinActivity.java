package gui.yst.photodiary;

import android.content.Intent;

import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;

//*LolliPin https://github.com/omadahealth/LolliPin

public class CustomPinActivity extends AppLockActivity {
    @Override
    public void showForgotDialog() {
    }

    @Override
    public void onPinFailure(int attempts) {

    }
    @Override
    public void onPinSuccess(int attempts) {
        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.getAppLock().disable();
    }
}
