package zelemon.zsx;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Implementation of {@link ClusterItem}.
 */
public class PlayerMarker implements ClusterItem {

    private final LatLng mPosition;
    private final String mTitle;
    private final String mSnippet;

    public PlayerMarker(LatLng position) {
        mPosition = position;
        mTitle = "";
        mSnippet = "";
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}
