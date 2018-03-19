package com.simplemobiletools.calculator.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.simplemobiletools.calculator.R
import com.simplemobiletools.calculator.conversions.*
import com.simplemobiletools.calculator.helpers.Calculator
import com.simplemobiletools.calculator.helpers.CalculatorImpl
import com.simplemobiletools.calculator.helpers.Formatter
import com.simplemobiletools.commons.extensions.performHapticFeedback
import com.simplemobiletools.commons.extensions.toast
import kotlinx.android.synthetic.main.activity_unit_conversion.*
import java.lang.Math.abs


class UnitConversionActivity : SimpleActivity(), Calculator {

    private lateinit var calc: CalculatorImpl
    private lateinit var converter: Converter

    private var vibrateOnButtonPress = true
    private fun getButtonIds() = arrayOf(btn_decimal, btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9)



    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_conversion)
        calc = CalculatorImpl(this, applicationContext)

        //hookup for keypad
        getButtonIds().forEach {
            it.setOnClickListener { calc.numpadClicked(it.id); checkHaptic(it) }
        }

        //other buttons
        btn_del.setOnClickListener { before.text = before.text.dropLast(1); checkHaptic(it) }
        btn_all_clear.setOnClickListener { calc.handleReset()}


        btn_equals.setOnClickListener{
            var res =   converter.calculate(
                        before.text.toString().toDoubleOrNull(),
                        units_before_spinner.selectedItem.toString(),
                        units_after_spinner.selectedItem.toString()
                        )

            when {
                abs(res) > 10 -> after.text = shortenBig(res.toBigDecimal().toPlainString())
                abs(res) < 0.001 -> after.text = shortenSmall(res.toBigDecimal().toPlainString())
                else -> after.text = res.toString()
            }
        }


        //Three drop down menus. The conversionChoiceSpinner changes the other two automatically.
        val conversionChoiceSpinner: Spinner = findViewById(R.id.conversion_type_spinner)
        val unitsBeforeSpinner: Spinner = findViewById(R.id.units_before_spinner)
        val unitsAfterSpinner: Spinner = findViewById(R.id.units_after_spinner)

        //Main List for conversion choices from helper.
        val conversionChoiceList = listOf("Distance", "Speed", "Time", "Volume", "Weight")

        //Empty list that will be populated with the relevant conversion units.
        val unitList = ArrayList<String>()

        val choiceAdapter: ArrayAdapter<String>
        val beforeAdapter: ArrayAdapter<String>
        val afterAdapter: ArrayAdapter<String>

        //Create adapters for each of the three spinners.
        //TODO: Make custom layouts for the drop down menus.
        choiceAdapter = ArrayAdapter(this, R.layout.spinner_item, conversionChoiceList)
        beforeAdapter = ArrayAdapter(this, R.layout.spinner_item_units, unitList)
        afterAdapter = ArrayAdapter(this, R.layout.spinner_item_units, unitList)

        //Connect each spinner to its respective adapter.
        conversionChoiceSpinner.adapter = choiceAdapter
        unitsBeforeSpinner.adapter = beforeAdapter
        unitsAfterSpinner.adapter = afterAdapter

        conversionChoiceSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {
                val s = conversionChoiceSpinner.getItemAtPosition(arg2).toString()
                when (s){
                    "Speed" -> converter = SpeedConversion()
                    "Distance" -> converter = LengthConversion()
                    "Weight" -> converter = WeightConversion()
                    "Time" -> converter = TimeConversion()
                    "Volume" -> converter = VolumeConversion()
                }
                unitList.clear()
                for(m in converter.getMap())
                    unitList.add(m.key)
                unitsBeforeSpinner.setSelection(0)
                beforeAdapter.notifyDataSetChanged()
                afterAdapter.notifyDataSetChanged()
            }
            override fun onNothingSelected(arg0: AdapterView<*>) {
                // TODO Auto-generated method stub
            }
        }
    }

    override fun setValue(value: String, context: Context) {
        before.text = value
    }

    // used only by Robolectric
    override fun setValueDouble(d: Double) {
        calc.setValue(Formatter.doubleToString(d))
    }

    override fun setFormula(value: String, context: Context) {
        if(value == ""){
            before.text = ""
        }
        else{
            before.text = before.text.toString() + value
        }
    }
    private fun checkHaptic(view: View) {
        if (vibrateOnButtonPress) {
            view.performHapticFeedback()
        }
    }

    private fun shortenBig(inStr: String) : String{
        var res = inStr
        if (res.count() > 7){
            var exp = -1
            while (res.count() > 5){
                if (exp >= 0){
                    exp++
                }
                if (res.endsWith('.')){
                    exp++
                }
                res = res.dropLast(1)
            }

            if (exp > 0){
                res = res.plus('E')
                res = res.plus(exp)
            }
            else if (res.endsWith('.')){
                toast(res, 5)
                res = res.dropLast(1)
            }
        }
        return res
    }
	
	private fun shortenSmall(inStr: String) : String {
        var res = inStr
        if (res.count() > 7){
            var exp = 1
            while (res.count() > 4){
                if (!res.startsWith('0') && exp < 1){
                    var oldFirst = res.get(0)
                    res = oldFirst + "." + res.substring(1, res.count() - 1)
                    break
                }
                if (exp < 1){
                    exp--
                }
                if (res.startsWith('.')){
                    exp--
                }
                res = res.drop(1)
            }

            res = res.dropLast(res.count() - 4)
            if (exp < 0){
                res = res.plus('E')
                res = res.plus(exp)
            }
        }
        toast(res, 2)
        return res
    }
}
