package com.rom1v.aimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Generalization of the standard {@link android.widget.ImageView} component.
 * <p/>
 * It always preserves the aspect-ratio, and generalizes the {@link android.widget
 * .ImageView.ScaleType scaleType} mechanism by using 4 parameters:
 * <ul>
 * <li><code>xWeight</code> and <code>yWeight</code> indicating where the crop or margins must be
 * applied.</li>
 * <li><code>scale</code> for enabling/disabling downscaling and/or upscaling</li>
 * <li><code>fit</code> for choosing if the aspect-ratio must be respecting by adding margins
 * (displaying the whole image) or by cropping (filling the component)</li>
 * </ul>
 * <p/>
 * See {@link com.rom1v.aimageview.R.styleable#AImageView AImageView attributes}.
 *
 * @attr ref R.styleable#AImageView_xWeight
 * @attr ref R.styleable#AImageView_yWeight
 * @attr ref R.styleable#AImageView_scale
 * @attr ref R.styleable#AImageView_fit
 */
public class AImageView extends ImageView {

    /**
     * Fit type, defining how to fit the image to the container.
     */
    public enum Fit {

        /** Fit the image inside (add margins to display the whole image content). */
        INSIDE,

        /** Fit the image outside (crop the image to fill the component). */
        OUTSIDE,

        /** Fit the image horizontally */
        HORIZONTAL,

        /** Fit the image vertically */
        VERTICAL;

        /**
         * Convert the attr value to a typed {@link Fit} value.
         *
         * @param attrValue value defined in <code>attrs.xml</code>.
         * @return the associated {@link Fit} instance
         * @throws java.lang.IllegalArgumentException if the value is unknown
         */
        private static Fit fromAttrValue(int attrValue) {
            switch (attrValue) {
                case 0:
                    return INSIDE;
                case 1:
                    return OUTSIDE;
                case 2:
                    return HORIZONTAL;
                case 3:
                    return VERTICAL;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    /** Flag indicating that downscaling is acceptable. */
    public static final int DOWNSCALE = 1 << 0;

    /** Flag indicating that upscaling is acceptable. */
    public static final int UPSCALE = 1 << 1;

    /** x weight (in [0;1]). */
    private float xWeight;

    /** y weight (in [0;1]). */
    private float yWeight;

    /** Scale flags (bitwise of {@link #DOWNSCALE} and {@link #UPSCALE}). */
    private int scaleFlags;

    /** Fit type. */
    private Fit fit;

    public AImageView(Context context) {
        super(context);
        forceMatrixScaleType();
    }

    public AImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        forceMatrixScaleType();
    }

    public AImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        forceMatrixScaleType();
    }

    private void forceMatrixScaleType() {
        // this component works only with scaleType MATRIX
        setScaleType(ScaleType.MATRIX);
    }

    private void init(Context context, AttributeSet attrs) {
        // load values from XML attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.AImageView, 0, 0);
        try {
            setXWeight(a.getFloat(R.styleable.AImageView_xWeight, .5f));
            setYWeight(a.getFloat(R.styleable.AImageView_yWeight, .5f));

            int fitInt = a.getInt(R.styleable.AImageView_fit, 0);
            setFit(Fit.fromAttrValue(fitInt));

            setScaleFlags(a.getInt(R.styleable.AImageView_scale, UPSCALE | DOWNSCALE));
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (getScaleType() != ScaleType.MATRIX) {
            // use the default ImageView behavior
            return;
        }
        Drawable drawable = getDrawable();
        if (drawable == null) {
            // nothing to draw
            return;
        }

        int w = right - left - getPaddingLeft() - getPaddingRight();
        int h = bottom - top - getPaddingTop() - getPaddingBottom();
        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();

        // static method for easier testing
        Matrix matrix = getTransformMatrix(w, h, imageWidth, imageHeight, xWeight, yWeight, fit,
                scaleFlags);
        setImageMatrix(matrix);
    }

    static Matrix getTransformMatrix(int w, int h, int imageWidth, int imageHeight, float xWeight,
                                     float yWeight, Fit fit, int scaleFlags) {
        Matrix matrix = new Matrix();

        float scale;
        if (scaleFlags == 0) {
            // do not scale
            scale = 1;
        } else {
            // the image has a greater aspect-ratio than the container ?
            boolean imageHasGreaterAR = imageWidth * h > imageHeight * w;
            if (fit == Fit.HORIZONTAL || imageHasGreaterAR && fit == Fit.INSIDE ||
                    !imageHasGreaterAR && fit == Fit.OUTSIDE) {
                // fit the width
                scale = (float) w / imageWidth;
            } else {
                // fit == Fit.VERTICAL || !imageHasGreaterAR && fit == Fit.INSIDE ||
                // imageHasGraterAR && fit == Fit.OUTSIDE

                // fit the height
                scale = (float) h / imageHeight;
            }
            if (scale > 1f && (scaleFlags & UPSCALE) == 0 || scale < 1f && (scaleFlags &
                    DOWNSCALE) == 0) {
                // respect scaling constraints
                scale = 1;
            } else if (scale < 0) {
                // may happen when w or h is negative, i.e. when the padding is greater than
                // the component; in that case, we do not want to reverse the image
                scale = 0;
            }
        }

        // resize proportionally
        matrix.setScale(scale, scale);

        // translate according to weights
        float tx = xWeight * (w - imageWidth * scale);
        float ty = yWeight * (h - imageHeight * scale);
        matrix.postTranslate(tx, ty);

        return matrix;
    }

    /**
     * Return the x weight.
     *
     * @return the x weight
     */
    public float getXWeight() {
        return xWeight;
    }

    /**
     * Set the x weight.
     * <p/>
     * This value must be in range {@code [0;1]}.
     * <p/>
     * A value of {@code 0} (resp. {@code 1}) indicates that the image and the component
     * must be bound to their left (resp. right). Intermediate values are a linear
     * interpolation of these two cases.
     * <p/>
     * Typically, a value of {@code 0.5} centers the crop or the margins horizontally.
     *
     * @param xWeight the x weight
     * @throws java.lang.IllegalArgumentException if {@code xWeight} is not between 0 and 1
     *                                            (inclusive)
     */
    public void setXWeight(float xWeight) {
        if (xWeight < 0 || xWeight > 1) {
            throw new IllegalArgumentException("xWeight must be in [0;1]: " + xWeight);
        }
        if (this.xWeight == xWeight) {
            return;
        }
        this.xWeight = xWeight;
        requestLayout();
    }

    /**
     * Return the y weight.
     *
     * @return the y weight
     */
    public float getYWeight() {
        return yWeight;
    }

    /**
     * Set the y weight.
     * <p/>
     * This value must be in range {@code [0;1]}.
     * <p/>
     * A value of {@code 0} (resp. {@code 1}) indicates that the image and the component
     * must be bound to their top (resp. bottom). Intermediate values are a linear
     * interpolation of these two cases.
     * <p/>
     * Typically, a value of {@code 0.5} centers the crop or the margins vertically.
     *
     * @param yWeight the y weight
     * @throws java.lang.IllegalArgumentException if {@code yWeight} is not between 0 and 1
     *                                            (inclusive)
     */
    public void setYWeight(float yWeight) {
        if (yWeight < 0 || yWeight > 1) {
            throw new IllegalArgumentException("yWeight must be in [0;1]: " + yWeight);
        }
        if (this.yWeight == yWeight) {
            return;
        }
        this.yWeight = yWeight;
        requestLayout();
    }

    /**
     * Return the scale flags (bitwise of {@link #DOWNSCALE} and {@link #UPSCALE}).
     *
     * @return the scale flags
     */
    public int getScaleFlags() {
        return scaleFlags;
    }

    /**
     * Set the scale flags.
     *
     * @param scaleFlags the scale flags
     * @throws java.lang.IllegalArgumentException if bits other than {@link #DOWNSCALE} and {@link
     *                                            #UPSCALE} are set
     */
    public void setScaleFlags(int scaleFlags) {
        if ((scaleFlags & (DOWNSCALE | UPSCALE)) != scaleFlags) {
            throw new IllegalArgumentException("Only DOWNSCALE and UPSCALE flags can be set");
        }
        if (this.scaleFlags == scaleFlags) {
            return;
        }
        this.scaleFlags = scaleFlags;
        requestLayout();
    }

    /**
     * Returns the downscale flag.
     *
     * @return the downscale flag
     */
    public boolean getDownscale() {
        return (scaleFlags & DOWNSCALE) != 0;
    }

    /**
     * Set the downscale flag.
     * <p/>
     * The image can be downscaled if and only if this flag is enabled.
     *
     * @param downscale the downscale flag
     */
    public void setDownscale(boolean downscale) {
        if (downscale == getDownscale()) {
            return;
        }
        if (downscale) {
            scaleFlags |= DOWNSCALE;
        } else {
            scaleFlags &= ~DOWNSCALE;
        }
        requestLayout();
    }

    /**
     * Return the upscale flag.
     *
     * @return the upscale flag
     */
    public boolean getUpscale() {
        return (scaleFlags & UPSCALE) != 0;
    }

    /**
     * Set the upscale flag.
     * <p/>
     * The image can be upscaled if and only if this flag is enabled.
     *
     * @param upscale the upscale flag
     */
    public void setUpscale(boolean upscale) {
        if (upscale == getUpscale()) {
            return;
        }
        if (upscale) {
            scaleFlags |= UPSCALE;
        } else {
            scaleFlags &= ~UPSCALE;
        }
        requestLayout();
    }

    /**
     * Get the {@code Fit} type.
     *
     * @return the fit type
     */
    public Fit getFit() {
        return fit;
    }

    /**
     * Fit the image {@link Fit#INSIDE} (add margins to display the whole image content) or {@link
     * Fit#OUTSIDE} (crop the image to fill the component).
     * <p/>
     * Has no effect if scale is {@code disabled} (i.e. if {@code {@link #getScaleFlags()} == 0}).
     *
     * @param fit the fit type
     */
    public void setFit(Fit fit) {
        if (fit == null) {
            throw new IllegalArgumentException("The fit value cannot be null");
        }
        if (this.fit == fit) {
            return;
        }
        this.fit = fit;
        requestLayout();
    }
}
