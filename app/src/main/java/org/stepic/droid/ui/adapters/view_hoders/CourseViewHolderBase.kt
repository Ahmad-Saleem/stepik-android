package org.stepic.droid.ui.adapters.view_hoders

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class CourseViewHolderBase(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun setDataOnView(position: Int)
}