package com.aseproject.tensorflow;///*

import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;

import com.mywebsite.arthree.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;


public class OcrDetectorProcessor
        implements Detector.Processor<TextBlock>
{

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    public TextView tvv;

    OcrDetectorProcessor(
            GraphicOverlay<OcrGraphic> ocrGraphicOverlay, TextView tvv) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.tvv=tvv;
    }


    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
            }
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            mGraphicOverlay.add(graphic);
            sb.append(item.getValue());
            sb.append(" ");
        }
        final String sbs=sb.toString();
        if(sbs.length()>6) {
            //tvv.setText(sbs);
            tvv.post(new Runnable() {
                public void run() {
                    tvv.setText(sbs);
                }
            });
            Global.txt=sbs;
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
