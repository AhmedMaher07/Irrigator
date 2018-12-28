package com.maher.irrigator

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.maher.irrigator.model.kc.KC
import com.maher.irrigator.model.kc.KcPlant
import com.maher.irrigator.model.l.L
import com.maher.irrigator.model.l.LPlant
import com.maher.irrigator.sharedTool.SavedData
import com.maher.irrigator.widget.spinner.SpinnerAdapter
import com.maher.irrigator.widget.spinner.SpinnerTextView
import com.maher.irrigator.widget.spinner.SpinnerTextViewBaseAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*


class HomeAdapter(context: Context?) : EmptyBaseAdapter<L>(context) {

    private var kcData: KC? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_area, parent, false))
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        holder.onBindData(position)
    }

    fun setKcData(data: KC) {
        kcData = data;
        retrofit()
    }

    inner class MyViewHolder(itemView: View) : GenericViewHolder(itemView), SpinnerTextViewBaseAdapter.OnItemSelectedListener {
        private val context: Context = inflater.context
        private val spinnerTextView: SpinnerTextView<LPlant> = itemView.findViewById(R.id.spinner)
        private val date: EditText = itemView.findViewById(R.id.date)
        private val count: EditText = itemView.findViewById(R.id.count)
        private val flow: EditText = itemView.findViewById(R.id.flow)
        private val start: Button = itemView.findViewById(R.id.start)
        private val reset: Button = itemView.findViewById(R.id.reset)
        private val proceed: Button = itemView.findViewById(R.id.proceed)
        private val calendar = Calendar.getInstance()
        private val dateFormat = SimpleDateFormat("dd - MM -yyyy")

        private var adapter = SpinnerAdapter()

        init {
            date.showSoftInputOnFocus = false
            start.setOnClickListener { onStart() }
            reset.setOnClickListener { onReset() }
            proceed.setOnClickListener { onProceed() }
            adapter.setListener(this)
            spinnerTextView.setAdapter(adapter)
        }

        private fun onStart() {
            when (adapterPosition) {
                0 -> startOperation(BuildConfig.WRITE_KEY_ONE, context)
                1 -> startOperation(BuildConfig.WRITE_KEY_ONE, context)
                2 -> startOperation(BuildConfig.WRITE_KEY_THIRD, context)
            }
        }

        private fun onProceed() {
            when (adapterPosition) {
                0 -> proceed(context, flow, count, spinnerTextView, SavedData.TAG_SAVE_FIRST_L, SavedData.TAG_SAVE_FIRST_KC, calendar, 0)
                1 -> proceed(context, flow, count, spinnerTextView, SavedData.TAG_SAVE_SECOND_L, SavedData.TAG_SAVE_SECOND_KC, calendar, 1)
                2 -> proceed(context, flow, count, spinnerTextView, SavedData.TAG_SAVE_THIRD_L, SavedData.TAG_SAVE_THIRD_KC, calendar, 2)
            }
        }

        private fun onReset() {
            when (adapterPosition) {
                0 -> onReset(context, SavedData.TAG_SAVE_FIRST_L, 0)
                1 -> onReset(context, SavedData.TAG_SAVE_SECOND_L, 1)
                2 -> onReset(context, SavedData.TAG_SAVE_THIRD_L, 2)
            }
        }

        override fun onBindData(position: Int) {
            getData()?.let { adapter.setItems(it.plants) }
            when (position) {
                0 -> validator(context, date, count, flow, spinnerTextView, SavedData.TAG_SAVE_FIRST_L, calendar, dateFormat)
                1 -> validator(context, date, count, flow, spinnerTextView, SavedData.TAG_SAVE_SECOND_L, calendar, dateFormat)
                2 -> validator(context, date, count, flow, spinnerTextView, SavedData.TAG_SAVE_THIRD_L, calendar, dateFormat)
            }
        }

        override fun onItemSelected(any: Any, title: String, position: Int) {
            spinnerTextView.notifySelectedItem(title, position)
        }
    }

    private fun validator(
        context: Context?,
        date: EditText,
        count: EditText,
        flow: EditText,
        spinnerTextView: SpinnerTextView<LPlant>,
        tag: String,
        calendar: Calendar,
        dateFormat: SimpleDateFormat
    ) {
        if (SavedData.checkL(context, tag)) {
            date.setOnClickListener(null)
            with(SavedData.getL(context, tag)) {
                calendar.set(Calendar.DAY_OF_YEAR, this.date)
                count.setText(this.count.toString())
                flow.setText(this.flow.toString())
                date.setText(dateFormat.format(calendar.time))
                spinnerTextView.notifySelectedItem(this.name, 0)
                spinnerTextView.isEnabled = false
            }
        } else {
            count.text = null
            date.text = null
            flow.text = null
            date.setOnClickListener { onDate(context, date, calendar, dateFormat) }
            spinnerTextView.notifyReset()
        }
    }

    private fun onDate(context: Context?, date: EditText, calendar: Calendar, dateFormat: SimpleDateFormat) {
        val datePickerDialog = DatePickerDialog(
            context, R.style.DatePickerDialogTheme,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                date.setText(dateFormat.format(calendar.time))

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun proceed(context: Context?, flow: EditText, count: EditText, spinnerTextView: SpinnerTextView<LPlant>, lTag: String, kcTag: String, calendar: Calendar, position: Int) {
        if (spinnerTextView.getSelectedItem() != null) {
            with(spinnerTextView.getSelectedItem()) {
                this?.let {
                    if (spinnerTextView.isEnabled && count.length() > 0 && flow.length() > 0) {
                        SavedData.saveDataObject(context, LPlant(it.dev, it.init, it.late, it.mid, this.name, count.text.toString().toInt(), calendar.get(Calendar.DAY_OF_YEAR), flow.text.toString().toInt()), lTag)
                        with(kcData?.plants?.get(position)) {
                            this?.let {
                                SavedData.saveDataObject(context, KcPlant(it.end, it.ini, it.mid, it.name), kcTag)
                            }
                        }
                    }
                }
            }
        }
        context?.startActivity(Intent(context, DetailsActivity::class.java).putExtra("lKey", lTag).putExtra("kcKey", kcTag).putExtra("area", position))
        notifyItemChanged(position)
    }

    private fun onReset(context: Context, tag: String, position: Int) {
        SavedData.clearData(context, tag)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int = 3

    private lateinit var thingSpeak: thingSpeakService

    private fun retrofit() {
        val client = OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        thingSpeak = retrofit.create(thingSpeakService::class.java)
    }

    fun startOperation(key: String, context: Context) {
        thingSpeak.setOperation(key, 1).enqueue(object : Callback<Int> {
            override fun onFailure(call: Call<Int>, t: Throwable) {
            }

            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    if (response.body()!! > 0) {
                        Toast.makeText(context.applicationContext, "Operation Started", Toast.LENGTH_SHORT).show()
                    } else {
                        call.clone().enqueue(this)
                    }
                } else {
                    call.clone().enqueue(this)
                }
            }
        })
    }
}
