package info.android.rowing.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Timer;
import java.util.TimerTask;

import info.android.rowing.R;
import info.android.rowing.Sensors.AccelerometerValues;

//import static info.android.rowing.Sensors.AccelerometerValues.getAccelerationValues;
import info.android.rowing.Stroke.StrokeDetection;
import info.android.rowing.Stroke.StrokeRate;
import info.android.rowing.Tilting.Tilting;

import static android.R.attr.max;


public class NewSessionActivity extends AppCompatActivity implements LocationListener, SensorEventListener, IBaseGpsListener {
//public class NewSessionActivity  extends AppCompatActivity implements SensorEventListener {
    private int seconds = 0;
    private boolean running ,wasRunning;
    private SensorManager mSensorManager;
    private Sensor senAccelerometer;
    private static double[]  AccelerationComponent=new double[3];
    private final Handler mHandler = new Handler();
    private Runnable mTimer0;
    private Runnable mTimer1;
    private int angle;
    GraphView graph;
    Viewport viewport;
    private LineGraphSeries<DataPoint> mSeries1;
    private int acc_count=50;
    private double[] acc_values = new double[acc_count];
    private int strokeCount=0,lastStrokeCount=0;
    private double avgStrRate=0, strRate;
    private TextView distance ,Speed , strokeRate;
    private int lastTime=0;
    private int time200m=0;
    private int j=0;
    private double m=0;
    double x_axes,y_axes;
    ///// smoothing
    private int  smooth_count=3 ,l=0; //j=0;
    private double[] acc_smooth = new double[smooth_count];
    private double avg_acc=0;
    private static NewSessionActivity mNewSessionActivity;
    private double max=0;
    private int accelDir=0;
    private double a=0;
    ////SPEED &DISTANCE
    private CLocation oldLocation = null;//////////////////////////////////
    private float[] results;
    float Dist = 0;
    float nCurrentSpeed = 0;
    ////firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;
    String str;////namexx
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private LocationManager locationManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_session);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(savedInstanceState!=null) {
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }
        mNewSessionActivity=this;


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            // Should we show an explanation?

             return;
        }*/





       // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);//////////////
        this.updateSpeed(null);////////////
        //to keep the screen light on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        runTimer();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            // Success! There's an  ACCELEROMETER.
            senAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //7atet el sensor el default fi el object bta3y
            mSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL); //3malt regiter lel accelerometer bta3y fi el maneger w ch8lto b normal delay
            for (int k = 0; k < acc_count; k++) {
                acc_values[k] = 0;
            }

        }
        else {
            // Failure! No accelerometer .
        }
        graph = (GraphView) findViewById(R.id.acceleration_graph);
        //Viewport viewport = graph.getViewport();
        mSeries1 = new LineGraphSeries<>(generateData());
        graph.addSeries(mSeries1);


        viewport = graph.getViewport();
        //viewport.scrollToEnd();
        viewport.setScrollable(true);

        //Viewport viewport2 = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(50);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-5);
        viewport.setMaxY(5);
        //////firebase/////
        mFirebaseDatabase = FirebaseDatabase.getInstance();
