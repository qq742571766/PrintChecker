package com.olc.printchecker;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import hardware.print.BarcodeUtil;
import hardware.print.printer;

/************************************************************
 * Copyright 2000-2066 Olc Corp., Ltd.
 * All rights reserved.
 * Description     : The Main activity for  PrintChecker
 * History        :( ID, Date, Author, Description)
 * v1.0, 2017/3/2,  zhangyong, create
 ************************************************************/

public class PrintCheckFragment extends Fragment implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{
    private LinearLayout mCard1ContentLayout;
    private int mKeyTextSize = 24;
    private int mValueTextSize = 20;
    private int mValue2TextSize = 22;
    private int mLineTextSize = 18;
    private int mTitleTextSize = 40;
    /* BarCode Image size  */
    private int mBarcodeSize = 80;
    private int mOffsetX = 210;
    private int mStepDis = 1;
    private boolean bold = true;
    private float mCardWidth = 0;

    List<LineObject> mDataList = new ArrayList<>();

    printer mPrinter = new printer();
    private Switch mToggleButton;
    private Button mStepButton;
    private Button mPrintButton;
    private Spinner mSpinner;
    private TextView mStatusTextView;

    private String mTitleStr = "Printer test ";
    private printer.PrintType mTitleType = printer.PrintType.Centering;
    private CheckBox mBoldCheckbox;
    private Spinner mAlignSpinner;
    private EditText mEditText;
    private StringObject mTitleObject;
    private boolean titleBold = true;
    private Spinner mTitleSizeSpinner;
    private CheckBox mAdvanceCheckbox;
    private View mBlodView;
    private View mTextSizeView;
    private View mAlignView;
    private View mGrayView;
    private View mToogleView;
    private View mView;

