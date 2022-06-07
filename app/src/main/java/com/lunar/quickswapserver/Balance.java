package com.lunar.quickswapserver;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Balance {



    public static String smsBalance() {
        String query_url = "https://api.lunar.cyou/api/smsbalance.php";
        String json = "{ \"data\":{\"senderid\":\""+"22136"+"\",\"secretkey\":\""+"asapbiz@2021!"+"\",\"userid\":\""+"asapbiz"+"\"";

        String m="";

        m=m+"}}";
        json=json+m;
        // System.err.println(json);
        try {
            URL url = new URL(query_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.close();
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String result = IOUtils.toString(in, "UTF-8");

            JSONObject myResponse = new JSONObject(result);



            in.close();
            conn.disconnect();
            if(myResponse.getString("ResponseCode").equalsIgnoreCase("0")){
                return " Ksh "+myResponse.getString("actual_Balance")+",Or Sms: "+myResponse.getString("sms_Balance")+" Item(s)";

            }
            else{
                return "Error";
            }
        } catch (Exception e) {
            System.out.println(e);
            return "Error Occured"+e.getMessage();
        }
    }

}
