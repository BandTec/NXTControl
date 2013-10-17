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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import br.com.bandtec.nxtcontrol.activity.ClientActivity;
import br.com.bandtec.nxtcontrol.activity.MainHandler;
import br.com.bandtec.nxtcontrol.ui.BgButton;
import br.com.bandtec.nxtcontrol.ui.BgDirControl;
import br.com.bandtec.nxtcontrol.ui.BgTextView;
import br.com.bandtec.nxtcontrol.ui.UI;
import br.com.bandtec.nxtcontrol.ui.drawable.ColorDrawable;
import br.com.bandtec.nxtcontrol.util.SerializableMap;

public final class ActivityMain extends ClientActivity implements View.OnClickListener, BgButton.OnPressingChangeListener, BgDirControl.OnBgDirControlChangeListener, BTConnectable, DialogInterface.OnClickListener {
	private static final int OPT_FORCEDORIENTATION = 0x0001;
	private static final int REQUEST_CONNECT_DEVICE = 1000;
	private static final int REQUEST_ENABLE_BT = 2000;
	private BTCommunicator btCommunicator;
	private boolean btErrorPending, btOnByUs, btAlreadyShown, pairing;
	private ProgressDialog connectingProgressDialog;
	private CharSequence lastError;
	private int forcedOrientation, lastDir;
	private BgButton btnExit, btnPortrait, btnLandscape, btnAbout;
	private BgButton[] btns;
	private BgDirControl dirControl;
	private Drawable windowDrawable;
	
	@Override
	public boolean isPairing() {
		return pairing;
	}
	
	private void destroyBTCommunicator() {
		BTCommunicator.destroyBTCommunicatorNow();
		btCommunicator = null;
	}
	
	private void showError(CharSequence error) {
		lastError = error;
		final BgTextView txtError = (BgTextView)findViewById(R.id.txtError);
		txtError.setText(error);
		txtError.setVisibility(View.VISIBLE);
		txtError.setTextColor(UI.colorState_current);
		dirControl.setVisibility(View.GONE);
		findViewById(R.id.panelMsg).setVisibility(View.GONE);
		findViewById(R.id.panelMsg2).setVisibility(View.GONE);
	}
	
	private void showError(int resId) {
		showError(getText(resId));
	}
	
