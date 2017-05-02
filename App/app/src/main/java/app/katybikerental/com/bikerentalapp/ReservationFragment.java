package app.katybikerental.com.bikerentalapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dhampton on 5/2/17.
 */

public class ReservationFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reservation, container, false);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        Fragment contentFragment = this;

        switch (id) {

        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, contentFragment);
        ft.commit();
    }
}
