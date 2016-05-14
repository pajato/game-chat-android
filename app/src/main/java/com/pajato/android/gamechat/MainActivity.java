package com.pajato.android.gamechat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;

import com.pajato.android.gamechat.chat.ChatManager;
import com.pajato.android.gamechat.chat.ChatManagerImpl;
import com.pajato.android.gamechat.game.GameManager;
import com.pajato.android.gamechat.game.GameManagerImpl;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.account.AccountManagerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * The main activity ... <tbd>
 */
public class MainActivity extends AppCompatActivity implements GitkitClient.SignInCallbacks {

    // Public class constants

    // Private class constants

    /** The logcat tag constant. */
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The preferences file name. */
    private static final String PREFS = "GameChatPrefs";

    // Private instance variables

    /** The account manager handles all things related to accounts: signing in, switching accounts, setup, aliases, etc. */
    private AccountManager mAccountManager;

    /** The chat manager handles all things chat: accessing rooms, history, settings, etc. */
    private ChatManager mChatManager;

    /** The game manager handles all game related activities: mode, players, game selection, etc. */

    private GameManager mGameManager;

    /** The top level container. */
    private DrawerLayout mDrawerLayout;

    // Public instance methods

    /**
     * Override to implement a successful login by ensuring that the account is registered and up to date..
     */
    @Override public void onSignIn(IdToken idToken, GitkitUser user) {
        // Create a session for the given user by saving the token in the account.
        Log.d(TAG, String.format("Processing a successful signin: idToken/user {%s/%s}.", idToken, user));
        Toast.makeText(this, "You are successfully signed in to GameChat", Toast.LENGTH_LONG).show();
        mAccountManager.handleSigninSuccess(user.getUserProfile(), idToken, getSharedPreferences(PREFS, 0));
    }

    /** Override to implement a failed signin by posting a message to the User. */
    @Override public void onSignInFailed() {
        // Post a message and head back to the main screen.
        Log.d(TAG, "Processing a failed signin attempt.");
        Toast.makeText(this, "Sign in failed", Toast.LENGTH_LONG).show();
        mAccountManager.handleSigninFailed();
     }

    // Protected instance methods

    /**
     * Handle the signin activity result value by passing it back to the account manager.
     *
     * @param requestCode ...
     * @param resultCode ...
     * @param intent ...
     */
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Pass the event off to the account manager for processing.
        Log.d(TAG, String.format("Processing a signin result: requestCode/resultCode/intent {%d/%d/%s}.", requestCode, resultCode, intent));
        if (!mAccountManager.handleSigninResult(requestCode, resultCode, intent)) {
            Log.d(TAG, "Signin result was not processed by the GIT.");
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    /**
     * Set up the app per the characteristics of the running device.
     *
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        // Initialize the app state as necessary.
        super.onCreate(savedInstanceState);
        mAccountManager = new AccountManagerImpl(savedInstanceState, getSharedPreferences(PREFS, 0));
        mGameManager = new GameManagerImpl(savedInstanceState);
        mChatManager = new ChatManagerImpl(savedInstanceState);

        // Start the app.  Setup the top level views: toolbar, action bar and drawer layout.
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

        // Determine if an account needs to be set up.
        if (!mAccountManager.hasAccount()) {
            // There is no account yet.  Give the User a chance to sign in even though it is not strictly necessary, for
            // example when playing games with the computer.
            mAccountManager.signin(this);
        }
    }

    /**
     * Override to implement by passing a given intent to the account manager to see if it is consuming the intent.
     *
     * @param intent The given Intent object.
     */
    @Override protected void onNewIntent(final Intent intent) {
        // Give the account manager a chance to consume the intent.
        if (!mAccountManager.handleIntent(intent)) {
            Log.d(TAG, "Signin intent was not processed by the GIT.");
            super.onNewIntent(intent);
        }
    }

    // Private instance methods

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

}
