package com.kv.kidsdrawiingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.brush_size_selector.*
import kotlinx.android.synthetic.main.brush_size_selector_slider.*
import kotlinx.android.synthetic.main.color_pallete.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private var colorint : Int=0

    var customProgressDialog : Dialog?=null

    val openGalleryLauncher : ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result->
                if(result.resultCode== RESULT_OK && result.data!=null){
                    iv_bg.setImageURI(result.data?.data)
                }
            }


    val rqPemission : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permission ->
            permission.entries.forEach {
                val permissionName = it.key
                val isgranted = it.value
                if(isgranted){
                    Toast.makeText(this@MainActivity,"Permission granted",Toast.LENGTH_LONG).show()
                    val pickIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                }else{
                    if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(this@MainActivity,"Oops! permission denied",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    private var drawingview : DrawingView? =null
    private var imagecutrentpaint : ImageButton? =null
    private fun showdialog(){
        customProgressDialog = Dialog(this@MainActivity)
        customProgressDialog?.setContentView(R.layout.dialog_view)
        customProgressDialog?.show()
    }
    private fun cancelDialog(){
        customProgressDialog?.dismiss()
        customProgressDialog=null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingview = findViewById(R.id.draw_view)
        drawingview?.setBrushSize(20f)
        imagecutrentpaint = colorpicker[1] as ImageButton
        imagecutrentpaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallete_normal)
        )
        brushSize.setOnClickListener {
            initializeView()
        }
        slider_btn.setOnClickListener {
//            val intent = Intent(this,colorpalleteActivity::class.java)
//            startActivity(intent)
//            finish()
            requestP()

        }
        undo_btn.setOnClickListener {
            drawingview?.undo()
        }
        erase_btn.setOnClickListener {
            drawingview?.clear()
        }
        redo_btn.setOnClickListener {
            drawingview?.redo()
        }
        save_btn.setOnClickListener {
            if(isReadAllow()){
                showdialog()
                lifecycleScope.launch {
                    saveBitmap(getBitmap(fl_drawing_view))
                }
            }
        }

    }

    private fun initializeView() {
        // create objects of TextView and Seekbar
        val sliderdialog = Dialog(this)
        sliderdialog.setContentView(R.layout.brush_size_selector_slider)
        sliderdialog.setTitle("Brush size: ")
        sliderdialog.show()
        val sliderbtn = sliderdialog.demoslider
        var x : Float = sliderbtn.value
        sliderbtn.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
            }
        })
        sliderbtn.addOnChangeListener { slider, value, fromUser ->
            // Responds to when slider's value is changed
            drawingview?.setBrushSize(value)
            x=value;
//            demoslider.value=value
        }
//        storeslider(x)

    }
    fun colorPicker(view : View){
        if(view != imagecutrentpaint){
            val imagebtn = view as ImageButton
            val colortag = imagebtn.tag.toString()
            drawingview?.setColor(colortag)
            imagebtn.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallete_pressed)
            )
            imagecutrentpaint!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallete_normal)
            )
            imagecutrentpaint = view

        }
    }
    private fun isReadAllow() : Boolean{
        return ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
    }
    private fun requestP(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            showRationalDialog("Kids Drawing App","Drawing APP needs to access storage")
        }else{
            rqPemission.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    private fun showRationalDialog(
        title : String,
        message : String
    ){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message).setPositiveButton("Cancel"){
            dialog,_ ->
            dialog.dismiss()
        }
        builder.create()
    }
    private fun getBitmap(view : View) : Bitmap{
        val returnBitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnBitmap)
        val bg = view.background
        if(bg!=null){
            bg.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnBitmap
    }
    private fun Share(result : String){
        MediaScannerConnection.scanFile(this,arrayOf(result),null){
            path, uri->
            val shareIntent = Intent()
            shareIntent.action= Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent,"Share"))

        }

    }
    private suspend fun saveBitmap(bitmap : Bitmap): String{
        var result =""
        withContext(Dispatchers.IO){
            if(bitmap!=null){
                try{
                    val bytes= ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,bytes)
                    val file = File(externalCacheDir?.absoluteFile.toString()+ File.separator+"Drawing App"
                    +System.currentTimeMillis()/1000+".png")
                    val fileOut = FileOutputStream(file)
                    fileOut.write(bytes.toByteArray())
                    fileOut.close()
                    result=file.absolutePath
                    runOnUiThread {
                        cancelDialog()
                        if(!result.isEmpty()){
                            Toast.makeText(this@MainActivity, "saved: $result",Toast.LENGTH_LONG).show()
                            Share(result)
                        }else{
                            Toast.makeText(this@MainActivity,"Something went wrong",Toast.LENGTH_LONG).show()
                        }
                    }
                }
                catch (e : Exception){
                    result=""
                    e.printStackTrace()
                }
            }
        }
        return result
    }



}