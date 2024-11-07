package services.device

import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import android.util.Log

fun toggleSIM(contentResolver: ContentResolver) {
    val currentSIM = Settings.Global.getInt(contentResolver, "multi_sim_data_call", 1)

    val newSIM = if (currentSIM == 1) 2 else 1

    Settings.Global.putInt(contentResolver, "multi_sim_data_call", newSIM)
    Settings.Global.putInt(contentResolver, "multi_sim_data_sub", newSIM)
    Log.d("DeviceManager", "Chip ativo alternado para: SIM $newSIM")
}

fun toggleMobileData(context: Context, enable: Boolean) {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (enable) {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                connectivityManager.bindProcessToNetwork(network)
                Log.d("DeviceManager", "Dados móveis ativados")
            }
        })
    } else {
        connectivityManager.bindProcessToNetwork(null)
        Log.d("DeviceManager", "Dados móveis desativados")
    }
}