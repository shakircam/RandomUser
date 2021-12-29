package com.itmedicus.randomuser.ui.activity

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings.System.getString
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.itmedicus.randomuser.R
import com.itmedicus.randomuser.data.local.UserDatabase
import com.itmedicus.randomuser.databinding.ActivityAlarmCreateBinding
import com.itmedicus.randomuser.model.AlarmTime
import com.itmedicus.randomuser.model.MultipleAlarm
import com.itmedicus.randomuser.ui.fragment.AlarmDialogFragment
import com.itmedicus.randomuser.ui.fragment.WeekDialogFragment
import kotlinx.coroutines.launch
import java.util.*
import java.util.UUID




class AlarmCreateActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityAlarmCreateBinding
    lateinit var context: Context
    lateinit var alarmManager: AlarmManager
    private val CHANNEL_ID = "1000"
    var repReqCode = 0
    private lateinit var picker : MaterialTimePicker
    private lateinit var calender : Calendar
    private var time = ""
    private var status = ""
    private var title = ""
    private var calenderTime = 0L
    var alarmList = mutableListOf<AlarmTime>()
    val stringBuilder = StringBuilder()
    var titleList = arrayOf("Napa", "Ciprocin", "Emcil", "Filmet", "Imotin", "Flazil")
    val NEW_SPINNER_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        binding.timeTv.setOnClickListener {
            showAlarmDialog()
        }

        binding.weekTv.setOnClickListener {
            showAlarmWeekDialog()
        }

        createNotificationChannel()

        binding.showTime.setOnClickListener {
            showTimePicker()
        }

            val intent = intent
            val sat = intent.getStringExtra("saturday")
            val sun = intent.getStringExtra("sunday")
            val mon = intent.getStringExtra("monday")
            val tue = intent.getStringExtra("tuesday")
            val wed = intent.getStringExtra("wednesday")
            val thu = intent.getStringExtra("thursday")
            val fri = intent.getStringExtra("friday")


        //.....
        val sharedPreferences = getSharedPreferences("my_sharedPreference",0)
        val editor = sharedPreferences.edit()
        val fk_id : Long = Calendar.getInstance().timeInMillis +2
        editor.putLong("id",fk_id)
        editor.apply()


        val spinner: Spinner = findViewById(R.id.titleTv)
        spinner.onItemSelectedListener = this
        ArrayAdapter(this,android.R.layout.simple_spinner_item,titleList
        ).also {adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        binding.saveBt.setOnClickListener {

            if (binding.selectBt.isChecked){


                if (sat == "1"){
                    setRepeatingSaturdayAlarm()
                    status = "Saturday"
                    stringBuilder.append(status+" ")
                    Log.d("tag","saturday")
                }
                if (sun == "2"){
                    setRepeatingSundayAlarm()
                    status = "Sunday"
                    stringBuilder.append(status+" ")
                    Log.d("tag","sunday")
                }
                if (mon == "3"){
                    setRepeatingMondayAlarm()
                    status = "Monday"
                    stringBuilder.append(status+" ")
                    Log.d("tag","monday")
                }
                if (tue == "4"){
                    setRepeatingTuesdayAlarm()
                    status = "Tuesday"
                    stringBuilder.append(" $status")
                    Log.d("tag","tuesday")
                }
                if (wed == "5"){
                    setRepeatingWednesdayAlarm()
                    status = "Wednesday"
                    stringBuilder.append(status+" ")
                    Log.d("tag","wednesday")
                }
                if (thu == "6"){
                    setRepeatingThursdayAlarm()
                    status = "Thursday"
                    stringBuilder.append(status+" ")
                    Log.d("tag","thursday")
                }
                if (fri == "7"){
                    setRepeatingFridayAlarm()
                    status = "Friday"
                    stringBuilder.append(status)
                    Log.d("tag","friday")
                }
            }else{

                repeatingAlarm()
                status = "The Alarm will continue"
                stringBuilder.append(status+"")
                Log.d("tag","repeating alarm")
            }

            Toast.makeText(this,"alarm set successfully", Toast.LENGTH_SHORT).show()
           // title = binding.titleTv.text.toString()
            val calenderTime = calender.timeInMillis

            val id = sharedPreferences.getLong("id",fk_id)
            val alarmTime= AlarmTime(time,id,calenderTime,title,repReqCode,stringBuilder.toString(),true)
            alarmList.add(alarmTime)
            val db = UserDatabase.getDatabase(this).userDao()
            lifecycleScope.launch {
                db.insertAlarmTime(alarmTime)
            }
            editor.clear()
            editor.commit()
            val intent = Intent(this,ShowAlarmActivity::class.java)
            startActivity(intent)
        }

    }

    private fun repeatingAlarm(){
        calenderTime = calender.timeInMillis
        val thuReq : Long = Calendar.getInstance().timeInMillis +1
         repReqCode = thuReq.toInt()

        val intent = Intent(this, AlarmReceiver::class.java)
        val  pendingIntent = PendingIntent.getBroadcast(this,repReqCode,intent,0)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
            // Interval one day
            24*60*60*1000,
            pendingIntent
        )

        Log.d("this", "When alarm will ring...$calenderTime")
        Log.d("this", "Current time when alarm create"+System.currentTimeMillis().toString())
        Log.d("this", "alarm create :::request code::$repReqCode")
        Toast.makeText(this,"alarm set successfully",Toast.LENGTH_SHORT).show()
    }


    // saturday
    private fun setRepeatingSaturdayAlarm(){
        calenderTime = calender.timeInMillis
        val thuReq : Long = Calendar.getInstance().timeInMillis +1
         repReqCode = thuReq.toInt()


        calender = Calendar.getInstance()
        calender.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY)
        val day = "Saturday"
        if(calender.timeInMillis < System.currentTimeMillis()) {
            calender.add(Calendar.DAY_OF_YEAR, 7)
        }

        val intent = Intent(this,AlarmReceiver::class.java)
        val  pendingIntent = PendingIntent.getBroadcast(this,repReqCode,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
             //AlarmManager.INTERVAL_DAY * 7,
            24*60*60*1000 * 7,
            pendingIntent
        )
        Log.d("this", "alarm created")
        val sharedPreferences = getSharedPreferences("my_sharedPreference",0)
        val id = sharedPreferences.getLong("id",-1)

        val multipleAlarm = MultipleAlarm(time,calenderTime,id,day,repReqCode)
        val db = UserDatabase.getDatabase(this).userDao()
        lifecycleScope.launch {
            db.insertMultipleAlarm(multipleAlarm)
        }
        Log.d("code", repReqCode.toString())
        Toast.makeText(this,"alarm set successfully",Toast.LENGTH_SHORT).show()
    }

    // sunday
    private fun setRepeatingSundayAlarm(){
        calenderTime = calender.timeInMillis
        val thuReq : Long = Calendar.getInstance().timeInMillis +2
         repReqCode = thuReq.toInt()

        calender = Calendar.getInstance()
        calender.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY)
        val day = "Sunday"
        if(calender.timeInMillis < System.currentTimeMillis()) {
            calender.add(Calendar.DAY_OF_YEAR, 7)
        }

        val intent = Intent(this,AlarmReceiver::class.java)
        val  pendingIntent = PendingIntent.getBroadcast(this,repReqCode,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
            //AlarmManager.INTERVAL_DAY * 7,
            24*60*60*1000 * 7,
            pendingIntent
        )
        Log.d("this", "alarm created")
        val sharedPreferences = getSharedPreferences("my_sharedPreference",0)
        val id = sharedPreferences.getLong("id",-1)
        val multipleAlarm = MultipleAlarm(time,calenderTime,id,day,repReqCode)
        val db = UserDatabase.getDatabase(this).userDao()
        lifecycleScope.launch {
            db.insertMultipleAlarm(multipleAlarm)
        }
        Log.d("code", repReqCode.toString())
        Toast.makeText(this,"alarm set successfully",Toast.LENGTH_SHORT).show()
    }

    // monday
    private fun setRepeatingMondayAlarm(){
        calenderTime = calender.timeInMillis
        val thuReq : Long = Calendar.getInstance().timeInMillis +3
         repReqCode = thuReq.toInt()
        calender = Calendar.getInstance()
        calender.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY)
        val day = "Monday"
        if(calender.timeInMillis < System.currentTimeMillis()) {
            calender.add(Calendar.DAY_OF_YEAR, 7)
        }

        val intent = Intent(this,AlarmReceiver::class.java)
        val  pendingIntent = PendingIntent.getBroadcast(this,repReqCode,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
            //AlarmManager.INTERVAL_DAY * 7,
            24*60*60*1000 * 7,
            pendingIntent
        )
        val sharedPreferences = getSharedPreferences("my_sharedPreference",0)
        val id = sharedPreferences.getLong("id",-1)
        val multipleAlarm = MultipleAlarm(time,calenderTime,id,day,repReqCode)
        val db = UserDatabase.getDatabase(this).userDao()
        lifecycleScope.launch {
            db.insertMultipleAlarm(multipleAlarm)
        }
        Log.d("code", repReqCode.toString())
        Toast.makeText(this,"alarm set successfully",Toast.LENGTH_SHORT).show()
    }

    // tuesday
    private fun setRepeatingTuesdayAlarm(){
        calenderTime = calender.timeInMillis
        val thuReq : Long = Calendar.getInstance().timeInMillis +4
         repReqCode = thuReq.toInt()
        calender = Calendar.getInstance()
        calender.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY)
        val day = "Tuesday"
        if(calender.timeInMillis < System.currentTimeMillis()) {
            calender.add(Calendar.DAY_OF_YEAR, 7)
        }

        val intent = Intent(this,AlarmReceiver::class.java)
        val  pendingIntent = PendingIntent.getBroadcast(this,repReqCode,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
            //AlarmManager.INTERVAL_DAY * 7,
            24*60*60*1000 * 7,
            pendingIntent
        )
        val sharedPreferences = getSharedPreferences("my_sharedPreference",0)
        val id = sharedPreferences.getLong("id",-1)
        val multipleAlarm = MultipleAlarm(time,calenderTime,id,day,repReqCode)
        val db = UserDatabase.getDatabase(this).userDao()
        lifecycleScope.launch {
            db.insertMultipleAlarm(multipleAlarm)
        }
        Log.d("code", repReqCode.toString())
        Log.d("tag","Tuesday alarm create")
        Toast.makeText(this,"alarm set successfully",Toast.LENGTH_SHORT).show()
    }

    // wednesday
    private fun setRepeatingWednesdayAlarm(){
        calenderTime = calender.timeInMillis
        val thuReq : Long = Calendar.getInstance().timeInMillis +5
         repReqCode = thuReq.toInt()
         calenderTime = calender.timeInMillis
        calender = Calendar.getInstance()
        calender.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY)
        val day = "Wednesday"
        if(calender.timeInMillis < System.currentTimeMillis()) {
            calender.add(Calendar.DAY_OF_YEAR, 7)
        }

        val intent = Intent(this,AlarmReceiver::class.java)
        val  pendingIntent = PendingIntent.getBroadcast(this,repReqCode,intent,0)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
            //AlarmManager.INTERVAL_DAY * 7,
            24*60*60*1000 * 7,
            pendingIntent
        )
        val sharedPreferences = getSharedPreferences("my_sharedPreference",0)
        val id = sharedPreferences.getLong("id",-1)
        val multipleAlarm = MultipleAlarm(time,calenderTime,id,day,repReqCode)
        val db = UserDatabase.getDatabase(this).userDao()
        lifecycleScope.launch {
            db.insertMultipleAlarm(multipleAlarm)
        }
        Log.d("code", repReqCode.toString())
        Toast.makeText(this,"alarm set successfully",Toast.LENGTH_SHORT).show()
    }

    // thursday
    private fun setRepeatingThursdayAlarm(){
        calenderTime = calender.timeInMillis
        val thuReq : Long = Calendar.getInstance().timeInMillis +6
        repReqCode = thuReq.toInt()

        calender = Calendar.getInstance()
        calender.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY)
        val day = "Thursday"
        if(calender.timeInMillis < System.currentTimeMillis()) {
            calender.add(Calendar.DAY_OF_YEAR, 7)
        }

        val intent = Intent(this,AlarmReceiver::class.java)
        val  pendingIntent = PendingIntent.getBroadcast(this,repReqCode,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
            //AlarmManager.INTERVAL_DAY * 7,
            24*60*60*1000 * 7,
            pendingIntent
        )
        val sharedPreferences = getSharedPreferences("my_sharedPreference",0)
        val id = sharedPreferences.getLong("id",-1)
        val multipleAlarm = MultipleAlarm(time,calenderTime,id,day,repReqCode)
        val db = UserDatabase.getDatabase(this).userDao()
        lifecycleScope.launch {
            db.insertMultipleAlarm(multipleAlarm)
        }
        Log.d("code", repReqCode.toString())
        Log.d("tag","Thursday alarm create")
        Toast.makeText(this,"alarm set successfully",Toast.LENGTH_SHORT).show()
    }

    // friday
    private fun setRepeatingFridayAlarm(){
        calenderTime = calender.timeInMillis
        val thuReq : Long = Calendar.getInstance().timeInMillis +7
         repReqCode = thuReq.toInt()

        calender = Calendar.getInstance()
        calender.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY)
        val day = "Friday"
        if(calender.timeInMillis < System.currentTimeMillis()) {
            calender.add(Calendar.DAY_OF_YEAR, 7)
        }

        val intent = Intent(this,AlarmReceiver::class.java)
        val  pendingIntent = PendingIntent.getBroadcast(this,repReqCode,intent,0)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
            //AlarmManager.INTERVAL_DAY * 7,
            24*60*60*1000 * 7,
            pendingIntent
        )
        val sharedPreferences = getSharedPreferences("my_sharedPreference",0)
        val id = sharedPreferences.getLong("id",-1)
        val multipleAlarm = MultipleAlarm(time,calenderTime,id,day,repReqCode)
        val db = UserDatabase.getDatabase(this).userDao()
        lifecycleScope.launch {
            db.insertMultipleAlarm(multipleAlarm)
        }
        Log.d("code", repReqCode.toString())
        Toast.makeText(this,"alarm set successfully",Toast.LENGTH_SHORT).show()
    }


    private fun showAlarmDialog() {

        val dialogFragment = AlarmDialogFragment()
        dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)

        dialogFragment.show(supportFragmentManager, "create_note")
    }

    private fun showAlarmWeekDialog() {

        val dialogFragment = WeekDialogFragment()
        dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)

        dialogFragment.show(supportFragmentManager, "create_alarm")
    }

    private fun showTimePicker() {
        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12).setMinute(0)
            .setTitleText("Select Alarm Time")
            .build()
        picker.show(supportFragmentManager,"itmedicus")
        picker.addOnPositiveButtonClickListener {
            if (picker.hour > 12){

                binding.showTime.text = String.format("%02d",picker.hour -12)+ " : "+String.format("%02d",picker.minute) + "PM"
                 time = String.format("%02d",picker.hour -12)+ " : "+String.format("%02d",picker.minute) + "PM"
            }else{
                binding.showTime.text =  String.format("%02d",picker.hour )+ " : "+String.format("%02d",picker.minute) + "AM"
                time = String.format("%02d",picker.hour )+ " : "+String.format("%02d",picker.minute) + "AM"
            }
            calender = Calendar.getInstance()
            calender[Calendar.HOUR_OF_DAY] =  picker.hour
            calender[Calendar.MINUTE] =  picker.minute
            calender[Calendar.SECOND] = 0
            calender[Calendar.MILLISECOND] =  0


        }

    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name : CharSequence = "Alarm manager"
            val descriptionText = "Alarm is set now."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    class AlarmReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val time = System.currentTimeMillis()
            Log.d("this", "Receive alarm:::" +Date().toString()+"::: current time::: $time" )

            // Set the alarm here.
            val vibrator = context?.getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        5000,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(5000)
            }
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()

            val intent = Intent(context, ShowAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = "Cancel"
                putExtra(EXTRA_NOTIFICATION_ID, 0)
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val builder = NotificationCompat.Builder(context!!, "1000")
                .setSmallIcon(R.drawable.ic_history)
                .setContentTitle("Alarm Clock")
                .setContentText("Time to take your medicine")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_cancel, "Cancel",
                    pendingIntent)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(111, builder.build())

        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
          title = titleList[pos]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
       // TODO("Not yet implemented")
    }


}