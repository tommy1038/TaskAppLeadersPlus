package com.noir.taskappleadersplus

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), TaskAdapter.OnCheckClickListener {

  companion object {
    const val EXTRA_TASK: String = "jp.tomiyama.noir.taskappleaders.TASK"
  }

  // realmの準備
  private val realm: Realm by lazy {
    Realm.getDefaultInstance()
  }

  private val mRealmListener: RealmChangeListener<Realm> = RealmChangeListener {
    setTaskList()
  }

  override fun onCheckClick(updateDate: String) {
    val realmTask = realm
      .where(Task::class.java)
      .equalTo("updateDate", updateDate)
      .findFirst()

    realm.executeTransaction {
      realmTask?.let {
        it.isChecked = !(it.isChecked)
      }
    }
  }

  // タスク分類用変数(0: 全て選択，1: 達成済，2: 未達成)
  private var mode: Int = 0

  private lateinit var tasks: List<Task>
  lateinit var adapter: TaskAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    realm.addChangeListener(mRealmListener)
    fab.setOnClickListener {
      val intent = Intent(applicationContext, CreateActivity::class.java)
      startActivity(intent)
    }

    tasks = getTasks()
    // 表示の切替え
    switchView(tasks)

    // アダプターの用意
    adapter = TaskAdapter(tasks)
    // アダプターにクリックリスナーを設定
    // チェックボックス周りの処理
    adapter.setOnCheckClickListener(this)
    // LinearLayoutManagerオブジェクトを生成
    val manager = LinearLayoutManager(applicationContext)
    // RecyclerViewにレイアウトマネージャーとしてLinearLayoutManagerを設定
    recyclerView.layoutManager = manager
    // RecyclerViewにアダプタオブジェクトを設定
    recyclerView.adapter = adapter

    // 区切り専用のオブジェクトを生成
    val decorator = DividerItemDecoration(applicationContext, manager.orientation)
    // RecyclerViewに区切り線オブジェクトを設定
    recyclerView.addItemDecoration(decorator)

  }

  private fun getTasks(): List<Task> {
    when (mode) {
      0 -> statusText.text = "All TO-DOs"
      1 -> statusText.text = "Completed TO-DOs"
      2 -> statusText.text = "Active TO-DOs"
    }

    val results: RealmResults<Task> = when (mode) {
      // 全タスク
      0 -> {
        realm.where(Task::class.java)
          .findAll()
          .sort("updateDate", Sort.ASCENDING)
      }
      // 達成済みタスク
      1 -> {
        realm.where(Task::class.java)
          .equalTo("isChecked", true)
          .findAll()
          .sort("updateDate", Sort.ASCENDING)
      }
      // 未達成タスク
      2 -> {
        realm.where(Task::class.java)
          .equalTo("isChecked", false)
          .findAll()
          .sort("updateDate", Sort.ASCENDING)
      }
      else -> return mutableListOf()
    }

    // realmからtaskListを読み取る
    tasks = realm.copyFromRealm(results)
    return tasks
  }

  private fun switchView(tasks: List<Task>) {
    // 表示の切り替え
    if (tasks.isEmpty()) {
      imageView.visibility = View.VISIBLE
      explainText.visibility = View.VISIBLE
      explainText.text = "You have no TO-DOs!"
      statusText.visibility = View.INVISIBLE
    } else {
      imageView.visibility = View.INVISIBLE
      explainText.visibility = View.INVISIBLE
      statusText.visibility = View.VISIBLE
    }
  }

  override fun onResume() {
    super.onResume()
    setTaskList()
  }


  private fun setTaskList() {
    val results: RealmResults<Task> = when (mode) {
      // 全タスク
      0 -> {
        realm.where(Task::class.java)
          .findAll()
          .sort("updateDate", Sort.ASCENDING)
      }
      // 達成済みタスク
      1 -> {
        realm.where(Task::class.java)
          .equalTo("isChecked", true)
          .findAll()
          .sort("updateDate", Sort.ASCENDING)
      }
      // 未達成タスク
      2 -> {
        realm.where(Task::class.java)
          .equalTo("isChecked", false)
          .findAll()
          .sort("updateDate", Sort.ASCENDING)
      }
      else -> return
    }

    // realmからtaskListを読み取る
    tasks = realm.copyFromRealm(results)

    // 表示の切替え
    switchView(tasks)

    // データ更新時の処理
    adapter.update(tasks)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.filter_tasks, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {

    when (item.itemId) {
      R.id.active -> {
        // 未達成タスクの表示
        Toast.makeText(applicationContext, "active", Toast.LENGTH_SHORT).show()
        statusText.text = "Active TO-DOs"
        mode = 2
        setTaskList()
      }
      R.id.completed -> {
        // 達成済タスクの表示
        Toast.makeText(applicationContext, "completed", Toast.LENGTH_SHORT).show()
        statusText.text = "Completed TO-DOs"
        mode = 1
        setTaskList()
      }
      R.id.all -> {
        // 全てのタスクの表示
        Toast.makeText(applicationContext, "all", Toast.LENGTH_SHORT).show()
        statusText.text = "All TO-DOs"
        mode = 0
        setTaskList()
      }
      else -> {
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onDestroy() {
    super.onDestroy()

    // realmを閉じる
    realm.close()
  }
}
