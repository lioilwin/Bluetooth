package win.lioil.bluetooth.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import win.lioil.bluetooth.R;

public class BtDevAdapter extends RecyclerView.Adapter<BtDevAdapter.VH> {
    private static final String TAG = BtDevAdapter.class.getSimpleName();
    private final List<BluetoothDevice> mDevices = new ArrayList<>();
    private final Listener mListener;

    BtDevAdapter(Listener listener) {
        mListener = listener;
        addBound();
    }

    private void addBound() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedDevices != null)
            mDevices.addAll(bondedDevices);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        BluetoothDevice dev = mDevices.get(position);
        String name = dev.getName();
        String address = dev.getAddress();
        int bondState = dev.getBondState();
        holder.name.setText(name == null ? "" : name);
        holder.address.setText(String.format("%s (%s)", address, bondState == 10 ? "未配对" : "配对"));
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void add(BluetoothDevice dev) {
        if (mDevices.contains(dev))
            return;
        mDevices.add(dev);
        notifyDataSetChanged();
    }

    public void reScan() {
        mDevices.clear();
        addBound();
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (!bt.isDiscovering())
            bt.startDiscovery();
        notifyDataSetChanged();
    }

    class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView name;
        final TextView address;

        VH(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(TAG, "onClick, getAdapterPosition=" + pos);
            if (pos >= 0 && pos < mDevices.size())
                mListener.onItemClick(mDevices.get(pos));
        }
    }

    public interface Listener {
        void onItemClick(BluetoothDevice dev);
    }
}
