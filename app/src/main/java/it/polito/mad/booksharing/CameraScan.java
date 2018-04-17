package it.polito.mad.booksharing;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CameraScan extends AppCompatActivity {

    private ZXingScannerView mScannerView;
    private ProgressDialog pd;
    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_scan);
        code = new String("");
        //Scan code start the function that read the bar code
        scanCode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanCode();
    }

    @Override
    public void onBackPressed(){
       mScannerView.stopCamera();
       Intent intent = new Intent();
       setResult(RESULT_CANCELED, intent);
       finish();

    }


    public void scanCode() {
        //Instantiate the class
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);

        //Set the handler that manage the result
        mScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
            @Override
            public void handleResult(Result result) {
                //Do anything with result here
                mScannerView.stopCamera();
                setContentView(R.layout.activity_add_book);
                result.getText();
                //Here i get the ISBN
                code = result.getText();
                //Ask to the server the JSON file that contain informations about the searched book.
                //Add the ISBN (code) to the request
                //Start an AsyncTask that ask for the Json
                new DownloadJson(CameraScan.this).execute("https://www.googleapis.com/books/v1/volumes?q=isbn:" + code);
            }
        });
        mScannerView.startCamera();
    }


    private class DownloadJson extends AsyncTask<String, String, List<String>> {

        private Context context;

        public DownloadJson(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Before the download, start a progressDialog
            pd = new ProgressDialog(context);
            pd.setMessage(getString(R.string.wait));
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected List<String> doInBackground(String... params) {

            //First take the selfLink. Self link contains more information than the normal request.
            //For example the publisher often is not present in the normal request.
            //- I collect the selfLinks
            //- I ask to google the Json file related to that self lonk
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            List<String> selfLinks = new ArrayList<>();

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                //Parse the Json
                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                String resultJson = buffer.toString();

                //Potentially could be different items, so starting to now I will manage a list of items.
                //But for this application, can be only one item because there is only one book for one ISBN
                JSONObject jsonObject = new JSONObject(resultJson);
                JSONArray jArray = jsonObject.getJSONArray("items");

                for (int i = 0; i < jArray.length(); i++) {
                    selfLinks.add(jArray.getJSONObject(i).getString("selfLink"));
                }

                //Handle error
            } catch (JSONException e) {
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

            //Put all the Json file linked to the selfLink into an array called results.
            List<String> results = new ArrayList<>();
            for (String selfLink : selfLinks) {
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
                    results.add(buffer.toString());
                } catch (
                        MalformedURLException e)

                {
                } catch (
                        IOException e)

                {
                    e.printStackTrace();
                } finally
                {
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
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<String> results) {
            super.onPostExecute(results);

            if (pd.isShowing()) {
                pd.dismiss();
            }

            //Here all the Json file have been downloaded
            Intent intent = new Intent();
            if(code!=null){
                intent.putExtra("isbn", code);
            }
            if (results.isEmpty()) {
                setResult(RESULT_CANCELED, intent);
            } else {

                //Collect all information downloaded from the Json file.
                //If some information miss, put "" in the book object so that the user will have the opportunity to fill it.
                List<Book> books = new ArrayList<>();
                setResult(RESULT_OK, intent);
                for(String result : results) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject volumeInfo = jsonObject.getJSONObject("volumeInfo");

                        String title = "";
                        if(volumeInfo.has("title")){
                            title =  volumeInfo.getString("title");
                        }

                        String subtitle = "";
                        if(volumeInfo.has("subtitle")){
                            subtitle =  volumeInfo.getString("subtitle");
                        }

                        String author = "";
                        if(volumeInfo.has("authors")){
                            JSONArray authors = volumeInfo.getJSONArray("authors");
                            for (int j = 0; j < authors.length(); j++) {
                                author += authors.getString(j) + " ";
                            }
                        }

                        String date = "";
                        if(volumeInfo.has("publishedDate")){
                            date =  volumeInfo.getString("publishedDate");
                        }

                        String publisher = "";
                        if(volumeInfo.has("publisher")){
                            publisher =  volumeInfo.getString("publisher");
                        }


                        String urlStr = "";
                        if(volumeInfo.has("imageLinks")){
                            JSONObject imageLink = volumeInfo.getJSONObject("imageLinks");
                            if(imageLink.has("thumbnail")){
                               urlStr = imageLink.getString("thumbnail");
                            }
                        }


                        //Take isbn
                        String isbn10 = null, isbn13 = null;
                        if(volumeInfo.has("industryIdentifiers")){
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
                        }

                        Book book = new Book(title, subtitle, author, date, publisher, "", urlStr, "", "Sergio", isbn10, isbn13, "");
                        books.add(book);
                    } catch (JSONException e) {
                        setResult(RESULT_CANCELED, intent);
                        e.printStackTrace();
                        break;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("books", (ArrayList<? extends Parcelable>) books);
                intent.putExtras(bundle);
            }
            finish();
        }
    }
}

