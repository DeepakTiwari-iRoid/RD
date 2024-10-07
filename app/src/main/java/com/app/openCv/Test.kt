package com.app.openCv

import org.opencv.core.Point


fun main() {

    val sample = listOf(
        listOf(Point(569.0, 252.0), Point(176.0, 261.0), Point(161.0, 871.0), Point(637.0, 850.0)),
        listOf(Point(568.0, 250.0), Point(177.0, 259.0), Point(161.0, 863.0), Point(639.0, 837.0)),
        listOf(Point(563.0, 250.0), Point(174.0, 259.0), Point(160.0, 859.0), Point(634.0, 836.0)),
        listOf(Point(558.0, 248.0), Point(175.0, 256.0), Point(163.0, 850.0), Point(628.0, 825.0)),
        listOf(Point(558.0, 247.0), Point(176.0, 257.0), Point(163.0, 847.0), Point(627.0, 824.0)),
        listOf(Point(558.0, 246.0), Point(177.0, 254.0), Point(163.0, 846.0), Point(628.0, 821.0)),
        listOf(Point(563.0, 249.0), Point(181.0, 258.0), Point(168.0, 848.0), Point(633.0, 821.0)),
    )

    val averagedPairs = sample[0].indices.map { i ->
        val avgX = sample.map { it[i].x }.average()
        val avgY = sample.map { it[i].y }.average()
        Point(avgX, avgY)
    }

    averagedPairs.forEach {
        println(it)
    }

}