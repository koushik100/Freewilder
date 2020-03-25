package com.koushik.myrestapi

import android.os.AsyncTask
import com.google.gson.Gson
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import java.io.IOException
import java.util.concurrent.TimeUnit


class MAPCALL(
    val url: String,
    val method: String,
    val params: HashMap<String, Any>,
    val header: HashMap<String, Any>?,
    val listner: Responselistener
) : AsyncTask<Void, String, ResponseData?>() {
    override fun onPostExecute(result: ResponseData?) {
        super.onPostExecute(result)
        Klog.d("## RES-", "$url \n CODE- ${result!!.code} REQ- ${Gson().toJson(params)} \n $result")
        if (result == null) {
            listner.OnError(500, "Something Went Wrong please try again")
        } else {

            try {
                val respCode = result.code
                when (respCode) {
                    200 -> listner.OnSucess(result.response)
                    201 -> listner.OnSucess(result.response)
                    422 -> listner.OnError(422, result.response)
                    401 -> listner.OnError(401, "Authentication failed.")
                    503 -> listner.OnError(
                        503,
                        "The request was not completed. The server is temporarily overloading or down."
                    )
                    403 -> listner.OnError(403, "Access is forbidden to the requested page.")
                    404 -> listner.OnError(404, "The server can not find the requested page.")
                    else -> listner.OnError(respCode, "Unknown error please try again later.")
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                listner.OnError(500, "Something Went Wrong please try again later.")
            }

        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        listner.Start()
    }

    override fun doInBackground(vararg p0: Void?): ResponseData? {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val MEDIA_TYPE = "application/json".toMediaTypeOrNull()
//        final MediaType MEDIA_TYPE_PNG = MediaType.parse("*/*");

        val bodybuilder = MultipartBody.Builder()
        bodybuilder.setType(MultipartBody.FORM)

        val body = Gson().toJson(params).toRequestBody(MEDIA_TYPE)
        val builder = Headers.Builder()
        if (header != null)
            for (entry in header.entries) {
                println(entry.key + "/" + entry.value)
                builder.add(entry.key, entry.value.toString())
            }
        val h = builder.build()
        val request = Request.Builder()
            .url(url)
            .method(method, body)
//            .post(body)
            .addHeader("content-type", "application/json")
            .headers(h)
            .build()

        try {
            val response = client.newCall(request).execute()
            return ResponseData(response.code, response.body!!.string())
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }
}


