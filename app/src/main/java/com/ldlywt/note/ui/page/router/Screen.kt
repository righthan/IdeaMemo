package com.ldlywt.note.ui.page.router

import kotlinx.serialization.Serializable


@Serializable
sealed class Screen {

    @Serializable
    data object Main : Screen()

    @Serializable
    data object Explore : Screen()

    @Serializable
    data class InputDetail(val id: Long) : Screen()

    @Serializable
    object TagList : Screen()

    @Serializable
    data class TagDetail(val tag: String) : Screen()

    @Serializable
    data class YearDetail(val year: String) : Screen()

    @Serializable
    data class LocationDetail(val location: String) : Screen()

    @Serializable
    object Search : Screen()

    @Serializable
    data class Share(val id: Long) : Screen()

    @Serializable
    object DataManager : Screen()

    @Serializable
    object DataCloudConfig : Screen()

    @Serializable
    object RandomWalk : Screen()

    @Serializable
    object Gallery : Screen()

    @Serializable
    data class PictureDisplay(val pathList: List<String>, val curIndex: Int) : Screen()

    @Serializable
    object MoreInfo : Screen()

    @Serializable
    object LocationList : Screen()
}