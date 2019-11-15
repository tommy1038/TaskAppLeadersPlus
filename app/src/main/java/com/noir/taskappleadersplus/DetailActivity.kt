package com.noir.taskappleadersplus

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_detail.*
import java.util.*

class DetailActivity : AppCompatActivity() {

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
      this@DetailActivity,
      DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        mYear = year
        mMonth = monthOfYear
        mDay = dayOfMonth
        val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
        dateButton.text = dateString
      }, mYear, mMonth, mDay
    )
    datePickerDialog.show()
  }

  private val mOnTimeClickListener = View.OnClickListener {
    val timePickerDialog = TimePickerDialog(
      this@DetailActivity,
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
    setContentView(R.layout.activity_detail)

    dateButton.setOnClickListener(mOnDateClickListener)
    timeButton.setOnClickListener(mOnTimeClickListener)

    val calendar = Calendar.getInstance()
    mYear = calendar.get(Calendar.YEAR)
    mMonth = calendar.get(Calendar.MONTH)
    mDay = calendar.get(Calendar.DAY_OF_MONTH)
    mHour = calendar.get(Calendar.HOUR_OF_DAY)
    mMinute = calendar.get(Calendar.MINUTE)

    showData()

    updateFab.setOnClickListener {
      update()

      // 画面を終了する
      finish()
    }
  }

  private fun update() {
    val task = realm
      .where(Task::class.java)
      .equalTo(
        "updateDate", intent.getStringExtra("updateDate")
      ).findFirst()

    task?.let { mTask ->
      val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)

      // 更新する
      realm.executeTransaction {
        mTask.title = titleEditText.text.toString()
        mTask.content = contentEditText.text.toString()

        val date = calendar.time
        mTask.date = date
      }

      val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
      resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask.id)
      val resultPendingIntent = PendingIntent.getBroadcast(
        this,
        mTask.id,
        resultIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
      )

      Log.d("resultPendingIntent", resultPendingIntent.toString())

      val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
      alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)

      val intent = Intent()
      setResult(RESULT_OK, intent)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      // Respond to the action bar's Up/Home button
      android.R.id.home -> {
        // NavUtils.navigateUpFromSameTask(this);
        Log.d("onOptionsItemSelected", "ok")
        finish()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun showData() {
    // 値をtask変数に代入
    val task = realm.where(Task::class.java).equalTo(
      "updateDate",
      intent.getStringExtra("updateDate")
    ).findFirst()

    task?.let { mTask ->
      titleEditText.setText(mTask.title)
      contentEditText.setText(mTask.content)

      val calendar = Calendar.getInstance()
      calendar.time = mTask.date
      mYear = calendar.get(Calendar.YEAR)
      mMonth = calendar.get(Calendar.MONTH)
      mDay = calendar.get(Calendar.DAY_OF_MONTH)
      mHour = calendar.get(Calendar.HOUR_OF_DAY)
      mMinute = calendar.get(Calendar.MINUTE)

      val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
      val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
      dateButton.text = dateString
      timeButton.text = timeString
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    // realmを閉じる
    realm.close()
  }

}
