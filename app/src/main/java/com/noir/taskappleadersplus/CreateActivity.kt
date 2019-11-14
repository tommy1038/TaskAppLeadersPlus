package com.noir.taskappleadersplus

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_create.*
import java.text.SimpleDateFormat
import java.util.*

class CreateActivity : AppCompatActivity() {

  private val realm: Realm by lazy {
    Realm.getDefaultInstance()
  }

  private var mYear: Int = 0
  private var mMonth: Int = 0
  private var mDay: Int = 0
  private var mHour: Int = 0
  private var mMinute: Int = 0

  private val mOnDateClickListener = View.OnClickListener {
    val datePickerDialog = DatePickerDialog(
      this@CreateActivity,
      DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        mYear = year
        mMonth = monthOfYear
        mDay = dayOfMonth
        val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
        dateButton.setText(dateString)
      }, mYear, mMonth, mDay
    )
    datePickerDialog.show()
  }

  private val mOnTimeClickListener = View.OnClickListener {
    val timePickerDialog = TimePickerDialog(
      this@CreateActivity,
      TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        mHour = hourOfDay
        mMinute = minute
        val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
        timeButton.text = timeString
      }, mHour, mMinute, false
    )
    timePickerDialog.show()
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create)

    createFab.setOnClickListener {
      createTask()

      // 画面を終了する
      finish()
    }

    // 日付処理の追加
    dateButton.setOnClickListener(mOnDateClickListener)
    timeButton.setOnClickListener(mOnTimeClickListener)

    // 新規作成の場合
    val calendar = Calendar.getInstance()
    mYear = calendar.get(Calendar.YEAR)
    mMonth = calendar.get(Calendar.MONTH)
    mDay = calendar.get(Calendar.DAY_OF_MONTH)
    mHour = calendar.get(Calendar.HOUR_OF_DAY)
    mMinute = calendar.get(Calendar.MINUTE)
  }

  // EditText に入力されたデータを元にTaskを作る
  private fun createTask() {
    // タイトルを取得する
    val title = titleEditText.text.toString()
    // 日付を取得
    val date = Date()
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPANESE)
    val updateDate = simpleDateFormat.format(date)
    //内容を取得
    val content = contentEditText.text.toString()

    // 保存する
    save(title, updateDate, content)
  }

  // データをRealmに保存する
  private fun save(title: String, updateDate: String, content: String) {

    // 期限などの日付の情報の取得
    val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)

    val taskRealmResults = realm.where(Task::class.java).findAll()

    // 識別子の設定
    val identifier = if (taskRealmResults.max("id") != null) {
      taskRealmResults.max("id")!!.toInt() + 1
    } else {
      0
    }

    //メモを保存する
    realm.executeTransaction { realm ->
      val task = realm.createObject(Task::class.java, identifier)
      task.updateDate = updateDate
      task.title = title
      task.content = content
      task.isChecked = false

      // 通知時間の処理の追加
      val date = calendar.time
      task.date = date

//      task.id = identifier
    }

    val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
    resultIntent.putExtra(MainActivity.EXTRA_TASK, identifier)
    val resultPendingIntent = PendingIntent.getBroadcast(
      this,
      identifier,
      resultIntent,
      PendingIntent.FLAG_UPDATE_CURRENT
    )

    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
  }


  override fun onDestroy() {
    super.onDestroy()

    realm.close()
  }
}
