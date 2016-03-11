package com.jorge.flowlayoutdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/11.
 */
public class FlowLayout extends ViewGroup {


    /** 当前行已用的宽度，由子View宽度加上横向间隔 */
    private int mUsedWidth = 0;
    /** 代表一行 */
    private Line mLine = null;
    /** 默认间隔 */
    public static final int DEFAULT_SPACING = 20;
    /** 横向间隔 */
    private int mHorizontalSpacing = DEFAULT_SPACING;
    /** 纵向间隔 */
    private int mVerticalSpacing = DEFAULT_SPACING;
    /** 代表行的集合 */
    private final List<Line> mLines = new ArrayList<Line>();
    /** 默认的最大的行数 */
    private int mMaxLinesCount = Integer.MAX_VALUE;
    /** 是否让子view充满该行 */
    private boolean fillLine = false;
    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    /**是否填充满改行，如果是false，右边有空间*/
    public void setFillLine(boolean fill) {
        fillLine = fill;
    }
    // 设置两个view水平间距
    public void setHorizontalSpacing(int spacing) {
        if (mHorizontalSpacing != spacing) {
            mHorizontalSpacing = spacing;
            requestLayout();
        }
    }
    /** 设置纵向间距*/
    public void setVerticalSpacing(int spacing) {
        if (mVerticalSpacing != spacing) {
            mVerticalSpacing = spacing;
            requestLayout();
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 1获取with height 以及mode
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec)
                - getPaddingBottom() - getPaddingTop();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec)
                - getPaddingLeft() - getPaddingRight();
        restoreLine();
        int count = getChildCount();
        // 2 测量子View
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            int widthSpec = MeasureSpec.makeMeasureSpec(widthSize,
                    widthMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST
                            : widthMode);
            int heightSpec = MeasureSpec.makeMeasureSpec(heightSize,
                    heightMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST
                            : heightMode);

            child.measure(widthSpec, heightSpec);
            if (mLine == null) {
                mLine = new Line();
            }
            // 将childview 添加到每一行中
            int childWidth = child.getMeasuredWidth();
            // 当前行已经占用的宽度
            mUsedWidth += childWidth;
            if (mUsedWidth < widthSize) {
                // 当前行还没有达到上限，那么该child就添加进这一行
                mLine.addView(child);
                mUsedWidth += mHorizontalSpacing; // 添加上两个子View之间水平方向的间隔
            } else {
                // 说明长度超出了当前的最大宽度
                if (mLine.getViewCount() == 0) {
                    // 表示当前行中还没有元素，添加的第一个元素 长度就超过了最大宽度，那么也要把该child 添加进去保证有数据
                    mLine.addView(child);
                    // 同时换行
                } else {
                    // 表示当前行中已经有元素，那么换一行，添加进去
                    newLine();
                    mLine.addView(child);
                    // 改变已使用的宽度
                    mUsedWidth += mHorizontalSpacing + childWidth;
                }

            }
        }

        // 前面只有换行的时候才将Line 添加到lines 集合中，这里要判断一下最后一行，将最后一行也添加进去
        if (mLine != null && mLine.getViewCount() > 0
                && !mLines.contains(mLine)) {
            // 表示有数据
            mLines.add(mLine);
        }

        // 设置测量的宽高setMeasuredDimension
        int totoalHeight = 0;
        for (int i = 0; i < mLines.size(); i++) {
            totoalHeight += mLines.get(i).mHeight;// N行的高度
        }
        // 加上 行间距
        totoalHeight += (mLines.size() - 1) * mVerticalSpacing;
        // 加上padding
        totoalHeight += getPaddingBottom() + getPaddingTop();
        // 设置FlowLayout的宽度值 高度值 宽度就是默认的宽度，高度是总的高度
        int measuredHeight = resolveSize(totoalHeight, heightMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 主要是调用child.layout
        int count = mLines.size();
        int left = getPaddingLeft();
        int top = getPaddingTop();

        for (int i = 0; i < count; i++) {
            Line line = mLines.get(i);
            line.layout(left, top);
            top += mVerticalSpacing + line.mHeight;
        }
    }


    /** 还原所有数据 */
    private void restoreLine() {
        mLines.clear();
        mLine = new Line();
        mUsedWidth = 0;
    }

    /** 新增加一行 */
    private boolean newLine() {
        mLines.add(mLine);
        if (mLines.size() < mMaxLinesCount) {
            mLine = new Line();
            mUsedWidth = 0;
            return true;
        }
        return false;
    }

    /***
     * 代表着一行，封装了一行所占高度，该行子View的集合，以及所有View的宽度总和
     *
     * @author Administrator
     *
     */
    public class Line {

        int mWidth = 0;// 该行中所有的子View累加的宽度
        int mHeight = 0;// 该行中所有的子View中高度最高的那个子View的高度

        List<View> views = new ArrayList<View>();// 存放一行中的View

        public void addView(View child) {
            views.add(child);
            mWidth += child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            mHeight = mHeight < childHeight ? childHeight : mHeight;// 高度等于一行中最高的View
        }

        /***
         * layout 子view
         * @param l
         * @param t
         */
        public void layout(int l, int t) {
            int left = l;
            int top = t;
            // 父布局的宽度
            int totoalWidth = getMeasuredWidth() - getPaddingLeft()
                    - getPaddingRight();
            // 当前line 中view的个数
            int count = getViewCount();
            // 剩余空间平分给每个View
            int spaceLast = totoalWidth - mWidth - (count - 1)
                    * mHorizontalSpacing;
            int averageWidth = spaceLast / count;
            // 平分的宽度
            // int splitSpacing = (int) (spaceLast / count + 0.5);
            for (int i = 0; i < count; i++) {
                View child = views.get(i);
                int childHeight = child.getMeasuredHeight();
                int childWidth = child.getMeasuredWidth();

                if (fillLine) {// 要充满该行
                    childWidth += averageWidth;
                    child.getLayoutParams().width = childWidth;
                    // 改变了原来的宽高，重新测量一次
                    int widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            childWidth, MeasureSpec.EXACTLY);
                    int heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            childHeight, MeasureSpec.EXACTLY);
                    // 再次测量
                    child.measure(widthMeasureSpec, heightMeasureSpec);

                }
                // 布局View
                child.layout(left, top, left + childWidth, top + childHeight);
                left += childWidth + mHorizontalSpacing; // 为下一个View的left赋值
            }
        }

        /**
         * 该行中view的个数
         * @return
         */
        public int getViewCount() {
            return views.size();
        }

    }
}
