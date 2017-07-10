package info.android.rowing.Stroke;
import info.android.rowing.activity.NewSessionActivity;
import  info.android.rowing.activity.SessionActivity;

/**
 * Created by Shorouk Ramzi on 4/16/2017.
 */

public class StrokeDetection {
    private int acc_count=50 , smooth_count=3 ,l=0; //j=0;
    private static double[] acc_values = new double[50];
    private double[] acc_smooth = new double[smooth_count];
    private double avg_acc=0;
    private double pthreshold=2,nthreshold= -0.25;
    private static int flag=0, flag2=0, flag3=0;
    private static double ymin=-0.25;
    private double ymax=0,ymaxGraph=5,yminGraph=-5;
    private  static int strokeCount=0,lastStrokeCount=0;
    private double max=0;
    private int accelDir=0;
    private double a=0;
    private final static int ROWER_MODE = 1;
    private final static int COXSWAIN_MODE = -1;
    private int accMode;


    public  double[] DetectStroke( double accX,double accY,double accZ)
    //public void  DetectStroke( double accX,double accY,double accZ)
    {
       // SessionActivity mSessionActivity=new SessionActivity();
       // NewSessionActivity mSessionActivity=new NewSessionActivity();
        //mSeries1.appendData(new DataPoint((double)j, acc_x), true, 250);
        max = Math.abs(accY) > Math.abs(accZ) ? accY : accZ; // if device is exactly flat or vertical, one axis has to be ignored

        accelDir = max < 0 ? -1 : 1;

         a = accelDir * Math.sqrt(accY* accY+ accZ*accZ);

        acc_smooth[l]=a;
        if (l < (smooth_count - 1) ) {
            l++;
        }
        else {
            l = 0;
        }

        avg_acc = (acc_smooth[0] + acc_smooth[1] + acc_smooth[2]) / 3;

        for (int k = 1; k < acc_count; k++)
        {
            acc_values[k-1]=acc_values[k];
        }
        acc_values[49]=avg_acc;


        if (avg_acc > (pthreshold) && flag == 0) {
            if (avg_acc > ymax) {
                ymax = avg_acc;
            }
            flag2 = 1; //lma da yb2a 1 m3nah en el acc 3det el +ve threshold
        }

        if (avg_acc <= (nthreshold) && flag == 0 && flag2 == 1) {
            if (ymax > ymaxGraph) {
                ymaxGraph=ymax;

               // viewport.setMaxY((int) 1.5 * ymaxGraph);
                NewSessionActivity.getInstance().setViewportMax(ymaxGraph);

               // mSessionActivity.setViewportMax(ymaxGraph);
            }
            flag = 1; //da ma3nah eny lazem ad5ol fi el -ve
            flag2 = 0; //l2any hdawar 3ala el min
            flag3 = 0;
            pthreshold = 0.6 * ymax;
            if(pthreshold<1){
                pthreshold=1;
            }
            ymax = 0;
        }

        if (avg_acc <= nthreshold && flag == 1) {
            if (avg_acc < ymin) {
                ymin = avg_acc;
            }
            flag2 = 1;
        }

        if (avg_acc >= pthreshold && flag == 1 && flag2 == 1) {
            strokeCount++;
            NewSessionActivity.getInstance().strokeCount(strokeCount);
            //mSessionActivity.strokeCount(strokeCount);
                      /*  distance = (TextView)findViewById(R.id.distance);
                        distance.setText(String.valueOf(strokeCount) );*/
            flag = 0; //da ma3nah eny lazem ad5ol fi el +ve
            flag2 = 0; //l2any hdawar 3ala el max
            //nthreshold = 0.25 * ymin;
            nthreshold = 0.6 * ymin;
            if(nthreshold>-1){
                nthreshold=-1;
            }
            if (ymin < yminGraph) {
                yminGraph=ymin;
               // viewport.setMinY((int) 1.5 * yminGraph);
                NewSessionActivity.getInstance().setViewportMin(yminGraph);

               // mSessionActivity.setViewportMin(yminGraph);

            }
            ymin = 0;
            //for (int k = 0; k < acc_count; k++) {
            //   acc_values[k] = 0;
            //}
        }
       return acc_values;
    }
    public int strokeCount(){
        return strokeCount;
    }

}
