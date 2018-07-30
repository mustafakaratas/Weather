package com.example.mustafa_karatas.a18_07_03_havadurumu

import android.annotation.SuppressLint
import android.app.Application
import android.app.VoiceInteractor
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ArrayAdapter.createFromResource
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.security.Permission
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    var location : SimpleLocation? = null
    var latitude : String? = null
    var longitude : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var spinnerAdapter = createFromResource(this,R.array.sehirler, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSehirler.setAdapter(spinnerAdapter)

        spSehirler.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if(position ==0){

                    location = SimpleLocation(this@MainActivity)
                    if(!location!!.hasLocationEnabled()){
                        Toast.makeText(this@MainActivity,"GPS HAS DİSABLED",Toast.LENGTH_LONG).show()
                        object : CountDownTimer(2*1000,1000){
                            override fun onFinish() {
                                SimpleLocation.openSettings(this@MainActivity)
                            }
                            override fun onTick(millisUntilFinished: Long) {}
                        }.start()

                    }else{

                        if ( ContextCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this@MainActivity,"LOCATION PERMISSION HAS DISABLED",Toast.LENGTH_LONG).show()
                            object : CountDownTimer(2*1000,1000){
                                override fun onFinish() {
                                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),10)
                                }
                                override fun onTick(millisUntilFinished: Long) {}
                            }.start()

                        }else{

                            location = SimpleLocation(this@MainActivity)
                            latitude = String.format("%.2f",location?.latitude)
                            longitude = String.format("%.2f",location?.longitude)

                            location?.setListener(object : SimpleLocation.Listener{
                                override fun onPositionChanged() {

                                    latitude = String.format("%.2f",location?.latitude)
                                    longitude = String.format("%.2f",location?.longitude)
                                    sehirBul(latitude, longitude)
                                }
                            })
                            sehirBul(latitude, longitude)
                        }
                    }
                }else{
                    var secilenSehir = parent?.getItemAtPosition(position).toString()
                    requestOlustur(secilenSehir)
                }
            }
        }
        spSehirler.setSelection(1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if( requestCode == 10 ){

            if(grantResults.size > 0 && grantResults[0] ==  0){

                location = SimpleLocation(this@MainActivity)
                latitude = String.format("%.2f",location?.latitude)
                longitude = String.format("%.2f",location?.longitude)

                location?.setListener(object : SimpleLocation.Listener{
                    override fun onPositionChanged() {

                        latitude = String.format("%.2f",location?.latitude)
                        longitude = String.format("%.2f",location?.longitude)
                        sehirBul(latitude, longitude)
                    }
                })
                sehirBul(latitude, longitude)

            }
        }
    }

    private fun sehirBul(latitude: String?, longitude: String?){

        var sehir : String? = null
        var url = "https://api.openweathermap.org/data/2.5/weather?&appid=afb13c1b59de90956ef11bc13cbb0acf&lang=tr&units=metric&lat="+latitude+"&lon="+longitude
        var request2 = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener {

                sehir = it?.getString("name")
                requestOlustur(sehir.toString())

            }, Response.ErrorListener {})

        MySingleton.getInstance(this).addToRequestQueue(request2)
    }

    private fun requestOlustur(sehir : String) {

        var url = "http://api.openweathermap.org/data/2.5/weather?q="+sehir+"&appid=afb13c1b59de90956ef11bc13cbb0acf&lang=tr&units=metric"

        val request = JsonObjectRequest(Request.Method.GET,url,null,
            Response.Listener {

                var sehirAdi = it?.getString("name")
                tvSehir.text = sehirAdi

                var sicaklik = it?.getJSONObject("main")?.getInt("temp")
                tvSicaklik.text = sicaklik.toString()

                var aciklama = it?.getJSONArray("weather")?.getJSONObject(0)?.getString("description")
                tvAciklama.text = aciklama?.toUpperCase()

                var icon = it?.getJSONArray("weather")?.getJSONObject(0)?.getString("icon")
                if(icon?.last() == 'd'){
                    clHavaDurumu.setBackgroundResource(R.drawable.bg_gunduz)
                }else{
                    clHavaDurumu.setBackgroundResource(R.drawable.bg_gece)
                }
                var iconAdi = resources.getIdentifier(icon.BasinaIcon_Koy(),"drawable",packageName)
                ivIcon.setImageResource(iconAdi)

                tvTarih.text = tarihYazdir()
                tvSaat.text = saatYazdir()

            },
            Response.ErrorListener {
                Toast.makeText(this,"RESPONSE ERROR : "+it.toString(),Toast.LENGTH_SHORT).show()
        })

        MySingleton.getInstance(this).addToRequestQueue(request)
    }

    fun tarihYazdir() : String{

        var zaman = Calendar.getInstance().time
        var formatlayici = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
        var tarih = formatlayici.format(zaman)

        return tarih
    }

    fun saatYazdir() : String{

        var zaman = Calendar.getInstance().time
        var dateFormat = SimpleDateFormat("HH:mm", Locale("tr"))
        var saat = dateFormat.format(zaman)

        return saat
    }
}

private fun String?.BasinaIcon_Koy(): String {

    return "icon_"+this
}
