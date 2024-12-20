package com.easyotpview.easyotpview

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.MovementMethod
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.easyotpview.R
import com.easyotpview.defaultMethod.EasyOtpMovementMethod
import com.easyotpview.listeners.EasyOtpViewCompleteListener
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 *
 * @author Tech Brain
 * All copy write are reserved
 *
 * EasyOTPView: A customizable OTP input view for Android applications.
 * This component allows users to enter OTP (One-Time Password) securely.
 * It supports customizable UI elements, input validation, and focus management.
 *
 */

open class EasyOtpView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.easyOtpStyle
) :
    AppCompatEditText(context, attrs, defStyleAttr) {
    private val viewType: Int
    private var easyOtpViewItemCount: Int
    private var easyOtpViewItemWidth: Int
    private var easyOtpViewItemHeight: Int
    private var easyOtpViewItemRadius: Int
    private var easyOtpViewItemSpacing: Int
    private val paint: Paint
    private val animatorTextPaint: TextPaint = TextPaint()


    /**
     * This property holds the line colors for the different states (normal, selected, focused) of the OtpView.
     * The ColorStateList allows you to define different colors based on the state of the view.
     *
     * @attr ref R.styleable#OtpView_lineColor
     *      This attribute references the line color settings from XML, allowing customization.
     *
     * @see .setLineColor
     *      This method is used to set the line color for the OtpView based on a ColorStateList.
     * @see .getLineColor
     *      This method is used to retrieve the current line color for the OtpView.
     */
    private var lineColors: ColorStateList?


    /**
     * Retrieves the current color selected for the normal line state of the OtpView.
     * This color is applied when the view is in its default or unselected state.
     *
     * @return Returns the current line color for the normal state, which is the default color when the OTP view is not focused or selected.
     */
    @get:ColorInt
    var currentLineColor = Color.BLACK
        private set
    private var lineWidth: Int
    private val textRect = Rect()
    private val itemBorderRect = RectF()
    private val itemLineRect = RectF()
    private val path = Path()
    private val itemCenterPoint = PointF()
    private var defaultAddAnimator: ValueAnimator? = null
    private var isAnimationEnable = false
    private var blink: Blink? = null
    private var isCursorVisible: Boolean
    private var drawCursor = false
    private var cursorHeight = 0f
    private var cursorWidth: Int
    private var cursorColor: Int
    private var itemBackgroundResource = 0
    private var itemBackground: Drawable?
    private var hideLineWhenFilled: Boolean
    private val rtlTextDirection: Boolean
    private var maskingChar: String?
    private var easyOtpCompletionListener: EasyOtpViewCompleteListener? = null


    /**
     * Drawable for the background of the item when it is filled (e.g., when OTP input is completed).
     * This drawable is used to represent the filled state of the item.
     */
    private var itemBackgroundFilled: Drawable? = null

    /**
     * Drawable for the background of the item when it is unfilled (e.g., when OTP input is in progress).
     * This drawable is used to represent the unfilled state of the item.
     */
    private var itemBackgroundUnfilled: Drawable? = null

    /**
     * Color for the line of the item when it is filled (e.g., when OTP is entered and validated).
     * This color is applied to the line in the filled state.
     */
    private var filledLineColor: Int = Color.BLACK

    /**
     * Color for the line of the item when it is unfilled (e.g., when OTP is not entered).
     * This color is applied to the line in the unfilled state.
     */
    private var unfilledLineColor: Int = Color.GRAY

    /**
     * Color for the rectangle background of the item when it is filled.
     * This color is applied to the filled rectangle background.
     */
    private var filledRectangleColor: Int = Color.BLACK

    /**
     * Color for the rectangle background of the item when it is unfilled.
     * This color is applied to the unfilled rectangle background.
     */
    private var unfilledRectangleColor: Int = Color.GRAY


    /**
     * Sets the typeface (font) for the text of the view.
     *
     * This method overrides the default implementation to allow for custom typeface settings.
     * It also updates the `animatorTextPaint` with the current paint used by the view, if it is not null.
     *
     * @param tf The Typeface to be set for the view's text.
     */
    override fun setTypeface(tf: Typeface?) {
        super.setTypeface(tf)
        if(animatorTextPaint != null){
            animatorTextPaint.set(getPaint())
        }

    }



    /**
     * Sets the maximum length for the input field.
     * This method updates the filters applied to the input, restricting the number of characters
     * the user can enter based on the specified `maxLength`.
     *
     * @param maxLength The maximum number of characters allowed in the input field.
     *                  If the value is greater than or equal to 0, the input will be restricted
     *                  to this length. If the value is negative, no filter is applied.
     */
    private fun setMaxLength(maxLength: Int) {
        // Apply a length filter if the maxLength is non-negative, otherwise apply no filter
        filters = if (maxLength >= 0) arrayOf<InputFilter>(LengthFilter(maxLength)) else NO_FILTERS
    }

    /**
     * Sets up the animator for the view.
     * This method creates a `ValueAnimator` to animate the scaling and alpha (opacity) of the text.
     * The animation smoothly increases the scale of the text from 0.5x to 1x and adjusts the text's alpha
     * based on the scale, creating a fade-in effect as the text grows.
     */
    private fun setupAnimator() {
        defaultAddAnimator = ValueAnimator.ofFloat(0.5f, 1f)
        defaultAddAnimator!!.duration = 150
        defaultAddAnimator!!.interpolator = DecelerateInterpolator()
        defaultAddAnimator!!.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            val alpha = (255 * scale).toInt()
            animatorTextPaint.textSize = textSize * scale
            animatorTextPaint.alpha = alpha
            postInvalidate()
        }
    }

    /**
     * Checks if the radius of the OTP view items is within valid limits.
     * This method ensures that the item radius does not exceed certain boundaries based on the current view type:
     * - For a line-based view type, the item radius must not be greater than half the width of the line.
     * - For a rectangle-based view type, the item radius must not be greater than half the width of the item.
     *
     * If the radius exceeds these limits, an IllegalArgumentException is thrown with an appropriate error message.
     */
    private fun checkItemRadius() {
        // Check for line-based view type
        if (viewType == VIEW_TYPE_LINE) {
            // Calculate half of the line width
            val halfOfLineWidth = lineWidth.toFloat() / 2

            // Ensure that the item radius is not greater than half of the line width
            require(easyOtpViewItemRadius <= halfOfLineWidth) {
                "The itemRadius can not be greater than lineWidth when viewType is line"
            }
        }
        // Check for rectangle-based view type
        else if (viewType == VIEW_TYPE_RECTANGLE) {
            // Calculate half of the item width
            val halfOfItemWidth = easyOtpViewItemWidth.toFloat() / 2

            // Ensure that the item radius is not greater than half of the item width
            require(easyOtpViewItemRadius <= halfOfItemWidth) {
                "The itemRadius can not be greater than itemWidth"
            }
        }
    }




    /**
     * Measures the width and height of the view to properly size it based on the provided specifications.
     * This method takes into account the padding, item count, item spacing, and other factors to calculate
     * the required dimensions for the OTP input view.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Get the measurement mode and size for width and height from the measure specs
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Int
        val height: Int

        // Get the height of a single OTP input item
        val boxHeight = easyOtpViewItemHeight

        // Measure the width based on the provided width mode
        if (widthMode == MeasureSpec.EXACTLY) {
            // If the width is exactly defined, use the provided width size
            width = widthSize
        } else {
            // Calculate the total width required for all OTP input items
            val boxesWidth = (easyOtpViewItemCount - 1) * easyOtpViewItemSpacing + easyOtpViewItemCount * easyOtpViewItemWidth
            width = boxesWidth + ViewCompat.getPaddingEnd(this) + ViewCompat.getPaddingStart(this)

            // Adjust the width if item spacing is 0 (handle line width adjustment)
            if (easyOtpViewItemSpacing == 0) {
                width -= (easyOtpViewItemCount - 1) * lineWidth
            }
        }

        // Measure the height based on the height mode
        height = if (heightMode == MeasureSpec.EXACTLY) {
            // If the height is exactly defined, use the provided height size
            heightSize
        } else {
            // Otherwise, set the height based on the item height plus padding
            boxHeight + paddingTop + paddingBottom
        }

        // Set the final measured dimensions for the view
        setMeasuredDimension(width, height)
    }


    /**
     * Called when the text in the OTP input field is changed.
     * This method handles updating the text selection, notifying the completion listener when all OTP fields are filled,
     * triggering animations when enabled, and logging relevant information.
     *
     * @param text The current text after the change.
     * @param start The starting position of the changed text.
     * @param lengthBefore The length of the text before the change.
     * @param lengthAfter The length of the text after the change.
     */
    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        // Move the text cursor to the end if it isn't already at the end
        if (start != text.length) {
            moveSelectionToEnd()
        }

        // Log the current length of the entered text and the OTP item count for debugging purposes
        Log.e("check_complete_item", "easyOtpViewItemCount $easyOtpViewItemCount text.length ${text.length}")

        // Check if the text length matches the expected number of OTP items and trigger the completion listener
        if (text.length == easyOtpViewItemCount && easyOtpCompletionListener != null) {
            easyOtpCompletionListener!!.otpCompleteListener(rootView, text.toString())
        }

        // Trigger the blink effect after each text change
        makeBlink()

        // If animations are enabled, trigger the animation when a new character is added
        if (isAnimationEnable) {
            val isAdd = lengthAfter - lengthBefore > 0  // Check if characters were added
            if (isAdd && defaultAddAnimator != null) {
                // If characters were added, end any existing animation and start a new one
                defaultAddAnimator!!.end()
                defaultAddAnimator!!.start()
            }
        }
    }





    /**
     * Called when the focus state of the view changes.
     * This method handles actions to be taken when the view gains or loses focus,
     * such as moving the text cursor to the end and triggering the blink effect when focused.
     *
     * @param focused Boolean indicating whether the view has gained focus.
     * @param direction The direction from which the focus is coming (e.g., up, down, left, right).
     * @param previouslyFocusedRect The rectangle representing the previously focused view's position.
     */
    override fun onFocusChanged(
        focused: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        // Call the superclass's implementation to handle any default behavior
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        // If the view has gained focus, perform specific actions
        if (focused) {
            // Move the selection cursor to the end of the text input
            moveSelectionToEnd()

            // Trigger the blink effect to indicate the view is focused
            makeBlink()
        }
    }

    /**
     * Called when the selection in the text view changes, such as when the user moves the cursor.
     * This method ensures the cursor stays at the end of the text input if it's not already there.
     *
     * @param selStart The starting position of the selection.
     * @param selEnd The ending position of the selection.
     */
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        // Call the superclass's implementation to handle any default behavior
        super.onSelectionChanged(selStart, selEnd)

        // If the selection is not at the end of the text, move it to the end
        if (text != null && selEnd != text!!.length) {
            moveSelectionToEnd()
        }
    }

    /**
     * Moves the selection (cursor) to the end of the text in the view.
     * This ensures that the cursor always appears at the end of the entered text,
     * especially after modifications like adding or deleting characters.
     */
    private fun moveSelectionToEnd() {
        // Check if the text is not null before trying to set the selection
        if (text != null) {
            // Move the selection to the end of the text by setting the selection at the last character
            setSelection(text!!.length)
        }
    }

    /**
     * Called when the drawable state of the view changes, such as when the view's focus or pressed state changes.
     * This method updates the line colors if necessary when the state of the drawable changes.
     */
    override fun drawableStateChanged() {
        // Call the superclass's implementation to handle any default drawable state change behavior
        super.drawableStateChanged()

        // If the lineColors are not set or if they are stateful (i.e., they change based on different states), update the colors
        if (lineColors == null || lineColors!!.isStateful) {
            updateColors()
        }
    }

    /**
     * Called to draw the view on the canvas.
     * This method is responsible for rendering the OTP view, including any visual components like lines and text.
     *
     * @param canvas The canvas on which the view is drawn.
     */
    override fun onDraw(canvas: Canvas) {
        // Save the current state of the canvas to restore it later (e.g., to prevent altering global canvas settings)
        canvas.save()

        // Update the paint properties (e.g., line color, text color, stroke width) before drawing
        updatePaints()

        // Draw the OTP view using the current canvas and paint settings
        drawOtpView(canvas)

        // Restore the canvas to its previous state to avoid side effects on other drawing operations
        canvas.restore()
    }

    /**
     * Updates the paint objects used for drawing.
     * This function sets the paint's color, style, and stroke width for drawing the lines and text.
     */
    private fun updatePaints() {
        // Set the paint color to the current line color
        paint.color = currentLineColor

        // Set the paint style to STROKE, meaning only the outline of shapes will be drawn
        paint.style = Paint.Style.STROKE

        // Set the paint stroke width to the line width (converted to float for drawing)
        paint.strokeWidth = lineWidth.toFloat()

        // Set the paint color for the text to the current text color
        getPaint().color = currentTextColor
    }

    /**
     * Draws the OTP view on the given canvas. This function handles drawing each individual OTP item
     * (rectangle or line style), updating their state (selected, filled, etc.), and drawing the appropriate
     * elements like the background, input, and hint for each item.
     *
     * @param canvas The canvas on which the OTP view is drawn.
     */
    private fun drawOtpView(canvas: Canvas) {
        // Determine the next item to be filled based on text direction (LTR or RTL).
        val nextItemToFill: Int = if (rtlTextDirection) {
            easyOtpViewItemCount - 1  // For RTL, the last item is filled first.
        } else {
            // For LTR, the next item to be filled is based on the length of the entered text.
            text?.length ?: 0
        }

        // Loop through each OTP view item.
        for (i in 0 until easyOtpViewItemCount) {
            // Determine if the current item is selected or filled.
            val itemSelected = isFocused && nextItemToFill == i
            val itemFilled = i < nextItemToFill
            var itemState: IntArray? = IntArray(6) // Default state

            // Set item state based on whether it's filled or selected.
            if (itemFilled) {
                itemState = FILLED_STATE  // If the item is filled, set it to the filled state.
            } else if (itemSelected) {
                itemState = SELECTED_STATE  // If the item is selected, set it to the selected state.
            }

            // Update the rectangle and center point for the current item.
            updateItemRectF(i)
            updateCenterPoint()

            // Save the canvas state before drawing.
            canvas.save()

            // For rectangle view type, clip the canvas path to the OTP item box shape.
            if (viewType == VIEW_TYPE_RECTANGLE) {
                updateOtpViewBoxPath(i)
                canvas.clipPath(path)
            }

            // Draw the background for the item (custom background based on item state and fill status).
            drawItemBackgroundCustom(canvas, itemState, itemFilled)

            // Restore the canvas to the previous state.
            canvas.restore()

            // If the item is selected, draw the cursor (indicating the current focus).
            if (itemSelected) {
                drawCursor(canvas)
            }

            // Draw either the OTP box (for rectangle view) or the OTP line (for line view).
            if (viewType == VIEW_TYPE_RECTANGLE) {
                drawOtpBox(canvas, i)
            } else if (viewType == VIEW_TYPE_LINE) {
                drawOtpLine(canvas, i)
            }

            // Debugging: Optionally draw anchor lines for development purposes.
            if (DBG) {
                drawAnchorLine(canvas)
            }

            // Handle drawing the input or hint based on text direction (LTR or RTL).
            if (rtlTextDirection) {
                // For RTL, draw input or hint in reverse order based on the text length.
                val reversedPosition = easyOtpViewItemCount - i
                if (text!!.length >= reversedPosition) {
                    drawInput(canvas, i)  // Draw input if the text length is greater than or equal to the reversed position.
                } else if (!TextUtils.isEmpty(hint) && hint.length == easyOtpViewItemCount) {
                    drawHint(canvas, i)  // Draw hint if available.
                }
            } else {
                // For LTR, draw input or hint normally based on the current position.
                if (text!!.length > i) {
                    drawInput(canvas, i)  // Draw input if the text length is greater than the current index.
                } else if (!TextUtils.isEmpty(hint) && hint.length == easyOtpViewItemCount) {
                    drawHint(canvas, i)  // Draw hint if available.
                }
            }
        }

        // If the view is focused and not all items are filled, highlight the next box to be filled.
        if (isFocused && text != null && text!!.length != easyOtpViewItemCount && viewType == VIEW_TYPE_RECTANGLE) {
            val index = text!!.length
            updateItemRectF(index)  // Update the rectangle for the next item.
            updateCenterPoint()     // Update the center point for the next item.
            updateOtpViewBoxPath(index)  // Update the OTP box path for the next item.
            paint.color = getLineColorForState(*SELECTED_STATE)  // Set the paint color to the selected state color.
            drawOtpBox(canvas, index)  // Draw the OTP box for the next item.
        }
    }


    /**
     * Draws the input (or masked input) for a specific OTP item on the canvas.
     * Depending on the input type (number or password), it either masks the input,
     * draws a circle (for password), or displays the entered text.
     *
     * @param canvas The canvas on which to draw the input.
     * @param i The index of the current OTP item.
     */
    private fun drawInput(canvas: Canvas, i: Int) {
        // If masking character is provided and input type is number or password, mask the input.
        if (maskingChar != null &&
            (isNumberInputType(inputType) || isPasswordInputType(inputType))
        ) {
            // Mask the input by drawing the masking character (e.g., "*").
            drawMaskingText(canvas, i, maskingChar!![0].toString())
        } else if (isPasswordInputType(inputType)) {
            // If input type is password and no masking char is defined, draw a circle to represent the input.
            drawCircle(canvas, i)
        } else {
            // If input type is neither number nor password, draw the actual text entered.
            drawText(canvas, i)
        }
    }




    /**
     * Retrieves the line color for the given states.
     * If `lineColors` is defined, it uses `lineColors` to get the appropriate color for the given states.
     * Otherwise, it returns the default `currentLineColor`.
     *
     * @param states The states to check for the appropriate line color (e.g., focused, selected, etc.).
     * @return The color to be used for the line based on the current state.
     */
    private fun getLineColorForState(vararg states: Int): Int {
        // If lineColors is defined, use it to get the color for the given states, otherwise return the default color.
        return if (lineColors != null) {
            lineColors!!.getColorForState(states, currentLineColor)
        } else {
            currentLineColor
        }
    }






    /**
     * Draws a custom background for each OTP input item based on its filled state.
     * The background drawable is selected based on whether the item is filled or unfilled.
     * The bounds for the background drawable are adjusted based on the item border and line width.
     *
     * @param canvas The canvas on which the background drawable will be drawn.
     * @param backgroundState The state of the drawable (e.g., focused, selected) to determine its appearance.
     * @param itemFilled A boolean indicating whether the item is filled or not. This determines which drawable to use.
     */
    private fun drawItemBackgroundCustom(
        canvas: Canvas,
        backgroundState: IntArray?,
        itemFilled: Boolean
    ) {
        // Select the appropriate background drawable based on whether the item is filled or not
        val backgroundDrawable = if (itemFilled) itemBackgroundFilled else itemBackgroundUnfilled

        // If no background drawable is available, do not draw anything and exit early
        if (backgroundDrawable == null) {
            return
        }

        // Calculate the padding to apply around the background based on the line width
        val delta = lineWidth.toFloat() / 2

        // Adjust the bounds of the background drawable to fit within the item border, accounting for padding
        val left = (itemBorderRect.left - delta).roundToInt()
        val top = (itemBorderRect.top - delta).roundToInt()
        val right = (itemBorderRect.right + delta).roundToInt()
        val bottom = (itemBorderRect.bottom + delta).roundToInt()

        // Set the bounds for the background drawable
        backgroundDrawable.setBounds(left, top, right, bottom)

        // If the view type is not 'none', set the state of the background drawable (e.g., focused, selected)
        if (viewType != VIEW_TYPE_NONE) {
            backgroundDrawable.state = backgroundState ?: drawableState
        }

        // Draw the background drawable on the canvas
        backgroundDrawable.draw(canvas)
    }


    /**
     * Updates the OTP view item's path to define the shape of its box.
     * Determines whether to draw rounded corners based on the item's position and spacing.
     *
     * @param i The index of the OTP item whose path is being updated.
     */
    private fun updateOtpViewBoxPath(i: Int) {
        var drawRightCorner = false
        var drawLeftCorner = false

        // Check if there is spacing between OTP items
        if (easyOtpViewItemSpacing != 0) {
            // If spacing exists, always draw both left and right corners for all items
            drawRightCorner = true
            drawLeftCorner = drawRightCorner
        } else {
            // No spacing between items; selectively draw corners for first and last items
            if (i == 0 && i != easyOtpViewItemCount - 1) {
                // For the first item, draw only the left corner
                drawLeftCorner = true
            }
            if (i == easyOtpViewItemCount - 1 && i != 0) {
                // For the last item, draw only the right corner
                drawRightCorner = true
            }
        }

        // Update the rectangle path with rounded corners based on calculated flags
        updateRoundRectPath(
            itemBorderRect, // The bounds of the item
            easyOtpViewItemRadius.toFloat(), // Radius for horizontal rounding
            easyOtpViewItemRadius.toFloat(), // Radius for vertical rounding
            drawLeftCorner, // Whether to draw the left corner as rounded
            drawRightCorner // Whether to draw the right corner as rounded
        )
    }




    /**
     * Draws the OTP box for a specific item on the canvas.
     *
     * @param canvas The canvas to draw on.
     * @param i The index of the OTP item being drawn.
     */
    private fun drawOtpBox(canvas: Canvas, i: Int) {
        // Check if the current OTP item should hide its line when filled.
        // If `hideLineWhenFilled` is enabled and the item is already filled (i.e., index < text length),
        // skip drawing the box for this item.
        if (text != null && hideLineWhenFilled && i < text!!.length) {
            return
        }

        // Draw the path representing the OTP box using the configured paint settings.
        canvas.drawPath(path, paint)
    }


    /**
     * Draws a line for the OTP item at the specified index on the canvas.
     *
     * @param canvas The canvas to draw on.
     * @param i The index of the OTP item being drawn.
     */
    private fun drawOtpLine(canvas: Canvas, i: Int) {
        // Skip drawing the line if the current item is already filled and `hideLineWhenFilled` is enabled.
        if (text != null && hideLineWhenFilled && i < text!!.length) {
            return
        }

        // Flags to determine whether to draw the left and right corners of the line.
        var drawLeft: Boolean
        var drawRight: Boolean
        drawRight = true
        drawLeft = drawRight // Initially, set both corners to be drawn.

        // Adjust corner drawing based on spacing and item position.
        if (easyOtpViewItemSpacing == 0 && easyOtpViewItemCount > 1) {
            when (i) {
                0 -> {
                    // First item: Do not draw the right corner.
                    drawRight = false
                }
                easyOtpViewItemCount - 1 -> {
                    // Last item: Do not draw the left corner.
                    drawLeft = false
                }
                else -> {
                    // Middle items: Do not draw any corners.
                    drawRight = false
                    drawLeft = drawRight
                }
            }
        }

        // Configure paint for line drawing.
        paint.style = Paint.Style.FILL
        paint.strokeWidth = lineWidth.toFloat() / 10

        // Calculate half of the line width for precise bounds.
        val halfLineWidth = lineWidth.toFloat() / 2

        // Define the rectangle bounds for the line.
        itemLineRect.set(
            itemBorderRect.left - halfLineWidth,
            itemBorderRect.bottom - halfLineWidth,
            itemBorderRect.right + halfLineWidth,
            itemBorderRect.bottom + halfLineWidth
        )

        // Update the path for rounded corners based on the specified flags.
        updateRoundRectPath(
            itemLineRect,
            easyOtpViewItemRadius.toFloat(),
            easyOtpViewItemRadius.toFloat(),
            drawLeft,
            drawRight
        )

        // Draw the line path on the canvas using the configured paint.
        canvas.drawPath(path, paint)
    }


    /**
     * Draws a cursor at the center of the currently selected OTP item.
     *
     * @param canvas The canvas to draw on.
     */
    private fun drawCursor(canvas: Canvas) {
        // Check if the cursor drawing is enabled.
        if (drawCursor) {
            // Retrieve the x and y coordinates of the item center.
            val cx = itemCenterPoint.x // Center X-coordinate.
            val cy = itemCenterPoint.y // Center Y-coordinate.

            // Calculate the top position of the cursor.
            val y = cy - cursorHeight / 2

            // Save the current paint color and stroke width to restore later.
            val originalColor = paint.color
            val originalStrokeWidth = paint.strokeWidth

            // Configure paint for drawing the cursor.
            paint.color = cursorColor // Set the cursor color.
            paint.strokeWidth = cursorWidth.toFloat() // Set the cursor width.

            // Draw a vertical line to represent the cursor.
            canvas.drawLine(cx, y, cx, y + cursorHeight, paint)

            // Restore the original paint color and stroke width.
            paint.color = originalColor
            paint.strokeWidth = originalStrokeWidth
        }
    }


    /**
     * Updates the path for drawing a rounded rectangle with customizable corner visibility.
     *
     * @param rectF The rectangular bounds for the path.
     * @param rx The x-radius of the rounded corners.
     * @param ry The y-radius of the rounded corners.
     * @param l Whether to include the left corners as rounded.
     * @param r Whether to include the right corners as rounded.
     */
    private fun updateRoundRectPath(
        rectF: RectF,
        rx: Float,
        ry: Float,
        l: Boolean,
        r: Boolean
    ) {
        // Delegate to the overloaded method with detailed corner control.
        // By default, the left corners (l) and right corners (r) control
        // both the top and bottom corners on their respective sides.
        updateRoundRectPath(rectF, rx, ry, l, r, r, l)
    }


    /**
     * Updates the path to represent a rounded rectangle with customizable rounded corners.
     *
     * @param rectF The rectangular bounds for the path.
     * @param rx The x-radius of the rounded corners.
     * @param ry The y-radius of the rounded corners.
     * @param tl Whether the top-left corner should be rounded.
     * @param tr Whether the top-right corner should be rounded.
     * @param br Whether the bottom-right corner should be rounded.
     * @param bl Whether the bottom-left corner should be rounded.
     */
    private fun updateRoundRectPath(
        rectF: RectF, rx: Float, ry: Float,
        tl: Boolean, tr: Boolean, br: Boolean, bl: Boolean
    ) {
        // Reset the path to remove any previous drawings.
        path.reset()

        // Extract the edges of the rectangle.
        val l = rectF.left
        val t = rectF.top
        val r = rectF.right
        val b = rectF.bottom

        // Calculate the width, height, and adjusted lengths for the path.
        val w = r - l
        val h = b - t
        val lw = w - 2 * rx // Effective width excluding corner radii.
        val lh = h - 2 * ry // Effective height excluding corner radii.

        // Start the path at the left-middle of the top edge.
        path.moveTo(l, t + ry)

        // Top-left corner
        if (tl) {
            // Draw a rounded corner using a quadratic curve.
            path.rQuadTo(0f, -ry, rx, -ry)
        } else {
            // Draw a straight line for the corner.
            path.rLineTo(0f, -ry)
            path.rLineTo(rx, 0f)
        }

        // Top edge
        path.rLineTo(lw, 0f)

        // Top-right corner
        if (tr) {
            path.rQuadTo(rx, 0f, rx, ry)
        } else {
            path.rLineTo(rx, 0f)
            path.rLineTo(0f, ry)
        }

        // Right edge
        path.rLineTo(0f, lh)

        // Bottom-right corner
        if (br) {
            path.rQuadTo(0f, ry, -rx, ry)
        } else {
            path.rLineTo(0f, ry)
            path.rLineTo(-rx, 0f)
        }

        // Bottom edge
        path.rLineTo(-lw, 0f)

        // Bottom-left corner
        if (bl) {
            path.rQuadTo(-rx, 0f, -rx, -ry)
        } else {
            path.rLineTo(-rx, 0f)
            path.rLineTo(0f, -ry)
        }

        // Left edge
        path.rLineTo(0f, -lh)

        // Close the path to complete the shape.
        path.close()
    }


    /**
     * Updates the rectangle (`RectF`) that represents the border for a specific OTP view item.
     *
     * @param i The index of the OTP view item to update.
     */
    private fun updateItemRectF(i: Int) {
        // Calculate half the line width for precise positioning.
        val halfLineWidth = lineWidth.toFloat() / 2

        // Calculate the left boundary of the item rectangle.
        var left = scrollX +
                ViewCompat.getPaddingStart(this) +
                i * (easyOtpViewItemSpacing + easyOtpViewItemWidth) +
                halfLineWidth

        // Adjust the left position when there is no spacing between items
        // and for indices greater than 0, subtract the accumulated line width.
        if (easyOtpViewItemSpacing == 0 && i > 0) {
            left -= lineWidth * i
        }

        // Calculate the right boundary of the item rectangle.
        val right = left + easyOtpViewItemWidth - lineWidth

        // Calculate the top boundary of the item rectangle.
        val top = scrollY + paddingTop + halfLineWidth

        // Calculate the bottom boundary of the item rectangle.
        val bottom = top + easyOtpViewItemHeight - lineWidth

        // Set the calculated boundaries to the rectangle used for the item border.
        itemBorderRect.set(left, top, right, bottom)
    }


    /**
     * Draws the text for a specific OTP box on the canvas.
     *
     * @param canvas The canvas to draw on.
     * @param i The index of the OTP box being drawn.
     */
    private fun drawText(canvas: Canvas, i: Int) {
        // Retrieve the paint object for the given index and set its color to the current text color.
        val paint = getPaintByIndex(i)
        paint!!.color = currentTextColor

        // Handle text drawing for right-to-left (RTL) text direction.
        if (rtlTextDirection) {
            // Calculate the reversed position for the current OTP box.
            val reversedPosition = easyOtpViewItemCount - i

            // Determine the character position for RTL layout.
            val reversedCharPosition: Int = if (text == null) {
                reversedPosition
            } else {
                reversedPosition - text!!.length
            }

            // If the reversed character position is within the range of the text, draw it.
            if (reversedCharPosition <= 0 && text != null) {
                drawTextAtBox(canvas, paint, text, abs(reversedCharPosition))
            }
        }
        // Handle text drawing for left-to-right (LTR) text direction.
        else if (text != null) {
            drawTextAtBox(canvas, paint, text, i)
        }
    }


    /**
     * Draws the masking character (e.g., '*') for a specific OTP box on the canvas.
     *
     * @param canvas The canvas to draw on.
     * @param i The index of the OTP box being drawn.
     * @param maskingChar The character to use for masking the OTP text.
     */
    private fun drawMaskingText(
        canvas: Canvas,
        i: Int,
        maskingChar: String
    ) {
        // Retrieve the paint object for the given index and set its color to the current text color.
        val paint = getPaintByIndex(i)
        paint!!.color = currentTextColor

        // Handle masking text drawing for right-to-left (RTL) text direction.
        if (rtlTextDirection) {
            // Calculate the reversed position for the current OTP box.
            val reversedPosition = easyOtpViewItemCount - i

            // Determine the character position for RTL layout.
            val reversedCharPosition: Int = if (text == null) {
                reversedPosition
            } else {
                reversedPosition - text!!.length
            }

            // If the reversed character position is within the range of the text, draw the masking character.
            if (reversedCharPosition <= 0 && text != null) {
                drawTextAtBox(
                    canvas,
                    paint,
                    text.toString().replace(".".toRegex(), maskingChar), // Replace each character with the masking character.
                    abs(reversedCharPosition)
                )
            }
        }
        // Handle masking text drawing for left-to-right (LTR) text direction.
        else if (text != null) {
            drawTextAtBox(
                canvas,
                paint,
                text.toString().replace(".".toRegex(), maskingChar), // Replace each character with the masking character.
                i
            )
        }
    }


    /**
     * Draws the hint text (e.g., placeholder text) for a specific OTP box on the canvas.
     *
     * @param canvas The canvas to draw the hint text on.
     * @param i The index of the OTP box being drawn.
     */
    private fun drawHint(canvas: Canvas, i: Int) {
        // Retrieve the paint object for the given index and set its color to the current hint text color.
        val paint = getPaintByIndex(i)
        paint!!.color = currentHintTextColor

        // Handle hint text drawing for right-to-left (RTL) text direction.
        if (rtlTextDirection) {
            // Calculate the reversed position for the current OTP box in RTL layout.
            val reversedPosition = easyOtpViewItemCount - i

            // Determine the reversed character position in the hint text.
            val reversedCharPosition = reversedPosition - hint.length

            // If the reversed character position is within the range of the hint, draw the hint text.
            if (reversedCharPosition <= 0) {
                drawTextAtBox(canvas, paint, hint, abs(reversedCharPosition))
            }
        }
        // Handle hint text drawing for left-to-right (LTR) text direction.
        else {
            drawTextAtBox(canvas, paint, hint, i)
        }
    }


    /**
     * Draws a single character of text at the specified position in the OTP box.
     *
     * @param canvas The canvas to draw the text on.
     * @param paint The paint object used to style the text.
     * @param text The text to be drawn (typically a single character).
     * @param charAt The index of the character to draw from the text.
     */
    private fun drawTextAtBox(
        canvas: Canvas,
        paint: Paint?,
        text: CharSequence?,
        charAt: Int
    ) {
        // Ensure the paint object is not null and calculate the bounds of the character to be drawn.
        paint!!.getTextBounds(text.toString(), charAt, charAt + 1, textRect)

        // Calculate the center point of the item box where the character will be drawn.
        val cx = itemCenterPoint.x
        val cy = itemCenterPoint.y

        // Calculate the horizontal position (x) for centering the character within the box.
        val x = cx - abs(textRect.width().toFloat()) / 2 - textRect.left

        // Calculate the vertical position (y) for centering the character within the box.
        val y = cy + abs(textRect.height().toFloat()) / 2 - textRect.bottom

        // If the text is not null, draw the specified character at the calculated position.
        if (text != null) {
            canvas.drawText(text, charAt, charAt + 1, x, y, paint)
        }
    }


    /**
     * Draws a circle at the center of an OTP item box based on the given index.
     *
     * @param canvas The canvas on which the circle will be drawn.
     * @param i The index of the OTP item, used to determine the position of the circle.
     */
    private fun drawCircle(canvas: Canvas, i: Int) {
        // Get the paint object for the specified OTP item index.
        val paint = getPaintByIndex(i)

        // Get the center coordinates of the OTP item box where the circle will be drawn.
        val cx = itemCenterPoint.x
        val cy = itemCenterPoint.y

        // Check if the text direction is right-to-left (RTL).
        if (rtlTextDirection) {
            // Reverse the position for RTL and calculate the reversed character position.
            val reversedItemPosition = easyOtpViewItemCount - i
            val reversedCharPosition = reversedItemPosition - hint.length

            // If the reversed character position is less than or equal to 0, draw the circle.
            if (reversedCharPosition <= 0) {
                canvas.drawCircle(cx, cy, paint!!.textSize / 2, paint)
            }
        } else {
            // If not RTL, simply draw the circle using the center coordinates.
            canvas.drawCircle(cx, cy, paint!!.textSize / 2, paint)
        }
    }


    /**
     * Returns the Paint object to be used for drawing the text at a specified index.
     *
     * If animation is enabled and the current index is the last index of the text,
     * the paint used for animation is returned. Otherwise, the default paint is returned.
     *
     * @param i The index of the text item for which the paint is being retrieved.
     * @return The Paint object to be used for drawing at the specified index.
     */
    private fun getPaintByIndex(i: Int): Paint? {
        // Check if text is not null, animation is enabled, and the current index is the last one.
        return if (text != null && isAnimationEnable && i == text!!.length - 1) {
            // If the conditions are met, return the animator paint with the color set to the default paint's color.
            animatorTextPaint.color = getPaint().color
            animatorTextPaint
        } else {
            // Otherwise, return the default paint.
            getPaint()
        }
    }


    /**
     * Draws anchor lines at the center of the item (both vertically and horizontally)
     * within the bounds of the `itemBorderRect` using the provided paint.
     *
     * This method draws two lines:
     * 1. A vertical line at the center of the item.
     * 2. A horizontal line at the center of the item.
     *
     * The lines are drawn using a stroke width of 1 for precision, but it restores the original stroke width after drawing.
     *
     * @param canvas The Canvas object on which the anchor lines will be drawn.
     */
    private fun drawAnchorLine(canvas: Canvas) {
        // Get the center point of the item.
        var cx = itemCenterPoint.x
        var cy = itemCenterPoint.y

        // Set the stroke width for the anchor lines (temporary stroke width of 1 for accuracy).
        paint.strokeWidth = 1f

        // Adjust the center coordinates to account for the stroke width, so the lines are centered correctly.
        cx -= paint.strokeWidth / 2
        cy -= paint.strokeWidth / 2

        // Reset the path for the vertical line.
        path.reset()
        path.moveTo(cx, itemBorderRect.top) // Start the line at the top of the border.
        path.lineTo(cx, itemBorderRect.top + abs(itemBorderRect.height())) // Draw the vertical line.
        canvas.drawPath(path, paint) // Draw the path on the canvas.

        // Reset the path for the horizontal line.
        path.reset()
        path.moveTo(itemBorderRect.left, cy) // Start the line at the left side of the border.
        path.lineTo(itemBorderRect.left + abs(itemBorderRect.width()), cy) // Draw the horizontal line.
        canvas.drawPath(path, paint) // Draw the path on the canvas.

        // Reset the stroke width back to the original value.
        paint.strokeWidth = lineWidth.toFloat()
    }




    /**
     * Updates the color of the line based on the current drawable state and
     * invalidates the view if the color has changed.
     *
     * This function checks if the line color should be updated based on the drawable's
     * state and the current line color. If the color has changed, the view will be invalidated
     * to trigger a redraw with the new color.
     */
    private fun updateColors() {
        // Flag to track whether the view needs to be invalidated (redrawn)
        var shouldInvalidate = false

        // Determine the new color for the line. If lineColors is not null, use the color
        // corresponding to the current drawable state, otherwise, fall back to the current text color.
        val color = if (lineColors != null) {
            // Get the color for the current drawable state
            lineColors!!.getColorForState(drawableState, 0)
        } else {
            // Fallback to the default text color if lineColors is null
            currentTextColor
        }

        // Check if the new color is different from the current line color
        if (color != currentLineColor) {
            // Update the current line color if it has changed
            currentLineColor = color
            // Mark that the view needs to be invalidated
            shouldInvalidate = true
        }

        // If the color has changed, invalidate the view to trigger a redraw with the new color
        if (shouldInvalidate) {
            invalidate()
        }
    }


    /**
     * Updates the center point of the item based on its bounding rectangle.
     * The center point is calculated as the midpoint of the itemBorderRect
     * (the rectangle representing the item's borders).
     */
    private fun updateCenterPoint() {
        // Calculate the x-coordinate of the center point
        val cx = itemBorderRect.left + abs(itemBorderRect.width()) / 2

        // Calculate the y-coordinate of the center point
        val cy = itemBorderRect.top + abs(itemBorderRect.height()) / 2

        // Update the itemCenterPoint with the calculated center coordinates
        itemCenterPoint[cx] = cy
    }

    /**
     * Returns the default movement method for the OTP input field.
     *
     * This method defines how the cursor and text should behave when navigating
     * through the input field. In this case, it returns an instance of the custom
     * EasyOtpMovementMethod for handling movement within the OTP input field.
     */
    override fun getDefaultMovementMethod(): MovementMethod {
        return EasyOtpMovementMethod()
    }


    /**
     * Sets the line color for all the states (normal, selected,
     * focused) to be this color.
     *
     * @param color A color value in the form 0xAARRGGBB.
     * Do not pass a resource ID. To get a color value from a resource ID, call
     * [getColor][androidx.core.content.ContextCompat.getColor].
     * @attr ref R.styleable#OtpView_lineColor
     * @see .setLineColor
     * @see .getLineColors
     */
    fun setLineColor(@ColorInt color: Int) {
        lineColors = ColorStateList.valueOf(color)
        updateColors()
    }

    /**
     * Sets the line color.
     *
     * @attr ref R.styleable#OtpView_lineColor
     * @see .setLineColor
     * @see .getLineColors
     */
    fun setLineColor(colors: ColorStateList?) {
        requireNotNull(colors) { "Color cannot be null" }
        lineColors = colors
        updateColors()
    }

    /**
     * Sets the line width.
     *
     * @attr ref R.styleable#OtpView_lineWidth
     * @see .getLineWidth
     */
    fun setLineWidth(@Px borderWidth: Int) {
        lineWidth = borderWidth
        checkItemRadius()
        requestLayout()
    }

    /**
     * @return Returns the width of the item's line.
     * @see .setLineWidth
     */
    fun getLineWidth(): Int {
        return lineWidth
    }

    /**
     * @return Returns the count of items.
     * @see .setItemCount
     */
    /**
     * Sets the count of items.
     *
     * @attr ref R.styleable#OtpView_itemCount
     * @see .getItemCount
     */
    var itemCount: Int
        get() = easyOtpViewItemCount
        set(count) {
            easyOtpViewItemCount = count
            setMaxLength(count)
            requestLayout()
        }

    /**
     * @return Returns the radius of square.
     * @see .setItemRadius
     */
    /**
     * Sets the radius of square.
     *
     * @attr ref R.styleable#OtpView_itemRadius
     * @see .getItemRadius
     */
    var itemRadius: Int
        get() = easyOtpViewItemRadius
        set(itemRadius) {
            easyOtpViewItemRadius = itemRadius
            checkItemRadius()
            requestLayout()
        }

    /**
     * @return Returns the spacing between two items.
     * @see .setItemSpacing
     */
    /**
     * Specifies extra space between two items.
     *
     * @attr ref R.styleable#OtpView_itemSpacing
     * @see .getItemSpacing
     */
    @get:Px
    var itemSpacing: Int
        get() = easyOtpViewItemSpacing
        set(itemSpacing) {
            easyOtpViewItemSpacing = itemSpacing
            requestLayout()
        }

    /**
     * @return Returns the height of item.
     * @see .setItemHeight
     */
    /**
     * Sets the height of item.
     *
     * @attr ref R.styleable#OtpView_itemHeight
     * @see .getItemHeight
     */
    var itemHeight: Int
        get() = easyOtpViewItemHeight
        set(itemHeight) {
            easyOtpViewItemHeight = itemHeight
            updateCursorHeight()
            requestLayout()
        }

    /**
     * @return Returns the width of item.
     * @see .setItemWidth
     */
    /**
     * Sets the width of item.
     *
     * @attr ref R.styleable#OtpView_itemWidth
     * @see .getItemWidth
     */
    var itemWidth: Int
        get() = easyOtpViewItemWidth
        set(itemWidth) {
            easyOtpViewItemWidth = itemWidth
            checkItemRadius()
            requestLayout()
        }

    /**
     * Specifies whether the text animation should be enabled or disabled.
     * By the default, the animation is disabled.
     *
     * @param enable True to start animation when adding text, false to transition immediately
     */
    fun setAnimationEnable(enable: Boolean) {
        isAnimationEnable = enable
    }

    /**
     * Specifies whether the line (border) should be hidden or visible when text entered.
     * By the default, this flag is false and the line is always drawn.
     *
     * @param hideLineWhenFilled true to hide line on a position where text entered,
     * false to always show line
     * @attr ref R.styleable#OtpView_hideLineWhenFilled
     */
    fun setHideLineWhenFilled(hideLineWhenFilled: Boolean) {
        this.hideLineWhenFilled = hideLineWhenFilled
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        updateCursorHeight()
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        updateCursorHeight()
    }

    fun setOtpCompletionListener(easyOtpCompletionListener: EasyOtpViewCompleteListener?) {
        this.easyOtpCompletionListener = easyOtpCompletionListener
    }


    //region ItemBackground
    /**
     * Set the item background to a given resource. The resource should refer to
     * a Drawable object or 0 to remove the item background.
     *
     * @param resId The identifier of the resource.
     * @attr ref R.styleable#OtpView_android_itemBackground
     */
    fun setItemBackgroundResources(@DrawableRes resId: Int) {
        if (resId != 0 && itemBackgroundResource != resId) {
            return
        }
        itemBackground = ResourcesCompat.getDrawable(resources, resId, context.theme)
        setItemBackground(itemBackground)
        itemBackgroundResource = resId
    }


    /**
     * Sets the item background color for this view.
     *
     * @param color the color of the item background
     */
    fun setItemBackgroundColor(@ColorInt color: Int) {
        if (itemBackground is ColorDrawable) {
            (itemBackground!!.mutate() as ColorDrawable).color = color
            itemBackgroundResource = 0
        } else {
            setItemBackground(ColorDrawable(color))
        }
    }


    /**
     * Set the item background to a given Drawable, or remove the background.
     *
     * @param background The Drawable to use as the item background, or null to remove the
     * item background
     */
    private fun setItemBackground(background: Drawable?) {
        itemBackgroundResource = 0
        itemBackground = background
        invalidate()
    }


    //endregion
    //region Cursor
    /**
     * Sets the width (in pixels) of cursor.
     *
     * @attr ref R.styleable#OtpView_cursorWidth
     * @see .getCursorWidth
     */
    fun setCursorWidth(@Px width: Int) {
        cursorWidth = width
        if (isCursorVisible()) {
            invalidateCursor(true)
        }
    }


    /**
     * @return Returns the width (in pixels) of cursor.
     * @see .setCursorWidth
     */
    fun getCursorWidth(): Int {
        return cursorWidth
    }


    /**
     * Sets the cursor color.
     *
     * @param color A color value in the form 0xAARRGGBB.
     * Do not pass a resource ID. To get a color value from a resource ID, call
     * [getColor][androidx.core.content.ContextCompat.getColor].
     * @attr ref R.styleable#OtpView_cursorColor
     * @see .getCursorColor
     */
    fun setCursorColor(@ColorInt color: Int) {
        cursorColor = color
        if (isCursorVisible()) {
            invalidateCursor(true)
        }
    }


    /**
     * Gets the cursor color.
     *
     * @return Return current cursor color.
     * @see .setCursorColor
     */
    fun getCursorColor(): Int {
        return cursorColor
    }


    fun setMaskingChar(maskingChar: String?) {
        this.maskingChar = maskingChar
        requestLayout()
    }


    fun getMaskingChar(): String? {
        return maskingChar
    }


    override fun setCursorVisible(visible: Boolean) {
        if (isCursorVisible != visible) {
            isCursorVisible = visible
            invalidateCursor(isCursorVisible)
            makeBlink()
        }
    }


    override fun isCursorVisible(): Boolean {
        return isCursorVisible
    }


    override fun onScreenStateChanged(screenState: Int) {
        super.onScreenStateChanged(screenState)
        if (screenState == View.SCREEN_STATE_ON) {
            resumeBlink()
        } else if (screenState == View.SCREEN_STATE_OFF) {
            suspendBlink()
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resumeBlink()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        suspendBlink()
    }


    private fun shouldBlink(): Boolean {
        return isCursorVisible() && isFocused
    }


    private fun makeBlink() {
        if (shouldBlink()) {
            if (blink == null) {
                blink = Blink()
            }
            removeCallbacks(blink)
            drawCursor = false
            postDelayed(blink, BLINK.toLong())
        } else {
            if (blink != null) {
                removeCallbacks(blink)
            }
        }
    }


    private fun suspendBlink() {
        if (blink != null) {
            blink!!.cancel()
            invalidateCursor(false)
        }
    }


    private fun resumeBlink() {
        if (blink != null) {
            blink!!.unCancel()
            makeBlink()
        }
    }


    private fun invalidateCursor(showCursor: Boolean) {
        if (drawCursor != showCursor) {
            drawCursor = showCursor
            invalidate()
        }
    }


    private fun updateCursorHeight() {
        val delta = 2 * dpToPx()
        cursorHeight =
            if (easyOtpViewItemHeight - textSize > delta) textSize + delta else textSize
    }


    private inner class Blink : Runnable {
        private var cancelled = false
        override fun run() {
            if (cancelled) {
                return
            }
            removeCallbacks(this)
            if (shouldBlink()) {
                invalidateCursor(!drawCursor)
                postDelayed(this, BLINK.toLong())
            }
        }

        fun cancel() {
            if (!cancelled) {
                removeCallbacks(this)
                cancelled = true
            }
        }

        fun unCancel() {
            cancelled = false
        }
    }


    //endregion
    private fun dpToPx(): Int {
        return (2.toFloat() * resources.displayMetrics.density + 0.5f).toInt()
    }


    companion object {
        // Debug flag for logging purposes, set to false by default
        private const val DBG = false

        // Constant for blink duration (500ms)
        private const val BLINK = 500

        // Default item count for OTP input (4 items by default)
        private const val DEFAULT_COUNT = 4

        // Empty array of InputFilters (no filters applied)
        private val NO_FILTERS = arrayOfNulls<InputFilter>(0)

        // State array for the selected state of the view
        private val SELECTED_STATE = intArrayOf(android.R.attr.state_selected)

        // State array for the filled state of the view
        private val FILLED_STATE = intArrayOf(R.attr.state_filled)

        // View types for customizing the appearance of the OTP input field
        private const val VIEW_TYPE_RECTANGLE = 0  // Rectangle type view
        private const val VIEW_TYPE_LINE = 1       // Line type view
        private const val VIEW_TYPE_NONE = 2       // No specific view type

        // Function to check if the input type is password (including variations for text and number passwords)
        private fun isPasswordInputType(inputType: Int): Boolean {
            // Check the variation of the input type (mask class and variation)
            val variation = inputType and (EditorInfo.TYPE_MASK_CLASS or EditorInfo.TYPE_MASK_VARIATION)

            // Return true if the input type is any form of password (text or number variation)
            return (variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) ||
                    (variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD) ||
                    (variation == EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)
        }

        // Function to check if the input type is a number
        private fun isNumberInputType(inputType: Int): Boolean {
            // Return true if the input type is a number
            return inputType == EditorInfo.TYPE_CLASS_NUMBER
        }
    }



    init {
        // Initialize resources and paint object for drawing
        val res = resources
        paint = Paint(Paint.ANTI_ALIAS_FLAG) // Paint object with anti-aliasing
        paint.style = Paint.Style.STROKE // Set the paint style to STROKE
        animatorTextPaint.set(getPaint()) // Initialize animator text paint

        // Obtain styled attributes from XML
        val theme = context.theme
        val typedArray = theme.obtainStyledAttributes(attrs, R.styleable.EasyOtpView, defStyleAttr, 0)

        // Retrieve the view type, item count, height, width, and other attributes
        viewType = typedArray.getInt(R.styleable.EasyOtpView_viewType, VIEW_TYPE_NONE)
        easyOtpViewItemCount = typedArray.getInt(R.styleable.EasyOtpView_itemCount, DEFAULT_COUNT)
        easyOtpViewItemHeight = typedArray.getDimension(
            R.styleable.EasyOtpView_itemHeight,
            res.getDimensionPixelSize(R.dimen._40sdp).toFloat()
        ).toInt()
        easyOtpViewItemWidth = typedArray.getDimension(
            R.styleable.EasyOtpView_itemWidth,
            res.getDimensionPixelSize(R.dimen._40sdp).toFloat()
        ).toInt()
        easyOtpViewItemSpacing = typedArray.getDimensionPixelSize(
            R.styleable.EasyOtpView_itemSpacing,
            res.getDimensionPixelSize(R.dimen._5sdp)
        )
        easyOtpViewItemRadius = typedArray.getDimension(R.styleable.EasyOtpView_itemRadius, 0f).toInt()
        lineWidth = typedArray.getDimension(
            R.styleable.EasyOtpView_lineWidth,
            res.getDimensionPixelSize(R.dimen._2sdp).toFloat()
        ).toInt()
        lineColors = typedArray.getColorStateList(R.styleable.EasyOtpView_lineColor)
        isCursorVisible = typedArray.getBoolean(R.styleable.EasyOtpView_android_cursorVisible, true)
        cursorColor = typedArray.getColor(R.styleable.EasyOtpView_cursorColor, currentTextColor)
        cursorWidth = typedArray.getDimensionPixelSize(
            R.styleable.EasyOtpView_cursorWidth,
            res.getDimensionPixelSize(R.dimen._2sdp)
        )
        itemBackground = typedArray.getDrawable(R.styleable.EasyOtpView_android_itemBackground)
        hideLineWhenFilled = typedArray.getBoolean(R.styleable.EasyOtpView_hideLineWhenFilled, false)
        rtlTextDirection = typedArray.getBoolean(R.styleable.EasyOtpView_rtlTextDirection, false)
        maskingChar = typedArray.getString(R.styleable.EasyOtpView_maskingChar)
        typedArray.recycle() // Recycle typedArray after use

        // Set default line color if lineColors are provided
        if (lineColors != null) {
            currentLineColor = lineColors!!.defaultColor
        }

        // Retrieve additional attributes for background when filled/unfilled
        val a = context.obtainStyledAttributes(attrs, R.styleable.EasyOtpView, defStyleAttr, 0)
        itemBackgroundFilled = a.getDrawable(R.styleable.EasyOtpView_itemBackgroundFilled)
        itemBackgroundUnfilled = a.getDrawable(R.styleable.EasyOtpView_itemBackgroundUnfilled)
        a.recycle() // Recycle the additional styled attributes

        // Update cursor height, check item radius, and set the maximum length of OTP input
        updateCursorHeight()
        checkItemRadius()
        setMaxLength(easyOtpViewItemCount)

        // Set stroke width for line drawing
        paint.strokeWidth = lineWidth.toFloat()

        // Initialize the animator for text and cursor
        setupAnimator()

        // Disable cursor visibility and make the text non-selectable by default
        super.setCursorVisible(false)
        setTextIsSelectable(false)
    }

}