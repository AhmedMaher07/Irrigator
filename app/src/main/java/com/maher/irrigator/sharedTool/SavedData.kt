package com.maher.irrigator.sharedTool

import android.content.Context
import com.maher.irrigator.model.kc.KcPlant
import com.maher.irrigator.model.l.LPlant

object SavedData {

    const val TAG_SAVE_FIRST_L = "data_l_first"
    const val TAG_SAVE_SECOND_L = "data_l_second"
    const val TAG_SAVE_THIRD_L = "data_l_third"
    const val TAG_SAVE_FIRST_KC = "data_kc_first"
    const val TAG_SAVE_SECOND_KC = "data_kc_second"
    const val TAG_SAVE_THIRD_KC = "data_kc_third"


    fun saveDataObject(context: Context?, myObject: Any?, tag: String) {
        SharedPreferencesTool.saveObject(context, tag, myObject)
    }

    fun getL(context: Context?, tag: String): LPlant {
        return SharedPreferencesTool.getObject(context, tag, LPlant::class.java)
    }

    fun checkL(context: Context?, tag: String): Boolean {
        return SharedPreferencesTool.getObject(context, tag, LPlant::class.java) != null
    }

    fun getKc(context: Context?, tag: String): KcPlant {
        return SharedPreferencesTool.getObject(context, tag, KcPlant::class.java)
    }

    fun checkKc(context: Context?, tag: String): Boolean {
        return SharedPreferencesTool.getObject(context, tag, KcPlant::class.java) != null
    }

    fun clearData(context: Context, tag: String) {
        SharedPreferencesTool.clearObject(context, tag)
    }
}
