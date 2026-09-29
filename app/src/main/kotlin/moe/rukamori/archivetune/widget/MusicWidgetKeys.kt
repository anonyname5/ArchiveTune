/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey

object MusicWidgetKeys {
    val TRACK_TITLE = stringPreferencesKey("widget_track_title")
    val TRACK_ARTIST = stringPreferencesKey("widget_track_artist")
    val ART_PATH = stringPreferencesKey("widget_art_path")
    val IS_PLAYING = booleanPreferencesKey("widget_is_playing")
    val IS_AVAILABLE = booleanPreferencesKey("widget_is_available")
    val DOMINANT_COLOR = intPreferencesKey("widget_dominant_color")
    val PLAYBACK_POSITION = floatPreferencesKey("widget_position")
    val POSITION_MS = longPreferencesKey("widget_position_ms")
    val DURATION_MS = longPreferencesKey("widget_duration_ms")
    val VOLUME_PROGRESS = floatPreferencesKey("widget_volume_progress")
    val LISTENING_TIME = stringPreferencesKey("widget_listening_time")
    val TOTAL_PLAYS = stringPreferencesKey("widget_total_plays")
    val RECENT_SONGS = stringPreferencesKey("widget_recent_songs")
    val GENRES = stringPreferencesKey("widget_genres")
    val RECOMMENDATIONS = stringPreferencesKey("widget_recommendations")
    val TOP_SONG_SUMMARY = stringPreferencesKey("widget_top_song_summary")
    val LYRIC_ACTIVE = stringPreferencesKey("widget_lyric_active")
    val LYRIC_PREV = stringPreferencesKey("widget_lyric_prev")
    val LYRIC_NEXT = stringPreferencesKey("widget_lyric_next")
    val LYRIC_NEXT2 = stringPreferencesKey("widget_lyric_next2")
    val HAS_LYRICS = booleanPreferencesKey("widget_has_lyrics")
}

internal fun Preferences.toMutableWidgetPreferences(): MutablePreferences =
    mutablePreferencesOf().also { mutable ->
        this[MusicWidgetKeys.TRACK_TITLE]?.let { mutable[MusicWidgetKeys.TRACK_TITLE] = it }
        this[MusicWidgetKeys.TRACK_ARTIST]?.let { mutable[MusicWidgetKeys.TRACK_ARTIST] = it }
        this[MusicWidgetKeys.ART_PATH]?.let { mutable[MusicWidgetKeys.ART_PATH] = it }
        this[MusicWidgetKeys.IS_PLAYING]?.let { mutable[MusicWidgetKeys.IS_PLAYING] = it }
        this[MusicWidgetKeys.IS_AVAILABLE]?.let { mutable[MusicWidgetKeys.IS_AVAILABLE] = it }
        this[MusicWidgetKeys.DOMINANT_COLOR]?.let { mutable[MusicWidgetKeys.DOMINANT_COLOR] = it }
        this[MusicWidgetKeys.PLAYBACK_POSITION]?.let { mutable[MusicWidgetKeys.PLAYBACK_POSITION] = it }
        this[MusicWidgetKeys.POSITION_MS]?.let { mutable[MusicWidgetKeys.POSITION_MS] = it }
        this[MusicWidgetKeys.DURATION_MS]?.let { mutable[MusicWidgetKeys.DURATION_MS] = it }
        this[MusicWidgetKeys.VOLUME_PROGRESS]?.let { mutable[MusicWidgetKeys.VOLUME_PROGRESS] = it }
        this[MusicWidgetKeys.LISTENING_TIME]?.let { mutable[MusicWidgetKeys.LISTENING_TIME] = it }
        this[MusicWidgetKeys.TOTAL_PLAYS]?.let { mutable[MusicWidgetKeys.TOTAL_PLAYS] = it }
        this[MusicWidgetKeys.RECENT_SONGS]?.let { mutable[MusicWidgetKeys.RECENT_SONGS] = it }
        this[MusicWidgetKeys.GENRES]?.let { mutable[MusicWidgetKeys.GENRES] = it }
        this[MusicWidgetKeys.RECOMMENDATIONS]?.let { mutable[MusicWidgetKeys.RECOMMENDATIONS] = it }
        this[MusicWidgetKeys.TOP_SONG_SUMMARY]?.let { mutable[MusicWidgetKeys.TOP_SONG_SUMMARY] = it }
        this[MusicWidgetKeys.LYRIC_ACTIVE]?.let { mutable[MusicWidgetKeys.LYRIC_ACTIVE] = it }
        this[MusicWidgetKeys.LYRIC_PREV]?.let { mutable[MusicWidgetKeys.LYRIC_PREV] = it }
        this[MusicWidgetKeys.LYRIC_NEXT]?.let { mutable[MusicWidgetKeys.LYRIC_NEXT] = it }
        this[MusicWidgetKeys.LYRIC_NEXT2]?.let { mutable[MusicWidgetKeys.LYRIC_NEXT2] = it }
        this[MusicWidgetKeys.HAS_LYRICS]?.let { mutable[MusicWidgetKeys.HAS_LYRICS] = it }
    }
