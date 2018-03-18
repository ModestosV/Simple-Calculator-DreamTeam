package com.simplemobiletools.calculator.conversions

/**
 * Created by George on 3/17/2018.
 */
class VolumeConversion:Converter {

    val mapOfVolumes = mapOf(
            "Litres" to 1.0,
            "Millilitres" to 1000.0
    )

    override fun calculate(beginningQty: Double?, beginningUnitType: String, endingUnitType: String): Double {
        var endingQty: Double = (1.0 / mapOfVolumes.getValue(beginningUnitType))
        endingQty *= mapOfVolumes.getValue(endingUnitType)
        if (beginningQty != null)
            endingQty *= beginningQty
        else
            return Double.NaN
        return endingQty
    }

    override fun getMap(): Map<String, Double> {
        return mapOfVolumes
    }
}