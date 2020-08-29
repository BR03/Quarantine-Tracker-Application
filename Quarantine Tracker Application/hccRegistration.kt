package com.learn.quarantinetrack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.hcc_registration.*

class hccRegistration:AppCompatActivity(){

    val CAMERA_REQUEST_CODE = 0
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10
    lateinit var btnRegister: Button
    lateinit var key:String
    var lattitude:Double = 0.0
    var longitude:Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hcc_registration)

        mLocationRequest = LocationRequest()

        btnRegister = findViewById(R.id.button2)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        btnRegister.setOnClickListener {
            var database = FirebaseDatabase.getInstance()
            var ref = database.getReference("HCC")

            var fullname = editText.text.toString()
            var patientID = Integer.parseInt(editText3.text.toString())
            var aadharID = Integer.parseInt(editText4.text.toString())
            var address = editText2.text.toString()

            if (checkPermissionForLocation(this)) {
                startLocationUpdates()
            }
            Handler().postDelayed({
                stoplocationUpdates()
                var HCC = hcc(fullname, patientID, aadharID, address, lattitude, longitude)
                key = ref.push().key.toString()
                ref.child(key).setValue(HCC).addOnCompleteListener {
                    Toast.makeText(this, "HCC Registered", Toast.LENGTH_SHORT).show()
                }

                val intent = Intent(this, homepage::class.java)
                intent.putExtra("keyID", key)
                startActivity(intent)
            }, 1000)
        }
    }


    private fun getExternsion(uri: Uri): String? {
        val cr = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri))
    }

    private fun fileUploader(ref: StorageReference, imguri: Uri?) {
        val myref = ref.child(System.currentTimeMillis().toString() + "." + getExternsion(imguri!!))
        myref.putFile(imguri)
            .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot?>() {
                fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
                    // Get a URL to the uploaded content
                    //val downloadUrl: Uri = taskSnapshot.getDownloadUrl()
                    Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show()
                }
            })
            .addOnFailureListener(OnFailureListener() {
                fun onFailure(@NonNull exception: Exception?) {
                    // Handle unsuccessful uploads
                    // ...
                }
            })
    }

    private fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , 11)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()


    }


    protected fun startLocationUpdates() {

        // Create the location request to start receiving updates

        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.setInterval(INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // do work here
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    fun onLocationChanged(location: Location) {
        // New location has now been determined
        //val ref = FirebaseDatabase.getInstance().getReference("HCC")

        mLastLocation = location
        lattitude = mLastLocation.latitude
        longitude = mLastLocation.longitude
        //val locObj = loc(mLastLocation.latitude,mLastLocation.longitude)
        //ref.child(key).setValue(locObj).addOnCompleteListener {
        //    Toast.makeText(this,"Location Entered",Toast.LENGTH_SHORT).show()
        //}
    }

    private fun stoplocationUpdates() {
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
                btnRegister.isEnabled = false
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                // Show the permission request
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
                false
            }
        } else {
            true
        }
    }
}