//        mMessageDatabaseReference = mFirebaseDatabase.getReference().child(str);
        ///send data every second 1000ms////////////////
        /*new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {


                name=" mmmmmmm "; //double as=7;
                RowerData mRowerData=new RowerData( name,strRate, nCurrentSpeed,angle,Dist,time) ;

                mMessageDatabaseReference.push().setValue(mRowerData); //---+-+-+-+

                if(nCurrentSpeed>0) {
                    int Tpower =(int)( 500 / nCurrentSpeed);
                    int tmin =  (Tpower / 60);
                    int tsec =  (Tpower % 60);
                    TextView kh3 = (TextView) findViewById(R.id.m500);
                    kh3.setText(tmin + ":" + tsec);
                }


            }
        }, 0, 8000); // 7 sec*/


    }
    public static NewSessionActivity getInstance(){
        return   mNewSessionActivity;
    }

    private void runTimer() {

        final TextView timeView = (TextView) findViewById(R.id.time);
        strokeRate= (TextView)findViewById(R.id.stroke_rate);

        mTimer0 = new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);


                if (running) {
                    seconds++;
                }
                //avgStrRate = (double)(strokeCount*60) / (double)seconds;
                mHandler.postDelayed(this,1000);
            }
        };
        //handler.postDelayed(mTimer0,1000);
        mHandler.postDelayed(mTimer0, 100);
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                if(running) {
                    GraphView graph = (GraphView) findViewById(R.id.acceleration_graph);
                    Viewport viewport = graph.getViewport();

                    mSeries1.resetData(generateData());//generateData());
                    viewport.setMinX(m-10);
                    viewport.setMaxX(m);
                    // mSeries2.resetData(generateData());//generateData());
                   /* time200m++;
                    if (strokeCount > (lastStrokeCount + 4)) {
                        strRate = (strokeCount - lastStrokeCount) * 60 * 5 / (time200m-lastTime);
                        strokeRate.setText(String.valueOf(strRate) + "str/min");
                        lastTime = time200m;
                        lastStrokeCount = strokeCount;
                    }*/
                    //stroke rate
                    StrokeRate mStrokeRate=new StrokeRate();
                    strRate=mStrokeRate.strRate(strokeCount);
                    TextView strokeRate=(TextView)findViewById(R.id.stroke_rate);
                    strokeRate.setText(String.valueOf(strRate));
                }
                mHandler.postDelayed(this, 200);
               // mHandler.postDelayed(this, 10000);
            }
        };
        mHandler.postDelayed(mTimer1, 100);

    }
   /* @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }*/
    /////////GPS/////////////////////////////////

   @Override
   public void onRequestPermissionsResult(int requestCode,
                                          String permissions[], int[] grantResults) {
       switch (requestCode) {
           case MY_PERMISSIONS_REQUEST_LOCATION: {
               // If request is cancelled, the result arrays are empty.
               if (grantResults.length > 0
                       && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                   // permission was granted, yay! Do the
                   // location-related task you need to do.
                   if (ContextCompat.checkSelfPermission(this,
                           Manifest.permission. ACCESS_FINE_LOCATION)
                           == PackageManager.PERMISSION_GRANTED) {

                       //Request location updates:
                       locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                   }

               } else {

                   // permission denied, boo! Disable the
                   // functionality that depends on this permission.

               }
               return;
           }

       }
   }
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Allow Rowing Mate to access location?")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(NewSessionActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return true;
        } else {
            return false;
        }
    }

    public void finish() {
        super.finish();
        System.exit(0);
    }

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        double nCurrentLong = 0;
        double nCurrentLat = 0;

        results = new float[10];
        if (location != null) {
            nCurrentSpeed = location.getSpeed();
            nCurrentLong = location.getLongitude();
            nCurrentLat = location.getLatitude();
            if (oldLocation != null) {
                Location.distanceBetween(oldLocation.getLatitude(), oldLocation.getLongitude(), nCurrentLat, nCurrentLong, results);
            }
            oldLocation = location;
        }
        Dist = Dist + results[0];
        String strUnits = "m/sec";
        TextView Speed = (TextView) this.findViewById(R.id.pace);
        Speed.setText(String.valueOf(nCurrentSpeed) + " " + strUnits);
        TextView distance = (TextView) this.findViewById(R.id.distance);
        distance.setText(String.valueOf(Dist) + "m");
        TextView COMPZ = (TextView) findViewById(R.id.compz);
         COMPZ.setText("here");
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        if (location != null) {
            CLocation myLocation = new CLocation(location);
            if (running) {
                this.updateSpeed(myLocation);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onGpsStatusChanged(int event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }


    @Override
    protected void onResume() {
        super.onResume();
       // mSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (wasRunning) {
            running = true;
        }
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                //Request location updates:
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, this);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        wasRunning = running;
        running = false;

        mSensorManager.unregisterListener(this);
        mHandler.removeCallbacks(mTimer0);
        mHandler.removeCallbacks(mTimer1);
    }


