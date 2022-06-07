package com.lunar.quickswapserver;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class InitialConfigs {
    public String rescueValue ="";

    public String switchState ="";
    public String loaderSwitch ="";
    public String serverId ="";

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(String serverStatus) {
        this.serverStatus = serverStatus;
    }

    public String serverStatus;

    public String getloaderSwitch() {
        return loaderSwitch;
    }

    public void setloaderSwitch(String loaderSwitch) {
        this.loaderSwitch = loaderSwitch;
    }

    public void setrescueValue(String rescueValue) {
        this.rescueValue = rescueValue;
    }



    public void setswitchState(String switchState) {
        this.switchState = switchState;
    }

    public String getrescueValue() {
        return rescueValue;
    }



    public String getswitchState() {
        return switchState;
    }




 public InitialConfigs(Context context)  {


        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();


            InputStream inputStream = context.openFileInput("config.xml");


            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);


            myParser.setInput(bufferedReader);

            readEntry(myParser);
        }
        catch (Exception sq)
        {
            sq.printStackTrace();
            try {
                FileOutputStream fos ;


                fos = context.openFileOutput("config.xml",context.MODE_PRIVATE);

                fos.close();;

                FileOutputStream fileos= context.openFileOutput("config.xml", Context.MODE_PRIVATE);
                XmlSerializer xmlSerializer = Xml.newSerializer();
                StringWriter writer = new StringWriter();
                xmlSerializer.setOutput(writer);
                xmlSerializer.startDocument("UTF-8", true);
                xmlSerializer.startTag(null, "configurations");
                xmlSerializer.startTag(null, "rescueValue");
                xmlSerializer.text("10");
                xmlSerializer.endTag(null, "rescueValue");


                xmlSerializer.startTag(null,"switchState");
                xmlSerializer.text("false");
                xmlSerializer.endTag(null, "switchState");

                xmlSerializer.startTag(null,"loaderSwitch");
                xmlSerializer.text("false");
                xmlSerializer.endTag(null, "loaderSwitch");

                xmlSerializer.startTag(null,"serverStatus");
                xmlSerializer.text("false");
                xmlSerializer.endTag(null, "serverStatus");

                xmlSerializer.startTag(null,"serverId");
                xmlSerializer.text("false");
                xmlSerializer.endTag(null, "serverId");


                xmlSerializer.endTag(null, "configurations");
                xmlSerializer.endDocument();
                xmlSerializer.flush();
                String dataWrite = writer.toString();
                fileos.write(dataWrite.getBytes());
                fileos.close();

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }


        }

    }


    // Parses the contents of an entry. If it encounters a mpesaPhone, rechargePhone, or schoolDb tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
   public void readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
     parser.next();
       parser.require(XmlPullParser.START_TAG,null, "configurations");
        String mpesaPhone = null;
        String rechargePhone = null;
        String loaderSwitch=null;
        String serverStatus=null;
        String serverId=null;
        String schoolDb = null;
        String admissionNumber;
        String firstName;
        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("rescueValue")) {

                mpesaPhone = readMpesaPhone(parser);
                setrescueValue(mpesaPhone);
            } else if (name.equals("switchState")) {
                rechargePhone = readRechargePhone(parser);
                setswitchState(rechargePhone);
            }
            else if (name.equals("loaderSwitch")) {
                loaderSwitch = readLoaderSwitch(parser);
                setloaderSwitch(loaderSwitch);
            }
            else if (name.equals("serverStatus")) {
                serverStatus = readserverStatus(parser);
                setServerStatus(serverStatus);
            }

            else if (name.equals("serverId")) {
                serverId = readserverId(parser);
                setServerId(serverId);
            }

            else {
                skip(parser);
            }
        }

    }

    // Processes mpesaPhone tags in the feed.
    private String readMpesaPhone(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "rescueValue");
        String mpesaPhone = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "rescueValue");
        return mpesaPhone;
    }

    private String readserverStatus(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "serverStatus");
        String serverStatus = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "serverStatus");
        return serverStatus;
    }


    private String readserverId(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "serverId");
        String serverId = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "serverId");
        return serverId;
    }
    // Processes rechargePhone tags in the feed.
    private String readRechargePhone(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "switchState");
        String rechargePhone = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "switchState");
        return rechargePhone;
    }

    private String readLoaderSwitch(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "loaderSwitch");
        String loaderSwitch = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "loaderSwitch");
        return loaderSwitch;
    }

    // For the tags mpesaPhone and rechargePhone, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }





    public static   void saveApplicationDetails(String rescueValue, String switchState, String loaderSwitch, Context context, String serverStatus,String serverId)
    {
        try {
            FileOutputStream fos ;


            fos = context.openFileOutput("config.xml", context.MODE_PRIVATE);

            fos.close();;

            FileOutputStream fileos= context.openFileOutput("config.xml", Context.MODE_PRIVATE);
            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(null, "configurations");
            xmlSerializer.startTag(null, "rescueValue");
            xmlSerializer.text(rescueValue);
            xmlSerializer.endTag(null, "rescueValue");



            xmlSerializer.startTag(null,"switchState");
            xmlSerializer.text(switchState);
            xmlSerializer.endTag(null, "switchState");

            xmlSerializer.startTag(null,"loaderSwitch");
            xmlSerializer.text(loaderSwitch);
            xmlSerializer.endTag(null, "loaderSwitch");

            xmlSerializer.startTag(null,"serverStatus");
            xmlSerializer.text(serverStatus);
            xmlSerializer.endTag(null, "serverStatus");


            xmlSerializer.startTag(null,"serverId");
            xmlSerializer.text(serverId);
            xmlSerializer.endTag(null, "serverId");
            xmlSerializer.endTag(null, "configurations");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            String dataWrite = writer.toString();
            fileos.write(dataWrite.getBytes());
            fileos.close();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
