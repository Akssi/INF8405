package us.wifisearcher.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import us.wifisearcher.R;
import us.wifisearcher.persistence.database.SerializableWifiNetwork;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Card.OnCardFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Card#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Card extends DialogFragment {
    private static final String LOCKED_CHAR = "\uD83D\uDD12";
    private static final String UNLOCKED_CHAR = "\uD83D\uDD13";
    private static final String OPEN = "Open";

    private static final String ARG_WIFI_NETWORK = "wifiNetwork";

    private SerializableWifiNetwork mWifiNetwork;

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
    public static Card newInstance(SerializableWifiNetwork wifiNetwork) {
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
            mWifiNetwork = (SerializableWifiNetwork) getArguments().getSerializable(ARG_WIFI_NETWORK);
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

        TextView passwordLockState = view.findViewById(R.id.card_wifi_password_lock_state);
        passwordLockState.setText((mWifiNetwork.getKeyType().equals(OPEN)) ? UNLOCKED_CHAR : LOCKED_CHAR);

        ImageView rssi = view.findViewById(R.id.card_wifi_rssi);

        ImageView favorite = view.findViewById(R.id.favoriteToggle_card);

        // Set image for signal strength indication
        switch (mWifiNetwork.getSignalStrength() - 1) {
            case 1:
                rssi.setImageResource(R.drawable.ic_signal_wifi_1_bar_black_24dp);
                break;
            case 2:
                rssi.setImageResource(R.drawable.ic_signal_wifi_2_bar_black_24dp);
                break;
            case 3:
                rssi.setImageResource(R.drawable.ic_signal_wifi_3_bar_black_24dp);
                break;
            case 4:
                rssi.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_24dp);
                break;
            default:
                rssi.setImageResource(R.drawable.ic_signal_wifi_0_bar_black_24dp);
        }

        final Button shareButton = view.findViewById(R.id.card_share_button);
        shareButton.setOnClickListener((View v) -> {
            if (mListener != null) {
                mListener.onShareButtonPressed(mWifiNetwork);
            }
        });

        final Button navigationButton = view.findViewById(R.id.card_navigation_button);
        navigationButton.setOnClickListener((View v) -> {
            if (mListener != null) {
                mListener.onNavigationButtonPressed(mWifiNetwork);
            }
        });

        favorite.setOnClickListener((View v) -> {
            mWifiNetwork.setFavorite(mWifiNetwork.getFavorite() == 1 ? 0 : 1);
            if (mListener != null) {
                mListener.onFavoriteButtonPressed(mWifiNetwork);
            }

            ImageView button = v.findViewById(R.id.favoriteToggle_card);
            if (mWifiNetwork.getFavorite() == 1) {
                button.setImageResource(R.drawable.ic_star_black_32dp);
            } else {
                button.setImageResource(R.drawable.ic_star_border_black_32dp);
            }
        });

        if (mWifiNetwork.getFavorite() == 1) {
            favorite.setImageResource(R.drawable.ic_star_black_32dp);
        } else {
            favorite.setImageResource(R.drawable.ic_star_border_black_32dp);
        }

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
        void onShareButtonPressed(SerializableWifiNetwork wifiNetwork);

        void onNavigationButtonPressed(SerializableWifiNetwork wifiNetwork);

        void onFavoriteButtonPressed(SerializableWifiNetwork wifiNetwork);
    }
}