//////////////////////////
/*@Override
protected void onPause() {
    super.onPause();
    wasRunning = running;
    running = false;

    senSensorManager.unregisterListener(this);
    handler.removeCallbacks(mTimer0);
    handler.removeCallbacks(mTimer1);
    //handler.removeCallbacks(mTimer2);
}

    @Override
    protected void onResume() {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }*/
    /////////////////////////////
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void setViewportMax(double v){
        viewport.setMaxY((int) 1.5 * v);
    }
    public void setViewportMin(double v){
        viewport.setMinY((int) 1.5 * v);
    }
    public void strokeCount(int count){ strokeCount=count;}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        GraphView graph = (GraphView) findViewById(R.id.acceleration_graph);
        Viewport viewport = graph.getViewport();
        Sensor mySensor = sensorEvent.sensor;
        if(running) {
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                //get values of accelerometer
                AccelerometerValues mAccelerometerValues=new AccelerometerValues();
                AccelerationComponent = mAccelerometerValues.getAccelerationValues(sensorEvent);

                //detec stroke
                StrokeDetection mStrokeDetection = new StrokeDetection();
                acc_values= mStrokeDetection.DetectStroke(AccelerationComponent[0], AccelerationComponent[1], AccelerationComponent[2]);
                //stroke count
                strokeCount=mStrokeDetection.strokeCount();
                TextView count=(TextView)findViewById(R.id.stroke_count);
                count.setText(String.valueOf(strokeCount));




               //measure angle of tilting
                Tilting mTilting = new Tilting();
                angle = mTilting.TiltMeasure(AccelerationComponent[0], AccelerationComponent[1], AccelerationComponent[2],sensorEvent);
                TextView s=(TextView) findViewById(R.id.angle);
                s.setText(String.valueOf(angle));
                Tilting mTiltingg = new Tilting();
                float[] gg = mTiltingg.displayGravity(AccelerationComponent[0], AccelerationComponent[1], AccelerationComponent[2],sensorEvent);

                //TextView COMPZ = (TextView) findViewById(R.id.compz);
               // COMPZ.setText(String.valueOf(gg[2]));
            }
        }

    }

    private DataPoint[] generateData() {
        //int count = 50;
        DataPoint[] values = new DataPoint[acc_count];
        for (int i = 0; i < acc_count; i++) {
            x_axes = m;
            y_axes = acc_values[i];
            DataPoint v = new DataPoint(x_axes, y_axes);
            values[i] = v;
           // m=m+0.2;
            m=m+0.2;

        }
        return values;
    }
    // ----------------menu-----------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        distance = (TextView)findViewById(R.id.distance);
        Speed = (TextView)findViewById(R.id.pace);
        strokeRate= (TextView)findViewById(R.id.stroke_rate);
        if (id == R.id.start_session) {
            running = true;
            if(wasRunning) {
                running = true;
            }

        }
        if (id == R.id.stop_session) {
            running = false;
            distance.setText(String.valueOf(Dist) + " m ");
            Speed.setText(String.valueOf(nCurrentSpeed) + " m/s");
            strokeRate.setText(String.valueOf(strRate) + "str/min");
        }

        if (id == R.id.reset_session) {
            running = false;
            seconds = 0;
            Dist = 0 ;
            nCurrentSpeed = 0 ;
            strokeCount = 0;
            time200m=0;
            distance.setText(String.valueOf(Dist) + " m ");
            Speed.setText(String.valueOf(nCurrentSpeed) + " m/s");
            strokeRate.setText(String.valueOf(strRate) + "str/min");
        }
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            // finish the activity
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
