package com.pairip.licensecheck;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import com.pairip.licensecheck.ILicenseV2ResultListener;
import com.pairip.licensecheck.LicenseActivity;

/* loaded from: classes.dex */
public class LicenseClient implements ServiceConnection {
    private static final int ERROR_INVALID_PACKAGE_NAME = 3;
    private static final int FLAG_RPC_CALL = 0;
    private static final int LICENSED = 0;
    private static final int MAX_RETRIES = 3;
    private static final int MILLIS_PER_SEC = 1000;
    private static final int NOT_LICENSED = 2;
    private static final String PAYLOAD_PAYWALL = "PAYWALL_INTENT";
    private static final int RETRY_DELAY_MILLIS = 1000;
    private static final String SERVICE_INTERFACE_CLASS_NAME = "com.android.vending.licensing.ILicensingService";
    private static final String SERVICE_PACKAGE = "com.android.vending";
    private static final String TAG = "LicenseClient";
    private static final int TRANSACTION_CHECK_LICENSE_V2 = 2;
    protected static Runnable exitAction = new Runnable() { // from class: com.pairip.licensecheck.LicenseClient.1
        @Override // java.lang.Runnable
        public void run() {
            System.exit(0);
        }
    };
    protected static LicenseCheckState licenseCheckState = LicenseCheckState.CHECK_REQUIRED;
    protected static String licensePubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiWJ1403npDbE7pCoEaincU4G8fpbjIipRFSxFQE76uFgY9UYMzKORmSAVT8CTi9PzeLBFB9f5JmDrL+XJonjz9XhZNHao5F0UPlSvgFkzvumZyBeq+GN6NBeEchJe/trlueOviBamwbUy6Ytyebn9pKFi2YipwxPj8rqwb70vCdTYgz0oVzd83WCbwg4xc4TKhJsoA14jpKEi4TvfcZKrFAriBtyMPA4SVRO3isuuHj+Me8zGV8AMwU8d9Rgf0K69mxv8rTPhXVNlcUoATq6xDZ8JtLtz6z7+P4g7sFzwk1COd8jFGL+qL22bSLIuKwWCI9WXom5eE8tzo0go4j0YQIDAQAB";
    protected static String packageName = "com.listpro.xyon.pro";
    private static Bundle responsePayload;
    private final Context context;
    private final DelayedTaskExecutor delayedTaskExecutor = new DelayedTaskExecutor();
    private int retryNum = 0;

    public enum LicenseCheckState {
        CHECK_REQUIRED,
        OK
    }

    public LicenseClient(Context context) {
        this.context = context;
    }

    public static String getLicensePubKey() {
        return licensePubKey;
    }

