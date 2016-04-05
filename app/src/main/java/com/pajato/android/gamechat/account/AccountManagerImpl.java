/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.pajato.android.gamechat.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;

/**
 * Manages the account related aspects of the GameChat application.  These include setting up the first time sign-in,
 * creating a persona (nickname and avatar), switching accounts and personas, ...
 *
 * @author Paul Michael Reilly
 */
public class AccountManagerImpl implements AccountManager {

    // Private class constants

    // Local storage keys.
    private static final String KEY_ACCOUNT_NAME = "keyAccountName";
    private static final String KEY_ACCOUNT_NICKNAME = "keyAccountNickname";
    private static final String KEY_ACCOUNT_TYPE = "keyAccountType";

    /** The logcat tag constant. */
    private static final String TAG = AccountManagerImpl.class.getSimpleName();

    // Activity request codes.
    private static final int ACCOUNTS_PERMISSION_REQUEST = 1;
    private static final int ACCOUNT_SETUP_REQUEST = 2;

    // Private instance variables

    /** The GIT client. */
    private GitkitClient mClient;

    /** The account containing the email address, list of personas, ... */
    //private Account mAccount;

    /** The account name for the currently active account. null to signify no account access. */
    private String mAccountName;

    /** The OAuth2 provider name for the currently active account. */
    private String mAccountType;

    /** The account avatar used in chats. */
    private Object mAccountAvatar;

    /** The account nickname used in chats. */
    private String mAccountNickname;

    // Public constructor

    /**
     * Construct a manager with a given set of parameters.
     *
     * @param bundle The parameter container.
     */
    public AccountManagerImpl(final Bundle bundle) {
        // Initialize using the given bundle.
    }

    // Public instance methods

    /**
     * Override to determine if the given intent should be processed by GIT.
     *
     * @param intent The givent Intent object.
     *
     * @return TRUE iff the given intent has been processed by GIT.
     */
    @Override public boolean handleIntent(final Intent intent) {
        boolean result = mClient.handleIntent(intent);
        Log.d(TAG, String.format("Handling intent: intent/result {%s/%s}.", intent, result));
        return result;
    }

    /**
     * Override to implement by logging a signin failure.
     *
     * @see com.pajato.android.gamechat.account.AccountManager#handleSigninFailed()
     */
    @Override public void handleSigninFailed() {
        // Simply log the failure.
        mClient = null;
        Log.w(TAG, "Signin attempt using GIT failed.");
    }

    /**
     * Override to implement.  Let the GIT attempt to handle the callback.
     * resource.
     *
     * @see com.pajato.android.gamechat.account.AccountManager#handleSigninResult(int, int, Intent)
     */
    @Override public boolean handleSigninResult(int requestCode, int resultCode, Intent intent) {
        boolean result = mClient.handleActivityResult(requestCode, resultCode, intent);
        String format = "Handling signin result: requestCode/resultCode/intent/result {%d/%d/%s/%s}.";
        Log.d(TAG, String.format(format, requestCode, resultCode, intent, result));
        return result;
    }

    /**
     * Override to implement.  Let the GIT attempt to handle the callback.  If it does, then free up the client
     * resource.
     *
     * @see com.pajato.android.gamechat.account.AccountManager#handleSigninResult(int, int, Intent)
     */
    @Override public void handleSigninSuccess(final IdToken idToken, GitkitUser user) {
        // TODO: figure out what to do here.
        mClient = null;
        Log.d(TAG, String.format("Handling a successful signin: token/user {%s/%s}.", idToken, user));
    }

    /**
     * Override to implement.
     *
     * @see com.pajato.android.gamechat.account.AccountManager#hasAccount()
     */
    @Override public boolean hasAccount() {
        Log.d(TAG, "No account exists yet.");
        return false;
    }

    /**
     * Override to implement.
     *
     * @see com.pajato.android.gamechat.account.AccountManager#signin(Activity)
     */
    @Override public void signin(final Activity activity) {
        // TODO: Use the Google Identity Toolkit via an intent that will eventually get moved into an independent
        // library.
        if (activity instanceof GitkitClient.SignInCallbacks) {
            GitkitClient.SignInCallbacks handler = (GitkitClient.SignInCallbacks) activity;
            Log.d(TAG, String.format("Signing in to GameChat using handler {%s}.", handler));
            mClient = GitkitClient.newBuilder(activity, handler).build();
            mClient.startSignIn();
        }
    }

    // Protected instance methods

    // Private instance methods.

    // Private classes

}
