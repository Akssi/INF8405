package us.wifisearcher;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import us.wifisearcher.persistence.database.WifiNetwork;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> implements View.OnClickListener {

    private List<WifiNetwork> wifiNetworkList = new ArrayList<>();
    private int expandedPosition = -1;
    private String wifi_mac_address;
    private String wifi_key_type;
    private String wifi_encryption;

    public RecyclerViewAdapter(List<WifiNetwork> wifiNetworkList) {
        this.wifiNetworkList = wifiNetworkList;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.wifi_list_item, parent, false);
        RecyclerViewHolder holder = new RecyclerViewHolder(v);

        holder.itemView.setOnClickListener(RecyclerViewAdapter.this);
        holder.itemView.setTag(holder);
        return holder;
    }

    @Override
    public void onClick(View view) {
        RecyclerViewHolder holder = (RecyclerViewHolder) view.getTag();

        // Check for an expanded view, collapse if you find one
        if (expandedPosition >= 0) {
            int prev = expandedPosition;
            notifyItemChanged(prev);
        }
        // Set the current position to "expanded"
        if (holder.getAdapterPosition() != expandedPosition) {
            expandedPosition = holder.getAdapterPosition();
            notifyItemChanged(expandedPosition);
        } else {
            expandedPosition = -1;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        WifiNetwork wifiNetwork = wifiNetworkList.get(position);
        holder.nameTextView.setText(wifiNetwork.getName());
        holder.macAddressTextView.setText(String.format(wifi_mac_address, wifiNetwork.getMacAddress()));
        switch (wifiNetwork.getSignalStrength() - 1) {
            case 1:
                holder.signalStrength.setImageResource(R.drawable.ic_signal_wifi_1_bar_black_24dp);
                break;
            case 2:
                holder.signalStrength.setImageResource(R.drawable.ic_signal_wifi_2_bar_black_24dp);
                break;
            case 3:
                holder.signalStrength.setImageResource(R.drawable.ic_signal_wifi_3_bar_black_24dp);
                break;
            case 4:
                holder.signalStrength.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_24dp);
                break;
            default:
                holder.signalStrength.setImageResource(R.drawable.ic_signal_wifi_0_bar_black_24dp);

        }
        holder.keyTypeTextView.setText(String.format(wifi_key_type, wifiNetwork.getKeyType()));
        holder.encryptionTextView.setText(String.format(wifi_encryption, wifiNetwork.getEncryption()));
        holder.passwordLockStateTextView.setText(wifiNetwork.getPasswordLockState());

        // Expanding/Collapsing elements
        if (position == expandedPosition) {
            holder.expandArea.setVisibility(View.VISIBLE);
        } else {
            holder.expandArea.setVisibility(View.GONE);

        }
    }

    @Override
    public int getItemCount() {
        return wifiNetworkList.size();
    }

    public void addItems(List<WifiNetwork> wififNetworkList) {
        System.out.println(String.format("Adding %d items to recycler view", wififNetworkList.size()));
        if (wififNetworkList.size() != 0) {
            this.wifiNetworkList = wififNetworkList;
        }
        notifyDataSetChanged();
    }

    public void setWifi_mac_address(String wifi_mac_address) {
        this.wifi_mac_address = wifi_mac_address;
    }

    public void setWifi_key_type(String wifi_key_type) {
        this.wifi_key_type = wifi_key_type;
    }

    public void setWifi_encryption(String wifi_encryption) {
        this.wifi_encryption = wifi_encryption;
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout expandArea;
        private TextView nameTextView;
        private TextView macAddressTextView;
        private ImageView signalStrength;
        private TextView keyTypeTextView;
        private TextView encryptionTextView;
        private TextView passwordLockStateTextView;

        RecyclerViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.wifi_name);
            macAddressTextView = view.findViewById(R.id.mac_address);
            signalStrength = view.findViewById(R.id.signal_strength);
            keyTypeTextView = view.findViewById(R.id.key_type);
            encryptionTextView = view.findViewById(R.id.encryption);
            passwordLockStateTextView = view.findViewById(R.id.password_lock_state);
            expandArea = view.findViewById(R.id.expandArea);
        }
    }
}
