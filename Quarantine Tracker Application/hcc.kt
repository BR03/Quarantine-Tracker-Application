package com.learn.quarantinetrack

class hcc(var fullname:String, var patientID:Int, var aadharID:Int, var address:String, var lattitude:Double, var longitude:Double){
    init {
        this.fullname = fullname
        this.patientID = patientID
        this.aadharID = aadharID
        this.address = address
        this.lattitude = lattitude
        this.longitude = longitude
    }
}