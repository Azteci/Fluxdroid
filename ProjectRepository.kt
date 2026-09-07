package com.azteci.fluxdroid.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.azteci.fluxdroid.domain.CanvasDocument
import com.azteci.fluxdroid.domain.Layer
import com.azteci.fluxdroid.domain.Stroke
import com.azteci.fluxdroid.domain.StrokePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Offline-first repository. The file format is deliberately human-inspectable JSON for now. */
class ProjectRepository(private val context: Context) {
    private val root: File get() = File(context.filesDir, "projects").apply { mkdirs() }

    suspend fun save(document: CanvasDocument, thumbnail: Bitmap? = null) = withContext(Dispatchers.IO) {
        val file = File(root, "${document.id}.flux.json")
        file.writeText(encode(document).toString())
        thumbnail?.compress(Bitmap.CompressFormat.PNG, 90, File(root, "${document.id}.png").outputStream())
    }

    suspend fun load(id: String): CanvasDocument? = withContext(Dispatchers.IO) {
        val file = File(root, "$id.flux.json")
        if (!file.exists()) null else decode(JSONObject(file.readText()))
    }

    suspend fun list(): List<String> = withContext(Dispatchers.IO) {
        root.listFiles { f -> f.name.endsWith(".flux.json") }?.map { it.name.removeSuffix(".flux.json") }.orEmpty()
    }

    fun exportPng(document: CanvasDocument, target: File) {
        val bitmap = Bitmap.createBitmap(document.width, document.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(document.background.toArgb())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        document.layers.asReversed().filter { it.visible }.forEach { layer ->
            layer.strokes.forEach { stroke ->
                if (stroke.points.isEmpty()) return@forEach
                paint.color = stroke.color.toArgb()
                paint.alpha = (stroke.opacity * layer.opacity * 255f).toInt().coerceIn(0, 255)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = stroke.width
                paint.strokeCap = Paint.Cap.ROUND
                val p = android.graphics.Path()
                p.moveTo(stroke.points.first().position.x, stroke.points.first().position.y)
                stroke.points.drop(1).forEach { pt -> p.lineTo(pt.position.x, pt.position.y) }
                canvas.drawPath(p, paint)
            }
        }
        target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun encode(document: CanvasDocument): JSONObject = JSONObject().apply {
        put("id", document.id)
        put("name", document.name)
        put("width", document.width)
        put("height", document.height)
        put("background", document.background.toArgb())
        put("activeLayerId", document.activeLayerId)
        put("layers", JSONArray().apply {
            document.layers.forEach { layer -> put(encodeLayer(layer)) }
        })
    }

    private fun encodeLayer(layer: Layer): JSONObject = JSONObject().apply {
        put("id", layer.id); put("name", layer.name); put("visible", layer.visible); put("opacity", layer.opacity); put("locked", layer.locked)
        put("strokes", JSONArray().apply { layer.strokes.forEach { stroke -> put(encodeStroke(stroke)) } })
    }

    private fun encodeStroke(stroke: Stroke): JSONObject = JSONObject().apply {
        put("id", stroke.id); put("color", stroke.color.toArgb()); put("width", stroke.width); put("opacity", stroke.opacity); put("tool", stroke.tool.name)
        put("points", JSONArray().apply { stroke.points.forEach { p -> put(JSONObject().put("x", p.position.x).put("y", p.position.y).put("pressure", p.pressure).put("tilt", p.tilt)) } })
    }

    private fun decode(obj: JSONObject): CanvasDocument {
        val layersArray = obj.optJSONArray("layers") ?: JSONArray()
        val layers = buildList {
            for (i in 0 until layersArray.length()) {
                val o = layersArray.getJSONObject(i)
                val strokes = buildList {
                    val s = o.optJSONArray("strokes") ?: JSONArray()
                    for (j in 0 until s.length()) {
                        val so = s.getJSONObject(j)
                        val pts = buildList {
                            val p = so.optJSONArray("points") ?: JSONArray()
                            for (k in 0 until p.length()) {
                                val po = p.getJSONObject(k)
                                add(StrokePoint(androidx.compose.ui.geometry.Offset(po.getDouble("x").toFloat(), po.getDouble("y").toFloat()), po.optDouble("pressure", 1.0).toFloat(), po.optDouble("tilt", 0.0).toFloat()))
                            }
                        }
                        add(Stroke(so.getString("id"), pts, androidx.compose.ui.graphics.Color(so.getInt("color")), so.getDouble("width").toFloat(), so.getDouble("opacity").toFloat(), com.azteci.fluxdroid.domain.Tool.valueOf(so.getString("tool"))))
                    }
                }
                add(Layer(o.getString("id"), o.getString("name"), o.optBoolean("visible", true), o.optDouble("opacity", 1.0).toFloat(), o.optBoolean("locked", false), strokes))
            }
        }
        return CanvasDocument(obj.getString("id"), obj.getString("name"), obj.getInt("width"), obj.getInt("height"), androidx.compose.ui.graphics.Color(obj.getInt("background")), layers, obj.optString("activeLayerId"), null).normalized()
    }
}
