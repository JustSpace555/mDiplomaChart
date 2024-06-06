package presentation

import com.sun.javafx.charts.ChartLayoutAnimator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.SimpleLongProperty
import javafx.scene.chart.Axis
import javafx.util.Duration
import javafx.util.StringConverter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013, Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * An axis that displays date and time values.
 *
 *
 * Tick labels are usually automatically set and calculated depending on the range
 * unless you explicitly [set a formatter][.setTickLabelFormatter].
 *
 *
 * You also have the chance to specify fix lower and upper bounds, otherwise they are calculated by your data.
 *
 *
 *
 *
 * <h3>Screenshots</h3>
 *
 *
 * Displaying date values, ranging over several months:
 * <img src="doc-files/DateAxisMonths.png" alt="DateAxisMonths"></img>
 *
 *
 * Displaying date values, ranging only over a few hours:
 * <img src="doc-files/DateAxisHours.png" alt="DateAxisHours"></img>
 *
 *
 *
 *
 * <h3>Sample Usage</h3>
 * <pre>
 * `ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();
 *
 * ObservableList<XYChart.Data<Date, Number>> series1Data = FXCollections.observableArrayList();
 * series1Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2012, 11, 15).getTime(), 2));
 * series1Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2014, 5, 3).getTime(), 4));
 *
 * ObservableList<XYChart.Data<Date, Number>> series2Data = FXCollections.observableArrayList();
 * series2Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2014, 0, 13).getTime(), 8));
 * series2Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2014, 7, 27).getTime(), 4));
 *
 * series.add(new XYChart.Series<>("Series1", series1Data));
 * series.add(new XYChart.Series<>("Series2", series2Data));
 *
 * NumberAxis numberAxis = new NumberAxis();
 * DateAxis dateAxis = new DateAxis();
 * LineChart<Date, Number> lineChart = new LineChart<>(dateAxis, numberAxis, series);
 *
</pre> *
 *
 * @author Christian Schudt
 * @author Diego Cirujano
 */
class DateAxis : Axis<Date>() {
    /**
     * These property are used for animation.
     */
    private val currentLowerBound: LongProperty = SimpleLongProperty(this, "currentLowerBound")

    private val currentUpperBound: LongProperty = SimpleLongProperty(this, "currentUpperBound")

    private val tickLabelFormatter: ObjectProperty<StringConverter<Date?>?> =
        object : ObjectPropertyBase<StringConverter<Date?>?>() {
            override fun invalidated() {
                if (!isAutoRanging) {
                    invalidateRange()
                    requestAxisLayout()
                }
            }

            override fun getBean(): Any = this@DateAxis

            override fun getName(): String = "tickLabelFormatter"
        }

    /**
     * Stores the min and max date of the list of dates which is used.
     * If [.autoRanging] is true, these values are used as lower and upper bounds.
     */
    private var minDate: Date? = null
    private var maxDate: Date? = null

    private val lowerBound: ObjectProperty<Date?> = object : ObjectPropertyBase<Date?>() {
        override fun invalidated() {
            if (!isAutoRanging) {
                invalidateRange()
                requestAxisLayout()
            }
        }

        override fun getBean(): Any = this@DateAxis

        override fun getName(): String = "lowerBound"
    }

    private val upperBound: ObjectProperty<Date?> = object : ObjectPropertyBase<Date?>() {
        override fun invalidated() {
            if (!isAutoRanging) {
                invalidateRange()
                requestAxisLayout()
            }
        }

        override fun getBean(): Any = this@DateAxis

        override fun getName(): String = "upperBound"
    }

    private val animator = ChartLayoutAnimator(this)

    private var currentAnimationID: Any? = null

    private var actualInterval = Interval.DECADE

    override fun invalidateRange(list: List<Date>) {
        super.invalidateRange(list)

        val sortedList = list.toMutableList().sorted()
        when {
            sortedList.isEmpty() -> {
                maxDate = Date()
                minDate = maxDate
            }
            sortedList.size == 1 -> {
                maxDate = sortedList.first()
                minDate = maxDate
            }
            else -> {
                minDate = sortedList.first()
                maxDate = sortedList.last()
            }
        }
    }

    override fun autoRange(length: Double): Any = if (isAutoRanging) {
        arrayOf<Any?>(minDate, maxDate)
    } else {
        require(!(lowerBound.get() == null || upperBound.get() == null)) {
            "If autoRanging is false, a lower and upper bound must be set."
        }
        range
    }

    override fun setRange(range: Any, animating: Boolean) {
        val r = range as Array<*>
        val oldLowerBound = lowerBound.get()
        val oldUpperBound = upperBound.get()
        val lower = r[0] as Date
        val upper = r[1] as Date
        lowerBound.set(lower)
        upperBound.set(upper)

        if (animating) {
            animator.stop(currentAnimationID)
            currentAnimationID = animator.animate(
                KeyFrame(
                    Duration.ZERO,
                    KeyValue(currentLowerBound, oldLowerBound!!.time),
                    KeyValue(currentUpperBound, oldUpperBound!!.time)
                ),
                KeyFrame(
                    Duration.millis(700.0),
                    KeyValue(currentLowerBound, lower.time),
                    KeyValue(currentUpperBound, upper.time)
                )
            )
        } else {
            currentLowerBound.set(lowerBound.get()!!.time)
            currentUpperBound.set(upperBound.get()!!.time)
        }
    }

    override fun getRange(): Any = arrayOf<Any?>(lowerBound.get(), upperBound.get())

    override fun getZeroPosition(): Double = 0.0

    override fun getDisplayPosition(date: Date): Double {
        val length = if (side.isHorizontal) width else height

        // Get the difference between the max and min date.
        val diff = (currentUpperBound.get() - currentLowerBound.get()).toDouble()

        // Get the actual range of the visible area.
        // The minimal date should start at the zero position, that's why we subtract it.
        val range = length - zeroPosition

        // Then get the difference from the actual date to the min date and divide it by the total difference.
        // We get a value between 0 and 1, if the date is within the min and max date.
        val d = (date.time - currentLowerBound.get()) / diff

        // Multiply this percent value with the range and add the zero offset.
        return if (side.isVertical) {
            height - d * range + zeroPosition
        } else {
            d * range + zeroPosition
        }
    }

    override fun getValueForDisplay(displayPosition: Double): Date {
        val length = if (side.isHorizontal) width else height

        // Get the difference between the max and min date.
        val diff = (currentUpperBound.get() - currentLowerBound.get()).toDouble()

        // Get the actual range of the visible area.
        // The minimal date should start at the zero position, that's why we subtract it.
        val range = length - zeroPosition

        return if (side.isVertical) {
            // displayPosition = getHeight() - ((date - lowerBound) / diff) * range + getZero
            // date = (displayPosition - getZero - getHeight()) / range * diff + lowerBound
            Date(((displayPosition - zeroPosition - height) / -range * diff + currentLowerBound.get()).toLong())
        } else {
            // displayPosition = ((date - lowerBound) / diff) * range + getZero
            // date = (displayPosition - getZero) / range * diff + lowerBound
            Date(((displayPosition - zeroPosition) / range * diff + currentLowerBound.get()).toLong())
        }
    }

    override fun isValueOnAxis(date: Date): Boolean {
        return date.time > currentLowerBound.get() && date.time < currentUpperBound.get()
    }

    override fun toNumericValue(date: Date): Double = date.time.toDouble()

    override fun toRealValue(v: Double): Date = Date(v.toLong())

    override fun calculateTickValues(v: Double, range: Any): List<Date> {
        val r = range as Array<*>
        val lower = r[0] as Date
        val upper = r[1] as Date

        var dateList: MutableList<Date> = ArrayList()
        val calendar = Calendar.getInstance()

        // The preferred gap which should be between two tick marks.
        val averageTickGap = 100.0
        val averageTicks = v / averageTickGap

        val previousDateList: MutableList<Date> = ArrayList()

        var previousInterval = Interval.entries[0]

        // Starting with the greatest interval, add one of each calendar unit.
        for (interval in Interval.entries) {
            // Reset the calendar.
            calendar.time = lower
            // Clear the list.
            dateList.clear()
            previousDateList.clear()
            actualInterval = interval

            // Loop as long we exceeded the upper bound.
            while (calendar.time.time <= upper.time) {
                dateList.add(calendar.time)
                calendar.add(interval.interval, interval.amount)
            }
            // Then check the size of the list. If it is greater than the amount of ticks, take that list.
            if (dateList.size > averageTicks) {
                calendar.time = lower
                // Recheck if the previous interval is better suited.
                while (calendar.time.time <= upper.time) {
                    previousDateList.add(calendar.time)
                    calendar.add(previousInterval.interval, previousInterval.amount)
                }
                break
            }

            previousInterval = interval
        }
        if (previousDateList.size - averageTicks > averageTicks - dateList.size) {
            dateList = previousDateList
            actualInterval = previousInterval
        }

        // At last add the upper bound.
        dateList.add(upper)

        val evenDateList = makeDatesEven(dateList, calendar)
        // If there are at least three dates, check if the gap between the lower date and the second date is
        // at least half the gap of the second and third date.
        // Do the same for the upper bound.
        // If gaps between dates are too small, remove one of them.
        // This can occur, e.g. if the lower bound is 25.12.2013 and years are shown.
        // Then the next year shown would be 2014 (01.01.2014) which would be too narrow to 25.12.2013.
        if (evenDateList.size > 2) {
            val secondDate = evenDateList[1]
            val thirdDate = evenDateList[2]
            val lastDate = evenDateList[dateList.size - 2]
            val previousLastDate = evenDateList[dateList.size - 3]

            // If the second date is too nearby the lower bound, remove it.
            if (secondDate.time - lower.time < (thirdDate.time - secondDate.time) / 2) {
                evenDateList.remove(secondDate)
            }

            // If difference from the upper bound to the last date is less than the half of the difference of the previous two dates,
            // we better remove the last date, as it comes to close to the upper bound.
            if (upper.time - lastDate.time < (lastDate.time - previousLastDate.time) / 2) {
                evenDateList.remove(lastDate)
            }
        }

        return evenDateList
    }

    override fun layoutChildren() {
        if (!isAutoRanging) {
            currentLowerBound.set(lowerBound.get()!!.time)
            currentUpperBound.set(upperBound.get()!!.time)
        }
        super.layoutChildren()
    }

    override fun getTickMarkLabel(date: Date): String {
        tickLabelFormatter.value?.let { return it.toString(date) }

        val calendar = Calendar.getInstance().apply { time = date }

        val dateFormat = when {
            actualInterval.interval == Calendar.YEAR &&
                    calendar[Calendar.MONTH] == 0 &&
                    calendar[Calendar.DATE] == 1 -> {
                        SimpleDateFormat("yyyy")
                    }

            actualInterval.interval == Calendar.MONTH && calendar[Calendar.DATE] == 1 -> {
                SimpleDateFormat("MMM yy")
            }

            else -> when (actualInterval.interval) {
                Calendar.DATE, Calendar.WEEK_OF_YEAR -> DateFormat.getDateInstance(DateFormat.MEDIUM)
                Calendar.HOUR, Calendar.MINUTE -> DateFormat.getTimeInstance(DateFormat.SHORT)
                Calendar.SECOND -> DateFormat.getTimeInstance(DateFormat.MEDIUM)
                Calendar.MILLISECOND -> DateFormat.getTimeInstance(DateFormat.FULL)
                else -> DateFormat.getDateInstance(DateFormat.MEDIUM)
            }
        }
        return dateFormat.format(date)
    }

    /**
     * Makes dates even, in the sense of that years always begin in January,
     * months always begin on the 1st and days always at midnight.
     *
     * @param dates The list of dates.
     * @return The new list of dates.
     */
    private fun makeDatesEven(dates: MutableList<Date>, calendar: Calendar): MutableList<Date> {
        // If the dates contain more dates than just the lower and upper bounds, make the dates in between even.
        return if (dates.size > 2) {
            val evenDates: MutableList<Date> = ArrayList()

            // For each interval, modify the date slightly by a few millis, to make sure they are different days.
            // This is because Axis stores each value and won't update the tick labels, if the value is already known.
            // This happens if you display days and then add a date many years in the future the tick label
            // will still be displayed as day.
            for (i in dates.indices) {
                calendar.time = dates[i]
                when (actualInterval.interval) {
                    Calendar.YEAR -> {
                        // If it's not the first or last date (lower and upper bound), make the year begin with
                        // first month and let the months begin with first day.
                        if (i != 0 && i != dates.size - 1) {
                            calendar[Calendar.MONTH] = 0
                            calendar[Calendar.DATE] = 1
                        }
                        calendar[Calendar.HOUR_OF_DAY] = 0
                        calendar[Calendar.MINUTE] = 0
                        calendar[Calendar.SECOND] = 0
                        calendar[Calendar.MILLISECOND] = 6
                    }

                    Calendar.MONTH -> {
                        // If it's not the first or last date (lower and upper bound),
                        // make the months begin with first day.
                        if (i != 0 && i != dates.size - 1) {
                            calendar[Calendar.DATE] = 1
                        }
                        calendar[Calendar.HOUR_OF_DAY] = 0
                        calendar[Calendar.MINUTE] = 0
                        calendar[Calendar.SECOND] = 0
                        calendar[Calendar.MILLISECOND] = 5
                    }

                    Calendar.WEEK_OF_YEAR -> {
                        // Make weeks begin with first day of week?
                        calendar[Calendar.HOUR_OF_DAY] = 0
                        calendar[Calendar.MINUTE] = 0
                        calendar[Calendar.SECOND] = 0
                        calendar[Calendar.MILLISECOND] = 4
                    }

                    Calendar.DATE -> {
                        calendar[Calendar.HOUR_OF_DAY] = 0
                        calendar[Calendar.MINUTE] = 0
                        calendar[Calendar.SECOND] = 0
                        calendar[Calendar.MILLISECOND] = 3
                    }

                    Calendar.HOUR -> {
                        if (i != 0 && i != dates.size - 1) {
                            calendar[Calendar.MINUTE] = 0
                            calendar[Calendar.SECOND] = 0
                        }
                        calendar[Calendar.MILLISECOND] = 2
                    }

                    Calendar.MINUTE -> {
                        if (i != 0 && i != dates.size - 1) {
                            calendar[Calendar.SECOND] = 0
                        }
                        calendar[Calendar.MILLISECOND] = 1
                    }

                    Calendar.SECOND -> calendar[Calendar.MILLISECOND] = 0
                }
                evenDates.add(calendar.time)
            }
            evenDates
        } else {
            dates
        }
    }

    /**
     * The intervals, which are used for the tick labels.
     * Beginning with the largest interval, the axis tries to calculate the tick values for this interval.
     * If a smaller interval is better suited for, that one is taken.
     */
    private enum class Interval(val interval: Int, val amount: Int) {
        DECADE(Calendar.YEAR, 10),
        YEAR(Calendar.YEAR, 1),
        MONTH_6(Calendar.MONTH, 6),
        MONTH_3(Calendar.MONTH, 3),
        MONTH_1(Calendar.MONTH, 1),
        WEEK(Calendar.WEEK_OF_YEAR, 1),
        DAY(Calendar.DATE, 1),
        HOUR_12(Calendar.HOUR, 12),
        HOUR_6(Calendar.HOUR, 6),
        HOUR_3(Calendar.HOUR, 3),
        HOUR_1(Calendar.HOUR, 1),
        MINUTE_15(Calendar.MINUTE, 15),
        MINUTE_5(Calendar.MINUTE, 5),
        MINUTE_1(Calendar.MINUTE, 1),
        SECOND_15(Calendar.SECOND, 15),
        SECOND_5(Calendar.SECOND, 5),
        SECOND_1(Calendar.SECOND, 1),
        MILLISECOND(Calendar.MILLISECOND, 1)
    }
}