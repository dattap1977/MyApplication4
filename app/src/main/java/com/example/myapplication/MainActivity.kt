package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cashfree.pg.api.CFPaymentGatewayService
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.cashfree.pg.core.api.CFSession
import com.cashfree.pg.core.api.CFTheme
import com.cashfree.pg.core.api.exception.CFException
import com.cashfree.pg.ui.api.CFDropCheckoutPayment

import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var amt: Double = 0.0
    private var psessionId : String = ""
    private var iOrderId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //AndroidNetworking.initialize(getApplicationContext());

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        var button = findViewById<Button>(R.id.button2)

        var textView = findViewById<TextView>(R.id.textView2)

        var amount = findViewById<TextView>(R.id.txtAmount)

        var currency = findViewById<EditText>(R.id.editTextTextPersonName)

        var makePaymentBtn = findViewById<Button>(R.id.button)

        var settlementBtn = findViewById<Button>(R.id.button3)

        var refundBtn = findViewById<Button>(R.id.button4)

        button.setOnClickListener {
            //textView.text = "Hello World"
            addData(amount.text.toString(), currency.text.toString())

        }

        makePaymentBtn.setOnClickListener {
            addOrderPayData(amt,psessionId)
        }

        settlementBtn.setOnClickListener {
            addSettlementData()
        }

        refundBtn.setOnClickListener {
            addRefundData()
        }
        }

    private fun addData(amount: String, currency: String) {
        val queue = Volley.newRequestQueue(this@MainActivity)

        // making a string request to update our data and
        // passing method as PUT. to update our data.
        val request: StringRequest =
            object : StringRequest(Request.Method.POST, "http://192.168.43.198:8080/api/v1/orders", object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    // hiding our progress bar.
                    //loadingPB.visibility = View.GONE

                    // inside on response method we are
                    // setting our edit text to empty.
                    // jobEdt.setText("")
                    // nameEdt.setText("")

                    // on below line we are displaying a toast message as data updated.
                    Toast.makeText(this@MainActivity, "Data Updated..", Toast.LENGTH_SHORT).show()
                    try {
                        // on below line we are extracting data from our json object
                        // and passing our response to our json object.
                        val jsonObject = JSONObject(response)

                        psessionId = jsonObject.getString("id")
                        amt = jsonObject.getString("amount").toDouble()
                        val orderNote =  jsonObject.getJSONObject("notes")
                        val orderId : MutableIterator<String?>
                        orderId = orderNote.keys();


                        for(ordId in orderId){
                           iOrderId = ordId.toString()
                        }
                        // creating a string for our output.
                        val result =
                            "Payment session id: " + jsonObject.getString("id") + "\n" + "Amount : " + jsonObject.getString(
                                "amount"
                            ) + "\n"

                        // on below line we are setting
                        // our string to our text view.
                        var textView = findViewById<TextView>(R.id.textView2)

                        textView.setText(result)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    // displaying toast message on response failure.
                    Log.e("tag", "error is " + error!!.message)
                    Toast.makeText(this@MainActivity, "Fail to update data..", Toast.LENGTH_SHORT)
                        .show()
                    var textView = findViewById<TextView>(R.id.textView2)
                    textView.setText("Failed to update data ..")
                }
            }) {
                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray? {
                    val params2 = HashMap<Any?, Any?>()
                    params2["order_amount"] = amount.toDouble()
                    params2["order_currency"] = currency
                    return JSONObject(params2).toString().toByteArray()
                }

                override fun getBodyContentType(): String? {
                    return "application/json"
                }
            }
        // below line is to make
        // a json object request.
        queue.add(request)

    }


    private fun addOrderPayData(amt: Double, sessionId: String) {
        try {
            val cfSession: CFSession = CFSession.CFSessionBuilder()
                .setEnvironment(CFSession.Environment.SANDBOX)
                .setPaymentSessionID(sessionId)
                .setOrderId(iOrderId)
                .build()
//            val cfPaymentComponent = CFPaymentComponentBuilder()
//                .add(CFPaymentComponent.CFPaymentModes.CARD)
//                .add(CFPaymentComponent.CFPaymentModes.UPI)
//                .build()
            val cfTheme = CFTheme.CFThemeBuilder()
                .setNavigationBarBackgroundColor("#006EE1")
                .setNavigationBarTextColor("#ffffff")
                .setButtonBackgroundColor("#006EE1")
                .setButtonTextColor("#ffffff")
                .setPrimaryTextColor("#000000")
                .setSecondaryTextColor("#000000")
                .build()
            val cfDropCheckoutPayment = CFDropCheckoutPayment.CFDropCheckoutPaymentBuilder()
                .setSession(cfSession) //By default all modes are enabled. If you want to restrict the payment modes uncomment the next line
                //                        .setCFUIPaymentModes(cfPaymentComponent)
                .setCFNativeCheckoutUITheme(cfTheme)
                .build()
            val gatewayService = CFPaymentGatewayService.getInstance()
            gatewayService.doPayment(this@MainActivity, cfDropCheckoutPayment)
        } catch (exception: CFException) {
            exception.printStackTrace()
        }


    }

    private fun addSettlementData() {
        val queue = Volley.newRequestQueue(this@MainActivity)

        // making a string request to update our data and
        // passing method as PUT. to update our data.
        val request: StringRequest =
            object : StringRequest(Request.Method.POST, "http://192.168.43.198:8080/api/v1/orders/" + iOrderId + "/split", object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    // hiding our progress bar.
                    //loadingPB.visibility = View.GONE

                    // inside on response method we are
                    // setting our edit text to empty.
                    // jobEdt.setText("")
                    // nameEdt.setText("")

                    // on below line we are displaying a toast message as data updated.
                    Toast.makeText(this@MainActivity, "Data Updated..", Toast.LENGTH_SHORT).show()
                    try {
                        // on below line we are extracting data from our json object
                        // and passing our response to our json object.
                        val jsonObject = JSONObject(response)

//                        psessionId = jsonObject.getString("id")
//                        amt = jsonObject.getString("amount").toDouble()
//                        val orderNote =  jsonObject.getJSONObject("notes")
//                        val orderId : MutableIterator<String?>
//                        orderId = orderNote.keys();
//
//
//                        for(ordId in orderId){
//                            iOrderId = ordId.toString()
//                        }
                        // creating a string for our output.
//                        val result =
//                            "Payment session id: " + jsonObject.getString("id") + "\n" + "Amount : " + jsonObject.getString(
//                                "amount"
//                            ) + "\n"

                        // on below line we are setting
                        // our string to our text view.
                        var textView = findViewById<TextView>(R.id.textView2)

                        textView.setText("Settled")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    // displaying toast message on response failure.
                    Log.e("tag", "error is " + error!!.message)
                    Toast.makeText(this@MainActivity, "Fail to update data..", Toast.LENGTH_SHORT)
                        .show()
                    var textView = findViewById<TextView>(R.id.textView2)
                    textView.setText("Failed to update data ..")
                }
            }) {
                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray? {
                    var params2 = HashMap<Any?, Any?>()
                    var split = HashMap<Any?, Any?>()
                    split["vendorId"] = "Vendor_1"
                    split["percentage"] = 100
                    params2["VendorShares"] = split
                    params2["splitType"] = "ORDER_AMOUNT"
                    return JSONObject(params2).toString().toByteArray()
                }

                override fun getBodyContentType(): String? {
                    return "application/json"
                }
            }
        // below line is to make
        // a json object request.
        queue.add(request)

    }

    private fun addRefundData() {
        val queue = Volley.newRequestQueue(this@MainActivity)

        // making a string request to update our data and
        // passing method as PUT. to update our data.
        val request: StringRequest =
            object : StringRequest(Request.Method.POST, "http://192.168.43.198:8080/api/v1/orders/"+ iOrderId + "/refunds", object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    // hiding our progress bar.
                    //loadingPB.visibility = View.GONE

                    // inside on response method we are
                    // setting our edit text to empty.
                    // jobEdt.setText("")
                    // nameEdt.setText("")

                    // on below line we are displaying a toast message as data updated.
                    Toast.makeText(this@MainActivity, "Data Updated..", Toast.LENGTH_SHORT).show()
                    try {
                        // on below line we are extracting data from our json object
                        // and passing our response to our json object.
                        val jsonObject = JSONObject(response)

//                        psessionId = jsonObject.getString("id")
//                        amt = jsonObject.getString("amount").toDouble()
//                        val orderNote =  jsonObject.getJSONObject("notes")
//                        val orderId : MutableIterator<String?>
//                        orderId = orderNote.keys();
//
//
//                        for(ordId in orderId){
//                            iOrderId = ordId.toString()
//                        }
                        // creating a string for our output.
//                        val result =
//                            "Payment session id: " + jsonObject.getString("id") + "\n" + "Amount : " + jsonObject.getString(
//                                "amount"
//                            ) + "\n"

                        // on below line we are setting
                        // our string to our text view.
                        var textView = findViewById<TextView>(R.id.textView2)

                        textView.setText("Money Refunded")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    // displaying toast message on response failure.
                    Log.e("tag", "error is " + error!!.message)
                    Toast.makeText(this@MainActivity, "Fail to update data..", Toast.LENGTH_SHORT)
                        .show()
                    var textView = findViewById<TextView>(R.id.textView2)
                    textView.setText("Failed to update data ..")
                }
            }) {
                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray? {
                    var params2 = HashMap<Any?, Any?>()
                    params2["refund_amount"] = amt
                    params2["refund_id"] = "refund10000"
                    params2["refund_note"] = "Money refunded"
                    return JSONObject(params2).toString().toByteArray()
                }

                override fun getBodyContentType(): String? {
                    return "application/json"
                }
            }
        // below line is to make
        // a json object request.
        queue.add(request)

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}