	@Override
	public void activityFinished(ClientActivity activity, int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode > 0) {
				final String address = data.getExtras().getString(ActivityDeviceList.EXTRA_DEVICE_ADDRESS);
				pairing = data.getExtras().getBoolean(ActivityDeviceList.PAIRING);
				connectingProgressDialog = ProgressDialog.show(getHostActivity(), "", getResources().getString(R.string.connecting_please_wait), true);
				destroyBTCommunicator();
				btCommunicator = BTCommunicator.getBTCommunicator(this, MainHandler.handler, BluetoothAdapter.getDefaultAdapter(), getResources());
				btCommunicator.setMACAddress(address);
				btCommunicator.start();
			} else {
				showError(R.string.none_paired);
			}
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			switch (resultCode) {
				case Activity.RESULT_OK:
					btOnByUs = true;
					lastError = null;
					startActivity(new ActivityDeviceList(), REQUEST_CONNECT_DEVICE);
					break;
				case Activity.RESULT_CANCELED:
					showError(R.string.bt_needs_to_be_enabled);
					break;
				default:
					showError(R.string.problem_at_connecting);
					break;
			}
			break;
		}
	}
	
	@Override
	public void onClick(View view) {
		if (view == btnExit) {
			finish();
		} else if (view == btnPortrait) {
			forcedOrientation = 1;
			getHostActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (view == btnLandscape) {
			forcedOrientation = -1;
			getHostActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (view == btnAbout) {
			startActivity(new ActivityAbout());
		}
	}
	
	@Override
	public void onPressingChanged(BgButton button, boolean pressed) {
		for (int i = 0; i < 8; i++) {
			if (button == btns[i]) {
				if (btCommunicator != null)
					btCommunicator.sendMessage(BTCommunicator.WRITE_MAILBOX, Character.toString((char) ((pressed ? 'A' : 'a') + i)));
				return;
			}
		}
	}
	
	@Override
	public void onDirectionChanged(BgDirControl dirControl, String direction) {
		if (btCommunicator != null)
			btCommunicator.sendMessage(BTCommunicator.WRITE_MAILBOX, direction);
	}
	
	@Override
	protected boolean onBackPressed() {
		return true; //prevent back key from going back to the home screen
	}
	
	@Override
	public void onClick(DialogInterface dialog, int id) {
		btErrorPending = false;
		dialog.cancel();
		startActivity(new ActivityDeviceList(), REQUEST_CONNECT_DEVICE);
	}
	
	public void handleMessage(Message message) {
		switch (message.getData().getInt("message")) {
		case BTCommunicator.DISPLAY_TOAST:
			UI.toast(getApplication(), message.getData().getString("toastText"));
			break;
		case BTCommunicator.STATE_CONNECTED:
			connectingProgressDialog.dismiss();
			UI.toast(getApplication(), R.string.connected);
			//btCommunicator.sendMessage(BTCommunicator.GET_FIRMWARE_VERSION, 0);
			break;
		case BTCommunicator.STATE_CONNECTERROR_PAIRING:
			connectingProgressDialog.dismiss();
			destroyBTCommunicator();
			startActivity(new ActivityDeviceList(), REQUEST_CONNECT_DEVICE);
			break;
		case BTCommunicator.STATE_CONNECTERROR:
			connectingProgressDialog.dismiss();
		case BTCommunicator.STATE_RECEIVEERROR:
		case BTCommunicator.STATE_SENDERROR:
			destroyBTCommunicator();
			if (!btErrorPending) {
				btErrorPending = true;
				final AlertDialog.Builder builder = new AlertDialog.Builder(getHostActivity());
				builder.setTitle(getResources().getString(R.string.oops))
						.setMessage(getResources().getString(R.string.bt_error_dialog_message))
						.setCancelable(false)
						.setPositiveButton(R.string.got_it, this);
				builder.create().show();
			}
			break;
		/*case BTCommunicator.FIRMWARE_VERSION:
			if (btCommunicator != null) {
				final byte[] firmwareMessage = btCommunicator.getReturnMessage();
				// check if we know the firmware
				boolean isLejosMindDroid = true;
				for (int pos = 0; pos < 4; pos++) {
					if (firmwareMessage[pos + 3] != LCPMessage.FIRMWARE_VERSION_LEJOSMINDDROID[pos]) {
						isLejosMindDroid = false;
						break;
					}
				}
				//UI.toast(getApplication(), isLejosMindDroid ? "TRUE" : "FALSE");
				//if (isLejosMindDroid) {
				//	mRobotType = R.id.robot_type_lejos;
				//}
				// afterwards we search for all files on the robot
				btCommunicator.sendMessage(BTCommunicator.FIND_FILES, 0);
			}
			break;
		case BTCommunicator.FIND_FILES:
			if (btCommunicator != null) {
				byte[] fileMessage = btCommunicator.getReturnMessage();
				String fileName = new String(fileMessage, 4, 20);
				System.out.println(fileName.replaceAll("\0", ""));
				fileName = fileName.replaceAll("\0", "");
				if (mRobotType == R.id.robot_type_lejos
						|| fileName.endsWith(".nxj")
						|| fileName.endsWith(".rxe")) {
					programList.add(fileName);
				}
				// find next entry with appropriate handle,
				// limit number of programs (in case of error (endless
				// loop))
				if (programList.size() <= MAX_PROGRAMS)
					sendBTCmessage(BTCommunicator.NO_DELAY,
							BTCommunicator.FIND_FILES, 1,
							byteToInt(fileMessage[3]));
			}
			break;
		case BTCommunicator.PROGRAM_NAME:
			if (btCommunicator != null) {
				byte[] returnMessage = btCommunicator.getReturnMessage();
				startRXEprogram(returnMessage[2]);
			}
			break;*/
		}
	}
	
	@Override
	protected void onCreate() {
		MainHandler.activity = this;
		final Context context = getApplication();
		SerializableMap opts = SerializableMap.deserialize(context, "_NXTControl");
		if (opts == null)
			opts = new SerializableMap();
		forcedOrientation = opts.getInt(OPT_FORCEDORIENTATION, 1);
		addWindowFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (forcedOrientation < 0)
			getHostActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		else
			getHostActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		btns = new BgButton[8];
		lastError = null;
		lastDir = BgDirControl.CENTER_DIRECTION;
		btAlreadyShown = false;
	}
	
	@Override
	protected void onResume() {
		if (BluetoothAdapter.getDefaultAdapter() == null) {
			showError(R.string.bt_initialization_failure);
			return;
		}
		if (!btAlreadyShown) {
			btAlreadyShown = true;
			if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
				getHostActivity().startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
			else
				startActivity(new ActivityDeviceList(), REQUEST_CONNECT_DEVICE);
		}
	}
	
	@Override
	protected void onCreateLayout(boolean firstCreation) {
		if (windowDrawable == null) {
			windowDrawable = new ColorDrawable(UI.color_bg);
			getHostActivity().getWindow().setBackgroundDrawable(windowDrawable);
		}
		setContentView(UI.isLandscape ? R.layout.activity_main_l : R.layout.activity_main);
		btnExit = (BgButton)findViewById(R.id.btnExit);
		btnExit.setOnClickListener(this);
		btnPortrait = (BgButton)findViewById(R.id.btnPortrait);
		btnPortrait.setOnClickListener(this);
		btnLandscape = (BgButton)findViewById(R.id.btnLandscape);
		btnLandscape.setOnClickListener(this);
		btnAbout = (BgButton)findViewById(R.id.btnAbout);
		btnAbout.setOnClickListener(this);
		btnExit.setIcon(UI.ICON_EXIT);
		btnPortrait.setIcon(UI.ICON_PORTRAIT);
		btnLandscape.setIcon(UI.ICON_LANDSCAPE);
		btnAbout.setIcon(UI.ICON_INFO);
		dirControl = (BgDirControl)findViewById(R.id.dirControl);
		dirControl.setOnBgDirControlChangeListener(this);
		dirControl.setDirectionValue(lastDir);
		btns[0] = (BgButton)findViewById(R.id.btn1);
		btns[1] = (BgButton)findViewById(R.id.btn2);
		btns[2] = (BgButton)findViewById(R.id.btn3);
		btns[3] = (BgButton)findViewById(R.id.btn4);
		btns[4] = (BgButton)findViewById(R.id.btn5);
		btns[5] = (BgButton)findViewById(R.id.btn6);
		btns[6] = (BgButton)findViewById(R.id.btn7);
		btns[7] = (BgButton)findViewById(R.id.btn8);
		for (int i = 0; i < 8; i++) {
			btns[i].setText(Character.toString((char) ('A' + i)));
			btns[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, UI._22sp);
			btns[i].setOnPressingChangeListener(this);
		}
		if (UI.isLowDpiScreen) {
			findViewById(R.id.panelControls).setPadding(0, 0, 0, 0);
			findViewById(R.id.panelMsg).setPadding(0, 0, 0, 0);
			findViewById(R.id.panelMsg2).setPadding(0, 0, 0, 0);
		} else if (UI.isLargeScreen) {
			final MarginLayoutParams lp = (MarginLayoutParams)dirControl.getLayoutParams();
			lp.leftMargin = UI.dpToPxI(100);
			lp.topMargin = UI.dpToPxI(100);
			lp.rightMargin = UI.dpToPxI(100);
			lp.bottomMargin = UI.dpToPxI(100);
			dirControl.setLayoutParams(lp);
		}
		if (UI.isLandscape) {
			for (int i = 0; i < 8; i++)
				btns[i].setPadding(UI._8dp, 0, UI._8dp, 0);
		}
		if (lastError != null)
			showError(lastError);
	}
	
	@Override
	protected void onOrientationChanged() {
		onCleanupLayout();
		onCreateLayout(false);
	}
	
	@Override
	protected void onCleanupLayout() {
		btnExit = null;
		btnPortrait = null;
		btnLandscape = null;
		btnAbout = null;
		if (dirControl != null) {
			lastDir = dirControl.getDirectionValue();
			dirControl = null;
		}
		for (int i = 0; i < 8; i++)
			btns[i] = null;
	}
	
	@Override
	protected void onDestroy() {
		setExitOnDestroy(true);
		windowDrawable = null;
		lastError = null;
		btns = null;
		SerializableMap opts = new SerializableMap(32);
		opts.put(OPT_FORCEDORIENTATION, forcedOrientation);
		opts.serialize(getApplication(), "_NXTControl");
		destroyBTCommunicator();
		if (btOnByUs) {
			BluetoothAdapter.getDefaultAdapter().disable();
			btOnByUs = false;
		}
		MainHandler.activity = null;
	}
}
