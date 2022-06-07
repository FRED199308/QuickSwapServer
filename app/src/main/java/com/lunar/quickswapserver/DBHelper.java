package com.lunar.quickswapserver;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "AirtimeServer.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_EMAIL = "email";
    public static final String CONTACTS_COLUMN_STREET = "street";
    public static final String CONTACTS_COLUMN_CITY = "place";
    public static final String CONTACTS_COLUMN_PHONE = "phone";
    private HashMap hp;
    @SuppressLint("Range")
    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
       // System.err.println("Created Db");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table airtimelogs " +
                        "(id integer primary key, name text,phone text,amount integer, rechargeDate  DATETIME,value integer,status text)"
        );

    }
public  void creatLogsTable()
{
    SQLiteDatabase db=this.getWritableDatabase();

    db.execSQL(
            "create table airtimelogs " +
                    "(id integer primary key, name text,phone text,amount integer, rechargeDate  DATETIME,airtimevalue integer,status text,remarks text,response text,network text)"
    );
    db.close();
}



    public  void creatpackagesTable()
    {
        SQLiteDatabase db=this.getWritableDatabase();

        db.execSQL(
                "create table plans " +
                        "( planName text,plantype text,cost text, network  text,actualCost text,agentCost text)"
        );
        db.close();
    }

    public  void createAgentTable()
    {
        SQLiteDatabase db=this.getWritableDatabase();

        db.execSQL(
                "create table agents " +
                        "( agentName text,contact text,dateEnrolled text,status text)"
        );



//        db.execSQL("ALTER TABLE plans ADD COLUMN agentCost text");
//        db.execSQL("ALTER TABLE orders ADD COLUMN orderNumber text");
//        db.execSQL("ALTER TABLE plansRequests ADD COLUMN orderNumber text");
        db.close();
    }

    public  void createBulkSMSTable()
    {
        SQLiteDatabase db=this.getWritableDatabase();

        db.execSQL(
                "create table agents " +
                        "( sendMode text,contact text,deviceId text,dateSent text,status text)"
        );


        db.close();
    }


    public  void creatpackagesRequestsTable()
    {
        SQLiteDatabase db=this.getWritableDatabase();

        db.execSQL(
                "create table plansRequests " +
                        "( planName text,plantype text,cost text, network  text,phone text,dateRequested DATETIME,status text,mode text,orderNumber text,payingPhone text,requestingPhone text,deviceId text)"
        );
        db.close();
    }

    public  void createOrdersTable()
    {
        SQLiteDatabase db=this.getWritableDatabase();

        db.execSQL(
                "create table orders " +
                        "( planName text,plantype text,cost text, network  text,phone text,dateRequested DATETIME,status text,paymentCode text,orderNumber text )"
        );

        db.close();
    }

    public  void clearAitimeLogs()
    {
        SQLiteDatabase db=this.getWritableDatabase();

        db.execSQL(
                "Delete from airtimelogs"
        );
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS airtimelogs");
        onCreate(db);

    }

    public String registerPlanRequest(String planName, String planType, int amount, String network, String phone, String dateRequested, String status, String mode,String orderNumber,String payingPhone) {


        SQLiteDatabase db = this.getReadableDatabase();

            ContentValues contentValues = new ContentValues();

            contentValues.put("planName", planName);
            contentValues.put("plantype", planType);
            contentValues.put("cost", amount);

            contentValues.put("network", network);
            contentValues.put("phone", phone);
            contentValues.put("dateRequested", dateRequested);
            contentValues.put("status", status);
            contentValues.put("mode", mode);
            contentValues.put("payingPhone", payingPhone);
            contentValues.put("orderNumber", orderNumber);


            db.insert("plansRequests", null, contentValues);
            db.close();
            return "saved";








    }
    @SuppressLint("Range")
    public Map<String,String> getRequestDetails(String orderNumber) {
        ArrayList array_list = new ArrayList();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from plansRequests where  orderNumber='"+orderNumber+"'   order by dateRequested asc", null );
        res.moveToFirst();

        Map map=new HashMap();
        if(res.moveToFirst()){
            map=new HashMap();
            map.put("planName",res.getString(res.getColumnIndex("planName")));
            map.put("cost",res.getString(res.getColumnIndex("cost")));
            map.put("network",res.getString(res.getColumnIndex("network")));
            map.put("planType",res.getString(res.getColumnIndex("plantype")));
            map.put("payingPhone",res.getString(res.getColumnIndex("payingPhone")));
            map.put("rechargePhone",res.getString(res.getColumnIndex("phone")));
            map.put("dateRequested",res.getString(res.getColumnIndex("dateRequested")));
            map.put("status",res.getString(res.getColumnIndex("status")));
            map.put("orderNumber",res.getString(res.getColumnIndex("orderNumber")));




            System.err.println("Request Deatils:"+map);

            res.moveToNext();
        }
        res.close();
        db.close();
        return map;
    }


    public String registerAgent(String agentName, String contact, String date) {


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from agents where  contact='"+contact+"'", null );
        res.moveToFirst();

        if(res.moveToFirst()){

            return "This Number is already Registered With Another Agent.Please Contact Support If You Did Not Register it";


        }
        else{

            ContentValues contentValues = new ContentValues();

            contentValues.put("AgentName", agentName);
            contentValues.put("Contact", contact);
            contentValues.put("dateEnrolled", date);
            contentValues.put("status", "Pending");



            db.insert("agents", null, contentValues);
            res.close();
            db.close();
            return "saved";
        }












    }
    public String registerOrder(String planName, String planType, int amount, String network, String phone, String dateRequested, String status, String mode, String paymentCode,String orderNumber) {


        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put("planName", planName);
        contentValues.put("plantype", planType);
        contentValues.put("cost", amount);

        contentValues.put("network", network);
        contentValues.put("phone", phone);


        contentValues.put("dateRequested", dateRequested);

        contentValues.put("status", status);
        contentValues.put("orderNumber", orderNumber);
        contentValues.put("paymentCode", paymentCode);

        db.insert("orders", null, contentValues);
        db.close();
        return "saved";








    }



    public String registerPlan(String planName, String planType, int amount, String network, String actualCost,String agentCost ) {







        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from plans where  planName='"+planName+"' and network='"+network+"'", null );
        res.moveToFirst();

        if(res.moveToFirst()){
            db.close();
            res.close();
            return "Plan Already Exist";


        }
        else{
            ContentValues contentValues = new ContentValues();

            contentValues.put("planName", planName);
            contentValues.put("plantype", planType);
            contentValues.put("cost", amount);

            contentValues.put("network", network);
            contentValues.put("actualCost", actualCost);
            contentValues.put("agentcost", agentCost);
            db.insert("plans", null, contentValues);

            db.close();
            return "Saved";
        }






    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean insertAirtimeRecord (String name, String phone, int amount, String rechargeDate, int value, String status, String remark, String response, String network) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("amount", amount);
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        contentValues.put("rechargeDate", myFormatObj.format(LocalDateTime.now()));
        contentValues.put("airtimeValue", value);
        contentValues.put("status", status);
        contentValues.put("remarks", remark);
        contentValues.put("response", response);
        contentValues.put("network", network);
        db.insert("airtimelogs", null, contentValues);
        db.close();
        return true;
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from airtimelogs ", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (Integer id, String name, String phone, String email, String street, String place) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    @SuppressLint("Range")
    public ArrayList getAllLOgs() {
        ArrayList array_list = new ArrayList();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from airtimelogs order by rechargeDate desc", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            Map map=new HashMap();
           map.put("name",res.getString(res.getColumnIndex("name")));
    map.put("phone",res.getString(res.getColumnIndex("phone")));
       map.put("amount",res.getString(res.getColumnIndex("amount")));
         map.put("rechargeDate",res.getString(res.getColumnIndex("rechargeDate")));
            map.put("airtimeValue",res.getString(res.getColumnIndex("airtimevalue")));
            map.put("status",res.getString(res.getColumnIndex("status")));
            map.put("remarks",res.getString(res.getColumnIndex("remarks")));
 map.put("response",res.getString(res.getColumnIndex("response")));
            map.put("network",res.getString(res.getColumnIndex("network")));
            array_list.add(map);

            res.moveToNext();
        }
        db.close();
        res.close();
        System.out.println("heheh "+array_list);
        return array_list;
    }

    public ArrayList getAllplans() {
        ArrayList array_list = new ArrayList();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from plans", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            Map map=new HashMap();
            map.put("planname",res.getString(res.getColumnIndex("planName")));
            map.put("cost",res.getString(res.getColumnIndex("cost")));
            map.put("network",res.getString(res.getColumnIndex("network")));
            map.put("planType",res.getString(res.getColumnIndex("plantype")));

            array_list.add(map);

            res.moveToNext();
        }
        System.out.println("heheh "+array_list);
        res.close();


        db.close();
        return array_list;
    }
    public ArrayList getAllOrders() {
        ArrayList array_list = new ArrayList();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from orders  order by dateRequested desc", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            Map map=new HashMap();
            map.put("planname",res.getString(res.getColumnIndex("planName")));
            map.put("cost",res.getString(res.getColumnIndex("cost")));
            map.put("network",res.getString(res.getColumnIndex("network")));
            map.put("planType",res.getString(res.getColumnIndex("plantype")));
            map.put("phone",res.getString(res.getColumnIndex("phone")));

            map.put("dateRequested",res.getString(res.getColumnIndex("dateRequested")));
            map.put("status",res.getString(res.getColumnIndex("status")));
            map.put("paymentCode",res.getString(res.getColumnIndex("paymentCode")));





            array_list.add(map);

            res.moveToNext();
        }
        res.close();
        db.close();
        return array_list;
    }

    public ArrayList getAllNetworkCategoryPlans(String category, String network) {
        ArrayList array_list = new ArrayList();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from plans where  plantype='"+category+"' and network='"+network+"'", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            Map map=new HashMap();
            map.put("planname",res.getString(res.getColumnIndex("planName")));
            map.put("cost",res.getString(res.getColumnIndex("cost")));
            map.put("network",res.getString(res.getColumnIndex("network")));
            map.put("planType",res.getString(res.getColumnIndex("plantype")));
            map.put("agentCost",res.getString(res.getColumnIndex("agentCost")));

            array_list.add(map);

            res.moveToNext();
        }
        res.close();
        db.close();
        System.out.println("heheh "+array_list);
        return array_list;
    }
    public Map<String, String> getPlanCostDetails(String planName, String network) {
        ArrayList array_list = new ArrayList();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from plans where  planName='"+planName+"' and network='"+network+"'", null );
        res.moveToFirst();

       if(res.moveToFirst()){
            Map map=new HashMap();
           map.put("planName",res.getString(res.getColumnIndex("planName")));
           map.put("cost",res.getString(res.getColumnIndex("cost")));
           map.put("network",res.getString(res.getColumnIndex("network")));
           map.put("actualCost",res.getString(res.getColumnIndex("actualCost")));
           map.put("agentCost",res.getString(res.getColumnIndex("agentCost")));
           db.close();
           res.close();
          return  map;


        }
       else{
           Map map=new HashMap();
           map.put("planName","");
           map.put("cost","");
           map.put("network","");
           map.put("actualCost","");
           map.put("agentCost","");
           db.close();
           res.close();
           return map;
       }

    }

    public Map<String, String> getAgentDetails(String contact) {
        ArrayList array_list = new ArrayList();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from agents where  contact='"+contact+"'", null );
        res.moveToFirst();

        if(res.moveToFirst()){
            Map map=new HashMap();
            map.put("agentName",res.getString(res.getColumnIndex("agentName")));
            map.put("status",res.getString(res.getColumnIndex("status")));
            map.put("contact",res.getString(res.getColumnIndex("contact")));
            db.close();
            res.close();
            return  map;


        }
        else{
            Map map=new HashMap();
            map.put("agentName","");
            db.close();
            res.close();
            return map;
        }

    }

    public ArrayList getOrderDetails(String phone) {
        ArrayList array_list = new ArrayList();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from orders where  phone='"+phone+"'   order by dateRequested desc", null );
        res.moveToFirst();


        while(res.isAfterLast() == false){
            Map map=new HashMap();
            map.put("planname",res.getString(res.getColumnIndex("planName")));
            map.put("cost",res.getString(res.getColumnIndex("cost")));
            map.put("network",res.getString(res.getColumnIndex("network")));
            map.put("planType",res.getString(res.getColumnIndex("plantype")));
            map.put("phone",res.getString(res.getColumnIndex("phone")));
            map.put("dateRequested",res.getString(res.getColumnIndex("dateRequested")));
            map.put("status",res.getString(res.getColumnIndex("status")));
            map.put("paymentCode",res.getString(res.getColumnIndex("paymentCode")));
            map.put("orderNumber",res.getString(res.getColumnIndex("orderNumber")));




            array_list.add(map);

            res.moveToNext();
        }
        db.close();
        res.close();
        return array_list;
    }


    public ArrayList getagentsDetails() {
        ArrayList array_list = new ArrayList();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select rowid,* from agents  order by dateEnrolled desc", null );
        res.moveToFirst();

System.err.println("Reached");
        while(res.isAfterLast() == false){
            Map map=new HashMap();
            map.put("agentName",res.getString(res.getColumnIndex("agentName")));
            map.put("contact",res.getString(res.getColumnIndex("contact")));
            map.put("dateEnrolled",res.getString(res.getColumnIndex("dateEnrolled")));
            map.put("rowId",res.getString(res.getColumnIndex("rowid")));
            map.put("status",res.getString(res.getColumnIndex("status")));





            array_list.add(map);
System.err.println(map);
            res.moveToNext();
        }
        db.close();
        res.close();
        return array_list;
    }


    public boolean updateplanDetails (String planName, String network, int cost,int agentcost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("planName", planName);
        contentValues.put("network", network);
        contentValues.put("cost", cost);
        contentValues.put("agentCost", agentcost);

        db.update("plans", contentValues, "planName = ? and network=? ", new String[] { planName,network } );
        db.close();

        return true;
    }

    public boolean updateAgentDetails (String agentName, String nominatedNUmber,String rowId,String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("agentName", agentName);
        contentValues.put("contact", nominatedNUmber);
        contentValues.put("status", status);


System.err.println(agentName+"  "+nominatedNUmber);
        db.update("agents", contentValues, "rowid=?", new String[] { rowId } );
        db.close();


        return true;
    }

    public boolean deletplanDetails (String planName, String network, int cost) {
        SQLiteDatabase db = this.getWritableDatabase();


        db.delete("plans",  "planName = ? and network=? ", new String[] { planName,network } );

db.close();
        return true;
    }
    public boolean deleteAgent (String agentName, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();


        db.delete("agents",  "agentName = ? and contact=? ", new String[] { agentName,contact } );

        db.close();
        return true;
    }
    public boolean deleteOrderDetails (String phone, String mpesacode, String cost) {
        SQLiteDatabase db = this.getWritableDatabase();


        db.delete("orders",  "paymentcode = ? and cost=?  and phone=?", new String[] { mpesacode,cost,phone } );

        db.close();
        return true;
    }


    public ArrayList<String> FilterLOgs(String status) {
        ArrayList array_list = new ArrayList();
System.err.println("status:"+status);
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from airtimelogs where remarks='"+status+"' order by rechargeDate desc", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            Map map=new HashMap();
            map.put("name",res.getString(res.getColumnIndex("name")));
            map.put("phone",res.getString(res.getColumnIndex("phone")));
            map.put("amount",res.getString(res.getColumnIndex("amount")));
            map.put("rechargeDate",res.getString(res.getColumnIndex("rechargeDate")));
            map.put("airtimeValue",res.getString(res.getColumnIndex("airtimevalue")));
            map.put("status",res.getString(res.getColumnIndex("status")));
            map.put("remarks",res.getString(res.getColumnIndex("remarks")));
            map.put("response",res.getString(res.getColumnIndex("response")));
            map.put("network",res.getString(res.getColumnIndex("network")));
            array_list.add(map);

            res.moveToNext();
        }
        res.close();
        db.close();
        return array_list;

    }
}