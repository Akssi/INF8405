package us.wifisearcher.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import us.wifisearcher.R;
import us.wifisearcher.persistence.database.WifiNetwork;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Card.OnCardFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Card#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Card extends DialogFragment {

    private static final String ARG_WIFI_NETWORK = "wifiNetwork";

    private WifiNetwork mWifiNetwork;

    private OnCardFragmentInteractionListener mListener;

    public Card() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param wifiNetwork The wifi network to be displayed on a card.
     * @return A new instance of fragment Card.
     */
    public static Card newInstance(WifiNetwork wifiNetwork) {
        Card fragment = new Card();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WIFI_NETWORK, wifiNetwork);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWifiNetwork = (WifiNetwork) getArguments().getSerializable(ARG_WIFI_NETWORK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card, container, false);

        TextView name = view.findViewById(R.id.card_wifi_name);
        name.setText(mWifiNetwork.getName());

        TextView macAddress = view.findViewById(R.id.card_wifi_mac);
        macAddress.setText(mWifiNetwork.getMacAddress());

        TextView keyType = view.findViewById(R.id.card_wifi_key_type);
        keyType.setText(mWifiNetwork.getKeyType());

        TextView encryption = view.findViewById(R.id.card_wifi_encryption);
        encryption.setText(mWifiNetwork.getEncryption());

        TextView rssid = view.findViewById(R.id.card_wifi_rssi);
        rssid.setText(String.valueOf(mWifiNetwork.getSignalStrength()));

        TextView passwordLockState = view.findViewById(R.id.card_wifi_password_lock_state);
        passwordLockState.setText(mWifiNetwork.getPasswordLockState());

        final Button button = view.findViewById(R.id.card_share_button);
        button.setOnClickListener((View v) -> {
            if (mListener != null) {
                mListener.onShareButtonPressed();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCardFragmentInteractionListener) {
            mListener = (OnCardFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnCardFragmentInteractionListener {
        void onShareButtonPressed();
    }
}
