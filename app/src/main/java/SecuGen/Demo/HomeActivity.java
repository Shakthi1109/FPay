package SecuGen.Demo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import SecuGen.FDxSDKPro.SGFingerPresentEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import SecuGen.FDxSDKPro.*;

public class HomeActivity extends Activity implements View.OnClickListener, java.lang.Runnable, SGFingerPresentEvent{

    private static final String TAG = "SecuGen USB";
    private static final int IMAGE_CAPTURE_TIMEOUT_MS = 10000;
    private static final int IMAGE_CAPTURE_QUALITY = 50;

    int SUCCESSFLAG;
    int VALIDATEFLAG;
    int SUBMITFLAG;
    private Button mButtonCapture;
    private Button mButtonLed;
    private android.widget.TextView mTextViewResult;
    private PendingIntent mPermissionIntent;
    ImageView mImageViewFingerprint;
    private int[] mMaxTemplateSize;
    private int mImageWidth;
    private int mImageHeight;
    private int[] grayBuffer;
    private Bitmap grayBitmap;
    private IntentFilter filter; //2014-04-11
    private SGAutoOnEventNotifier autoOn;
    private boolean mLed;
    private boolean mAutoOnEnabled;
    private boolean bSecuGenDeviceOpened;
    private JSGFPLib sgfplib;
    private boolean usbPermissionRequested;
    String NFIQString,buffer;


    private byte[] mRegisterImage;
    private byte[] mRegisterTemplate;

    byte [] regFP;  //this and above are used for comparison

    String emailInp;
    EditText EmailInp;


    Button registerBtn,choose,upload;
    EditText Name, Amt;
    TextView hi;
    String nameStr,registeredFpTemplate,TemplateComparisonInput, name,amount;
    float amtFloat;

    String fpimg;

    Member member;
    long maxid=0;
    StorageReference mStorageRef;
    public Uri imguri;

    Button nextbtn, regBtn;
    EditText cost;
    float payable;

    TextView nameTV, amountTV, templateTV;
    Button buttonRetrive;
    DatabaseReference reff;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @SuppressLint("HandlerLeak")
    public Handler fingerDetectedHandler = new Handler(){
        // @Override
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void handleMessage(Message msg) {
            //Handle the message
            CaptureFingerPrint();
            if (mAutoOnEnabled) {
                EnableControls();
            }
        }
    };

    public void EnableControls(){
        this.mButtonCapture.setClickable(true);
        this.mButtonCapture.setTextColor(getResources().getColor(android.R.color.white));
        this.mButtonLed.setClickable(true);
        this.mButtonLed.setTextColor(getResources().getColor(android.R.color.white));
    }


    public void DisableControls(){
        this.mButtonCapture.setClickable(false);
        this.mButtonCapture.setTextColor(getResources().getColor(android.R.color.black));
        this.mButtonLed.setClickable(false);
        this.mButtonLed.setTextColor(getResources().getColor(android.R.color.black));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2a1735")));
        setTitle("FPay");

        //Adding pf scan
        SUCCESSFLAG=0;
        SUBMITFLAG=0;
        VALIDATEFLAG=0;
        mButtonCapture = (Button)findViewById(R.id.scanFpBtn);
        mButtonCapture.setOnClickListener(this);
        mButtonLed = (Button)findViewById(R.id.buttonLedOnPayment);
        mButtonLed.setOnClickListener(this);
        mImageViewFingerprint = (ImageView)findViewById(R.id.imageViewPayment);
        // ends here
        Button paybtn;
        nextbtn = (Button) findViewById(R.id.nextbtn);
        paybtn = (Button) findViewById(R.id.paybtn);
        regBtn = (Button) findViewById(R.id.regBtn);
        cost = (EditText) findViewById(R.id.cost);
        EmailInp=(EditText) findViewById(R.id.emailInput);

        nameTV = (TextView) findViewById(R.id.textViewName);
        hi=(TextView) findViewById(R.id.hi);
        amountTV = (TextView) findViewById(R.id.textViewAmount);
//       templateTV = (TextView) findViewById(R.id.textViewTemplate);
        buttonRetrive = (Button) findViewById(R.id.buttonRetrive);


        grayBuffer = new int[JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES*JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES];
        for (int i=0; i<grayBuffer.length; ++i)
            grayBuffer[i] = android.graphics.Color.GRAY;
        grayBitmap = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES, Bitmap.Config.ARGB_8888);
        grayBitmap.setPixels(grayBuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES);
        mImageViewFingerprint.setImageBitmap(grayBitmap);

