package com.test.http.httptest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URL
import java.security.cert.CertificateException
import javax.net.ssl.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var handler = Handler() {

            when (it.what) {
                0 -> {
                    tv.text = it.obj as String
                }

                -1 -> {
                    tv.text = it.obj as String
                }
            }
            true
        }
        Thread {
            kotlin.run {
                var msg = Message.obtain()
                msg.what = -1
                msg.obj = "正在加载..."
                handler.sendMessage(msg)

                var httpURLConnection = URL("https://shshopping.online.sh.cn/culture/req.do").openConnection() as HttpsURLConnection

                //todo
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, null)
                httpURLConnection.sslSocketFactory = sslContext.socketFactory
                httpURLConnection.hostnameVerifier = createHostCerifier()
                httpURLConnection.connectTimeout = 5000
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.readTimeout = 5000

                var o = httpURLConnection.outputStream

                o.write(("{\n" +
                        "\t\"platform\":1,\n" +
                        "\t\"ts\":${System.currentTimeMillis()}\n" +
                        "}").toByteArray())
                o.flush()
                o.close()
                var i = httpURLConnection.inputStream
                var reader = BufferedReader(InputStreamReader(i))
                var result = StringBuilder()
                var line: String? = ""
                try {
                    do {
                        result.append(line)
                        line = reader.readLine()
                    } while (line != null)

                    var msg = Message.obtain()
                    msg.what = 0
                    msg.obj = result.toString()
                    handler.sendMessage(msg)
                } catch (ex: Exception) {
                }
            }
        }.start()
    }


    fun createHostCerifier(): HostnameVerifier {

        return HostnameVerifier { _, session ->
            val hv = HttpsURLConnection.getDefaultHostnameVerifier()
            hv.verify("shshopping.online.sh.cn", session)

        }
    }

}
