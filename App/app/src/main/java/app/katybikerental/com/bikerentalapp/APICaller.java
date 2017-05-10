package app.katybikerental.com.bikerentalapp;

/**
 * Created by dhampton on 5/2/17.
 */

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.client.util.Strings;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

import com.google.api.services.sheets.v4.model.*;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class APICaller implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    BikeRentalActivity mBikeActivity;
    public ProgressDialog mProgress;
    public String mOutputText;
    public MakeRequestTask mMakeRequestTask;
    public MakePostTask mMakePostTask;
    public String mResults = "Empty";
    public String mSheet = "Reservations";


    public APICaller(BikeRentalActivity c) {
        this.mBikeActivity = c;
        this.mCredential = GoogleAccountCredential.usingOAuth2(
                c, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        this.mMakeRequestTask = new MakeRequestTask(mCredential, this, "Reservations");
        try {
            this.mMakePostTask = new MakePostTask(mCredential);
        }
        catch (IOException ex) {
            Log.e(TAG, "IOException issue", ex);
        }
        this.mProgress = new ProgressDialog(c);
        mProgress.setMessage("Calling google api ...");
    }

    private String TAG = "APICALLER";

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public void getResultsFromApi(String sheet) {
        this.mSheet = sheet;
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            this.mOutputText = ("No network connection available.");
        } else {
            new MakeRequestTask(mCredential, this, mSheet).execute();
            Log.d(TAG, mResults);
        }
    }



    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    public void chooseAccount() {
        mBikeActivity.chooseAccount();
    }



    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mBikeActivity.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    public boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) mBikeActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mBikeActivity);
        Log.e(TAG, String.valueOf(connectionStatusCode));
        Log.e(TAG, String.valueOf(ConnectionResult.SUCCESS));
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }



    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    public void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(mBikeActivity);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(mBikeActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public String updateSheets(List<Object> submission) {
        String response = "Nothing Happend";
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            response = "No network connection available.";
        } else {
            try {
                new MakePostTask(mCredential, submission, "Reservations").execute();
                response = "Post Task Started";
            }
            catch (IOException ex) {
                Log.e(TAG, "Error", ex);
                response = "Error";
            }
        }
        return response;
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    public class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        public com.google.api.services.sheets.v4.Sheets mService = null;
        public Exception mLastError = null;
        public APICaller mCaller;
        public String mSheet;
        public Integer mColumns = 2;

        MakeRequestTask(GoogleAccountCredential credential, APICaller caller, String sheet) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
            mCaller = caller;
            mSheet = sheet;

        }

        @Override
        protected void onPreExecute() {
            mOutputText = "";
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            String textOutput = "";
            if (output == null || output.size() == 0) {
                Log.e(TAG, "No results returned.");
            } else {
                output.add(0, "Data retrieved using the Google Sheets API:");
                textOutput = TextUtils.join("\n", output);
            }
            Log.d(TAG, textOutput);
            mOutputText = textOutput;
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    mBikeActivity.startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            APICaller.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText = "The following error occurred:\n"
                            + mLastError.getMessage();
                }
            } else {
                mOutputText = "Request cancelled.";
            }
            Log.e(TAG, mOutputText);
        }

        /**
         * Background task to call Google Sheets API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        public List<String> getDataFromApi() throws IOException {
            String range = mSheet;
            if (mSheet == "Info") {
                mColumns = 1;
            }
            String spreadsheetId = "1iflc3utd0p6V6cubjb3byDzsgf2OPdUv8fjJL55MABY";
            List<String> results = new ArrayList<String>();

            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                for (List row : values) {
                    for (int i = 0; i < mColumns; i++ ) {
                        results.add(row.get(i) + "");
                    }
                }
            }
            mOutputText = results.toString();
            mCaller.mResults = results.toString();
            Log.d(TAG, mResults.toString() + "FROM SHEETS");
            return results;
        }


    }

    public class MakePostTask extends AsyncTask<Void, Void, List<String>> {
        public com.google.api.services.sheets.v4.Sheets mService = null;
        public Exception mLastError;
        public Boolean mSent;
        public ValueRange mValueRange;
        public String mSheet;
        String spreadsheetId = "1iflc3utd0p6V6cubjb3byDzsgf2OPdUv8fjJL55MABY";

        MakePostTask(GoogleAccountCredential credential) throws IOException {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mSheet = "Errors";
            this.mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
            ArrayList<List<Object>> emptyValues = new ArrayList<>();
            List<Object> submission = new ArrayList<>();
            submission.add("Nothing here");
            emptyValues.add(submission);
            this.mValueRange = new ValueRange();
            this.mValueRange.setValues(emptyValues);
            this.mValueRange.setMajorDimension("ROWS");


        }

        MakePostTask(GoogleAccountCredential credential, List<Object> submission, String sheet) throws IOException {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mSheet = sheet;
            this.mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
            ArrayList<List<Object>> values = new ArrayList<>();
            try {
                values.add(submission);
            }
            catch (NullPointerException ex) {
                List<Object> emptyValues = new ArrayList<>();
                emptyValues.add("Nothing");
                emptyValues.add("Was");
                emptyValues.add("Submitted");
                values.add(emptyValues);
            }
            this.mValueRange = new ValueRange();
            this.mValueRange.setValues(values);
            this.mValueRange.setMajorDimension("ROWS");


        }

        public void updateSheets(ValueRange valueRange) throws IOException {
            Sheets.Spreadsheets.Values.Append request = this.mService.spreadsheets().values().append(spreadsheetId, "Reservations", valueRange).setValueInputOption("USER_ENTERED");
            AppendValuesResponse response = request.execute();
            Log.d(TAG, response.toString());
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                updateSheets(mValueRange);
                Log.d(TAG, "Append has been sent");
                this.mSent = true;
            } catch (Exception e) {
                this.mLastError = e;
                Log.e(TAG, "Things went wrong", e);
                this.mSent = false;
                cancel(true);
                return null;
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            mOutputText = "";
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            String textOutput = "";
            if (mSent) {
                Log.e(TAG, "No results returned.");
            } else {
                output.add(0, "Data retrieved using the Google Sheets API:");
                textOutput = TextUtils.join("\n", output);
            }
            Log.d(TAG, textOutput);
        }
    }
}