        int[] sintbuffer = new int[(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2)*(JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2)];
        for (int i=0; i<sintbuffer.length; ++i)
            sintbuffer[i] = android.graphics.Color.GRAY;
        Bitmap sb = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2, Bitmap.Config.ARGB_8888);
        sb.setPixels(sintbuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2);
        mMaxTemplateSize = new int[1];

        //USB Permissions
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        sgfplib = new JSGFPLib((UsbManager)getSystemService(Context.USB_SERVICE));
        bSecuGenDeviceOpened = false;
        usbPermissionRequested = false;

        mLed = false;
        mAutoOnEnabled = false;
        autoOn = new SGAutoOnEventNotifier (sgfplib, this);



        buttonRetrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SUCCESSFLAG=0;
                if(EmailInp.getText().toString().length()==0 && emailInp==null|| cost.getText().toString().length()==0)
                {
                    Toast.makeText(HomeActivity.this,"Fill all details!",Toast.LENGTH_SHORT).show();
                }
                else {
                    SUBMITFLAG=1;
                    emailInp = EmailInp.getText().toString().trim();

                    Log.d(TAG, "nowcheck: " + emailInp);
                    reff = FirebaseDatabase.getInstance().getReference().child("Member").child(emailInp);
                    reff.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            name = dataSnapshot.child("nameStr").getValue().toString();
                            amount = dataSnapshot.child("amtFloat").getValue().toString();
                            registeredFpTemplate = dataSnapshot.child("template").getValue().toString();
//                        Log.d("Redistered fp template",registeredFpTemplate);

                            String a[] = registeredFpTemplate.split(", ");
                            regFP = new byte[a.length];
                            for (int i = 0; i < a.length; i++) {
                                Log.d(TAG, "onDataChange: " + a[i]);
                                if (i == 0) {
                                    regFP[i] = Byte.valueOf(a[i].substring(1));

                                } else if (i == a.length - 1) {
                                    regFP[i] = Byte.valueOf(a[i].substring(0, a[i].length() - 1));

                                } else {
                                    regFP[i] = Byte.valueOf(a[i]);
                                }
                            }
                            Log.d(TAG, "array from db: " + Arrays.toString(regFP));


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });


        mButtonLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLed = !mLed;
                //dwTimeStart = System.currentTimeMillis();
                long result = sgfplib.SetLedOn(mLed);
//                dwTimeEnd = System.currentTimeMillis();
//                dwTimeElapsed = dwTimeEnd-dwTimeStart;
//            mTextViewResult.setText("setLedOn(" + mLed +") ret: " + result + " [" + dwTimeElapsed + "ms]\n");

            }
        });

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SUBMITFLAG==0)
                {
                    Toast.makeText(HomeActivity.this,"Press Submit First!",Toast.LENGTH_SHORT).show();
                }
                else {
                    matchingFingerprint(mRegisterTemplate, regFP);
                }
            }
        });

        paybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VALIDATEFLAG==0)
                {
                    Toast.makeText(HomeActivity.this,"Press Validate First",Toast.LENGTH_SHORT).show();
                }
                else {
                    String message = emailInp + "    " + amtFloat;

                    Intent intent = new Intent(HomeActivity.this, pinActivity.class);
                    intent.putExtra("message", message);
                    startActivity(intent);
                }
            }
        });
