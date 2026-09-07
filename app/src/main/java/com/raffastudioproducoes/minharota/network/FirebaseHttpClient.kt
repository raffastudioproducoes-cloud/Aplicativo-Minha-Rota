package com.raffastudioproducoes.minharota.network

import android.content.Context
import android.util.Log
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object FirebaseHttpClient {

    private val TAG = "FirebaseHttpClient"

    /**
     * Cria OkHttpClient com certificate pinning para Firebase
     * Pinning contra certificados de produção do Firebase
     */
    fun createFirebaseClient(context: Context): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder()
            // Firebase Firestore endpoints
            .add("firestore.googleapis.com", "sha256/WoiWRyIOVytfq+UpZYk+59QbUFgoB2SLhFdHKwmqoCc=")  // Google Internet Authority G3
            .add("firestore.googleapis.com", "sha256/RRM1dGqnDFEcF6PN5OUEwWTjH3xQnxQitsPQIsggPgM=")  // GlobalSign Root R2
            .add("firestore.googleapis.com", "sha256/2a/Q5g6v5fEzTsWKqG2/kKlhHKZzMEVaJKQ4vB8jEh6A=")  // Issuing CA Intermediate R3

            // Firebase Auth endpoints
            .add("identitytoolkit.googleapis.com", "sha256/WoiWRyIOVytfq+UpZYk+59QbUFgoB2SLhFdHKwmqoCc=")
            .add("identitytoolkit.googleapis.com", "sha256/RRM1dGqnDFEcF6PN5OUEwWTjH3xQnxQitsPQIsggPgM=")

            // Firebase App Check endpoints
            .add("firebaseappcheck.googleapis.com", "sha256/WoiWRyIOVytfq+UpZYk+59QbUFgoB2SLhFdHKwmqoCc=")
            .add("firebaseappcheck.googleapis.com", "sha256/RRM1dGqnDFEcF6PN5OUEwWTjH3xQnxQitsPQIsggPgM=")

            .build()

        return OkHttpClient.Builder()
            // Certificate pinning
            .certificatePinner(certificatePinner)

            // Timeouts
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

            // Logging
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d(TAG, "Request: ${request.url}")
                try {
                    val response = chain.proceed(request)
                    Log.d(TAG, "Response: ${response.code} from ${request.url}")
                    response
                } catch (e: Exception) {
                    Log.e(TAG, "Network error: ${e.message}", e)
                    throw e
                }
            }

            .build()
    }

    /**
     * Certificados para pinning (SHA-256 hashes)
     * Atualizar periodicamente conforme Firebase renova certificados
     *
     * Para extrair hash de um certificado:
     * openssl s_client -connect firestore.googleapis.com:443 < /dev/null | \
     *   openssl x509 -outform DER | \
     *   openssl dgst -sha256 -binary | \
     *   openssl enc -base64
     */
    object CertificatePins {
        // Google Internet Authority G3 (atual)
        const val GOOGLE_IA_G3 = "sha256/WoiWRyIOVytfq+UpZYk+59QbUFgoB2SLhFdHKwmqoCc="

        // GlobalSign Root R2 (backup)
        const val GLOBALSIGN_ROOT_R2 = "sha256/RRM1dGqnDFEcF6PN5OUEwWTjH3xQnxQitsPQIsggPgM="

        // Issuing CA Intermediate R3
        const val ISSUING_CA_R3 = "sha256/2a/Q5g6v5fEzTsWKqG2/kKlhHKZzMEVaJKQ4vB8jEh6A="
    }
}
