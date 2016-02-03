package com.pajato.android.gamechat;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

/**
 * The main activity ... <tbd>
 *
 */
public class MainActivity extends AppCompatActivity {

    /** The top level container. */
    private DrawerLayout mDrawerLayout;

    /**
     * Set up the app per the characteristics of the running device.
     *
     * @param savedInstanceState The saved state on re-creation.  nil on initial creation.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup the top level views: toolbar, action bar and drawer layout.
        super.onCreate(savedInstanceState);
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
        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

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

    // Private classes

    /**
     * Provide a class to handle the view pager setup.
     */
    private class GameChatPagerAdapter extends FragmentStatePagerAdapter {

        /** A list of panels ordered left to right. */
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
