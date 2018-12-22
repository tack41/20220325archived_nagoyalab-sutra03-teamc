package xuzhongwei.gunsecury.util.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import xuzhongwei.gunsecury.model.BLEDeviceDAO;

public class DeviceScanResultAdapter extends BaseAdapter {

    ArrayList<BLEDeviceDAO> mBLEDeviceList = new ArrayList<BLEDeviceDAO>();
    LayoutInflater layoutInflater = null;
    Context mContext;

    public DeviceScanResultAdapter(Context _context) {
        this.mContext = _context;
        this.layoutInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setmBLEDeviceList(ArrayList<BLEDeviceDAO> mBLEDeviceList) {
        this.mBLEDeviceList = mBLEDeviceList;
    }

    @Override
    public int getCount() {
        return mBLEDeviceList.size();
    }

    @Override
    public Object getItem(int i) {
        return mBLEDeviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.layout.ble_device_scan_list,viewGroup,false);
        TextView deviceName = (TextView) view.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.deviceName);
        TextView deviceAddress = view.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.deviceAddrress);
        deviceAddress.setText(mBLEDeviceList.get(i).getDeviceAddress());
        deviceName.setText(mBLEDeviceList.get(i).getDeviceName());
        return view;
    }
}
