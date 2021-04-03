package uz.muhammadyusuf.kurbonov.myclinic.utils


import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.Action
import uz.muhammadyusuf.kurbonov.myclinic.works.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.works.DataHolder
import uz.muhammadyusuf.kurbonov.myclinic.works.StartRecognizeWork
import java.io.IOException
import java.net.InetSocketAddress
import javax.net.SocketFactory


object DoesNetworkHaveInternet {

    // Make sure to execute this on a background thread.
    fun execute(socketFactory: SocketFactory): Boolean {
        return try {
            Timber.d("PINGING google.")
            val socket = socketFactory.createSocket() ?: throw IOException("Socket is null.")
            socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            socket.close()
            Timber.d("PING success.")
            true
        } catch (e: IOException) {
            Timber.e("No internet connection. $e")
            false
        }
    }
}

/**
 * Save all available networks with an internet connection to a set (@validNetworks).
 * As long as the size of the set > 0, this LiveData emits true.
 * MinSdk = 21.
 *
 * Inspired by:
 * https://github.com/AlexSheva-mason/Rick-Morty-Database/blob/master/app/src/main/java/com/shevaalex/android/rickmortydatabase/utils/networking/ConnectionLiveData.kt
 */
private lateinit var networkCallback: ConnectivityManager.NetworkCallback
private lateinit var cm: ConnectivityManager
private var connected = true
val validNetworks: MutableSet<Network> = HashSet()


fun startNetworkMonitoring(context: Context) {

    fun verify() {
        if (validNetworks.size > 0) {
            if (connected) return

            connected = true
            val starterWork = OneTimeWorkRequestBuilder<StartRecognizeWork>()

            starterWork.setInputData(Data.Builder().apply {
                putString(StartRecognizeWork.INPUT_PHONE, DataHolder.phoneNumber)
                putString(
                    StartRecognizeWork.INPUT_TYPE,
                    if (DataHolder.type == CallDirection.OUTGOING) "outgoing" else "incoming"
                )
            }.build())

        } else {
            App.appViewModel.reduceBlocking(Action.SetNoConnectionState)
            connected = false
        }
    }

    cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    networkCallback = object : ConnectivityManager.NetworkCallback() {

        /*
          Called when a network is detected. If that network has internet, save it in the Set.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onAvailable(android.net.Network)
         */
        override fun onAvailable(network: Network) {
            Timber.d("onAvailable: $network")
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            Timber.d("onAvailable: ${network}, $hasInternetCapability")
            if (hasInternetCapability == true) {
                // check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if (hasInternet) {
                        withContext(Dispatchers.Main) {
                            Timber.d("onAvailable: adding network. $network")
                            validNetworks.add(network)
                            verify()
                        }
                    }
                }
            }
        }

        /*
          If the callback was registered with registerNetworkCallback() it will be called for each network which no longer satisfies the criteria of the callback.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onLost(android.net.Network)
         */
        override fun onLost(network: Network) {
            Timber.d("onLost: $network")
            validNetworks.remove(network)
            verify()
        }
    }
    val networkRequest = NetworkRequest.Builder()
        .addCapability(NET_CAPABILITY_INTERNET)
        .build()
    cm.registerNetworkCallback(networkRequest, networkCallback)
}

fun stopMonitoring() {
    try {
        cm.unregisterNetworkCallback(networkCallback)
    } catch (e: IllegalArgumentException) {

    }
}
