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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.GitkitUser.UserProfile;
import com.google.identitytoolkit.IdToken;
import com.google.identitytoolkit.IdProvider;

import java.util.HashMap;
import java.util.Map;

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
    private static final String KEY_ACCOUNT_DISPLAY_NAME = "keyAccountDisplayName";
    private static final String KEY_ACCOUNT_TYPE = "keyAccountType";
    private static final String KEY_ACCOUNT_URL = "keyAccountUrl";
    private static final String KEY_ACCOUNT_TOKEN = "keyAccountToken";

    /** The logcat tag constant. */
    private static final String TAG = AccountManagerImpl.class.getSimpleName();

    // Activity request codes.
    private static final int ACCOUNTS_PERMISSION_REQUEST = 1;
    private static final int ACCOUNT_SETUP_REQUEST = 2;

    // Private instance variables

    /** The GIT client. */
    private GitkitClient mClient;

    /** The account containing the email address, list of personas, ... */
    private UserProfile mAccount;

    /** The token obtained from the GIT. */
    private IdToken mToken;

    // Public constructor

    /**
     * Construct a manager with a given set of parameters.
     *
     * @param bundle The parameter container.
     */
    public AccountManagerImpl(final Bundle bundle, final SharedPreferences preferences) {
        // Initialize using the given bundle and determine if there are preferences to set up the account manager state.
        //init(bundle);
        if (preferences.contains(KEY_ACCOUNT_NAME)) {
            // There are preferences.  Set up the account state, logging but otherwise ignoring errors.
            buildProfileAndToken(preferences);
        }
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
    @Override public void handleSigninSuccess(final UserProfile profile, final IdToken idToken, final SharedPreferences preferences) {
        // Store the given data in the current account.
        setActive(profile, idToken, preferences);
        mClient = null;
        Log.d(TAG, String.format("Handling a successful signin: token/user {%s/%s}.", idToken.getTokenString(), profile));
    }

    /**
     * Override to implement.
     *
     * @see com.pajato.android.gamechat.account.AccountManager#hasAccount()
     */
    @Override public boolean hasAccount() {
        // The account is considered missing if neither the account nor the token has been loaded from the preference
        // store, or if the token has expired.
        return mAccount != null && mToken != null && !mToken.isExpired();
    }

    /**
     * Override to implement the signin process using GIT.
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

    /**
     * Build the User GIT profile and token using preferences.
     *
     * @param preferences The given shared preferences object.
     */
    private void buildProfileAndToken(final SharedPreferences preferences) {
        // Build a map of parameter values, ensuring that each is not null in order to proceed.
        Map<String, String> data = new HashMap<>();
        String[] keys = {KEY_ACCOUNT_NAME, KEY_ACCOUNT_DISPLAY_NAME, KEY_ACCOUNT_TYPE, KEY_ACCOUNT_URL, KEY_ACCOUNT_TOKEN};
        for (String key : keys) {
            String value = preferences.getString(key, null);
            if (value == null) {
                Log.w(TAG, String.format("Invalid null preference value for key {%s}.", key));
                return;
            }
            Log.d(TAG, String.format("Capturing preference for key {%s} with value {%s}.", key, value));
            data.put(key, value);
        }

        // Process the data to construct the account state.
        IdProvider provider = IdProvider.valueOf(data.get(KEY_ACCOUNT_TYPE));
        mAccount = new UserProfile(data.get(KEY_ACCOUNT_NAME), data.get(KEY_ACCOUNT_DISPLAY_NAME), data.get(KEY_ACCOUNT_URL), provider);
        mToken = IdToken.parse(data.get(KEY_ACCOUNT_TOKEN));
        Log.d(TAG, String.format("Account info: profile: {%s}; token: {%s}.", mAccount, mToken.getTokenString()));
    }

    /**
     * Sets the given User GIT profile and token as the active account.  Also persists that account to the shared
     * preferences store.
     *
     * @param profile The given GIT profile.
     * @param token The given GIT token.
     * @param preferences The given SharedPreferences store.
     */
    private void setActive(final UserProfile profile, final IdToken token, final SharedPreferences preferences) {
        // Update the member variables and persist the relevant details to the preferences store.
        mAccount = profile;
        mToken = token;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ACCOUNT_NAME, profile.getEmail());
        editor.putString(KEY_ACCOUNT_DISPLAY_NAME, profile.getDisplayName());
        editor.putString(KEY_ACCOUNT_TYPE, profile.getIdProvider().name());
        editor.putString(KEY_ACCOUNT_URL, profile.getPhotoUrl());
        editor.putString(KEY_ACCOUNT_TOKEN, token.getTokenString());
        editor.apply();
    }

    // Private instance methods.

    // Private classes

}
