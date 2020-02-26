package com.noir.taskappleadersplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

  private val realm: Realm by lazy {
    Realm.getDefaultInstance()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detail)

    showData()

    updateFab.setOnClickListener {
      update()

      // 画面を終了する
      finish()
    }
  }

  private fun update() {
    val task = realm.where(Task::class.java).equalTo(
      "updateDate", intent.getStringExtra("updateDate")
    ).findFirst()

    task?.let { mTask ->

      // 更新する
      realm.executeTransaction {
        mTask.title = titleEditText.text.toString()
        mTask.content = contentEditText.text.toString()
      }

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
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    // realmを閉じる
    realm.close()
  }
}
