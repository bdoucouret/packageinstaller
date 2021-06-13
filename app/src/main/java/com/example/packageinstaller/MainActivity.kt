package com.example.packageinstaller

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File


class MainActivity : AppCompatActivity() {
    var APPLICATION_UUID = 10110;

    @RequiresApi(Build.VERSION_CODES.M)
    fun downloadAndInstall() {
        Log.i("ApplicationInstaller", "Start.")
        var context = this.applicationContext;
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        val fileName = "AppName.apk"
        var destination: String = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/$fileName"
        val file = File(destination)
        if (file.exists()) {
            file.delete()
        }
        val uri: Uri = Uri.parse("file://$destination")
        //var uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermission(Manifest.permission.INTERNET)
        //facebook app
        Log.i("ApplicationInstaller", "Going to download.")
        val url: String = "https://www.apkmirror.com/wp-content/themes/APKMirror/download.php?id=2312371";
        val request = DownloadManager.Request(Uri.parse(url))
        request.setDescription("Mise à jour")
        request.setTitle("Application X")
        request.setDestinationUri(uri)
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)
        val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context?, intent: Intent?) {
               // installLegacy(intent, uri, downloadId, manager, destination)
                installSilently(intent, uri, downloadId, manager, destination)
                unregisterReceiver(this)
                finish()
            }
        }
        //startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
        val notifClicked: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context?, intent: Intent?) {
                Log.i("ApplicationInstaller", "Download: notification clicked")
                unregisterReceiver(this)
                finish()
            }
        }
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        registerReceiver(notifClicked, IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED))
    }

    fun installLegacy(
        intent: Intent?,
        uri: Uri,
        downloadId: Long,
        manager: DownloadManager,
        destination: String
    ) {
        Log.i("ApplicationInstaller", "Downloaded. Going to install.")
        intent?.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        val install = Intent(Intent.ACTION_VIEW)
        install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        install.setDataAndType(
            uri,
            manager.getMimeTypeForDownloadedFile(downloadId)
        )
        startActivity(install)
    }

    fun installSilently(
        intent: Intent?,
        uri: Uri,
        downloadId: Long,
        manager: DownloadManager,
        filename: String
    ) {
        Log.i("ApplicationInstaller", "Downloaded. Going to install silently.")
        val file = File(filename)
        if (file.exists()) {
            try {
                val command: String
                command = "pm list features"
                val proc =
                    Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                //Runtime.getRuntime().exec(arrayOf( command))
                proc.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun requestPermission(permission: String) {
        Log.i("ApplicationInstaller", "Asking for permission for $permission.")
        if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                )
                != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("ApplicationInstaller", "Option 1")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            permission
                    )
            ) {
                Log.i("ApplicationInstaller", "Option A")
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permission),
                        APPLICATION_UUID
                )
                Log.i("ApplicationInstaller", "$permission permission successfully requested")
            }
        } else {
            Log.i("ApplicationInstaller", "$permission permission already granted.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            downloadAndInstall()
        }, 2000)
    }
}