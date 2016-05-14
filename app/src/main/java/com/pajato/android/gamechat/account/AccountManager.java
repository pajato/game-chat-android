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

import com.google.identitytoolkit.GitkitUser.UserProfile;
import com.google.identitytoolkit.IdToken;

/**
 * Provides the interfaces that must be supported by an account manager implementation.
 *
 * @author Paul Michael Reilly
 */
public interface AccountManager {

    /**
     * Handle an intent.
     *
     * @param intent The given intent.
     *
     * @return TRUE iff the intent has been processed.
     */
    boolean handleIntent(final Intent intent);

    /**
     * Handle a failed signin result.
     */
    void handleSigninFailed();

    /**
     * Handle a signin result.
     *
     * @param requestCode The key code that helps determine if this is the signin callback.
     * @param reseultCode An error response?
     * @param intent The Intent object that spawned the activity returning a result.
     *
     * @return TRUE iff the result has been handled.
     */
    boolean handleSigninResult(final int requestCode, final int resultCode, final Intent intent);

    /**
     * Handle a successful signing result.
     *
     * @param profile The GIT user object.
     * @param idToken The GIT token provided by Google to authorize subsequent actions.
     * @param preferences The shared preferences store where the GIT account data is persisted.G
     */
    void handleSigninSuccess(final UserProfile profile, final IdToken idToken, final SharedPreferences preferences);

    /** @returns TRUE iff the application has a persisted account on the device. */
    boolean hasAccount();

    /**
     * Kick off the signin process to create or select an account to use with the given activity.
     *
     * @param activity The activity on whose behalf the signin is happening.
     */
    void signin(final Activity activity);
}
