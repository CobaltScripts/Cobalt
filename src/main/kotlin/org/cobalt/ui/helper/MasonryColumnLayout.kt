package org.cobalt.ui.helper

/**
 * Lays out [items] into [columns] independently-flowing columns (item i goes to column i % columns), each
 * advancing its own running Y by the item's height + [gap]. Returns the tallest column's final Y, so callers
 * can size scroll content from it.
 */
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
