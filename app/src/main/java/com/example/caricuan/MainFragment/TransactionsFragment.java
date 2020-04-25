package com.example.caricuan.MainFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.caricuan.R;
import com.example.caricuan.Transactions.List.PayableFragment;
import com.example.caricuan.Transactions.List.ReceivableFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {

    // The UI attributes
    private TabLayout tabLayout;
    private ViewPager viewPager;
    // Adapter for ViewPager
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Make the XML components to java objects
        // Set up the tabLayout and viewPager
        tabLayout = view.findViewById(R.id.transaction_tab);
        viewPager = view.findViewById(R.id.transaction_viewpager);

        // Set up the adapter
        adapter = new TransactionAdapter(getChildFragmentManager());
        adapter.addFrag(new PayableFragment(), "Payable");
        adapter.addFrag(new ReceivableFragment(), "Receivable");

        // Finish it
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    // A FragmentPagerAdapter class for supporting the ViewPager
    public class TransactionAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public TransactionAdapter(FragmentManager fm){
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void addFrag(Fragment fragment, String title) {

            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);

        }
    }

}
