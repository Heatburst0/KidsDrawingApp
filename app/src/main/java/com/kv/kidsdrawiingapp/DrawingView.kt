package com.kv.kidsdrawiingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.lang.reflect.Type
import java.util.*

import java.util.jar.Attributes

class DrawingView(context : Context, attrs : AttributeSet) : View(context,attrs) {
    private var mDrawPath: CustomPath? =
        null // An variable of CustomPath inner class to use it further.
    private var mCanvasBitmap: Bitmap? = null // An instance of the Bitmap.

    private var mDrawPaint: Paint? =
        null // The Paint class holds the style and color information about how to draw geometries, text and bitmaps.
    private var mCanvasPaint: Paint? = null // Instance of canvas paint view.

    private var mBrushSize: Float =
        0.toFloat() // A variable for stroke/brush size to draw on the canvas.
    private val myPaths = Stack<CustomPath>()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private var undoPaths = Stack<CustomPath>()
    fun clear(){
        myPaths.clear()
        invalidate()
    }
    fun undo(){
        if(!myPaths.isEmpty()){
            undoPaths.push(myPaths.pop())
            invalidate()
        }

    }
    fun redo(){
        if(!undoPaths.isEmpty()){
            myPaths.push(undoPaths.pop())
            invalidate()
        }
    }

    init {
        setUpDrawing()
    }
    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint?.color = color
        mDrawPaint?.style = Paint.Style.STROKE // This is to draw a STROKE style
        mDrawPaint?.strokeJoin = Paint.Join.ROUND // This is for store join
        mDrawPaint?.strokeCap = Paint.Cap.ROUND // This is for stroke Cap
        mCanvasPaint = Paint(Paint.DITHER_FLAG) // Paint flag that enables dithering when blitting.
//        mBrushSize =20.0f

    }

    override fun onSizeChanged(w: Int, h: Int, wprev: Int, hprev: Int) {
        super.onSizeChanged(w, h, wprev, hprev)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)
        for(path in myPaths){
            mDrawPaint?.strokeWidth = path.brushThickness
            mDrawPaint?.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }
        if (!mDrawPath!!.isEmpty) {
            mDrawPaint?.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint?.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x // Touch event of X coordinate
        val touchY = event.y // touch event of Y coordinate

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath?.color = color
                mDrawPath?.brushThickness = mBrushSize
                mDrawPath?.reset()
                mDrawPath?.moveTo(
                    touchX,
                    touchY
                )
            }

            MotionEvent.ACTION_MOVE -> {
                mDrawPath?.lineTo(
                    touchX,
                    touchY
                )
            }

            MotionEvent.ACTION_UP -> {
                myPaths.push(mDrawPath)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }
    fun setBrushSize(value : Float) {
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            value, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }
    fun setColor(x : String){
        color = Color.parseColor(x)
        mDrawPaint!!.color=color
    }


    internal inner class CustomPath(var color:Int,var brushThickness:Float):Path(){
    }
}