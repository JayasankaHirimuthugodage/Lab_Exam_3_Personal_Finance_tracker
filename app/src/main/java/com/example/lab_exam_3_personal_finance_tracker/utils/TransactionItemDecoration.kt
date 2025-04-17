// Create this file: com/example/lab_exam_3_personal_finance_tracker/util/TransactionItemDecoration.kt
package com.example.lab_exam_3_personal_finance_tracker.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TransactionItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            // Add top spacing for the first item
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceHeight
            }

            left = spaceHeight
            right = spaceHeight
            bottom = spaceHeight
        }
    }
}