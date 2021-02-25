package com.example.characterapp

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var mWebSettings: WebSettings;

    private var filePathCallbackNormal: ValueCallback<Uri>? = null;
    private var filePathCallbackLollipop: ValueCallback<Array<Uri>>? = null

    private var cameraImageUri : Uri? = null
    companion object {
        val FILECHOOSER_NORMAL_REQ_CODE = 2001
        val FILECHOOSER_LOLLIPOP_REQ_CODE = 2002
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("test", Build.VERSION.SDK_INT.toString());


        // Here, thisActivity is the current activity
if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED) {
 //권한이 부여되면 PERMISSION_GRANTED 거부되면 PERMISSION_DENIED 리턴

//권한 요청 할 필요가 있는가?
    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.READ_CONTACTS)) {

        // Show an expanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.

    } else {
       //권한 요청을 해야할 필요가 있는 경우(사용자가 DONT ASK ME AGIAN CHECK + DENY 선택)

        ActivityCompat.requestPermissions(this,
                arrayOf<String>(Manifest.permission.READ_CONTACTS),1);

        //requestPermissions 메소드는 비동기적으로 동작한다. 왜냐면 이 권한 검사 및 요청 메소드는
        //메인 액티비티에서 동작하기떄문에(메인쓰레드) 사용자 반응성이 굉장히 중요한 파트이다. 여기서 시간을
        //오래 끌어버리면 사람들이 답답함을 느끼게 된다. requestPermissions의 결과로 콜백 메소드인
        //onRequestPermissionsResult()가 호출된다. 오버라이딩 메소드이다. Ctrl+O

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
    }
}


        webview.webViewClient = WebViewClient()
        mWebSettings = webview.settings
        mWebSettings.javaScriptEnabled = true // 자바스크립트 허용

        // 각종 권한 획득
        checkVerify();
        webview.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let { view?.loadUrl(it) }
                    return true
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    AlertDialog.Builder(view!!.context)
                        .setTitle("Alert")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                            DialogInterface.OnClickListener { dialog, which ->
                                result?.confirm()
                            })
                        .setCancelable(false)
                        .create()
                        .show()
                    return true
                }

                override fun onJsConfirm(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    AlertDialog.Builder(view!!.context)
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                            result?.confirm()
                        })
                        .setCancelable(false)
                        .create()
                        .show()
                    return true
                }

                //안드로이드 5.0 이상에서 카메라 - input type = "file"인 태그를 선택했을 때 이벤트
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if (filePathCallbackLollipop != null) {
                        filePathCallbackLollipop?.onReceiveValue(null)
                        filePathCallbackLollipop = null
                    }
                    filePathCallbackLollipop = filePathCallback

                    var isCapture : Boolean? = fileChooserParams?.isCaptureEnabled
                    runCamera(true)
                    return true
                }
            }
        }
        webview.loadUrl("http://192.168.35.217:3333/")
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
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(
                    Manifest.permission.CAMERA
                )
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.INTERNET, Manifest.permission.CAMERA,
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
                                        applicationContext.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
                                }).setCancelable(false).show()
                        return
                    }
                }
                Toast.makeText(this,"Succeed Read/Write external storage !",Toast.LENGTH_SHORT).show()
            }
        }
    }

    //액티비티가 종료될 때 결과를 받고 파일을 전송할 때 사용
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            FILECHOOSER_NORMAL_REQ_CODE ->{
                if(resultCode == Activity.RESULT_OK){
                    if(filePathCallbackNormal==null) return
                    var result = if(data==null || resultCode== Activity.RESULT_OK) null else data.data
                    //onReceiveValue로 파일을 전송한다.
                    filePathCallbackNormal?.onReceiveValue(result)
                    filePathCallbackNormal = null
                }
            }
            FILECHOOSER_LOLLIPOP_REQ_CODE ->{
                if(requestCode== Activity.RESULT_OK){
                    if(filePathCallbackLollipop==null) return
                    if(data == null){

                    }
                    if(data?.data==null) data?.data = cameraImageUri

                    filePathCallbackLollipop?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data))
                    filePathCallbackLollipop = null
                }else{
                    if (filePathCallbackLollipop != null)
                    {   //  resultCode에 RESULT_OK가 들어오지 않으면 null 처리하지 한다.(이렇게 하지 않으면 다음부터 input 태그를 클릭해도 반응하지 않음)
                        filePathCallbackLollipop?.onReceiveValue(null);
                        filePathCallbackLollipop = null;
                    }

                    if (filePathCallbackNormal != null)
                    {
                        filePathCallbackNormal?.onReceiveValue(null);
                        filePathCallbackNormal = null;
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun runCamera(_isCapture : Boolean?){
        val intentCamera : Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val path : File = filesDir
        var file : File = File(path,"sample.png");
        //File객체의 URI를 얻는다.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val strpa = applicationContext.packageName
               cameraImageUri = FileProvider.getUriForFile(this,strpa+".fileprovider",file)
        }else{
            cameraImageUri = Uri.fromFile(file)
        }
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,cameraImageUri)

        _isCapture?.let{
            if(_isCapture){
                //선택팝업 카메라, 갤러리 둘 다 띄우고 싶을 때
                val pickIntent : Intent = Intent(Intent.ACTION_PICK)
                pickIntent.type = MediaStore.Images.Media.CONTENT_TYPE
                pickIntent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                val pickTitle : String = "사진을 가져올 방법을 선택하세요";
                var chooseIntent : Intent = Intent.createChooser(pickIntent,pickTitle)

                //카메라 intent 포함 시키기
                chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,arrayOf<Parcelable>(intentCamera))
                startActivityForResult(chooseIntent, FILECHOOSER_LOLLIPOP_REQ_CODE)
            }else{
                //바로 카메라 실행
                startActivityForResult(intentCamera, FILECHOOSER_LOLLIPOP_REQ_CODE)
            }
        }

    }
}