    public PrintCheckFragment () {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_print_check, container, false);
        mCardWidth = getResources().getDimension(R.dimen.paper_content_size);
        initView();
        initData();
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String [] strAct={"0","1","2","3","4","5","6","7","8","9"};
        ArrayAdapter mAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,strAct);
        mAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mSpinner.setAdapter(mAdapter);
        mPrinter.SetGrayLevel((byte)0x05);
        mSpinner.setSelection(5);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                byte btGrayLevel=(byte)mSpinner.getSelectedItemPosition();
                mPrinter.SetGrayLevel(btGrayLevel);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        String[] alignArray = {"Center","Left","Right"};
        ArrayAdapter mAlignAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,alignArray);
        mAlignAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mAlignSpinner.setAdapter(mAlignAdapter);
        mAlignSpinner.setSelection(0);
        mAlignSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                int position = mAlignSpinner.getSelectedItemPosition();
                if (position == 0) {
                    mTitleType = printer.PrintType.Centering;
                } else if (position == 1) {
                    mTitleType = printer.PrintType.Left;
                } else if (position == 2) {
                    mTitleType = printer.PrintType.Right;
                }
                updateTitleViewLayoutParams();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        String[] sizeArray = {"18","20","22","24","40"};
        ArrayAdapter mSizeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,sizeArray);
        mSizeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mTitleSizeSpinner.setAdapter(mSizeAdapter);
        mTitleSizeSpinner.setSelection(4);
        mTitleSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                int titleSize = Integer.valueOf((String)(mTitleSizeSpinner.getSelectedItem()));
                mTitleTextSize = titleSize;
                mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,mTitleTextSize);

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mBoldCheckbox.setChecked(true);
        mBoldCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    titleBold = true;
                } else {
                    titleBold = false;
                }
                mEditText.getPaint().setFakeBoldText(titleBold);
            }
        });

        mAdvanceCheckbox.setChecked(false);
        mAdvanceCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    controlViewStatus(true);
                } else {
                    controlViewStatus(false);
                }
            }
        });

        mCard1ContentLayout.removeAllViews();
        fillLineViews(mCard1ContentLayout);

        Bitmap bm=null;
        try {
            bm = BarcodeUtil.encodeAsBitmap("Thanks for using our Android terminal",
                    BarcodeFormat.QR_CODE, mBarcodeSize, mBarcodeSize);
            ImageView imagView = new ImageView(getActivity());
            LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(mBarcodeSize, mBarcodeSize);
            layoutParam.gravity = Gravity.CENTER_HORIZONTAL;
            imagView.setLayoutParams(layoutParam);
            imagView.setImageBitmap(bm);
            mCard1ContentLayout.addView(imagView);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        controlViewStatus(false);
        mToggleButton.performClick();
    }

    private void initView() {
        mCard1ContentLayout = (LinearLayout) mView.findViewById(R.id.lyt_card1_content);
        mStatusTextView = (TextView)mView.findViewById(R.id.tv_status);
        mPrintButton = (Button)mView.findViewById(R.id.btn_print);
        mPrintButton.setOnClickListener(this);
        mStepButton = (Button)mView.findViewById(R.id.btn_step);
        mStepButton.setOnClickListener(this);

        mToggleButton = (Switch) mView.findViewById(R.id.toggleButton);
//        mToggleButton.setOnClickListener(this);
        mToggleButton.setOnCheckedChangeListener(this);
        mSpinner = (Spinner)mView.findViewById(R.id.spinner);
        mBoldCheckbox = (CheckBox)mView.findViewById(R.id.cb_blod);
        mAlignSpinner = (Spinner)mView.findViewById(R.id.spinner_align);
        mTitleSizeSpinner = (Spinner)mView.findViewById(R.id.spinner_textsize);

        mBlodView = mView.findViewById(R.id.lyt_blod);
        mTextSizeView = mView.findViewById(R.id.lyt_textsize);
        mAlignView = mView.findViewById(R.id.lyt_align);
        mGrayView = mView.findViewById(R.id.lyt_gray);
        mToogleView = mView.findViewById(R.id.lyt_toggle);

        mAdvanceCheckbox = (CheckBox)mView.findViewById(R.id.cb_setting);


    }

    private void updateTitleViewLayoutParams(){
        mTitleObject.direct = mTitleType;
        Paint paint = mEditText.getPaint();
        try{
            mTitleObject.content = mEditText.getEditableText().toString();
        }catch (Exception e){

        }
        AbsoluteLayout.LayoutParams params = generateLayoutParams(mEditText,mTitleObject,paint);
        if (params != null){
            mEditText.setLayoutParams(params);
            mEditText.postInvalidate();
        }
    }

    private void controlViewStatus(boolean flag){
        if (flag){
            mEditText.setEnabled(true);
            mView.findViewById(R.id.lyt_advance).setVisibility(View.VISIBLE);
            mBlodView.setVisibility(View.VISIBLE);
            mTextSizeView.setVisibility(View.VISIBLE);
            mAlignView.setVisibility(View.VISIBLE);
            mGrayView.setVisibility(View.VISIBLE);
            mToogleView.setVisibility(View.VISIBLE);
        } else {
            mEditText.setEnabled(false);
            mView.findViewById(R.id.lyt_advance).setVisibility(View.GONE);
            mBlodView.setVisibility(View.GONE);
            mTextSizeView.setVisibility(View.GONE);
            mAlignView.setVisibility(View.GONE);
            mGrayView.setVisibility(View.GONE);
            mToogleView.setVisibility(View.GONE);
        }
    }
    private void initData() {
        mDataList.clear();
        String spiltStr = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
        List<StringObject> mLine1List = new ArrayList<StringObject>();
        mLine1List.add(generateStringObject(mTitleStr,mTitleTextSize,-1,-1,true, printer.PrintType.Centering));
        LineObject line1Object = new LineObject();
        line1Object.stringObjects = mLine1List;
        line1Object.maxTextSize = mTitleTextSize;
        line1Object.blod = true;
        mDataList.add(line1Object);

        List<StringObject> mLine2List = new ArrayList<StringObject>();
        mLine2List.add(generateStringObject(spiltStr,mLineTextSize,-1,-1,false, printer.PrintType.Centering));
        LineObject line2Object = new LineObject();
        line2Object.stringObjects = mLine2List;
        line2Object.maxTextSize = mLineTextSize;
        mDataList.add(line2Object);

        List<StringObject> mLine3List = new ArrayList<StringObject>();
        mLine3List.add(generateStringObject("Type",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine3List.add(generateStringObject("Industrial Android",mValueTextSize,mOffsetX,-1,false,null));
        LineObject line3Object = new LineObject();
        line3Object.stringObjects = mLine3List;
        line3Object.maxTextSize = mKeyTextSize;
        line3Object.blod = true;
        mDataList.add(line3Object);

        List<StringObject> mLine4List = new ArrayList<StringObject>();
        mLine4List.add(generateStringObject("Intelligent Terminal",mValueTextSize,mOffsetX,-1,false,null));
        LineObject line4Object = new LineObject();
        line4Object.stringObjects = mLine4List;
        line4Object.blod = true;
        line4Object.maxTextSize = mValueTextSize;
        mDataList.add(line4Object);

        List<StringObject> mLine5List = new ArrayList<StringObject>();
        mLine5List.add(generateStringObject("Time to market",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine5List.add(generateStringObject("Year 2015",mValue2TextSize,mOffsetX,-1,false,null));
        LineObject line5Object = new LineObject();
        line5Object.stringObjects = mLine5List;
        line5Object.maxTextSize = mKeyTextSize;
        line5Object.blod = true;
        mDataList.add(line5Object);

        List<StringObject> mLine6List = new ArrayList<StringObject>();
        mLine6List.add(generateStringObject("Dimension",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine6List.add(generateStringObject("205.5*87*39mm",mValue2TextSize,mOffsetX,-1,false,null));
        LineObject line6Object = new LineObject();
        line6Object.stringObjects = mLine6List;
        line6Object.maxTextSize = mKeyTextSize;
        line6Object.blod = true;
        mDataList.add(line6Object);

        List<StringObject> mLine7List = new ArrayList<StringObject>();
        mLine7List.add(generateStringObject("CPU",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine7List.add(generateStringObject("Quad-core",mValue2TextSize,mOffsetX,-1,false,null));
        LineObject line7Object = new LineObject();
        line7Object.stringObjects = mLine7List;
        line7Object.maxTextSize = mKeyTextSize;
        line7Object.blod = true;
        mDataList.add(line7Object);

        List<StringObject> mLine8List = new ArrayList<StringObject>();
        mLine8List.add(generateStringObject("Operation System",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine8List.add(generateStringObject("Android 5.1.1",mValue2TextSize,mOffsetX,-1,false,null));
        LineObject line8Object = new LineObject();
        line8Object.stringObjects = mLine8List;
        line8Object.maxTextSize = mKeyTextSize;
        line8Object.blod = true;
        mDataList.add(line8Object);

        List<StringObject> mLine9List = new ArrayList<StringObject>();
        mLine9List.add(generateStringObject("Thermal printer",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine9List.add(generateStringObject(" 2'' 384dots ",mValue2TextSize,mOffsetX,-1,false,null));
        LineObject line9Object = new LineObject();
        line9Object.stringObjects = mLine9List;
        line9Object.maxTextSize = mKeyTextSize;
        line9Object.blod = true;
        mDataList.add(line9Object);

        List<StringObject> mLine10List = new ArrayList<StringObject>();
        mLine10List.add(generateStringObject("thermal printer ",mValue2TextSize,mOffsetX,-1,false,null));
        LineObject line10Object = new LineObject();
        line10Object.stringObjects = mLine10List;
        line10Object.maxTextSize = mValue2TextSize;
        line10Object.blod = true;
        mDataList.add(line10Object);

        List<StringObject> mLine13List = new ArrayList<StringObject>();
        mLine13List.add(generateStringObject("Standby time",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine13List.add(generateStringObject("72hours",mValue2TextSize,mOffsetX,-1,false,null));
        LineObject line13Object = new LineObject();
        line13Object.stringObjects = mLine13List;
        line13Object.maxTextSize = mKeyTextSize;
        line13Object.blod = true;
        mDataList.add(line13Object);


        List<StringObject> mLine15List = new ArrayList<StringObject>();
        mLine15List.add(generateStringObject("Working temperature",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine15List.add(generateStringObject("-25~65℃",mKeyTextSize,-1,-1,false, printer.PrintType.Right));
        LineObject line15Object = new LineObject();
        line15Object.stringObjects = mLine15List;
        line15Object.maxTextSize = mKeyTextSize;
        line15Object.blod = true;
        mDataList.add(line15Object);

        List<StringObject> mLine16List = new ArrayList<StringObject>();
        mLine16List.add(generateStringObject("Storage temperature",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine16List.add(generateStringObject("-30~70 ℃",mKeyTextSize,-1,-1,false, printer.PrintType.Right));
        LineObject line16Object = new LineObject();
        line16Object.stringObjects = mLine16List;
        line16Object.maxTextSize = mKeyTextSize;
        line16Object.blod = true;
        mDataList.add(line16Object);

        List<StringObject> mLine17List = new ArrayList<StringObject>();
        mLine17List.add(generateStringObject("Humidity",mKeyTextSize,-1,-1,true, printer.PrintType.Left));
        mLine17List.add(generateStringObject("0~95%",mKeyTextSize,-1,-1,false, printer.PrintType.Right));
        LineObject line17Object = new LineObject();
        line17Object.stringObjects = mLine17List;
        line17Object.maxTextSize = mKeyTextSize;
        line17Object.blod = true;
        mDataList.add(line17Object);

        List<StringObject> mLine18List = new ArrayList<StringObject>();
        mLine18List.add(generateStringObject(spiltStr,mLineTextSize,-1,-1,false, printer.PrintType.Centering));
        LineObject line18Object = new LineObject();
        line18Object.stringObjects = mLine18List;
        line18Object.maxTextSize = mLineTextSize;
        mDataList.add(line18Object);



        String[][] dataArray = {
                {"Printer test"},
                {"~~~~~~~~~~~~~~~~~~~~~~~~"},
                {"Type", "Industrial Android"},
                {"", "Intelligent Terminal"},
                {"Time to market", "Year 2015"},
                {"Dimension", "205.5*87*39mm"},
                {"CPU", "Quad-core"},
                {"Operation System", "Android 4.2.2"},
                {"Thermal printer", " 2'' 384dots "},
                {"Thermal printer", "Android 4.2.2"},
                {"Standby time", "72hours"},
                {"Working temper", "-25~65℃"},
                {"Storage temperature","-30~70 ℃"},
                {"Humidity", "0~95%"}
        };

    }

    private void fillLineViews(LinearLayout card1ContentLayout){
        for(int i = 0; i < mDataList.size(); i++) {

            LineObject lineObject = mDataList.get(i);
            List<StringObject> stringObjects = lineObject.stringObjects;

            AbsoluteLayout absoluteLayout = new AbsoluteLayout(getActivity());

            if (i == 0) {
                mTitleObject = stringObjects.get(0);
                mEditText = new EditText(getActivity());
                mEditText.setTextColor(Color.BLACK);
                mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleObject.textSize);
                mEditText.setEnabled(false);
                Paint paint = mEditText.getPaint();
                paint.setTextSize(mTitleObject.textSize);
                paint.setFakeBoldText(mTitleObject.blod);

                AbsoluteLayout.LayoutParams params = generateLayoutParams(mEditText,mTitleObject,paint);
                if (params != null){
                    mEditText.setLayoutParams(params);
                }
                mEditText.setText(mTitleObject.content);
                absoluteLayout.addView(mEditText);

                mEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s != null) {
                            mTitleStr = s.toString();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateTitleViewLayoutParams();
                    }
                });

            } else {

                for (StringObject object:stringObjects){
                    TextView textView = new TextView(getActivity());
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, object.textSize);
                    Paint paint = textView.getPaint();
                    paint.setTextSize(object.textSize);
                    paint.setFakeBoldText(object.blod);

                    AbsoluteLayout.LayoutParams params = generateLayoutParams(textView,object,paint);
                    if (params != null){
                        textView.setLayoutParams(params);
                    }
                    textView.setText(object.content);
                    absoluteLayout.addView(textView);

                }

            }
            card1ContentLayout.addView(absoluteLayout);

        }
    }

    private AbsoluteLayout.LayoutParams generateLayoutParams(TextView view,StringObject object,Paint paint){
        int newY = 0;
        AbsoluteLayout.LayoutParams params = null;
        if (object.x != -1 && object.direct == null) {
            params = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, object.x, newY);
        } else if (object.direct == printer.PrintType.Left) {
            params = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, newY);
        } else if (object.direct == printer.PrintType.Right) {
            float width = paint.measureText(object.content);
            int x = 0;
            if (mCardWidth > width) {
                x = (int)(mCardWidth - width);
            }
            params = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, x, newY);

        } else if (object.direct == printer.PrintType.Centering) {
            float width = paint.measureText(object.content);
            int x = 0;
            if (mCardWidth > width) {
                x = (int)((mCardWidth - width)/2);
            }
            params = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, x, newY);
        }else if (object.direct == printer.PrintType.VerticalCentering) {

        } else if (object.direct == printer.PrintType.VerticalHorizontalCentering) {

        } else if (object.direct == printer.PrintType.TopCentering) {

        }else if (object.direct == printer.PrintType.LeftTop) {

        }else if (object.direct == printer.PrintType.RightTop) {

        }
        return params;
    }


    private StringObject generateStringObject(String content, int textSize, int x, int y, boolean blod, printer.PrintType direct){
        StringObject object = new StringObject();
        object.content =content;
        object.textSize = textSize;
        object.x = x;
        object.y = y;
        object.blod = blod;
        object.direct = direct;
        return object;
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_print:
                testPrint();

                break;
            case R.id.btn_step:
                mPrinter.Step((byte)0x5f);
                /**
                 *
                 * "\n"+"test"
                 * "\n"+"test"+"\n"
                 * "\n"+"test"+"\n"+"hello"
                 * "\n"+"testtttttttttttttttttttttttttttttttttttttttttttttttttttttt"
                 * "\n"+"testttttttttttttttttzzz"+"\n"+"btttttttttttttttttttttttttttttttttt"
                 * "\n"+"testtttttttt"+"\n"+"ttttttttzzzbtttttttttttttttttttttttttttttttttt"
                 * "testsdlewklwekldfvlsklsdkewoweopssdoxoklqwlwpqwwpwpwlkdjdieke,eeeoeoeoeo"
                 * "\n"+"testsdlewklwekldfvlsklsdkewoweopssdoxoklqwlwpqwwpwpwlkdjdieke,eeeoeoeoeo"+"\n"
                 * "testsdlewklwekldfvlsklsdkewowe"+"\n"+"opssdoxoklqwlwpqwwpwpwlkdjdieke,eeeoeoeoeo"
                 * "test"+"\n"
                 */
                break;
//            case R.id.toggleButton:
//                String str = "isOutOfPaper: N/A      isOverTemper: N/A";
//                if (mToggleButton.isChecked()) {
//                    int result = mPrinter.Open();
//                    if (result == 0){
//                        mPrintButton.setEnabled(true);
//                        mStepButton.setEnabled(true);
//                        mBlodView.setAlpha(1f);
//                        mTextSizeView.setAlpha(1f);
//                        mAlignView.setAlpha(1f);
//                        mGrayView.setAlpha(1f);
//                        mCard1ContentLayout.setAlpha(1f);
//                        str = "The Printer is opened";
//                    } else {
//                        str = "Open the Printer fail";
//                        Toast.makeText(getActivity(),"Open the Printer fail",Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    int result = mPrinter.Close();
//                    if (result == 0){
//                        mPrintButton.setEnabled(false);
//                        mStepButton.setEnabled(false);
//                        mBlodView.setAlpha(0.3f);
//                        mTextSizeView.setAlpha(0.3f);
//                        mAlignView.setAlpha(0.3f);
//                        mGrayView.setAlpha(0.3f);
//                        mCard1ContentLayout.setAlpha(0.3f);
//                        str = "The Printer is closed";
//                    } else {
//                        str = "Close the Printer fail";
//                        Toast.makeText(getActivity(),"Close the Printer fail",Toast.LENGTH_SHORT).show();
//                    }
//                }
//                mStatusTextView.setText(str);
//                break;
        }
    }

    private void testPrint() {

        // TODO Auto-generated method stub
        mPrinter.PrintStringEx(mTitleStr, mTitleTextSize, false, titleBold,mTitleType);
        String str="~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
        //mPrinter.PrintString(str, mValueTextSize);
        mPrinter.PrintLineInit(mLineTextSize);
        mPrinter.PrintLineStringByType(str, mLineTextSize, printer.PrintType.Centering, false);
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("Type", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineString("Industrial Android ", mValueTextSize, mOffsetX, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineString("Intelligent Terminal", mValueTextSize, mOffsetX, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("Time to market", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineString("Year 2015", mValue2TextSize, mOffsetX, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("Dimension", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineString("205.5*87*39mm", mValue2TextSize, mOffsetX, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("CPU", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineString("Quad-core", mValue2TextSize, mOffsetX, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("Operation System", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineString("Android 5.1.1", mValue2TextSize, mOffsetX, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("Thermal printer", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineString(" 2'' 384dots ", mValue2TextSize, mOffsetX, false);//160
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(mKeyTextSize);
        //mPrinter.PrintLineString("Thermal printer", mKeyTextSize, PrintType.Left, true);
        mPrinter.PrintLineString("thermal printer ", mValue2TextSize, mOffsetX, false);//160
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(mKeyTextSize);

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("Standby time", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineString("72hours", mValue2TextSize, mOffsetX, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mLineTextSize);
//        mPrinter.PrintLineString(str, mLineTextSize, PrintType.Centering, true);
//        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("Working temperature", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineStringByType("-25~65℃", mKeyTextSize, printer.PrintType.Right, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("Storage temperature", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineStringByType("-30~70 ℃", mKeyTextSize, printer.PrintType.Right, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mKeyTextSize);
        mPrinter.PrintLineStringByType("Humidity", mKeyTextSize, printer.PrintType.Left, true);
        mPrinter.PrintLineStringByType("0~95%", mKeyTextSize, printer.PrintType.Right, false);//160
        mPrinter.PrintLineEnd();

        mPrinter.PrintLineInit(mLineTextSize);
        mPrinter.PrintLineStringByType(str, mLineTextSize, printer.PrintType.Centering, false);
        mPrinter.PrintLineEnd();
        //Bitmap bm=BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        Bitmap bm=null;
        try {
            bm = BarcodeUtil.encodeAsBitmap("Thanks for using our Android terminal",
                    BarcodeFormat.QR_CODE, 80, 80);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        if(bm!=null)
        {
            mPrinter.PrintBitmap(bm);
        }
        mPrinter.PrintLineInit(40);
        mPrinter.PrintLineStringByType("", mKeyTextSize, printer.PrintType.Right, true);//160
        mPrinter.PrintLineEnd();

        mPrinter.printBlankLine(40);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String str = "isOutOfPaper: N/A      isOverTemper: N/A";
        if (isChecked) {
            int result = mPrinter.Open();
            if (result == 0){
                mPrintButton.setEnabled(true);
                mStepButton.setEnabled(true);
                mBlodView.setAlpha(1f);
                mTextSizeView.setAlpha(1f);
                mAlignView.setAlpha(1f);
                mGrayView.setAlpha(1f);
                mCard1ContentLayout.setAlpha(1f);
                str = getResources().getString(R.string.printer_opened);
            } else {
                str = getString(R.string.printer_opene_fail);
                Toast.makeText(getActivity(),str,Toast.LENGTH_SHORT).show();
            }
        } else {
            int result = mPrinter.Close();
            if (result == 0){
                mPrintButton.setEnabled(false);
                mStepButton.setEnabled(false);
                mBlodView.setAlpha(0.3f);
                mTextSizeView.setAlpha(0.3f);
                mAlignView.setAlpha(0.3f);
                mGrayView.setAlpha(0.3f);
                mCard1ContentLayout.setAlpha(0.3f);
                str = getResources().getString(R.string.printer_closed);
            } else {
                str = getString(R.string.printer_close_fail);
                Toast.makeText(getActivity(),str,Toast.LENGTH_SHORT).show();
            }
        }
        mStatusTextView.setText(str);
    }
}
