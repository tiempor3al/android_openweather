package tiempor3al.mx.openweatherimagen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "openweatherimagen";
    private static final int REQUEST_LOCATION = 22;

    //Se utiliza para los servicios de ubicacion
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // Creamos una instancia del Google Api Client
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                            //Aqui ya nos conectamos al Servicio del Api de Google
                            //Podemos solicitar la ubicacion, este metodo esta definido abajo
                            getLocation();

                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }

    }


    /***
     * Solicita la ubicacion mediante GPS. Primero se tiene que verificar que el usuario otorgue los permisos.
     */
    private void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //La primera vez que se ejecuta la actividad, se solicitan permisos
            //Si el usuario selecciono ok, o cancel en la ventana de permisos se mandara el resultado a onRequestPermissionsResult. Este metodo
            //se define abajo
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        //Aqui, ya tenemos permisos

        //Iniciamos
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        LatLng newLocation = new LatLng(location.getLatitude(),location.getLongitude());

                        makeHttpRequest(newLocation);


                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }

            //Tenemos permisos
            getLocation();

        } else {
            // Permission was denied. Display an error message.
        }

    }



    @Override
    protected void onStart() {
        super.onStart();

        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }


    private void makeHttpRequest(LatLng newLocation){


        final ImageView iv_weather = (ImageView) findViewById(R.id.iv_weather);
        final TextView tv_temp = (TextView) findViewById(R.id.tv_temp);


        RequestQueue queue = Volley.newRequestQueue(this);
        //Cambiar url por la de su equipo:
        String url = "http://192.168.1.126:8081/actividad_2/clima/" + newLocation.latitude + "/" + newLocation.longitude;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.has("weather")) {

                                JSONArray weatherArray = response.getJSONArray("weather");
                                JSONObject weather = weatherArray.getJSONObject(0);

                                if (weather.has("icon")) {

                                    String icon = weather.getString("icon");
                                    int identificador = getResources().getIdentifier("imagen_" + icon, "drawable", getPackageName());
                                    iv_weather.setImageDrawable(getResources().getDrawable(identificador, null));
                                }


                            }

                            if (response.has("main")) {
                                JSONObject main = response.getJSONObject("main");
                                Double temp = main.getDouble("temp");
                                tv_temp.setText("" + temp + " \u00b0");

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.getMessage());

                    }
                });

        queue.add(jsonObjectRequest);

    }





}


