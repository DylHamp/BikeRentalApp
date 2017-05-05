package app.katybikerental.com.bikerentalapp;

import android.app.AlertDialog;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.app.ProgressDialog;
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

import com.google.api.services.sheets.v4.SheetsScopes;

import android.app.ProgressDialog;

import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * Created by dhampton on 4/28/17.
 */

public class MenuFragment extends Fragment implements View.OnClickListener {

    private Button mSignInButton, mReservationButtton, mInfoButton, mFaqButton;
    private TextView mOutputText;
    private static String TAG = "MENUFRAGMENT";

    public BikeRentalActivity mActivity;
    public APICaller mAPICaller;

    @Nullable
    @Override
    public View onCreateView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);

        mOutputText = (TextView) rootView.findViewById(R.id.api_return);
        mActivity = (BikeRentalActivity) getActivity();
        mAPICaller = mActivity.mAPIData;

        mSignInButton = (Button) rootView.findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);
        mReservationButtton = (Button) rootView.findViewById(R.id.reservation_button);
        mReservationButtton.setOnClickListener(this);
        mInfoButton = (Button) rootView.findViewById(R.id.info_button);
        mFaqButton = (Button) rootView.findViewById(R.id.faq_button);
        mFaqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.mAPIData.getResultsFromApi();
                mOutputText.setText(mActivity.mAPIData.mOutputText);
            }
        });

        return rootView;
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();

        Fragment contentFragment = null;

        switch (id) {
            case R.id.sign_in_button:
                contentFragment = new SignInFragment();
                Log.d(TAG, "Sign in button was clicked.");
                break;
            case R.id.reservation_button:
                contentFragment = new ReservationFragment();
                Log.d(TAG, "Reservation button was clicked.");
                break;

        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, contentFragment);
        ft.commit();
    }


}
