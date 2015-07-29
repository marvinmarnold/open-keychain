/*
 * Copyright (C) 2015 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (C) 2015 Adithya Abraham Philip <adithyaphilip@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;

import org.sufficientlysecure.keychain.Constants;
import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.compatibility.DialogFragmentWorkaround;
import org.sufficientlysecure.keychain.service.input.CryptoInputParcel;
import org.sufficientlysecure.keychain.util.Log;
import org.sufficientlysecure.keychain.util.ParcelableProxy;
import org.sufficientlysecure.keychain.util.Preferences;
import org.sufficientlysecure.keychain.util.orbot.OrbotHelper;

/**
 * Simply encapsulates a dialog. If orbot is not installed, it shows an install dialog, else a
 * dialog to enable orbot.
 */
public class OrbotRequiredDialogActivity extends FragmentActivity {

    // action supplied to not show dialog and directly start orbot
    public static final String ACTION_START_ORBOT = "start_orbot";

    // to provide any previous crypto input into which proxy preference is merged
    public static final String EXTRA_CRYPTO_INPUT = "extra_crypto_input";

    public static final String RESULT_CRYPTO_INPUT = "result_crypto_input";

    private CryptoInputParcel mCryptoInputParcel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getAction() != null) {
            switch(getIntent().getAction()) {
                case ACTION_START_ORBOT: {
                    Intent startOrbot = OrbotHelper.getOrbotStartIntent();
                    startActivity(startOrbot);
                    finish();
                    // orbot returns from the activity we start immediately. before it is started
                    // startActivityForResult(startOrbot, REQUEST_ORBOT_START);
                    return;
                }
            }
        }
        mCryptoInputParcel = getIntent().getParcelableExtra(EXTRA_CRYPTO_INPUT);
        if (mCryptoInputParcel == null) {
            mCryptoInputParcel = new CryptoInputParcel();
        }
        showDialog();
    }

    /**
     * Displays an install or start orbot dialog depending on orbot's presence and state
     */
    public void showDialog() {
        DialogFragmentWorkaround.INTERFACE.runnableRunDelayed(new Runnable() {
            public void run() {
                Runnable ignoreTor = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        mCryptoInputParcel.addParcelableProxy(ParcelableProxy.getForNoProxy());
                        intent.putExtra(RESULT_CRYPTO_INPUT, mCryptoInputParcel);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                };

                Runnable dialogDismissed = new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                };

                if (OrbotHelper.putOrbotInRequiredState(R.string.orbot_ignore_tor, ignoreTor, dialogDismissed,
                        Preferences.getPreferences(OrbotRequiredDialogActivity.this)
                                .getProxyPrefs(),
                        OrbotRequiredDialogActivity.this)) {
                    // no action required after all
                    Intent intent = new Intent();
                    intent.putExtra(RESULT_CRYPTO_INPUT, mCryptoInputParcel);
                    setResult(RESULT_OK, intent);
                }
            }
        });
    }
}