    public void initializeLicenseCheck() {
        int ordinal = licenseCheckState.ordinal();
        if (ordinal == 0) {
            connectToLicensingService();
        } else {
            if (ordinal != 1) {
                return;
            }
            try {
                ResponseValidator.validateResponse(responsePayload, packageName);
            } catch (LicenseCheckException e) {
                handleError(e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void connectToLicensingService() {
        Log.d(TAG, "Connecting to the licensing service...");
        try {
            if (this.context.bindService(new Intent(SERVICE_INTERFACE_CLASS_NAME).setPackage(SERVICE_PACKAGE).setAction(SERVICE_INTERFACE_CLASS_NAME), this, 1)) {
                return;
            }
            retryOrThrow(new LicenseCheckException("Could not bind with the licensing service."));
        } catch (SecurityException e) {
            retryOrThrow(new LicenseCheckException("Not allowed to bind with the licensing service.", e));
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName componentName, final IBinder licensingServiceBinder) {
        Log.d(TAG, "Connected to the licensing service.");
        if (licenseCheckState.equals(LicenseCheckState.OK)) {
            return;
        }
        new Thread(new Runnable() { // from class: com.pairip.licensecheck.LicenseClient$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                LicenseClient.this.lambda$onServiceConnected$0(licensingServiceBinder);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onServiceConnected$0(IBinder iBinder) {
        try {
            checkLicenseInternal(iBinder);
        } catch (LicenseCheckException e) {
            handleError(e);
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName componentName) {
        Log.w(TAG, "Unexpectedly disconnected from the licensing service.");
        retryOrThrow(new LicenseCheckException("Licensing service unexpectedly disconnected."));
    }

    public void checkLicenseInternal(IBinder licensingServiceBinder) throws LicenseCheckException {
        if (licensingServiceBinder == null) {
            retryOrThrow(new LicenseCheckException("Received a null binder."));
            return;
        }
        Log.d(TAG, "Sending request to licensing service...");
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            try {
                populateInputData(obtain, licensingServiceBinder);
                if (!licensingServiceBinder.transact(2, obtain, obtain2, 0)) {
                    handleError(new LicenseCheckException("Licensing service could not process request."));
                }
            } catch (DeadObjectException e) {
                retryOrThrow(new LicenseCheckException("Licensing service process died.", e));
            } catch (RemoteException e2) {
                handleError(new LicenseCheckException("Error when calling licensing service.", e2));
            }
        } finally {
            obtain.recycle();
            obtain2.recycle();
            Log.d(TAG, "Request to licensing service sent.");
        }
    }

    private void populateInputData(Parcel inputData, IBinder licensingService) throws RemoteException {
        inputData.writeInterfaceToken(licensingService.getInterfaceDescriptor());
        inputData.writeString(packageName);
        inputData.writeStrongBinder(createResultListener(this).asBinder());
        inputData.writeInt(0);
    }

    private static ILicenseV2ResultListener createResultListener(LicenseClient client) {
        return new ILicenseV2ResultListener.Stub() { // from class: com.pairip.licensecheck.LicenseClient.2
            @Override // com.pairip.licensecheck.ILicenseV2ResultListener
            public void verifyLicense(int responseCode, Bundle responsePayload2) {
                LicenseClient.this.processResponse(responseCode, responsePayload2);
            }
        };
    }

    private void retryOrThrow(LicenseCheckException error) {
        int i = this.retryNum;
        if (i < 3) {
            this.retryNum = i + 1;
            this.delayedTaskExecutor.schedule(new Runnable() { // from class: com.pairip.licensecheck.LicenseClient$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    LicenseClient.this.connectToLicensingService();
                }
            }, 1000L);
            Log.d(TAG, String.format("Retry #%d. License check failed with error '%s'. Next try in %ds...", Integer.valueOf(this.retryNum), error.getMessage(), 1L));
            return;
        }
        handleError(error);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processResponse(int responseCode, Bundle responsePayload2) {
        try {
            if (responseCode == 3) {
                throw new LicenseCheckException("Request package name invalid.");
            }
            if (responseCode != 0) {
                if (responseCode == 2) {
                    startPaywallActivity((PendingIntent) responsePayload2.getParcelable(PAYLOAD_PAYWALL));
                    return;
                }
                throw new LicenseCheckException(String.format("Unexpected response code %d received.", Integer.valueOf(responseCode)));
            }
            ResponseValidator.validateResponse(responsePayload2, packageName);
            Log.i(TAG, "License check succeeded.");
            licenseCheckState = LicenseCheckState.OK;
            responsePayload = responsePayload2;
        } catch (LicenseCheckException e) {
            handleError(e);
        }
    }

    private void handleError(LicenseCheckException ex) {
        Log.e(TAG, "Error while checking license: " + Log.getStackTraceString(ex));
        if (licenseCheckState.equals(LicenseCheckState.OK)) {
            return;
        }
        startErrorDialogActivity();
    }

    private void startPaywallActivity(PendingIntent paywallIntent) {
        Intent createCloseAppIntentOrExitIfAppInBackground = createCloseAppIntentOrExitIfAppInBackground();
        createCloseAppIntentOrExitIfAppInBackground.putExtra(LicenseActivity.PAYWALL_INTENT_ARG_NAME, paywallIntent);
        createCloseAppIntentOrExitIfAppInBackground.putExtra(LicenseActivity.ACTIVITY_TYPE_ARG_NAME, LicenseActivity.ActivityType.PAYWALL);
        this.context.startActivity(createCloseAppIntentOrExitIfAppInBackground);
    }

    private void startErrorDialogActivity() {
        Intent createCloseAppIntentOrExitIfAppInBackground = createCloseAppIntentOrExitIfAppInBackground();
        createCloseAppIntentOrExitIfAppInBackground.putExtra(LicenseActivity.ACTIVITY_TYPE_ARG_NAME, LicenseActivity.ActivityType.ERROR_DIALOG);
        this.context.startActivity(createCloseAppIntentOrExitIfAppInBackground);
    }

    private Intent createCloseAppIntentOrExitIfAppInBackground() {
        if (!isForeground()) {
            exitAction.run();
        }
        Intent intent = new Intent(this.context, (Class<?>) LicenseActivity.class);
        intent.addFlags(AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL);
        intent.addFlags(32768);
        intent.addFlags(268435456);
        return intent;
    }

    private boolean isForeground() {
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance <= 100;
    }

    private static final class DelayedTaskExecutor {
        private final Handler handler;

        private DelayedTaskExecutor() {
            this.handler = new Handler(Looper.getMainLooper());
        }

        public void schedule(Runnable task, long delayMillis) {
            this.handler.postDelayed(task, delayMillis);
        }
    }
}
