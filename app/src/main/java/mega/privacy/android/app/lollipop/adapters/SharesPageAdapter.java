package mega.privacy.android.app.lollipop.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.IncomingSharesFragmentLollipop;
import mega.privacy.android.app.lollipop.OutgoingSharesFragmentLollipop;
import mega.privacy.android.app.utils.Util;

public class SharesPageAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 2;
    private Context context;

    public SharesPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        log("getItem: "+position);
        switch (position){
            case 0: {
                return IncomingSharesFragmentLollipop.newInstance();
            }
            case 1:{
                return OutgoingSharesFragmentLollipop.newInstance();
            }
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position){
            case 0: {
                return context.getString(R.string.tab_incoming_shares);
            }
            case 1:{
                return context.getString(R.string.tab_outgoing_shares);
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    private static void log(String log) {
        Util.log("SharesPageAdapter", log);
    }
}
