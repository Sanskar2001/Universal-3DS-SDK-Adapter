package io.hyperswitch.threedslibrary.data

object Constants {
    fun getRetriveURL(paymentId:String,clientSecret:String):String
    {
        return "https://app.hyperswitch.io/api/payments/${paymentId}?client_secret=${clientSecret}&force_sync=true"
    }

    fun getAuthenticateURL(paymentId: String):String{
        return  "https://auth.app.hyperswitch.io/api/authenticate/${paymentId}/areq"
    }

}