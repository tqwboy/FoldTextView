package com.tqw.foldtextview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Hohenheim on 2016/11/24.
 */

public class FoldTextView extends FrameLayout implements OnClickListener {
    private TextView mContentTv;
    private ImageView mArrowImg;

    private int mMaxLines = 3;
    private boolean mIsExpanded;
    private boolean mIsExpanding;

    public FoldTextView(Context context) {
        this(context, null, 0);
    }

    public FoldTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_fold_textview, this, true);

        view.findViewById(R.id.fold_tv_layoout).setOnClickListener(this);
        mContentTv = (TextView) view.findViewById(R.id.content_tv);
        mArrowImg = (ImageView) view.findViewById(R.id.arrow_img);

        if(null != attrs) {
            Resources.Theme layoutTheme = context.getTheme();
            TypedArray attributeArray = layoutTheme.obtainStyledAttributes(attrs,
                    R.styleable.FoldTextView, defStyleAttr, 0);

            int attributeCount = attributeArray.getIndexCount();
            for(int i=0; i<attributeCount; ++i) {
                int attr = attributeArray.getIndex(i);

                if(attr == R.styleable.FoldTextView_android_maxLines) {
                    mMaxLines = attributeArray.getInt(i, 3);
                }
                else if(attr == R.styleable.FoldTextView_android_text) {
                    String txt = attributeArray.getString(i);
                    if(TextUtils.isEmpty(txt)) {
                        mArrowImg.setVisibility(View.GONE);
                    }
                    else {
                        setText(txt);
                    }
                }
                else if(attr == R.styleable.FoldTextView_android_textSize) {
                    float txtSize = attributeArray.getDimensionPixelSize(i, 0);
                    if(txtSize <= 0) {
                        txtSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                                16, getResources().getDisplayMetrics());
                    }
                    mContentTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
                }
                else if(attr == R.styleable.FoldTextView_android_textColor) {
                    int color = attributeArray.getColor(i, Color.BLACK);
                    mContentTv.setTextColor(color);
                }
                else if(attr == R.styleable.FoldTextView_arrowSrc) {
                    int resId = attributeArray.getResourceId(i, R.drawable.arrow);
                    mArrowImg.setImageResource(resId);
                }
            }

            attributeArray.recycle();
        }
    }

    @Override
    public void onClick(View view) {
        if(mIsExpanding)
            return;

        mIsExpanding = true;

        mContentTv.clearAnimation(); //清除动画效果
        mArrowImg.clearAnimation();

        final int expandHeight;
        final int startHeight = mContentTv.getHeight(); //起始高度
        int durationMillis = 100; //动画持续时间

        RotateAnimation arrowAnimation;

        if(mIsExpanded) {
            //折叠动画,从实际高度缩回起始高度
            expandHeight = mContentTv.getLineHeight() * mMaxLines - startHeight;
            arrowAnimation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }
        else {
            //展开动画,从起始高度增长至实际高度
            expandHeight = mContentTv.getLineHeight() * mContentTv.getLineCount() - startHeight;
            arrowAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }

        Animation txtAanimation = new Animation() {
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //根据动画的百分比来显示textview高度，达到动画效果
                mContentTv.setHeight((int) (startHeight + expandHeight * interpolatedTime));
            }
        };
        txtAanimation.setDuration(durationMillis);
        txtAanimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //TODO
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsExpanding = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //TODO
            }
        });

        arrowAnimation.setDuration(durationMillis);
        arrowAnimation.setFillAfter(true);

        mArrowImg.startAnimation(arrowAnimation);
        mContentTv.startAnimation(txtAanimation);

        mIsExpanded = !mIsExpanded;
    }

    public void setText(CharSequence text) {
        mContentTv.setText(text);
        mContentTv.setHeight(mContentTv.getLineHeight() * mMaxLines);

        post(new Runnable() {
            @Override
            public void run() {
                mArrowImg.setVisibility(mContentTv.getLineCount()>mMaxLines ? View.VISIBLE:View.GONE);
            }
        });
    }
}