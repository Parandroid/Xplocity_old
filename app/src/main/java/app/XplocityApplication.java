package app;

import android.app.Application;
import com.facebook.stetho.Stetho;

/**
 * Created by dmitry on 09.08.17.
 */

public class XplocityApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
