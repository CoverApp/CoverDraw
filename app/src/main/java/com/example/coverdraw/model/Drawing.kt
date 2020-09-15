package com.example.coverdraw.model

import android.graphics.Path
import android.graphics.Point

class Drawing(var color: Int, var strokeWidth: Float? = null, var path: Path? = null, var point: Point, val stencil: Stencil? = null) {
}