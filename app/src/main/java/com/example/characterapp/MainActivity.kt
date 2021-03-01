package com.example.characterapp

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var mWebSettings: WebSettings;

    public var filePathCallbackLollipop: ValueCallback<Array<Uri>>? = null
    lateinit var currentPhotoPath: String

    private var cameraImageUri : Uri? = null
    companion object {
        val FILECHOOSER_LOLLIPOP_REQ_CODE = 2002
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 각종 권한 획득
        checkVerify();
        mWebSettings = webview.settings
        mWebSettings.javaScriptEnabled = true // 자바스크립트 허용
        mWebSettings.domStorageEnabled = true // 로컬저장소 허용
        mWebSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webview.addJavascriptInterface(WebAppInterface(this),"Android")
        webview.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let { view?.loadUrl(it) }
                    return true
                }
            }
            webChromeClient = object : WebChromeClient() {
                //안드로이드 5.0 이상에서 카메라 - input type = "file"인 태그를 선택했을 때 이벤트
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    // Callback 초기화
                    if (filePathCallbackLollipop != null) {
                        filePathCallbackLollipop?.onReceiveValue(null);
                        filePathCallbackLollipop = null;
                    }
                    filePathCallbackLollipop = filePathCallback;
                    chooseImage() //사진을 불러올 방법 선택
                    return true
                }
            }
        }
        webview.loadUrl("http://192.168.0.2:3333/")

    }

    //뒤로가기 이벤트
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webview.canGoBack()) {
                webview.goBack()
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    //궈한 획득 여부 확인
    @TargetApi(Build.VERSION_CODES.M)
    fun checkVerify() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            //카메라 또는 저장공간 획득 여부 확인
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.INTERNET, Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 1
                )
            } else {

            }
        }
    }

    //권한 획득 여부에 따른 결과 반환
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 0) {
                for (i in 0..grantResults.size) {
                    if (grantResults.get(i) == PackageManager.PERMISSION_DENIED) {
                        //카메라, 저장소 중 하나라도 거부한다면 앱실행 불가 메세지를 띄운다.
                        AlertDialog.Builder(this).setTitle("알림")
                            .setMessage("권한을 허용해야 앱을 이용할 수 있습니다.")
                            .setPositiveButton(
                                "종료", DialogInterface.OnClickListener { dialog, which ->
                                    dialog.dismiss()
                                    finish()
                                }).setNegativeButton(
                                "권한 설정", DialogInterface.OnClickListener { dialog, which ->
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .setData(Uri.parse("package:"+applicationContext.packageName))
                                    applicationContext.startActivity(intent)
//                                        applicationContext.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
                                }).setCancelable(false).show()
                        return
                    }
                }
                Toast.makeText(this,"Succeed Read/Write external storage !",Toast.LENGTH_SHORT).show()
            }
        }
    }

    //액티비티가 종료될 때 결과를 받고 파일을 전송할 때 사용
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("DSfasdf", "onActivityResult: ${resultCode}")

        if(requestCode== FILECHOOSER_LOLLIPOP_REQ_CODE){
            if(resultCode ==0){
                cameraImageUri = null
                filePathCallbackLollipop?.onReceiveValue(cameraImageUri?.let { arrayOf<Uri>(it) })
            }else{
                if(data==null){
                    val intent = Intent()
                    intent.data = cameraImageUri
                    Log.d("onActivityResult",cameraImageUri.toString())
                    filePathCallbackLollipop?.onReceiveValue(cameraImageUri?.let { arrayOf<Uri>(it) });
                }else{
                    filePathCallbackLollipop?.onReceiveValue(arrayOf<Uri>(data?.data!!))
                }
            }
        } else {
            if (filePathCallbackLollipop != null)
            {   //  resultCode에 RESULT_OK가 들어오지 않으면 null 처리하지 한다.(이렇게 하지 않으면 다음부터 input 태그를 클릭해도 반응하지 않음)
                filePathCallbackLollipop?.onReceiveValue(null);

            }
        }
        filePathCallbackLollipop = null;
    }

    //이미지 파일 경로 설정
    @Throws(IOException::class)
    private fun createImage() : File{
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir : File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".png",
            storageDir
        ).apply{
            currentPhotoPath = absolutePath
        }
    }

    //카메라로 촬영 or 갤러리에서 가져오기
    private fun chooseImage(){

        val strpa = applicationContext.packageName
        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        intentCamera.also {intentCamera ->
            intentCamera.resolveActivity(packageManager)?.also{
                val photoFile: File? = try{
                    createImage()
                }catch (ex : IOException){
                    Log.e("imageError", "createImage is Error")
                    null
                }
                photoFile?.also{
                    val photoURI : Uri = FileProvider.getUriForFile(
                        applicationContext,

                        "$strpa.fileprovider",
                        it
                    )
                    cameraImageUri = photoURI
                    intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,photoURI)
                }
            }
        }
        val pickIntent  = Intent(Intent.ACTION_PICK)
        pickIntent.type = MediaStore.Images.Media.CONTENT_TYPE
        pickIntent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val pickTitle = "사진을 가져올 방법을 선택하세요";
        val chooserIntent = Intent.createChooser(pickIntent,pickTitle)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,arrayOf<Parcelable>(intentCamera))
        startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE)
    }

    class WebAppInterface(private val mContext : Context){

        @JavascriptInterface
        fun intentActivity(msg : String){
            Log.d("testMsg", msg)
            mContext.startActivity(Intent(mContext,ResultActivity::class.java).putExtra("msg",msg))
        }

        @JavascriptInterface
        fun loading(){
            mContext.startActivity(Intent(mContext,LoadingActivity::class.java))
        }
    }
}