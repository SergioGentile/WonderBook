package it.polito.mad.booksharing;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CameraScan extends AppCompatActivity {

    private ZXingScannerView mScannerView;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_scan);
        scanCode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("PAUSE", "CALLED!");
        mScannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("PAUSE", "CALLED!");
        mScannerView.startCamera();
    }



    public void scanCode() {
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
            @Override
            public void handleResult(Result result) {
                //Do anything with result here
                String code;
                mScannerView.stopCamera();
                setContentView(R.layout.activity_add_book);
                result.getText();
                code = result.getText();
                //Get the response
                new DownloadJson(CameraScan.this).execute("https://www.googleapis.com/books/v1/volumes?q=isbn:" + code);
            }
        });
        mScannerView.startCamera();
    }


    private class DownloadJson extends AsyncTask<String, String, String> {

        private Context context;

        public DownloadJson(Context context) {
            this.context = context;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {

            //First take the selfLink
            //Get the self link
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String selfLink = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                String resultJson = buffer.toString();

                JSONObject jsonObject = new JSONObject(resultJson);
                JSONArray jArray = jsonObject.getJSONArray("items");
                //for(int i = 0; i < jArray.length(); i++){
                selfLink = jArray.getJSONObject(0).getString("selfLink");
                Log.d("Link", selfLink);

            }catch (JSONException e ){
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //after take the json
            connection = null;
            reader = null;

            try {
                URL url = new URL(selfLink);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();


            } catch (MalformedURLException e) {
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (pd.isShowing()) {
                pd.dismiss();
            }

            Intent intent = new Intent();

            if (result == null) {
                setResult(RESULT_CANCELED, intent);
            } else {
                try {
                    setResult(RESULT_OK, intent);
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject volumeInfo = jsonObject.getJSONObject("volumeInfo");
                    String title = volumeInfo.getString("title");
                    if (title == null) {
                        Log.d("title", "Title è null!");
                    } else {
                        Log.d("title", "titolo " + title);
                    }
                    JSONArray authors = volumeInfo.getJSONArray("authors");
                    String author = "";
                    for (int j = 0; j < authors.length(); j++) {
                        author += authors.getString(j) + " ";
                    }
                    String date = volumeInfo.getString("publishedDate");
                    String publisher = volumeInfo.getString("publisher");
                    JSONObject imageLink = volumeInfo.getJSONObject("imageLinks");
                    String urlStr = imageLink.getString("thumbnail");

                    //Take isbn
                    String isbn10 = null, isbn13 = null;
                    JSONArray ibans = volumeInfo.getJSONArray("industryIdentifiers");
                    if (ibans.getJSONObject(0).getString("type").equals("ISBN_10")) {
                        isbn10 = ibans.getJSONObject(0).getString("identifier");
                    } else if (ibans.getJSONObject(0).getString("type").equals("ISBN_13")) {
                        isbn13 = ibans.getJSONObject(0).getString("identifier");
                    }
                    if (ibans.getJSONObject(1).getString("type").equals("ISBN_10")) {
                        isbn10 = ibans.getJSONObject(1).getString("identifier");
                    } else if (ibans.getJSONObject(1).getString("type").equals("ISBN_13")) {
                        isbn13 = ibans.getJSONObject(1).getString("identifier");
                    }

                    Log.d("ISBN10", isbn10);
                    Log.d("ISBN13", isbn13);

                    Book book = new Book(title, author, date, publisher, "", urlStr, "", "Sergio", isbn10, isbn13, "");
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("book", book);
                    intent.putExtras(bundle);

                } catch (JSONException e) {
                    setResult(RESULT_CANCELED, intent);
                    e.printStackTrace();
                }
            }
            finish();
        }
    }
}

