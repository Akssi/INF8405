package zelemon.zsx.Fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import zelemon.zsx.R;
import zelemon.zsx.persistence.database.PictureTypeConverter;
import zelemon.zsx.persistence.database.Profile;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment extends DialogFragment {
    private static final String ARG_PROFILE = "profile";
    private Profile mProfile;

    public CardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param profile The profile to be displayed on a card.
     * @return A new instance of fragment Card.
     */
    public static CardFragment newInstance(Profile profile) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROFILE, profile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mProfile = (Profile)getArguments().getSerializable(ARG_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card, container, false);

        TextView name = view.findViewById(R.id.card_player_name);
        name.setText(mProfile.getName());

        ImageView profilePicture = view.findViewById(R.id.card_profile_picture);
        profilePicture.setImageBitmap(PictureTypeConverter.toBitmap(mProfile.getPicture()));

        return view;
    }
}
