package com.noir.taskappleadersplus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_create.*
import java.text.SimpleDateFormat
import java.util.*

class CreateActivity : AppCompatActivity() {

  private val realm: Realm by lazy {
    Realm.getDefaultInstance()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create)

    createFab.setOnClickListener {
      createTask()

      // 画面を終了する
      finish()
    }
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

//      task.id = identifier
    }

  }


  override fun onDestroy() {
    super.onDestroy()

    realm.close()
  }
}
