package architect.jazzy.medicinereminder.Fragments.Practo.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import architect.jazzy.medicinereminder.Fragments.Practo.PractoSearchCriteria;

/**
 * Created by Jibin_ism on 15-Sep-15.
 */
public class SearchCriteriaAdapter extends FragmentStatePagerAdapter {

    private static final String TAG="SearchCriteriaAdapter";
    public static final int OPTION_CITY=101;
    public static final int OPTION_LOCALITY=102;
    public static final int OPTION_SORT_BY=103;
    public static final int OPTION_SEARCH_FOR=104;
    public static final int OPTION_SPECIALIZATION=105;

    public SearchCriteriaAdapter(FragmentManager fm) {
        super(fm);
        Log.e(TAG, "Constructor");
    }

    @Override
    public Fragment getItem(int position) {
        int criteria;
        switch (position){
            case 0:
                criteria=OPTION_CITY;
                break;
            case 1:
                criteria=OPTION_LOCALITY;
                break;
            case 2:
                criteria=OPTION_SEARCH_FOR;
                break;
            case 3:
                criteria=OPTION_SPECIALIZATION;
                break;
            case 4:
                criteria=OPTION_SORT_BY;
                break;
            default:
                criteria=OPTION_CITY;
                break;
        }
        Log.e(TAG,"Criteria: "+criteria);

        Fragment fragment= PractoSearchCriteria.newInstance(criteria);

        ((PractoSearchCriteria)fragment).setSpinnerItemSelectedListener(new PractoSearchCriteria.SpinnerItemSelectedListener() {
            @Override
            public void onSpinnerItemSelected(int position) {

            }
        });

        return fragment;
    }

    @Override
    public int getCount() {
        return 5;
    }
//
//    @Override
//    public int getItemPosition(Object object) {
//        return POSITION_NONE;
//    }
}
