package com.example.crossfade

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.crossfade.databinding.ActivityMainBinding
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.songs_list_view.view.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    var listSongs = ArrayList<iternalStorageSong>()
    var mp: MediaPlayer? = null
    var adapter: SongsAdapter? = null

    inner class SongsAdapter(myListSongs: ArrayList<iternalStorageSong>) : BaseAdapter() {
        var myListSongs = ArrayList<iternalStorageSong>()

        init {
            this.myListSongs = myListSongs
        }
        override fun getCount(): Int {
            return myListSongs.size
        }

        override fun getItem(position: Int): Any {
            return myListSongs[position]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {
            var myView = layoutInflater.inflate(R.layout.songs_list_view, null)
            var song: iternalStorageSong = myListSongs[position]
            myView.song_title.text = song.Title
            myView.song_author.text = song.Author
            myView.play_btn.setOnContextClickListener{
                if (myView.play_btn.text == "STOP") {
                    mp!!.stop()
                    myView.play_btn.text = "PLAY"
                    return@setOnContextClickListener true
                } else {
                    mp = MediaPlayer()
                    try {
                        mp!!.setDataSource(song.Url)
                        mp!!.prepare()
                        mp!!.start()
                        myView.play_btn.text = "STOP"

                    } catch (e:Exception) {}
                    return@setOnContextClickListener true
                }
            }
            return myView
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonOneTrack.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this,
                    "Пожалйста перезайдите в приложение и предоставьте необходимые права",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        else
            loadSong()
        }
        buttonTwoTrack.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                loadSong()
            }
            else {
                Toast.makeText(
                    this,
                    "Пожалйста перезайдите в приложение и предоставьте необходимые права",
                    Toast.LENGTH_SHORT)
                    .show()}
        }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onStoragePermissionGranted()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    RQ_PERMISSION_FOR_BUTTON_1
                )
        }
    }

    @SuppressLint("Range")
    private fun loadSong() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val result = contentResolver.query(uri, null, selection, null, null)
        if(result!=null){
            while (result.moveToNext()) {
                val url = result.getString(result.getColumnIndex(MediaStore.Audio.Media.DATA))
                val author = result.getString(result.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val title = result.getString(result.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                listSongs.add(iternalStorageSong(title, author, url))
            }
        }
        adapter = SongsAdapter(listSongs)
        twoTracksListView.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RQ_PERMISSION_FOR_BUTTON_1 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onStoragePermissionGranted()
                } else {
                    if (!shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        askUserForOpeningSettings()
                    } else {
                        Toast.makeText(this, "Прошу прощения, но мне очень нужны эти права", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun askUserForOpeningSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        if (packageManager.resolveActivity(
                appSettingsIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            ) == null
        ) {
            Toast.makeText(this, "Ура! Ну наконец-то!", Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Нужно разрешение")
                .setMessage("Надо зайти в настройки и включить. Подробно лень писать")
                .setPositiveButton("Open") { _, _ ->
                    startActivity(appSettingsIntent)
                }
                .create()
                .show()
        }
    }

    private companion object {
        const val RQ_PERMISSION_FOR_BUTTON_1 = 1
    }

    private fun onStoragePermissionGranted() {
        Toast.makeText(this, "Спасибо за доступ, мы можем продолжать! " +
                "Выберите 2 трэка для получения эффекта", Toast.LENGTH_SHORT).show()
    }


}