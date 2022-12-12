/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package rocketsim;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author CarlCritchfield
 */
public class RocketSim {
    
    static double[][] motorCells;
    static double[] rocketData;
    static final double timeStep = 0.01;
    static final double tempC = 20;
    static final double parachuteCd = 1.5;
    static final int maxSimLengthSeconds = 10;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String raw = Files.readString(Paths.get("motor.csv"));
            String[] tmp = raw.split("\n");
            motorCells = new double[tmp.length][2];
            for (int i = 0; i < tmp.length; i++) {
                motorCells[i][0] = Double.parseDouble(tmp[i].split(",")[0]);
                motorCells[i][1] = Double.parseDouble(tmp[i].split(",")[1]);
            }
            
            raw = Files.readString(Paths.get("rocket.csv"));
            tmp = raw.split("\n");
            tmp = tmp[1].split(",");
            rocketData = new double[tmp.length];
            for (int i = 0; i < tmp.length; i++) rocketData[i] = Double.parseDouble(tmp[i]);
            
            
            double v = 0;
            double h = 0;
            double t = 0;
            String res = "Time,Thrust,Mass,Accel,Vel,Alt,\n0,0,0,0,0,0\n";
            
            double Apogee = 0;
            
            while ((t < 2 || h > 0.1) && t < maxSimLengthSeconds) {
                t += timeStep;
                double thr = getThrust(t);
                double m = getMass(t);
                double cd = rocketData[5];
                double refa = rocketData[4];
                if (t > rocketData[6]) {
                    cd = parachuteCd;
                    refa = rocketData[7];
                }
                double d = 0.5*getDensity(h)*Math.pow(v, 2)*cd*refa;
                if (v < 0) d *= -1;
                double a = ((thr - (9.8*m)) - d)/m;
                if (a < 0 && t < rocketData[3]) a = 0;
                v += a * timeStep;
                h += v * timeStep;
                res = res.concat(Double.toString(t).concat(","))
                        .concat(Double.toString(thr).concat(","))
                        .concat(Double.toString(m).concat(","))
                        .concat(Double.toString(a).concat(","))
                        .concat(Double.toString(v).concat(","))
                        .concat(Double.toString(h).concat(","))
                        .concat("\n");
                if (h > Apogee) Apogee = h;
            }
            
            System.out.println(res);
            System.out.println("Apogee: "+Apogee);
            
            Files.deleteIfExists(Paths.get("RocketResults.csv"));
            
            Files.writeString(Files.createFile(Paths.get("RocketResults.csv")), res);
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static double getThrust(double t) {
        int i = motorCells.length -1;
        for (int j = 1; j < motorCells.length; j++) {
            if (t < motorCells[j][0]) {
                i = j;
                break;
            }
        }
        double upper = motorCells[i][0];
        double lower = motorCells[i-1][0];
        double lerpFactor = (t-lower)/(upper-lower);
        upper = motorCells[i][1];
        lower = motorCells[i-1][1];
        return lerp(lower, upper, lerpFactor);
    }
    
    static double getMass(double t) {
        return lerp(rocketData[0]+rocketData[1], rocketData[0], Math.min(t / rocketData[2], 1));
    }
    
    static double getDensity(double h) {
        return (101325*getPressure(h))/(287*(tempC+273.15));
    }
    
    static double getPressure(double h) {
        return Math.pow(1-((0.0065*h)/(tempC+273.15)), 5.167676581);
    }
    
    static double lerp(double a, double b, double factor) {
        double c = b-a;
        c = factor * c;
        return a + c;
    }
}
