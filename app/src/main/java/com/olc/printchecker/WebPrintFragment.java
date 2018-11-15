package com.olc.printchecker;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.Locale;

import hardware.print.printer;

/************************************************************
 * Copyright 2000-2066 Olc Corp., Ltd.
 * All rights reserved.
 * Description     : The Main activity for  PrintChecker
 * History        :( ID, Date, Author, Description)
 * v1.0, 2017/3/17,  zhangyong, create
 ************************************************************/

public class WebPrintFragment extends Fragment {

    private WebView mWebView;
    printer mPrinter = new printer();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.activity_web_print, null);
        initView(mView);
        return mView;
    }

    private void initView(View mView) {
        mWebView = (WebView) mView.findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new PrintObject(), "connect");
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        //        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            mWebView.loadUrl("file:///android_asset/print.html");
        else
            mWebView.loadUrl("file:///android_asset/print_en.html");

        try {
            mPrinter.Open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PrintObject {
        @JavascriptInterface
        public void doPrint(String text) {
            final String value = text;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "print text: " + value, Toast.LENGTH_SHORT).show();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mPrinter.PrintLineInit(23);
                    mPrinter.PrintLineStringByType(value, 23, printer.PrintType.Centering, false);
                    mPrinter.PrintLineEnd();
                }
            }).start();
        }

        @JavascriptInterface
        public void doPrintImage(String path) {
            final String value = path.replace("file:///android_asset/", "");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "print image  : " + value, Toast.LENGTH_SHORT).show();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap bm = null;
                        AssetManager am = getResources().getAssets();
                        bm = BitmapFactory.decodeStream(am.open(value));
                        if (bm != null) {
                            mPrinter.PrintBitmap(bm);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Error error) {
                        error.printStackTrace();
                    }
                }
            }).start();
        }

        @JavascriptInterface
        public void print(final int x, final int y, final int width, final int height) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Picture pic = mWebView.capturePicture();
                        int nw = pic.getWidth();
                        int nh = pic.getHeight();
                        Bitmap bitmap = Bitmap.createBitmap(nw, nh, Bitmap.Config.ARGB_4444);
                        Canvas can = new Canvas(bitmap);
                        pic.draw(can);
                        Utils.stroageBitmap(bitmap);

                        int newWidth = nw;
                        int newHeight = nh;
                        if (nw > 400) {
                            float rate = 400 * 1.0f / nw * 1.0f;
                            newWidth = 400;
                            newHeight = (int) (nh * rate);
                        }
                        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                        //                        Utils.stroageBitmap(bitmap);
                        Bitmap newBitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
                        Utils.stroageBitmap(newBitmap);
                        mPrinter.PrintBitmap(newBitmap);

                        mPrinter.printBlankLine(40);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Error error) {
                        error.printStackTrace();
                    }
                }
            });


        }

        @JavascriptInterface
        public void doFeed() {
            mPrinter.Step((byte) 0x5f);
        }
    }


}
