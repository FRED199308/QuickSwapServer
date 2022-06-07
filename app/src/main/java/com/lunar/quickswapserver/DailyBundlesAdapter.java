package com.lunar.quickswapserver;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DailyBundlesAdapter extends FragmentStateAdapter {
    String title,packageToLoad,network;
    Context context;
    public DailyBundlesAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, String title, String packageToLoad, String network, Context context) {
        super(fragmentManager, lifecycle);
        this.packageToLoad=packageToLoad;
        this.title=title;
        this.network=network;
        this.context=context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position==0)
        {
            return new fragmentModel(title,packageToLoad,"Safaricom",context);
        }
        else if(position==1)
        {
            return new fragmentModel(title,packageToLoad,"Airtel",context);
        }
        else{
            return new fragmentModel(title,packageToLoad,"Telkom",context);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
