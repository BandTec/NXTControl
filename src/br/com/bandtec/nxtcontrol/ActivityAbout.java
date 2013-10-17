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

import android.content.pm.PackageInfo;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import br.com.bandtec.nxtcontrol.activity.ClientActivity;
import br.com.bandtec.nxtcontrol.ui.BgButton;
import br.com.bandtec.nxtcontrol.ui.UI;
import br.com.bandtec.nxtcontrol.ui.drawable.BorderDrawable;

public final class ActivityAbout extends ClientActivity implements View.OnClickListener {
	private BgButton btnGoBack;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreateLayout(boolean firstCreation) {
		setContentView(R.layout.activity_about);
		btnGoBack = (BgButton)findViewById(R.id.btnGoBack);
		btnGoBack.setOnClickListener(this);
		btnGoBack.setIcon(UI.ICON_GOBACK);
		findViewById(R.id.list).setBackgroundDrawable(new BorderDrawable(false));
		((TextView)findViewById(R.id.lblTitle)).setText("NXT Control");
		try {
			final PackageInfo inf = getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
			((TextView)findViewById(R.id.lblVersion)).setText("v" + inf.versionName);
		} catch (Throwable e) {
		}
		final TextView lblMsg = (TextView)findViewById(R.id.lblMsg);
		lblMsg.setAutoLinkMask(Linkify.ALL);
		lblMsg.setLinksClickable(true);
		lblMsg.setText(getText(R.string.app_more_info));
		lblMsg.setLinkTextColor(UI.color_selected_grad_dk);
		lblMsg.setTextSize(TypedValue.COMPLEX_UNIT_PX, UI._14sp);
		if (UI.isLowDpiScreen) {
			findViewById(R.id.panelControls).setPadding(0, 0, 0, 0);
			findViewById(R.id.panelMsg).setPadding(UI._8dp, UI._8dp, UI._8dp, UI._8dp);
		} else if (UI.isLargeScreen) {
			lblMsg.setTextSize(TypedValue.COMPLEX_UNIT_PX, UI._18sp);
		}
	}
	
	@Override
	protected void onCleanupLayout() {
		btnGoBack = null;
	}
	
	@Override
	public void onClick(View view) {
		if (view == btnGoBack) {
			finish();
		}
	}
}
