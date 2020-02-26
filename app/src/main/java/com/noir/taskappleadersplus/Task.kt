package com.noir.taskappleadersplus

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

// id をプライマリーキーとして設定
open class Task(
  @PrimaryKey
  open var id: Int = 0,
  open var title: String = "",
  open var updateDate: String = "",
  open var content: String = "",
  open var isChecked: Boolean = false
) : RealmObject(), Serializable