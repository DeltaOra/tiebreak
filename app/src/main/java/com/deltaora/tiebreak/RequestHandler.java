package com.deltaora.tiebreak;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.widget.Toast;


public class RequestHandler
{
    public String getTargetRate(String strURL,MainActivity sf)
    {
        StringBuffer b = new StringBuffer();
        InputStream is = null;
        URL url;
        HttpURLConnection con = null;
        ThreadPolicy tp = ThreadPolicy.LAX;
        StrictMode.setThreadPolicy(tp);
        String site = "";
        try {
            String sUrl = strURL;

            url = new URL(sUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(10000 /* milliseconds */);
            con.setConnectTimeout(25000 /* milliseconds */);
            con.setRequestMethod("GET");
            con.setDoInput(true);
            // this.page=url.getPath();
            site = url.getQuery();
            // con.addRequestProperty("Referer", "http://blog.dahanne.net");
            // Start the query
            con.connect();
            is = con.getInputStream();

            int ch;
            while ((ch = is.read()) != -1)
            {
                b.append((char) ch);
            }
            if (is != null)
            {
                is.close();
            }

            if (con != null)
            {
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(sf, "error is " + e.toString() + " " + site,Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        // this.page=b.toString();
        // category = b.toString().split("#");

        return b.toString();
    }
}