//
//        nextbtn.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onClick(View v) {
////                regFP= Base64.getDecoder().decode(registeredFpTemplate);
////                Log.d("Registered PF",Arrays.toString(regFP));
//
//                Log.d(TAG, "from user: "+scannedFpTemplate);
//                Log.d("ANSWER",Integer.toString(SUCCESSFLAG));
//                //JUST FOR NOW COMMENT (TESTING)
//                /
//
//            }
//        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(HomeActivity.this, RegisterActivity.class);
                startActivity(intent1);
            }
        });


    }

    @Override
    public void onPause() {
        Log.d(TAG, "Enter onPause()");

        mRegisterTemplate = null;


        if (bSecuGenDeviceOpened)
        {
            autoOn.stop();
            EnableControls();
            sgfplib.CloseDevice();
            bSecuGenDeviceOpened = false;
        }

        mImageViewFingerprint.setImageBitmap(grayBitmap);
        super.onPause();
        Log.d(TAG, "Exit onPause()");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onResume(){
        Log.d(TAG, "Enter onResume()");
        super.onResume();
        DisableControls();
        long error = sgfplib.Init( SGFDxDeviceName.SG_DEV_AUTO);
        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE){
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND)
                dlgAlert.setMessage("The attached fingerprint device is not supported on Android");
            else
                dlgAlert.setMessage("Fingerprint device initialization failed!");
            dlgAlert.setTitle("SecuGen Fingerprint SDK");
            dlgAlert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int whichButton){
                            finish();
                            return;
                        }
                    }
            );
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
        }
        else {
            UsbDevice usbDevice = sgfplib.GetUsbDevice();
            if (usbDevice == null){
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage("SecuGen fingerprint sensor not found!");
                dlgAlert.setTitle("SecuGen Fingerprint SDK");
                dlgAlert.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton){
                                finish();
                                return;
                            }
                        }
                );
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
            }
            else {
                boolean hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                if (!hasPermission) {
                    if (!usbPermissionRequested)
                    {
                        usbPermissionRequested = true;
                        sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent);
                    }
                    else
                    {
                        //wait up to 20 seconds for the system to grant USB permission
                        hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                        //debugMessage("Waiting for USB Permission\n");
                        int i=0;
                        while ((hasPermission == false) && (i <= 40))
                        {
                            ++i;
                            hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
                if (hasPermission) {

                    error = sgfplib.OpenDevice(0);
                    if (error == SGFDxErrorCode.SGFDX_ERROR_NONE)
                    {
                        bSecuGenDeviceOpened = true;
                        SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
                        error = sgfplib.GetDeviceInfo(deviceInfo);
                        //debugMessage("GetDeviceInfo() ret: " + error + "\n");
                        mImageWidth = deviceInfo.imageWidth;
                        mImageHeight= deviceInfo.imageHeight;

                        sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
                        sgfplib.GetMaxTemplateSize(mMaxTemplateSize);

                        mRegisterTemplate = new byte[(int)mMaxTemplateSize[0]];


                        EnableControls();

                        if (mAutoOnEnabled){
                            autoOn.start();
                            DisableControls();
                        }
                    }

                }

            }
        }
        Log.d(TAG, "Exit onResume()");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onDestroy() {
        Log.d(TAG, "Enter onDestroy()");
        mRegisterTemplate = null;
        sgfplib.CloseDevice();
        sgfplib.Close();
        super.onDestroy();
        Log.d(TAG, "Exit onDestroy()");
    }

    public Bitmap toGrayscale(byte[] mImageBuffer)
    {
        byte[] Bits = new byte[mImageBuffer.length * 4];
        for (int i = 0; i < mImageBuffer.length; i++) {
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
            Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bmpGrayscale;
    }

    public void DumpFile(String fileName, byte[] buffer)
    {
        //Uncomment section below to dump images and templates to SD card

        try {
            File myFile = new File("/sdcard/Download/" + fileName);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            fOut.write(buffer,0,buffer.length);
            Toast.makeText(HomeActivity.this,"Downloaded",Toast.LENGTH_SHORT).show();
            fOut.close();
        } catch (Exception e) {
            Log.d("D","Exception when writing file" + fileName);
        }

    }

    public void SGFingerPresentCallback (){
        autoOn.stop();
        fingerDetectedHandler.sendMessage(new Message());
    }

    void matchingFingerprint(byte [] regFP, byte[] scannedFpTemplate){

        VALIDATEFLAG=1;

            boolean[] matched = new boolean[1];
            //dwTimeStart = System.currentTimeMillis();
            sgfplib.MatchTemplate(regFP,scannedFpTemplate,SGFDxSecurityLevel.SL_NORMAL, matched);

            Log.d("output user",Arrays.toString(scannedFpTemplate));
            Log.d("output data",Arrays.toString(regFP));


//			TextView test = (TextView) findViewById(R.id.test);
//			test.setText(Arrays.toString(mRegisterTemplate));

//            dwTimeEnd = System.currentTimeMillis();
//            dwTimeElapsed = dwTimeEnd-dwTimeStart;
//            debugMessage("MatchTemplate() ret:" + result+ " [" + dwTimeElapsed + "ms]\n");
            if (matched[0]) {
                Toast.makeText(HomeActivity.this,"MATCHED",Toast.LENGTH_SHORT).show();

                    payable = Float.valueOf(cost.getText().toString().trim());


                    //amountTV.setText(amount);
                amtFloat=Float.valueOf(amount);
                    if(payable<amtFloat)
                    {
                        hi.setText(R.string.hi);
                        nameTV.setText(name);
                        amountTV.setText(R.string.sufficient);
                        amtFloat=amtFloat-payable;
                    }
                    else
                    {
                        amountTV.setText(R.string.notSufficient);
                        VALIDATEFLAG=0;
                    }
            }
            else {
                Toast.makeText(HomeActivity.this,"NOT MATCHED",Toast.LENGTH_SHORT).show();
                VALIDATEFLAG=0;
            }
        }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void CaptureFingerPrint(){

//        Log.d("jajajajajaj", "Im getting exxt");
//        long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
//        byte[] buffer = new byte[mImageWidth*mImageHeight];
//        dwTimeStart = System.currentTimeMillis();
//        //long result = sgfplib.GetImage(buffer);
//        long result = sgfplib.GetImageEx(buffer, IMAGE_CAPTURE_TIMEOUT_MS,IMAGE_CAPTURE_QUALITY);
//
//
//        long nfiq = sgfplib.ComputeNFIQ(buffer, mImageWidth, mImageHeight);
//        //long nfiq = sgfplib.ComputeNFIQEx(buffer, mImageWidth, mImageHeight,500);
//        NFIQString =  new String("NFIQ="+ nfiq);
//        DumpFile("capture2016.raw", buffer);
//        dwTimeEnd = System.currentTimeMillis();
//        dwTimeElapsed = dwTimeEnd-dwTimeStart;
//
//        mTextViewResult.setText("getImageEx(10000,50) ret: " + result + " [" + dwTimeElapsed + "ms] " + NFIQString +"\n");
//        mImageViewFingerprint.setImageBitmap(this.toGrayscale(buffer));
//
////        Log.d("lwelele", Arrays.toString(buffer));
////        fpimg = Arrays.toString(buffer);
//
//        UploadTask uploadTask = mStorageRef.putBytes(buffer);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Log.d("D","Not uploaded");
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                String Uri = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
//                Log.d("D","Not uploaded");
//            }
//        });
//
//        buffer = null;




        //DEBUG Log.d(TAG, "Clicked REGISTER");
        if (mRegisterImage != null)
            mRegisterImage = null;
        mRegisterImage = new byte[mImageWidth*mImageHeight];

//        this.mCheckBoxMatched.setChecked(false);
//        dwTimeStart = System.currentTimeMillis();
          sgfplib.GetImageEx(mRegisterImage, IMAGE_CAPTURE_TIMEOUT_MS,IMAGE_CAPTURE_QUALITY);
//        DumpFile("register.raw", mRegisterImage);
//        dwTimeEnd = System.currentTimeMillis();
//        dwTimeElapsed = dwTimeEnd-dwTimeStart;
//        debugMessage("GetImageEx() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
        mImageViewFingerprint.setImageBitmap(this.toGrayscale(mRegisterImage));
//        dwTimeStart = System.currentTimeMillis();
        sgfplib.SetTemplateFormat(SecuGen.FDxSDKPro.SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
//        dwTimeEnd = System.currentTimeMillis();
//        dwTimeElapsed = dwTimeEnd-dwTimeStart;
//        debugMessage("SetTemplateFormat(ISO19794) ret:" +  result + " [" + dwTimeElapsed + "ms]\n");

        int quality1[] = new int[1];
        sgfplib.GetImageQuality(mImageWidth, mImageHeight, mRegisterImage, quality1);
        //debugMessage("GetImageQuality() ret:" +  result + "quality [" + quality1[0] + "]\n");

        SGFingerInfo fpInfo = new SGFingerInfo();
        fpInfo.FingerNumber = 1;
        fpInfo.ImageQuality = quality1[0];
        fpInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
        fpInfo.ViewNumber = 1;

        for (int i=0; i< mRegisterTemplate.length; ++i)
            mRegisterTemplate[i] = 0;
        sgfplib.CreateTemplate(fpInfo, mRegisterImage, mRegisterTemplate);
        DumpFile("register.min", mRegisterTemplate);

        int[] size = new int[1];
        sgfplib.GetTemplateSize(mRegisterTemplate, size);
        Log.d("RegisterAct",mRegisterTemplate.toString());

//        uploadToFirebase= android.util.Base64.encodeToString(mRegisterTemplate, android.util.Base64.DEFAULT);

//        uploadToFirebase=new String(mRegisterTemplate);
        Log.d("RegisterAct",Arrays.toString(mRegisterTemplate));



        //Printing byte Array
//        PrintStream ps = new PrintStream(System.out);
//
//        // write bytes 1-3
//        System.out.println("here here here");
//        String
//        Log.d("lolololo",);



        //Printing
//        String s1,s2,s3;
//        s1=Arrays.toString(mRegisterTemplate);   //best
//        s2= new String(mRegisterTemplate);
//        s3=Base64.getEncoder().encodeToString(mRegisterTemplate);
//
//
//        Log.d("TestToarraysbest",s1);
//        Log.d("TestToarrays",s2);
//        Log.d("TestToarrays",s3);
//
//        byte [] b1,b2,b3;
//
//        b1=s1.getBytes();
//        b2=s2.getBytes();
//        b3=Base64.getDecoder().decode(s3);  //best
//
//        Log.d("TestToarrays",Arrays.toString(b1));
//        Log.d("TestToarrays",Arrays.toString(b2));
//        Log.d("TestToarraysbest",Arrays.toString(b3));



        //loop for printing template

//        String out;
//        for (int i=0;i<264;i++)
//        {
//            out=out+
//        }
//
//        Log.d("Test111",uploadToFirebase);
//
//        Log.d("testType111",uploadToFirebase.getClass().getSimpleName());
//
//        byte [] downloadFrmFirebase= Base64.getDecoder().decode(uploadToFirebase);
//
//        Log.d("test222",Arrays.toString(downloadFrmFirebase));
//
//        Log.d("testType222",downloadFrmFirebase.getClass().getSimpleName());

        mRegisterImage = null;
        fpInfo = null;

    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClick(View v) {
        long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;

        if (v == mButtonCapture) {
            CaptureFingerPrint();
        }


        if (v == mButtonLed) {
            mLed = !mLed;
            dwTimeStart = System.currentTimeMillis();
            long result = sgfplib.SetLedOn(mLed);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            mTextViewResult.setText("setLedOn(" + mLed +") ret: " + result + " [" + dwTimeElapsed + "ms]\n");
        }

    }


    public void run() {

        while (true) {

        }
    }

}
