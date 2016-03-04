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

import java.util.ArrayList;

import com.pajato.android.gamechat.fragment.ChatFragment;
import com.pajato.android.gamechat.fragment.GameFragment;
import com.pajato.android.gamechat.fragment.HomeFragment;
import com.pajato.android.gamechat.fragment.MembersFragment;
import com.pajato.android.gamechat.fragment.RoomsFragment;

/**
 * Provide an enumeration of panels used in the app.
 */
public enum Panel {
    CHAT(R.string.chat, R.layout.fragment_chat, ChatFragment.class),
    GAME(R.string.game, R.layout.fragment_game, GameFragment.class),
    HOME(R.string.home, R.layout.fragment_home, HomeFragment.class),
    MEMBERS(R.string.members, R.layout.fragment_members, MembersFragment.class),
    ROOMS(R.string.rooms, R.layout.fragment_rooms, RoomsFragment.class);

    /** The panel title resource id. */
    private int titleId;

    /** The fragment associated with the panel. */
    private Fragment fragment;

    /** The fragment class. */
    private Class<? extends Fragment> fragmentClass;

    /** The panel layout id. */
    private int layoutId;

    /**
     * Create the enum value instance given a title resource id, layout resource id and fragment class..
     *
     * @param titleId The given title id.
     * @param layoutId The given layout id.
     * @param fragmentClass The given layout class.
     */
    Panel(final int titleId, final int layoutId, final Class<? extends Fragment> fragmentClass) {
        this.titleId = titleId;
        this.layoutId = layoutId;
        this.fragmentClass = fragmentClass;
    }

    /** @return The panel title resource id. */
    public int getTitleId() {
        return titleId;
    }

    /**
     * Builds a fragment associated with the panel using lazy creation, i.e. defer instantiation until the fragment
     * is actually needed.
     *
     * @return The panel fragment.
     */
    public Fragment getFragment() {
        if (fragment != null) {
            return fragment;
        }
        return createFragment();
    }

    /**
     * Create a panel fragment using the title resource id as a discriminant.
     *
     * @return The newly created fragment or null if the fragment cannot be created.
     */
    private Fragment createFragment() {
        try {
            fragment = fragmentClass.newInstance();
        } catch (IllegalAccessException exc) {
            // Log.e(TAG, "The class constructor is not accessible.");
        } catch (InstantiationException exc) {
            // Log.e(TAG, "The class is not concrete.");
        } catch (SecurityException exc) {
            // Log.e(TAG, "Not possible");
        } finally {
            return fragment;
        }
    }
}
