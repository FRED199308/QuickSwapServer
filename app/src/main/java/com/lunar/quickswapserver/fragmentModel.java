package com.lunar.quickswapserver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class fragmentModel extends Fragment {

    static AlertDialog.Builder alert ;
    RecyclerView recyclerView;
    String title,packageToLoad,network;
    TextView title_textview;
    Context context;
    DBHelper db;
    SQLiteDatabase sq;
    public fragmentModel(String title, String packageToLoad, String network, Context context) {
        this.title = title;
        this.packageToLoad = packageToLoad;
        this.network = network;
        this.context=context;
db=db.getInstance(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view= inflater.inflate(R.layout.fragment_model, container, false);


        initialiseRecycleview(view);
        return view;




    }

    public  void initialiseRecycleview(View view)
    {
        Bundle extras =getActivity().getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("key");
            //The key argument here must match that used in the other activity
        }

        recyclerView = view.findViewById(R.id.recycleviewModel);
        title_textview = view.findViewById(R.id.title);
        title_textview.setText(network+" "+packageToLoad);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.removeAllViews();
        String packages = "";

System.err.println("Network:"+network);
System.err.println("plan to load:"+packageToLoad);
System.err.println("All Plans"+db.getAllNetworkCategoryPlans(packageToLoad,network));
        ArrayList<String> myList=new ArrayList<>();
if(packageToLoad.equalsIgnoreCase("Airtime"))
{
    ArrayList<String> airtimeList=db.getAllNetworkCategoryPlans("Airtime",network);
    ArrayList<String> noexpcalls=db.getAllNetworkCategoryPlans("No Expiry Calls",network);
    ArrayList<String> noexpcallansms=db.getAllNetworkCategoryPlans("No Expiry Calls And SMS",network);
    ArrayList<String> noexpbundles=db.getAllNetworkCategoryPlans("No Expiry Bundles",network);
    myList.addAll(airtimeList);
    myList.addAll(noexpcalls);
    myList.addAll(noexpcallansms);
    myList.addAll(noexpbundles);

}
else{
    myList =db.getAllNetworkCategoryPlans(packageToLoad,network);
}

        Dailydapter dailydapter = new Dailydapter(myList,context,network,packageToLoad);
        recyclerView.setAdapter(dailydapter);
    }
}