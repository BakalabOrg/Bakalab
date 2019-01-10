package michaelbrabec.bakalab.Adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import michaelbrabec.bakalab.Fragments.UkolyPageFragment;
import michaelbrabec.bakalab.R;

public class UkolyPagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;

    //TODO Use argument for dataset

    public UkolyPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new UkolyPageFragment();
        } else {
            return new UkolyPageFragment();
        }
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 2;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return mContext.getString(R.string.homework_todo);
            case 1:
                return mContext.getString(R.string.homework_done);
            default:
                return null;
        }
    }

}