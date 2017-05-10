package app.katybikerental.com.bikerentalapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
        mAPICaller = mActivity.mAPICaller;

        mSignInButton = (Button) rootView.findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);
        mReservationButtton = (Button) rootView.findViewById(R.id.reservation_button);
        mReservationButtton.setOnClickListener(this);
        mInfoButton = (Button) rootView.findViewById(R.id.info_button);
        mInfoButton.setOnClickListener(this);
        mFaqButton = (Button) rootView.findViewById(R.id.faq_button);
        mFaqButton.setOnClickListener(this);

        return rootView;
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();

        Fragment contentFragment = this;

        switch (id) {
            case R.id.sign_in_button:
                contentFragment = new SignInFragment();
                Log.d(TAG, "Sign in button was clicked.");
                break;
            case R.id.reservation_button:
                contentFragment = new ReservationFragment();
                Log.d(TAG, "Reservation button was clicked.");
                break;
            case R.id.info_button:
                List<Object> submission = new ArrayList<Object>();
                submission.add("5/10/2017 12:05:47");
                submission.add("5/12/2017");
                submission.add("5:00:00 PM");
                submission.add("Dylan");
                submission.add(8675309);
                submission.add("");
                submission.add(1);
                submission.add("I want to go fast.");
                mOutputText.setText(mAPICaller.updateSheets(submission));
                break;
            case R.id.faq_button:
                mAPICaller.getResultsFromApi("FAQ");
                mOutputText.setText(mAPICaller.mResults);
                break;

        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, contentFragment);
        ft.commit();
    }


}
