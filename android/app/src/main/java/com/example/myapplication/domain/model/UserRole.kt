package com.example.myapplication.domain.model

/**
 * ユーザーの稼働モード・役割
 */
enum class UserRole {
    /** 個人セルフコントロール（バーチャル親のコミカルで大げさな表現） */
    INDIVIDUAL,

    /** ペアモード：親端末（管理・設定・リモート通知送信） */
    PARENT,

    /** ペアモード：子端末（監視対象・マイルドまたは親のカスタムメッセージ） */
    CHILD
}
