package com.ldlywt.note.api.memos

import kotlinx.serialization.Serializable

@Serializable
data class MemosResponse(
    val memos: List<Memo>,
    val nextPageToken: String = "",
    val totalSize: Int = 0
)

@Serializable
data class Memo(
    val name: String,
    val state: String = "NORMAL",
    val creator: String,
    val createTime: String,
    val updateTime: String,
    val displayTime: String,
    val content: String,
    val nodes: List<Node> = emptyList(),
    val visibility: String = "PRIVATE",
    val tags: List<String> = emptyList(),
    val pinned: Boolean = false,
    val attachments: List<MemoAttachment> = emptyList(),
    val relations: List<String> = emptyList(),
    val reactions: List<String> = emptyList(),
    val property: MemosProperty? = null,
    val snippet: String = ""
)

@Serializable
data class Node(
    val type: String,
    val paragraphNode: ParagraphNode? = null
)

@Serializable
data class ParagraphNode(
    val children: List<ChildNode> = emptyList()
)

@Serializable
data class ChildNode(
    val type: String,
    val autoLinkNode: AutoLinkNode? = null
)

@Serializable
data class AutoLinkNode(
    val url: String,
    val isRawText: Boolean = true
)

@Serializable
data class MemoAttachment(
    val name: String,
    val createTime: String,
    val filename: String,
    val content: String = "",
    val externalLink: String = "",
    val type: String,
    val size: String,
    val memo: String
)

@Serializable
data class MemosProperty(
    val hasLink: Boolean = false,
    val hasTaskList: Boolean = false,
    val hasCode: Boolean = false,
    val hasIncompleteTasks: Boolean = false
) 