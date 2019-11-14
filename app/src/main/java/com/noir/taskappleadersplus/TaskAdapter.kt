package com.noir.taskappleadersplus

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_task.view.*
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(private var mTasks: List<Task>) : RecyclerView.Adapter<TaskAdapter.RecyclerViewHolder>() {

  private var onCheckClickListener: OnCheckClickListener? = null

  interface OnCheckClickListener {
    fun onCheckClick(updateDate: String)
  }

  fun setOnCheckClickListener(onCheckClickListener: OnCheckClickListener) {
    this.onCheckClickListener = onCheckClickListener
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
    // レイアウトインフレータを取得
    val layoutInflater = LayoutInflater.from(parent.context)
    // layout_task.xmlをインフレートし，1行分の画面部品とする
    val view = layoutInflater.inflate(R.layout.layout_task, parent, false)
    // インフレートされた1行分の画面部品にリスナを設定
//    view.setOnClickListener(ItemClickListener())
    // ビューホルダーオブジェクトを生成
    val holder = RecyclerViewHolder(view)
    // 生成したビューホルダーをリターン
    return holder
  }

  override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
    // リストデータから該当1行分のデータを取得
    val task = mTasks[position]
    holder.titleTextView.text = task.title
    holder.checkBox.isChecked = task.isChecked

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
    val date = task.date
    holder.timestampTextView.text = simpleDateFormat.format(date)

    // チェック選択時の色変更
    if (task.isChecked) {
      holder.linearLayout.setBackgroundColor(Color.parseColor("#CCCCCC"))
    } else {
      holder.linearLayout.setBackgroundColor(Color.parseColor("#00000000"))
    }

    holder.linearLayout.setOnClickListener {
      // タップ時の色変更
      holder.linearLayout.setBackgroundColor(Color.parseColor("#CCCCCC"))
      // Intentの処理
      val intent = Intent(it.context, TaskActivity::class.java)
      // 日時データをintentに詰めて渡す
      intent.putExtra("updateDate", task.updateDate)
      it.context.startActivity(intent)
    }

    holder.checkBox.setOnClickListener {
      val checked = (it as CheckBox).isChecked
      if (checked) {
        // チェック時の色変更
        holder.linearLayout.setBackgroundColor(Color.parseColor("#CCCCCC"))
        Snackbar.make(it, "Task marked complete", Snackbar.LENGTH_SHORT)
          .setAction("Action", null).show()

        // MainActivityでRealmにチェックボッスク周りの処理をさせる
        onCheckClickListener?.onCheckClick(task.updateDate)
      } else {
        holder.linearLayout.setBackgroundColor(Color.parseColor("#00000000"))
        Snackbar.make(it, "Task marked active", Snackbar.LENGTH_SHORT)
          .setAction("Action", null).show()

        // MainActivityでRealmにチェックボッスク周りの処理をさせる
        onCheckClickListener?.onCheckClick(task.updateDate)
      }
    }

  }

//  private inner class ItemClickListener : View.OnClickListener {
//    override fun onClick(view: View) {
//      // クリック処理の移植
//    }
//  }

  override fun getItemCount(): Int {
    return mTasks.size
  }

  inner class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val linearLayout: LinearLayout = view.linearLayout
    var titleTextView: TextView = view.titleText
    val timestampTextView: TextView = view.timestamp
    var checkBox: CheckBox = view.checkBox
  }
}
