package com.philips.qrscanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.philips.qrscanner.utils.DataAttributes;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;


public class HomeActivity extends AppCompatActivity {

    // variables to store extracted xml data
    String name,first_name,last_name,gender,dateOfBirth,careOf,gName,house,street,lm,loc,villageTehsil,postOffice,district,subDistrict,state,postCode;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_INTERNET_REQUEST_CODE = 100;
    JSONObject aadhaarData;

    // UI Elements
    TextView tv_sd_name,tv_sd_gender,tv_sd_dob,tv_sd_gname,tv_sd_co,tv_sd_house, tv_sd_vtc,tv_sd_po,tv_sd_dist,
            tv_sd_state,tv_sd_pc,tv_cancel_action, tv_sd_subdist,tv_sd_st,tv_sd_lm,tv_sd_loc;
    EditText ipAddress;
    String strIpAddress = "";
    Button saveIPAddress;
    LinearLayout ll_scanned_data_wrapper,ll_data_wrapper,ll_action_button_wrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide the default action bar
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_home);
        final SharedPreferences settings = getApplicationContext().getSharedPreferences("IP_Storage", 0);
        final SharedPreferences.Editor editor = settings.edit();

        // init the UI Elements
        tv_sd_name = (TextView)findViewById(R.id.tv_sd_name);
        tv_sd_gender = (TextView)findViewById(R.id.tv_sd_gender);
        tv_sd_dob = (TextView)findViewById(R.id.tv_sd_dob);
        tv_sd_gname = (TextView)findViewById(R.id.tv_sd_gname);
        tv_sd_co = (TextView)findViewById(R.id.tv_sd_co);
        tv_sd_house = (TextView)findViewById(R.id.tv_sd_house);
        tv_sd_st = (TextView)findViewById(R.id.tv_sd_st);
        tv_sd_lm = (TextView)findViewById(R.id.tv_sd_lm);
        tv_sd_loc = (TextView)findViewById(R.id.tv_sd_loc);
        tv_sd_vtc = (TextView)findViewById(R.id.tv_sd_vtc);
        tv_sd_po = (TextView)findViewById(R.id.tv_sd_po);
        tv_sd_dist = (TextView)findViewById(R.id.tv_sd_dist);
        tv_sd_subdist = (TextView)findViewById(R.id.tv_sd_subdist);
        tv_sd_state = (TextView)findViewById(R.id.tv_sd_state);
        tv_sd_pc = (TextView)findViewById(R.id.tv_sd_pc);
        tv_cancel_action = (TextView)findViewById(R.id.tv_cancel_action);
        saveIPAddress = (Button)findViewById(R.id.save_ip);
        ipAddress = (EditText) findViewById(R.id.ip_address);

        ll_scanned_data_wrapper = (LinearLayout)findViewById(R.id.ll_scanned_data_wrapper);
        ll_data_wrapper = (LinearLayout)findViewById(R.id.ll_data_wrapper);
        ll_action_button_wrapper = (LinearLayout)findViewById(R.id.ll_action_button_wrapper);

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }

        if (checkSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET},
                    MY_INTERNET_REQUEST_CODE);
        }

        if(isConnected()){
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
        }

        saveIPAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(saveIPAddress.getText().toString().equals("SAVE")){
                    saveIPAddress.setText("EDIT");
                    saveIPAddress.setEnabled(true);
                    editor.putString("ip", ipAddress.getText().toString()).commit();
                    ipAddress.setEnabled(false);
                    strIpAddress = ipAddress.getText().toString();
                }else if(saveIPAddress.getText().toString().equals("EDIT")) {
                    ipAddress.setEnabled(true);
                    saveIPAddress.setText("SAVE");
                    editor.putString("ip", ipAddress.getText().toString()).commit();
                    strIpAddress = ipAddress.getText().toString();
                }
            }
        });

        strIpAddress = settings.getString("ip", "");
        if(!(strIpAddress.length()<1) && !(strIpAddress.isEmpty())){
            ipAddress.setText(strIpAddress);
            saveIPAddress.setText("EDIT");
            ipAddress.setEnabled(false);
        }
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
    /**
     * onclick handler for scan new card
     * @param view
     */
    public void scanNow( View view){
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan a Aadharcard QR Code");
        //integrator.setResultDisplayDuration(500);
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    /**
     * function handle scan result
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            //we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            // process received data
            if(scanContent != null && !scanContent.isEmpty()){
                processScannedData(scanContent);
            }else{
                Toast toast = Toast.makeText(getApplicationContext(),"Scan Cancelled", Toast.LENGTH_SHORT);
                toast.show();
            }

        }else{
            Toast toast = Toast.makeText(getApplicationContext(),"No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * process xml string received from aadhaar card QR code
     * @param scanData
     */
    protected void processScannedData(String scanData){
        //Jeevan gotta remove this
//        Log.d("philips",scanData);
        XmlPullParserFactory pullParserFactory;

        try {
            // init the parserfactory
            pullParserFactory = XmlPullParserFactory.newInstance();
            // get the parser
            XmlPullParser parser = pullParserFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(scanData));

            // parse the XML
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d("philips","Start document");
                } else if(eventType == XmlPullParser.START_TAG && DataAttributes.AADHAAR_DATA_TAG.equals(parser.getName())) {
                    // extract data from tag
                    //name
                    name = parser.getAttributeValue(null,DataAttributes.AADHAAR_NAME_ATTR);
                    //gender
                    gender = parser.getAttributeValue(null,DataAttributes.AADHAAR_GENDER_ATTR);
                    //date of birth
                    dateOfBirth = parser.getAttributeValue(null,DataAttributes.AADHAAR_DOB_ATTR);
                    // care of
                    careOf = parser.getAttributeValue(null,DataAttributes.AADHAAR_CO_ATTR);
                    // g name
                    gName = parser.getAttributeValue(null,DataAttributes.AADHAAR_GNAME_ATTR);
                    //house
                    house = parser.getAttributeValue(null,DataAttributes.AADHAAR_HOUSE_ATTR);
                    // street
                    street = parser.getAttributeValue(null,DataAttributes.AADHAAR_STREET_ATTR);
                    //lm
                    lm = parser.getAttributeValue(null,DataAttributes.AADHAAR_LM_ATTR);
                    //loc
                    loc = parser.getAttributeValue(null,DataAttributes.AADHAAR_LOC_ATTR);
                    // village Tehsil
                    villageTehsil = parser.getAttributeValue(null,DataAttributes.AADHAAR_VTC_ATTR);
                    // Post Office
                    postOffice = parser.getAttributeValue(null,DataAttributes.AADHAAR_PO_ATTR);
                    // district
                    district = parser.getAttributeValue(null,DataAttributes.AADHAAR_DIST_ATTR);
                    // sub-district
                    subDistrict = parser.getAttributeValue(null,DataAttributes.AADHAAR_SUB_DIST_ATTR);
                    // state
                    state = parser.getAttributeValue(null,DataAttributes.AADHAAR_STATE_ATTR);
                    // Post Code
                    postCode = parser.getAttributeValue(null,DataAttributes.AADHAAR_PC_ATTR);

                } else if(eventType == XmlPullParser.END_TAG) {
                    Log.d("philips","End tag "+parser.getName());

                } else if(eventType == XmlPullParser.TEXT) {
                    Log.d("philips","Text "+parser.getText());

                }
                // update eventType
                eventType = parser.next();
            }

            // display the data on screen
            displayScannedData();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }// EO function

    /**
     * show scanned information
     */
    public void displayScannedData(){
        ll_data_wrapper.setVisibility(View.GONE);
        ll_scanned_data_wrapper.setVisibility(View.VISIBLE);
        ll_action_button_wrapper.setVisibility(View.VISIBLE);

        // clear old values if any
        tv_sd_gender.setText("");
        tv_sd_dob.setText("");
        tv_sd_co.setText("");
        tv_sd_gname.setText("");
        tv_sd_house.setText("");
        tv_sd_st.setText("");
        tv_sd_lm.setText("");
        tv_sd_loc.setText("");
        tv_sd_vtc.setText("");
        tv_sd_po.setText("");
        tv_sd_dist.setText("");
        tv_sd_subdist.setText("");
        tv_sd_state.setText("");
        tv_sd_pc.setText("");

        // update UI Elements
        tv_sd_name.setText(name);
        tv_sd_gender.setText(gender);
        tv_sd_dob.setText(dateOfBirth);
        tv_sd_co.setText(careOf);
        tv_sd_gname.setText(gName);
        tv_sd_house.setText(house);
        tv_sd_st.setText(street);
        tv_sd_lm.setText(lm);
        tv_sd_loc.setText(loc);
        tv_sd_vtc.setText(villageTehsil);
        tv_sd_po.setText(postOffice);
        tv_sd_dist.setText(district);
        tv_sd_subdist.setText(subDistrict);
        tv_sd_state.setText(state);
        tv_sd_pc.setText(postCode);
    }

    class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... post_url) {
            try {
                String post_str = "http://"+strIpAddress+"/patients";
                URL url = new URL(post_str);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("charset", "utf-8");

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(aadhaarData.toString());

                wr.flush();
                wr.close();
                connection.getResponseCode();

                if(connection.getResponseCode() == 200) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Sent to the server", Toast.LENGTH_SHORT).show();
                            ll_data_wrapper.setVisibility(View.VISIBLE);
                            ll_scanned_data_wrapper.setVisibility(View.GONE);
                            ll_action_button_wrapper.setVisibility(View.GONE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Failed to send to the server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }catch (Exception e){
                Log.d("Posting to Url",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
    /**
     * display home screen onclick listener for cancel button
     * @param view
     */
    public void showHome(View view){
        ll_data_wrapper.setVisibility(View.VISIBLE);
        ll_scanned_data_wrapper.setVisibility(View.GONE);
        ll_action_button_wrapper.setVisibility(View.GONE);
    }

    /**
     * post data to storage
     */
    public void postData(View view){
        // We are going to use json to save our data
        // create json object
        aadhaarData = new JSONObject();
        final JSONObject addressData = new JSONObject();
        try {
            if(name == null){
                name = "";
            } else {
                if(name.contains(" ")) {
                    String[] names = name.split(" ");
                    first_name = "";
                    int count = names.length;
                    last_name = names[count-1];
                    for(int i = 0; i<count-1; i++) {
                        first_name = first_name+names[i];
                    }
                    aadhaarData.put("firstName", first_name);
                    aadhaarData.put("lastName", last_name);
                } else {
                    first_name = name;
                    aadhaarData.put("firstName", first_name);
                }
            }

            Random rnd = new Random();
            int mrn = 100 + rnd.nextInt(900);
            aadhaarData.put("mrn", "MRN-"+mrn);

            if(gender == null){gender = "";}
            aadhaarData.put(DataAttributes.AADHAAR_GENDER_ATTR, gender);

            if(gName == null || gName ==""){
                gName = "";
            } else {
                aadhaarData.put("gaurdianName", gName);
            }

            if(careOf == null || careOf =="" || careOf.isEmpty()){
                careOf = "";
            } else {
                aadhaarData.put("gaurdianName", careOf);
            }


            String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Calendar.getInstance().getTime());
            aadhaarData.put("createdDate",timeStamp);
//            "createdDate": "2018-04-17T04:08:07.130Z" 05/08/1996

            if(dateOfBirth == null) {
                dateOfBirth = "";
            } else {
                SimpleDateFormat given = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date birthDate = given.parse(dateOfBirth);
                String birthDateTo = to.format(birthDate);
                aadhaarData.put("birthDate", birthDateTo);
            }

            if(house == null){house = "";}
            addressData.put(DataAttributes.AADHAAR_HOUSE_ATTR, house);

            if(street == null){street = "";}
            addressData.put(DataAttributes.AADHAAR_STREET_ATTR, street);

            if(lm == null){lm = "";}
            addressData.put("landmark", lm);

            if(loc == null){loc = "";}
            addressData.put("lc", loc);

            if(villageTehsil == null){villageTehsil = "";}
            addressData.put(DataAttributes.AADHAAR_VTC_ATTR, villageTehsil);

            if(postOffice == null){postOffice = "";}
            addressData.put(DataAttributes.AADHAAR_PO_ATTR, postOffice);

            if(district == null){district = "";}
            addressData.put("district", district);

            if(subDistrict == null){subDistrict = "";}
            addressData.put("subDistrict", subDistrict);

            if(state == null){state = "";}
            addressData.put(DataAttributes.AADHAAR_STATE_ATTR, state);

            if(postCode == null){postCode = "";}
            addressData.put("pinCode", postCode);

            addressData.put("country", "India");

            aadhaarData.put("address", addressData);

            aadhaarData.toString();
            new HttpAsyncTask().execute( "http://"+ strIpAddress +"/patients");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
