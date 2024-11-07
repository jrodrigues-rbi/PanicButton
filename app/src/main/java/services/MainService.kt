package services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.uebrasil.panicbuttonapp.MainActivity
import com.uebrasil.panicbuttonapp.services.location.LocationDppService
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class MainService : Service() {

    private lateinit var webSocket: WebSocket
    private lateinit var locationDppService: LocationDppService
    private var isSocketConnected = false
    private val client = OkHttpClient()
    private val locationQueue =
        mutableListOf<JSONObject>() // Fila para armazenar localizações não enviadas
    private lateinit var locationRequest: LocationRequest
    private val retryInterval = 15000L // Intervalo de reconexão
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        const val CHANNEL_ID = "LocationUpdatesChannel"
        const val LOCATION_ACCURACY_THRESHOLD = 50.0f // Acurácia de 50 metros
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        startWebSocketConnection()
        configureLocationRequest()

        locationDppService = LocationDppService(this)
        locationDppService.startLocationUpdates(locationRequest)
    }

    private fun getDeviceIdentifier(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun configureLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()
    }

    private fun startWebSocketConnection() {
        val request = Request.Builder()
            .url("ws://dpp-manager.uebr.com.br")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                isSocketConnected = true
                Log.d("MainService", "WebSocket conectado com sucesso")
                sendStoredLocations()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("MainService", "Mensagem recebida: $text")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                Log.d("MainService", "Fechando: $code $reason")
                isSocketConnected = false
                attemptReconnect()
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                isSocketConnected = false
                Log.e("MainService", "Erro ao conectar ao WebSocket: ${t.message}")
                attemptReconnect()
            }
        })
    }

    private fun attemptReconnect() {
        Log.d("MainService", "Tentando reconectar...")
        handler.postDelayed({
            startWebSocketConnection()
        }, retryInterval)
    }

    private fun sendStoredLocations() {
        if (isSocketConnected) {
            locationQueue.forEach { locationData ->
                webSocket.send(locationData.toString())
                Log.d("MainService", "Enviando localização passada!")
            }
            locationQueue.clear() // Limpa a fila após enviar todos os dados
        }
    }

    fun onLocationUpdate(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val accuracy = location.accuracy
        val speed = location.speed * 3.6
        val imei = getDeviceIdentifier();

        if (accuracy > LOCATION_ACCURACY_THRESHOLD) {
            Log.d(
                "MainService",
                "Acurácia insuficiente, alterando para PRIORITY_BALANCED_POWER_ACCURACY."
            )
            locationRequest.priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        } else {
            locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val payload = JSONObject()
        payload.put("latitude", latitude)
        payload.put("longitude", longitude)
        payload.put("speed", speed)
        payload.put("imei", imei ?: "unknown")

        if (isSocketConnected) {
            webSocket.send(payload.toString())
            Log.d(
                "MainService",
                "Localização enviada: $latitude, $longitude, Acurácia: $accuracy, Velocidade: $speed km/h"
            )
        } else {
            Log.e("MainService", "WebSocket não está conectado. Armazenando a localização.")
            locationQueue.add(payload)
            attemptReconnect()
        }
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Serviço de Atualização de Localização")
            .setContentText("O serviço de localização está em execução.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Atualizações de Localização",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket.close(1000, null)
        client.dispatcher.executorService.shutdown()
        Log.d("MainService", "WebSocket desconectado e executor encerrado")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
