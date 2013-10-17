//
//    NXT Control
//    Copyright (c) 2013 Carlos Rafael Gimenes das Neves
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program. If not, see {http://www.gnu.org/licenses/}.
//
//    https://github.com/BandTec/NXTControl
//
package br.com.bandtec.nxtcontrol;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import br.com.bandtec.nxtcontrol.activity.ClientActivity;
import br.com.bandtec.nxtcontrol.list.BaseList;
import br.com.bandtec.nxtcontrol.list.DeviceItem;
import br.com.bandtec.nxtcontrol.ui.BgButton;
import br.com.bandtec.nxtcontrol.ui.BgListView;
import br.com.bandtec.nxtcontrol.ui.BgTextView;
import br.com.bandtec.nxtcontrol.ui.UI;

public class ActivityDeviceList extends ClientActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
	public static final String PAIRING = "pairing";
	public static final String DEVICE_NAME_AND_ADDRESS = "device_infos";
	public static final String EXTRA_DEVICE_ADDRESS = "device_address";
	private BluetoothAdapter btAdapter;
	private BgButton btnRefresh;
	private BaseList<DeviceItem> deviceList;
	private BroadcastReceiver receiver;
	
	@Override
	protected void onCreate() {
		setContentView(R.layout.activity_device_list);
		
		((BgTextView)findViewById(R.id.txtDevices)).setTextColor(UI.colorState_current);
		
		final BgListView list = (BgListView)findViewById(R.id.list);
		deviceList = new BaseList<DeviceItem>(DeviceItem.class);
		deviceList.setObserver(list);
		list.setOnItemClickListener(this);
		btnRefresh = (BgButton)findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(this);
		btnRefresh.setIcon(UI.ICON_REFRESH);
		if (UI.isLowDpiScreen) {
			findViewById(R.id.panelControls).setPadding(UI._8dp, 0, 0, 0);
			findViewById(R.id.panelScanning).setPadding(UI._8dp, UI._8dp, UI._8dp, UI._8dp);
		}
		
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (deviceList == null)
					return;
				final String action = intent.getAction();
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					final String address = device.getAddress();
					if (address.startsWith(BTCommunicator.OUI_LEGO)) {
						boolean paired = false;
						for (int i = deviceList.getCount() - 1; i >= 0; i--) {
							if (deviceList.getItemT(i).address.equals(address)) {
								paired = deviceList.getItemT(i).paired;
								deviceList.setSelection(i, false);
								deviceList.removeSelection();
								break;
							}
						}
						final String name = device.getName();
						deviceList.add(new DeviceItem(((name == null || name.length() == 0) ? getText(R.string.null_device_name).toString() : name) + " - " + address, address, paired), -1);
					}
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					if (btnRefresh != null)
						btnRefresh.setVisibility(View.VISIBLE);
					findViewById(R.id.panelScanning).setVisibility(View.GONE);
					if (deviceList.getCount() == 0)
						deviceList.add(new DeviceItem(getText(R.string.none_found).toString(), null, false), -1);
				}
			}
		};
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		getHostActivity().registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		getHostActivity().registerReceiver(receiver, filter);
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		final Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getAddress().startsWith(BTCommunicator.OUI_LEGO))
					deviceList.add(new DeviceItem(device.getName() + " - " + device.getAddress(), device.getAddress(), true), -1);
			}
		}
        if (btAdapter.isDiscovering())
        	btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
	}
	
	@Override
	protected void onDestroy() {
		btnRefresh = null;
		if (btAdapter != null) {
			btAdapter.cancelDiscovery();
			btAdapter = null;
		}
		if (receiver != null) {
			getHostActivity().unregisterReceiver(receiver);
			receiver = null;
		}
		if (deviceList != null) {
			deviceList.setObserver(null);
			deviceList = null;
		}
	}
	
	@Override
	public void onClick(View view) {
		if (view == btnRefresh) {
	        if (btAdapter == null)
	        	return;
			if (btAdapter.isDiscovering())
	        	btAdapter.cancelDiscovery();
	        btAdapter.startDiscovery();
			btnRefresh.setVisibility(View.GONE);
			findViewById(R.id.panelScanning).setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
		if (position < 0)
			return;
		if (btAdapter != null) {
			btAdapter.cancelDiscovery();
			btAdapter = null;
		}
		final DeviceItem item = deviceList.getItemT(position);
		if (item.address == null || item.address.length() < 17)
			return;
		final Intent intent = new Intent();
		final Bundle data = new Bundle();
		data.putString(DEVICE_NAME_AND_ADDRESS, item.description);
		data.putString(EXTRA_DEVICE_ADDRESS, item.address);
		data.putBoolean(PAIRING, !item.paired);
		intent.putExtras(data);
		finish(1, intent);
	}
}
