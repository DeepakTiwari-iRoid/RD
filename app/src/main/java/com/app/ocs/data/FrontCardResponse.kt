package com.app.ocs.data


import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class FrontCardResponse(
    @SerializedName("BL_corner")
    val bLCorner: Int = 0,
    @SerializedName("BR_corner")
    val bRCorner: Int = 0,
    @SerializedName("Bottom centering")
    val bottomCentering: String = "",
    @SerializedName("Card ID")
    val cardID: String = "",
    @SerializedName("Defects")
    val defects: String = "",
    @SerializedName("File Upload Status")
    val fileUploadStatus: String = "",
    @SerializedName("Flag")
    val flag: String = "",
    @SerializedName("Height")
    val height: Int = 0,
    @SerializedName("Left centering")
    val leftCentering: String = "",
    @SerializedName("Right centering")
    val rightCentering: String = "",
    @SerializedName("TL_corner")
    val tLCorner: Int = 0,
    @SerializedName("TR_corner")
    val tRCorner: Int = 0,
    @SerializedName("Top centering")
    val topCentering: String = "",
    @SerializedName("Width")
    val width: Int = 0,
    @SerializedName("edge")
    val edge: Int = 0,
    @SerializedName("grade")
    val grade: String = "",
    @SerializedName("surface")
    val surface: String = ""
) {

    data class Coordinates(
        @SerializedName("Blemish")
        val blemish: List<List<Int>> = emptyList(),
        @SerializedName("Worn_Corner")
        val wornCorner: List<List<Int>> = emptyList(),
        @SerializedName("Worn_Edge")
        val wornEdge: List<List<Int>> = emptyList(),
        @SerializedName("Crease")
        val crease: List<List<Int>> = emptyList()
    )

    val defectCoordinates get() = Gson().fromJson(defects, Coordinates::class.java)

    val coordinatesList get() = listOf(defectCoordinates.blemish, defectCoordinates.wornCorner, defectCoordinates.wornEdge, defectCoordinates.crease)

}