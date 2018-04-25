package com.aseproject.tensorflowexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetectionResults extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_results);

        Intent i = getIntent();
        String detectionresults= i.getStringExtra("detectionresults");
        WebView wv1=(WebView)findViewById(R.id.wv1);
        wv1.loadDataWithBaseURL("", detectionresults, "text/html", "UTF-8", "");

        final WebView wv2=(WebView)findViewById(R.id.wv2);
        final String detectionresultsfirst=i.getStringExtra("detectionresultsfirst");

        Button btnyoutube=(Button)findViewById(R.id.btnyoutube);
        btnyoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final RequestQueue queue = Volley.newRequestQueue(DetectionResults.this);
                    String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q="+detectionresultsfirst+"&type=video&key=AIzaSyAM8oBvEQUM3SIDy41ofXG-z6HgLeqyVFo";

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    //mTextView.setText("Response: " + response.toString());
                                    Toast.makeText(DetectionResults.this, "result from youtube:" + response, Toast.LENGTH_LONG).show();
                                    wv2.loadDataWithBaseURL("", response+"", "text/html", "UTF-8", "");

                                    List<String> videos=new ArrayList<String>() ;

                                    String list="--";
                                    try {
                                        JSONArray items = response.getJSONArray("items");
                                        for (int i = 0; i < items.length(); i++) {
                                            try {
                                                JSONObject id = ((JSONObject) items.get(i)).getJSONObject("id");
                                                String vid = id.getString("videoId");
                                                JSONObject snip = ((JSONObject) items.get(i)).getJSONObject("snippet");
                                                String title ="";
                                                try {
                                                    title = snip.getString("title");
                                                    list += vid + ":" + title + ",";
                                                }
                                                catch(Exception exp) {}
                                                videos.add(title + ":" + vid + "");
                                            }catch(Exception exp) {}
                                        }

                                        final ListView lv1=(ListView)findViewById(R.id.lv1);
                                        lv1.setAdapter(
                                                new ArrayAdapter<String>(
                                                        DetectionResults.this,
                                                        android.R.layout.simple_list_item_1,
                                                        videos));

                                        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

                                                try {
                                                    TextView tv=(TextView)v;
                                                    String value = tv.getText().toString();

                                                    //String value = lv1.getSelectedItem().toString(); //
                                                    Toast.makeText(DetectionResults.this,
                                                            "v "+value, Toast.LENGTH_SHORT).show();
                                                    String[] parts = value.split(":");
                                                    videoid = parts[parts.length-1]; //getter method
                                                }
                                                catch(Exception exp) {
                                                    System.out.println("errror :"+exp);}

                                            }
                                        });

                                    }
                                    catch(Exception exp)
                                    {

                                    }
                                    Toast.makeText(DetectionResults.this, list, Toast.LENGTH_LONG).show();


                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // TODO: Handle error
                                    Toast.makeText(DetectionResults.this, "e "+error, Toast.LENGTH_SHORT).show();
                                }
                            });

                    queue.add(jsonObjectRequest);


            }
        });
    }
    String videoid="";
    public void showvideo(View v)
    {
        try {
            ListView lv = (ListView) findViewById(R.id.lv1);
            String li = lv.getSelectedItem().toString();

            String[] parts = li.split(":");
            videoid = parts[1];
        }
        catch(Exception exp) {}
        Toast.makeText(this, "id "+videoid, Toast.LENGTH_SHORT).show();
        try {
            Intent i = new Intent(
                    DetectionResults.this,
                    ShowVideo.class);
            i.putExtra("videoid", videoid + "");
            startActivity(i);
        }
        catch(Exception exp)
        {

        }

    }
}
