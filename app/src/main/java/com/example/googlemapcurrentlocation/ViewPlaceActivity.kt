package com.example.googlemapcurrentlocation

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.googlemapcurrentlocation.databinding.ActivityViewPlaceBinding
import com.example.googlemapcurrentlocation.model.ViewPlaceList
import com.example.googlemapcurrentlocation.network.DrugStoreService
import com.example.googlemapcurrentlocation.utils.Common
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPlaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewPlaceBinding
    internal lateinit var mService: DrugStoreService
    var mPlace: ViewPlaceList? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mService = Common.googleApiService

        //Load photo of place
        if (Common.currentResult!!.photos != null && Common.currentResult!!.photos.size > 0) {
            Glide.with(this)
                .load(getPhotoOfPlace(Common.currentResult!!.photos[0].photo_reference, 1000))
                .into(binding.imgViewPlace)
        }

        //load rating
        if (Common.currentResult!!.rating != null)
            binding.ratingBar.rating = Common.currentResult!!.rating.toFloat()
        else
            binding.ratingBar.visibility = View.GONE

        // Use Service to fetch Address and Name
        mService.getDetailPlace(getPlaceDetailUrl(Common.currentResult!!.place_id!!))
            .enqueue(object : Callback<ViewPlaceList> {
                override fun onResponse(
                    call: Call<ViewPlaceList>,
                    response: Response<ViewPlaceList>
                ) {
                    mPlace = response.body()
                    binding.tvAddress.text = mPlace!!.viewPlace.formatted_address
                    binding.tvName.text = mPlace!!.viewPlace.name
                    binding.tvPhone.text = mPlace!!.viewPlace.formatted_phone_number
                }

                override fun onFailure(call: Call<ViewPlaceList>, t: Throwable) {
                    Toast.makeText(baseContext, "" + t.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun getPlaceDetailUrl(place_id: String): String {
        val placeDetailUrl =
            StringBuilder("https://maps.googleapis.com/maps/api/place/details/json")
        placeDetailUrl.append("?place_id=$place_id")
        placeDetailUrl.append("&key=AIzaSyDdUtQJNhArwSsRvXsiMyl5NIJ52JQ1h7Q")
        Log.d("placeDetailUrl_DEBUG", placeDetailUrl.toString())
        return placeDetailUrl.toString()
    }

    private fun getPhotoOfPlace(photo_referece: String, maxWidth: Int): String {
        val photoOfUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        photoOfUrl.append("?maxwidth=$maxWidth")
        photoOfUrl.append("&photoreference=$photo_referece")
        photoOfUrl.append("&key=AIzaSyDdUtQJNhArwSsRvXsiMyl5NIJ52JQ1h7Q")
        Log.d("photoOfUrl_DEBUG", photoOfUrl.toString())
        return photoOfUrl.toString()
    }

}