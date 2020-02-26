package com.noir.taskappleadersplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_task.*

class TaskActivity : AppCompatActivity() {

  // realmの準備
  private val realm: Realm by lazy {
    Realm.getDefaultInstance()
  }

  private var task: Task? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task)

    Log.d("onCreate:", "OK")

    task = realm
      .where(Task::class.java)
      .equalTo("updateDate", intent.getStringExtra("updateDate"))
      .findFirst()

    checkBox.setOnClickListener { v ->
      val checked = (v as CheckBox).isChecked

      if (checked) {
        Snackbar.make(v, "Task marked complete", Snackbar.LENGTH_SHORT)
          .setAction("Action", null).show()
      } else {
        Snackbar.make(v, "Task marked active", Snackbar.LENGTH_SHORT)
          .setAction("Action", null).show()
      }

      realm.executeTransaction {
        task?.isChecked = checked
      }
    }
    showData()

    fab.setOnClickListener {
      val intent = Intent(this@TaskActivity, DetailActivity::class.java)
      intent.putExtra("updateDate", task?.updateDate)
      startActivityForResult(intent, 1)
    }
  }

  private fun showData() {
    task?.let {
      titleTextView.text = it.title
      contentTextView.text = it.content
      checkBox.isChecked = it.isChecked
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.taskdetail_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_delete -> {
        // 削除処理
        // アラートダイアログを表示する
        task?.let { task ->
          val builder = AlertDialog.Builder(this@TaskActivity)
          builder.setTitle("削除")
          builder.setMessage(task.title + "を削除しますか")

          // OKボタン
          builder.setPositiveButton("OK") { _, _ ->
            // 削除する
            realm.executeTransaction {
              task.deleteFromRealm()
              finish()
            }
          }

          // NGボタン
          builder.setNegativeButton("CANCEL", null)

          val dialog = builder.create()
          dialog.show()
        }
      }
      else -> {
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == RESULT_OK) {
      finish()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d("onDestroy:", "OK")

    // realmを閉じる
    realm.close()
  }
}
