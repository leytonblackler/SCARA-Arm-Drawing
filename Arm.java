
/**
 * Class represents SCARA robotic arm.
 * 
 * @Arthur Roberts
 * @0.0
 */

import ecs100.UI;
import java.awt.Color;
import java.util.*;

public class Arm
{

    // fixed arm parameters
    private int xm1;  // coordinates of the motor(measured in pixels of the picture)
    private int ym1;
    private int xm2;
    private int ym2;
    private double r;  // length of the upper/fore arm
    // parameters of servo motors - linear function pwm(angle)
    // each of two motors has unique function which should be measured
    // linear function cam be described by two points
    // motor 1, point1 
    private double pwm1_val_1; 
    private double theta1_val_1;
    // motor 1, point 2
    private double pwm1_val_2; 
    private double theta1_val_2;

    // motor 2, point 1
    private double pwm2_val_1; 
    private double theta2_val_1;
    // motor 2, point 2
    private double pwm2_val_2; 
    private double theta2_val_2;

    // current state of the arm
    private double theta1; // angle of the upper arm
    private double theta2;

    private double xj1;     // positions of the joints
    private double yj1; 
    private double xj2;
    private double yj2; 
    private double xt;     // position of the tool
    private double yt;
    private double xt2;
    private double yt2;
    private boolean valid_state; // is state of the arm physically possible?

    
    /**
     * Constructor for objects of class Arm
     */
    public Arm()
    {
        xm1 = 290; // set motor coordinates
        ym1 = 372;
        xm2 = 379;
        ym2 = 374;
        r = 156.0;
        theta1 = -90.0*Math.PI/180.0; // initial angles of the upper arms
        theta2 = -90.0*Math.PI/180.0;
        valid_state = false;
    }

    // draws arm on the canvas
    public void draw()
    {
        // draw arm
        int height = UI.getCanvasHeight();
        int width = UI.getCanvasWidth();
        // calculate joint positions
        xj1 = xm1 + r*Math.cos(theta1);
        yj1 = ym1 + r*Math.sin(theta1);
        xj2 = xm2 + r*Math.cos(theta2);
        yj2 = ym2 + r*Math.sin(theta2);

        //draw motors and write angles
        int mr = 20;
        UI.setLineWidth(5);
        UI.setColor(Color.BLUE);
        UI.drawOval(xm1-mr/2,ym1-mr/2,mr,mr);
        UI.drawOval(xm2-mr/2,ym2-mr/2,mr,mr);
        // write parameters of first motor
        String out_str=String.format("t1=%3.1f",theta1*180/Math.PI);
        UI.drawString(out_str, xm1-2*mr,ym1-mr/2+2*mr);
        out_str=String.format("xm1=%d",xm1);
        UI.drawString(out_str, xm1-2*mr,ym1-mr/2+3*mr);
        out_str=String.format("ym1=%d",ym1);
        UI.drawString(out_str, xm1-2*mr,ym1-mr/2+4*mr);
        // ditto for second motor                
        out_str = String.format("t2=%3.1f",theta2*180/Math.PI);
        UI.drawString(out_str, xm2+2*mr,ym2-mr/2+2*mr);
        out_str=String.format("xm2=%d",xm2);
        UI.drawString(out_str, xm2+2*mr,ym2-mr/2+3*mr);
        out_str=String.format("ym2=%d",ym2);
        UI.drawString(out_str, xm2+2*mr,ym2-mr/2+4*mr);
        // draw Field Of View
        UI.setColor(Color.GRAY);
        UI.drawRect(0,0,640,480);

        UI.setColor(Color.RED);
        UI.drawOval(xj1 - 5, yj1 - 5, 10, 10);
        UI.drawOval(xj2 - 5, yj2 - 5, 10, 10);
        //UI.setColor(Color.CYAN);
        //UI.drawOval(xa - 5, ya - 5, 10, 10);

        // it can be uncommented later when
        // kinematic equations are derived
        if ( valid_state) {
            // draw upper arms
            UI.setColor(Color.GREEN);
            UI.drawLine(xm1,ym1,xj1,yj1);
            UI.drawLine(xm2,ym2,xj2,yj2);
            //draw forearms
            UI.drawLine(xj1,yj1,xt,yt);
            UI.drawLine(xj2,yj2,xt,yt);
            // draw tool
            double rt = 20;
            UI.drawOval(xt-rt/2,yt-rt/2,rt,rt);
        }

    }
    
    

