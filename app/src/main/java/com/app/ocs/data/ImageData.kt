package com.app.ocs.data

import androidx.compose.ui.graphics.Color
import com.app.ocs.ui.theme.Purple40
import com.app.ocs.ui.theme.blue
import com.app.ocs.ui.theme.green
import com.app.ocs.ui.theme.red
import com.app.ocs.ui.theme.yellow

data class ImageData(
    val licenses: List<License> = listOf(),
    val info: Info = Info(),
    val categories: List<Category> = listOf(),
    val images: List<Image> = listOf(),
    val annotations: List<Annotation> = listOf()
) {

    data class License(
        val name: String = "",
        val id: Int = 0,
        val url: String = ""
    )

    data class Info(
        val contributor: String = "",
        val date_created: String = "",
        val description: String = "",
        val url: String = "",
        val version: String = "",
        val year: String = ""
    )

    data class Category(
        val id: Int = 0,
        val name: String = "",
        val supercategory: String = ""
    )

    data class Image(
        val id: Int = 0,
        val width: Int = 0,
        val height: Int = 0,
        val file_name: String = "",
        val license: Int = 0,
        val flickr_url: String = "",
        val coco_url: String = "",
        val date_captured: Int = 0
    )

    data class Annotation(
        val id: Int = 0,
        val image_id: Int = 0,
        val category_id: Int = 0,
        val segmentation: List<List<Double>> = listOf(),
        val area: Int = 0,
        val bbox: List<Double> = listOf(),
        val iscrowd: Int = 0,
        val attributes: Attributes = Attributes()
    ) {
        data class Attributes(
            val occluded: Boolean = false
        )

        val colorHighlight
            get() = when (category_id) {
                3 -> green
                4 -> red
                6 -> yellow
                7 -> blue
                else -> {
                    Purple40
                }
            }
    }

}


fun List<ImageData>.groupCoordinatesByImgId(): List<List<Triple<Int, Color, List<Double>>>> {
    return this.map { imgData -> imgData.annotations.map { Triple(it.image_id, it.colorHighlight, it.segmentation.flatMap { it }) } }
}