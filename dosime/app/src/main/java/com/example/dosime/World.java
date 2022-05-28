package com.example.dosime;

import android.util.Log;

/**
 * Created by bodekjan on 2016/8/8.
 */
public class World {
    public static float dbCount = 0.0f;
    public static float minDB =100.0f;
    public static float maxDB =0.0f;
    public static float lastDbCount = dbCount;

    public static float pitchCount = 0.0f;
    public static float pitchProb = 0.0f;
    public static float minPitch = 100.0f;
    public static float maxPitch =0.0f;
    public static float lastPitchCount = pitchCount;

    private static float min = 0.5f;  //Set the minimum sound change
    private static float minP = 10.0f;  //Set the minimum sound change
    private static float value = 0.0f;   // Sound decibel value
    private static float Pvalue = 0.0f;   // Sound Hz value
    public static void setDbCount(float dbValue) {
        if(dbValue<=60.0f){
            lastDbCount=dbCount=0;
            return;
        }
        if (dbValue > lastDbCount) {
            value = dbValue - lastDbCount > min ? dbValue - lastDbCount : min;
        }else{
            value = dbValue - lastDbCount < -min ? dbValue - lastDbCount : -min;
        }
        dbCount = lastDbCount + value * 0.2f ; //To prevent the sound from changing too fast
        if(Float.isNaN(dbCount))
            dbCount=0;
        lastDbCount = dbCount;
        if(dbCount<minDB && dbCount>=0.0f) minDB=dbCount;
        if(dbCount>maxDB) maxDB=dbCount;
    }

//    public static void setPitchCount(float pValue) {
////        if (pValue > lastPitchCount) {
////            Pvalue = pValue - lastPitchCount > minP ? pValue - lastPitchCount : minP;
////        }else{
////            Pvalue = pValue - lastPitchCount < -minP ? pValue - lastPitchCount : -minP;
////        }
////        pitchCount = lastPitchCount + Pvalue * 0.2f ; //To prevent the sound from changing too fast
//        if(Float.isNaN(pitchCount))
//            pitchCount=0.0f;
////        lastPitchCount = pitchCount;
//        pitchCount = pValue;
//        if(pitchCount<minPitch && pitchCount>=0.0f) minPitch=pitchCount;
//        if(pitchCount>maxPitch) maxPitch=pitchCount;
//    }

    public static void setPitchCount(float pValue) {
        if (pValue > lastPitchCount) {
            Pvalue = pValue - lastPitchCount > minP ? pValue - lastPitchCount : minP;
        }else{
            Pvalue = pValue - lastPitchCount < -minP ? pValue - lastPitchCount : -minP;
        }
        pitchCount = lastPitchCount + Pvalue * 0.2f ; //To prevent the sound from changing too fast
        if(Float.isNaN(pitchCount))
            pitchCount=0.0f;
//        lastPitchCount = pitchCount;
        if(pitchCount<minPitch && pitchCount>=0.0f) minPitch=pitchCount;
        if(pitchCount>maxPitch) maxPitch=pitchCount;
    }
    public static void setPitchProb(float pValue) {
        pitchProb = pValue;
    }

}