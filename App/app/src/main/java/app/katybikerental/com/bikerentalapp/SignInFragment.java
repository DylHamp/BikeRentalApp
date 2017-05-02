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

/**
 * Created by dhampton on 5/1/17.
 */

public class SignInFragment extends Fragment implements View.OnClickListener{

    private Button mCreateAccount;

    private static String TAG = "CreateAccountFrame";

    @Nullable
    @Override
    public View onCreateView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        mCreateAccount = (Button) rootView.findViewById(R.id.create_account);
        mCreateAccount.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        Fragment contentFragment = null;

        switch (v.getId()) {
            case R.id.create_account:
                Log.d(TAG, "Create account was pressed.");
                contentFragment = new CreateAccountFragment();
                break;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, contentFragment);
        ft.commit();

    }
}