    // calculate tool position from motor angles 
    // updates variable in the class
    public void directKinematic(){
        // midpoint between joints
        double xa = xj1 + 0.5*(xj2 - xj1);
        double ya = yj1 + 0.5*(yj2 - yj1);
        double dx = xj2 - xj1;
        double dy = yj2 - yj1;
        // distance between joints
        double d = Math.sqrt((xj2 - xj1)*(xj2 - xj1)) + ((yj2 - yj1)*(yj2 - yj1));
        if (d<2*r){
            valid_state = true;
            // half distance between tool positions
            double l1 = d/2;
            double h = Math.sqrt((r*r) - (l1*l1));
            double alpha = Math.atan((yj1 - yj2) / (xj2 - xj1));
            // tool position
            xt = xa + h * Math.cos((Math.PI/2) - alpha); /*# Removed 'double' variable creation */
            yt = ya + h * Math.sin((Math.PI/2) - alpha);
        } else {
            valid_state = false;
        }

    }

    // motor angles from tool position
    // updetes variables of the class
    public void inverseKinematic(double xt_new,double yt_new){

        valid_state = true;
        xt = xt_new;
        yt = yt_new;
        valid_state = true;
        double xa1 = xm1 + 0.5*(xt - xm1);
        double ya1 = ym1 + 0.5*(yt - ym1);
        double dx1 = xt - xm1; 
        double dy1 = yt - ym1;
        // distance between pen and motor
        double d1 = Math.sqrt(dx1*dx1 + dy1*dy1);
        if (d1>2*r){
            UI.println("Arm 1 - can not reach");
            valid_state = false;
            return;
        }

        double l1 = d1/2;
        double h1 = Math.sqrt(r*r - (l1*l1));
        double alpha = -(Math.PI/2) + Math.atan2(dy1,dx1);
        xj1 = xa1 + h1*Math.cos(alpha);
        yj1 = ya1 + h1*Math.sin(alpha);
        theta1 = Math.atan2(yj1 - ym1, xj1 - xm1);

        if ((theta1>0)||(theta1<-Math.PI)){
            valid_state = false;
            UI.println("Angle 1 -invalid");
            return;
        }

        double xa2 = xm2 + 0.5*(xt - xm2);
        double ya2 = ym2 + 0.5*(yt - ym2);
        double dx2 = xt - xm2; 
        double dy2 = yt - ym2;
        double d2 = Math.sqrt(dx2*dx2 + dy2*dy2);
        double l2 = d2/2;
        double h2 = Math.sqrt(r*r - (l2*l2));
        if (d2>2*r){
            UI.println("Arm 2 - can not reach");
            valid_state = false;
            return;
        }
        double alpha2 = -(Math.PI/2) + Math.atan2(dy2,dx2);
        // elbows positions

        xj2 = xa2 - h2*Math.cos(alpha2);
        yj2 = ya2 - h2*Math.sin(alpha2);
        
        //SINGULARITY CHECK
        double jointDx = Math.abs(xj2-xj1);
        double jointDy = Math.abs(yj2-yj1);
        double jointDifference = Math.sqrt((jointDx*jointDx)+(jointDy*jointDy));
        
        //Distance between joints must be less than 98% of total length of 2 arms.
        double maxDifference = 0.98 * (2*r);
        if ((jointDifference > maxDifference) || (yt > yj1 && yt > yj2)) {
            UI.println("===Equilibrium singularity!===");
            valid_state = false;
            return;
        }

        // motor angles for both 1st elbow positions
        theta2 = Math.atan2(yj2-ym2, xj2-xm2);
        if (((theta2*180/Math.PI) < -90)||(theta2>0)||(theta2 < -Math.PI)){
            valid_state = false;
            UI.println("Angle 2 -invalid");
            return;
        }

        UI.printf("xt:%3.1f, yt:%3.1f\n",xt,yt);
        UI.printf("theta1:%3.1f, theta2:%3.1f\n",theta1*180/Math.PI,theta2*180/Math.PI);

        UI.drawString("dx1: " + dx1 + " dx2: " + dx2, 100, 100);
        return;
    }

    // returns angle of motor 1
    public double get_theta1(){
        return theta1;
    }
    // returns angle of motor 2
    public double get_theta2(){
        return theta2;
    }
    // sets angle of the motors
    public void set_angles(double t1, double t2){
        theta1 = t1;
        theta2 = t2;
        //CALCULATE PWM BASED ON EQUATION
        //pwm_val_1 = 
        //pwm_val_2 =
    }
    
    /*calibration points
    PWM   THETA1 (all negative)
    1400    108     
    1450    113
    1500    118
    1550    123
    1600    127
    1650    132
    
    PWM     THETA2 (all negative)
    1300    44
    1350    48
    1400    52
    1450    57
    1500    62
    1550    66
    */

    // returns motor control signal
    // for motor to be in position(angle) theta1
    // linear intepolation
    public int get_pwm1(){
        int pwm = (int) ((-9.8162 * theta1) + 314.34);
        return pwm;
    }
    // ditto for motor 2
    public int get_pwm2(){
        int pwm = (int) ((-9.9696 * theta2) + 1034.5);
        return pwm;
    }

}
