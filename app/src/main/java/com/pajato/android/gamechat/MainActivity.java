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

import android.Manifest;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Provides the main screens for the GameChat application.  These include the first time sign-in, the ...
 *
 * @author Paul Michael Reilly
 */
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    // Public class constants

    // Private class constants

    // Local storage keys.
    private static final String ACCOUNT_NAME = "accountName";
    private static final String EMAIL_ADDRESS = "emailAddress";

    /** The logcat tag constant. */
    private static final String TAG = MainActivity.class.getSimpleName();

    // Activity request codes.
    private static final int ACCOUNTS_PERMISSION_REQUEST = 1;
    private static final int ACCOUNTS_SELECTION_REQUEST = 2;

    // Private instance variables

    /** The email address associated with the currently active account. */
    private String mEmailAddress;

    /** The account name for the currently active account. null to signify no account access. */
    private String mAccountName;

    /** The OAuth2 provider for the currently active account. */
    private String mProviderName;

    /** The top level container. */
    private DrawerLayout mDrawerLayout;

    // Public instance methods

    /**
     * todo: document
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Establish the email account to be used. The User has selected an identity provider.  If the GET_ACCOUNTS
     * permission is granted then the email address will either be selected by the User from a list of account email
     * addresses or the User may create a new account.  Otherwise the User will specify the email address.
     *
     * @see android.support.v4.app.ActivityCompat#onRequestPermissionsResult(int, String[], int[])
     */
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        // Check the request code to ensure the integrity of the callback.
        if (requestCode == ACCOUNTS_PERMISSION_REQUEST) {
            boolean useDeviceAccounts = false;
            Log.d(TAG, String.format("Grant results size {%d}.", grantResults.length));
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The request code is ok. Follow the will of the User in using the device accounts or not.
                useDeviceAccounts = true;
            }

            // Set up the authentication email address by either picking or entering it.
            Log.d(TAG, String.format("The User has weighed in and %s access to the accounts.", useDeviceAccounts ? "granted" : "disallowed"));
            if (useDeviceAccounts) {
                chooseEmailAddressWithDeviceAccounts();
            } else {
                getEmailAddressFromDialog();
            }
        }
    }

    // Protected instance methods

    /**
     * Set up the app per the characteristics of the running device.
     *
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Determine if an account needs to be set up.
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        mEmailAddress = prefs.getString(EMAIL_ADDRESS, null);
        if (mEmailAddress == null) {
            // There is no account selected yet.  Do it now.
            setupAccount();
        } else {
            // Skip the account setup and go right to the main activity.
            setupMain();
        }
    }

    /**
     * Handle the result from picking an email address (or creating a new one) from an account by establishing the
     * current account name and email address.  This is followed by authentication and moving to the main screen.
     *
     * @see android.app.Activity#onActivityResult(int, int, Intent)
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // Determine if this is a request we are interested in.
        if (requestCode == ACCOUNTS_SELECTION_REQUEST && resultCode == RESULT_OK) {
            // It is.  The account name is the email address.
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            mEmailAddress = mAccountName;
            Log.d(TAG, String.format("The user will use {%s} to login from provider {%s}.", mEmailAddress, mProviderName));

            // Temporarily display the result in a snackbar.
            final View layout = findViewById(R.id.first_time_layout);
            final String format = "You will be logged in using the %s email address: %s";
            Snackbar.make(layout, String.format(format, mProviderName, mEmailAddress), Snackbar.LENGTH_LONG).show();
        }
    }

    // Private instance methods.

    /**
     * Choose an email address from a list of device account having previously selected an OAuth2 provider.
     */
    private void chooseEmailAddressWithDeviceAccounts() {
        // Pick an email address or add a new account.
        Providers provider = Providers.valueOf(mProviderName);
        Log.d(TAG, String.format("Choosing an account from provider {%s}.", provider.key));
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{provider.key}, false, null, null, null, null);
        startActivityForResult(intent, ACCOUNTS_SELECTION_REQUEST);
    }

    /**
     * Choose or configure an email address to use based on the User provided permission for account access.
     */
    private void chooseOrConfigureEmailAddress() {
        // Determine if the app can access the underlying accounts.
        final View layout = findViewById(R.id.first_time_layout);
        final String permission = Manifest.permission.GET_ACCOUNTS;
        final String[] permissionArray = new String[]{permission};
        final boolean hasAccountAccess = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        if (hasAccountAccess) {
            // Select an email address using device accounts.
            chooseEmailAddressWithDeviceAccounts();
        } else {
            // Access to device accounts has not been granted. Ask the User, maybe.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Provide some additional rationale for the User along with an action to request the permission again.
                Snackbar.make(layout, String.format(getString(R.string.request_account_rationale_format), mProviderName),
                              Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                  @Override public void onClick(View view) {
                                      ActivityCompat.requestPermissions(MainActivity.this, permissionArray, ACCOUNTS_PERMISSION_REQUEST);
                                  }
                              }).show();
            } else {
                // Request the permission. The result will be received in onRequestPermissionResult().
                Snackbar.make(layout, getText(R.string.request_account_permission), Snackbar.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, permissionArray, ACCOUNTS_PERMISSION_REQUEST);
            }
        }
    }

    /**
     * The User has disallowed account access.  Obtain the email address from a dialog.
     */
    private void getEmailAddressFromDialog() {
        // tbd: invoke a dialog to capture the
        final View layout = findViewById(R.id.first_time_layout);
        final String format = "You will be logged in using the %s email address: %s.";
        Snackbar.make(layout, String.format(format, mProviderName, "tbd"), Snackbar.LENGTH_LONG).show();
    }

    /**
     * Setup the initial account.  The User will be presented with an introductory welcome message and a list of
     * identity provider names. The selected provider will be used to query the associated accounts on the device. These
     * will then be presented to the User
     */
    private void setupAccount() {
        // As a debug aid, log the current authenticator types ...
        AuthenticatorDescription[] accountTypes = AccountManager.get(this).getAuthenticatorTypes();
        Log.d(TAG, String.format("Found {%d} authenticators.", accountTypes.length));
        for (int i = 0; i < accountTypes.length; i++) {
            Log.d(TAG, String.format("Authenticator (provider) type: {%s}.", accountTypes[i]));
        }

        // Create the initial login selection screen and a list of OAuth2 provider names.
        Log.d(TAG, "Setting up first time account.");
        setContentView(R.layout.first_time_account_selection);
        final ListView listview = (ListView) findViewById(R.id.provider_list_view);
        Providers[] providers = Providers.values();
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < providers.length; ++i) {
            list.add(providers[i].toString());
        }

        // Set up an adaptor to populate the list view used to select an OAuth2 provider.
        final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                // Allow the User to select or configure the email address to use with the selected provider.
                mProviderName = (String) parent.getItemAtPosition(position);
                Log.d(TAG, String.format("Selected provider {%s}.", mProviderName));
                chooseOrConfigureEmailAddress();
            }
        });
    }

    /**
     * Main setup.
     */
    private void setupMain() {
        // Setup the top level views: toolbar, action bar and drawer layout.
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Setup the navigation view and the main app pager.
        //
        // Todo: extend this to work with specific device classes based on size to provide optimal layouts.
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationHandler());
        GameChatPagerAdapter adapter = new GameChatPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    // Private classes

    /**
     * Provide a class to handle the view pager setup.
     */
    private class GameChatPagerAdapter extends FragmentStatePagerAdapter {

        /**
         * A list of panels ordered left to right.
         */
        private List<Panel> panelList = new ArrayList<>();

        /**
         * Build an adapter to handle the panels.
         *
         * @param fm The fragment manager.
         */
        public GameChatPagerAdapter(final FragmentManager fm) {
            super(fm);
            panelList.add(Panel.ROOMS);
            panelList.add(Panel.CHAT);
            panelList.add(Panel.MEMBERS);
            panelList.add(Panel.GAME);
        }

        @Override
        public Fragment getItem(int position) {
            return panelList.get(position).getFragment();
        }

        @Override
        public int getCount() {
            return panelList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return MainActivity.this.getString(panelList.get(position).getTitleId());
        }

    }

    /**
     * Provide a handler for navigation panel selections.
     */
    private class NavigationHandler implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(final MenuItem menuItem) {
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();
            Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
            return true;
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}
