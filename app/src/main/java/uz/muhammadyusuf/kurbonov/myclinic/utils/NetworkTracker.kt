package uz.muhammadyusuf.kurbonov.myclinic.utils


import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.io.IOException
import java.net.InetSocketAddress
import javax.net.SocketFactory

@ExperimentalCoroutinesApi
class NetworkTracker(context: Context) {
    private val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks = mutableListOf<Network>()


    val connectedToInternet = callbackFlow {

        this.trySend(true).isSuccess

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (ping()) {
                    validNetworks.add(network)
                }
                commit()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                validNetworks.remove(network)
                commit()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                validNetworks.clear()
                commit()
            }

            fun commit() {
                Timber.d("Network state is ${validNetworks.isNotEmpty()}")
                trySend(validNetworks.isNotEmpty())
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        manager.registerNetworkCallback(networkRequest, networkCallback)

        awaitClose {
            manager.unregisterNetworkCallback(networkCallback)
        }
    }

    private fun ping(): Boolean {
        return try {
            Timber.d("PINGING google.")

            val socket =
                SocketFactory.getDefault().createSocket() ?: throw IOException("Socket is null.")
            socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            socket.close()

            Timber.d("PING success.")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            Timber.d("No internet connection. $e")
            false
        }
    }
}

