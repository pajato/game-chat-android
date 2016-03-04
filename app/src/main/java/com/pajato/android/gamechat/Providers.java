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
package com.pajato.android.gamechat;

/**
 * Provide a set of common OAuth2 identity providers used in Android account types.
 */
public enum Providers {
    Google ("com.google"),
    Facebook ("com.facebook.auth.login"),
    LinkedIn ("com.linkedin.android"),
    Twitter ("com.twitter.android.auth.login"),
    WhatsApp ("com.whatsapp"),
    Microsoft ("com.google.android.gm.exchange");

    /** The account key associated with a provider. */
    public final String key;

    /**
     * Build a provider with the given account key value.
     *
     * @param key The key value used to access associated accounts.
     */
    Providers(final String key) {
        this.key = key;
    }
}
