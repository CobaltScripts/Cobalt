package org.cobalt.ui.helper

fun <T> layoutMasonryColumns(
  items: List<T>,
  columns: Int,
  startY: Float,
  gap: Float,
  columnX: (col: Int) -> Float,
  heightOf: (T) -> Float,
  place: (item: T, x: Float, y: Float) -> Unit,
): Float {
  val columnY = FloatArray(columns) { startY }

  items.forEachIndexed { index, item ->
    val col = index % columns
    place(item, columnX(col), columnY[col])
    columnY[col] += heightOf(item) + gap
  }

  return columnY.max()
}
