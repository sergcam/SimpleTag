/*
 * Copyright (C) 2025  Sergio Camacho
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.secam.simpletag.data

import dev.secam.simpletag.R
import org.jaudiotagger.tag.FieldKey

enum class SimpleTagField (val displayNameRes: Int, val fieldKey: FieldKey) {
    // Basic (ordinal 0-9)
    Title(R.string.title_field, FieldKey.TITLE),
    Artist(R.string.artist_field, FieldKey.ARTIST),
    Album(R.string.album_field, FieldKey.ALBUM),
    Year(R.string.year_field, FieldKey.YEAR),
    Track(R.string.track_field, FieldKey.TRACK),
    Genre(R.string.genre_field, FieldKey.GENRE),
    AlbumArtist(R.string.album_artist_field, FieldKey.ALBUM_ARTIST),
    Composer(R.string.composer_field, FieldKey.COMPOSER),
    DiscNumber(R.string.disc_number_field, FieldKey.DISC_NO),
    Comment(R.string.comment_field, FieldKey.COMMENT),

    // Advanced (ordinal 10+)
    AcoustIdFingerprint(R.string.acoustid_fingerprint_field, FieldKey.ACOUSTID_FINGERPRINT),
    AcoustIdId(R.string.acoustid_id, FieldKey.ACOUSTID_ID),
    AlbumArtistSortOrder(R.string.album_artist_sort_order_field, FieldKey.ALBUM_ARTIST_SORT),
    AlbumSortOrder (R.string.album_sort_order_field, FieldKey.ALBUM_SORT),
    Arranger(R.string.arranger_field, FieldKey.ARRANGER),
    ArtistSortOrder(R.string.artist_sort_order_field, FieldKey.ARTIST_SORT),
    Artists(R.string.artists_field, FieldKey.ARTISTS),
    ASIN(R.string.asin_field, FieldKey.AMAZON_ID),
    Barcode(R.string.barcode_field, FieldKey.BARCODE),
    BPM(R.string.bpm_field, FieldKey.BPM),
    CatalogNumber(R.string.catalog_number_field, FieldKey.CATALOG_NO),
    Compilation(R.string.compilation_field, FieldKey.IS_COMPILATION),
    ComposerSortOrder(R.string.composer_sort_order_field, FieldKey.COMPOSER_SORT),
    Conductor(R.string.conductor_field, FieldKey.CONDUCTOR),
    Copyright(R.string.copyright_field, FieldKey.COPYRIGHT),
    Country(R.string.country_field, FieldKey.COUNTRY),
    CustomOne(R.string.custom_one_field, FieldKey.CUSTOM1),
    CustomTwo(R.string.custom_two_field, FieldKey.CUSTOM2),
    CustomThree(R.string.custom_three_field, FieldKey.CUSTOM3),
    CustomFour(R.string.custom_four_field, FieldKey.CUSTOM4),
    CustomFive(R.string.custom_five_field, FieldKey.CUSTOM5),
    DiscogsArtistSiteUrl (R.string.discogs_artist_site_url_field, FieldKey.URL_DISCOGS_ARTIST_SITE),
    DiscogsReleaseSiteUrl (R.string.discogs_release_site_url_field, FieldKey.URL_DISCOGS_RELEASE_SITE),
    DjMixer(R.string.dj_mixer_field, FieldKey.DJMIXER),
    EncodedBy(R.string.encoded_by_field, FieldKey.ENCODER),
    Engineer(R.string.engineer_field, FieldKey.ENGINEER),
    FloatingPointBPM(R.string.floating_point_bpm_field, FieldKey.FBPM),
    Grouping(R.string.grouping_field, FieldKey.GROUPING),
    ISRC(R.string.isrc_field, FieldKey.ISRC),
    Key(R.string.key_field, FieldKey.KEY),
    Label(R.string.label_field, FieldKey.RECORD_LABEL),
    Language(R.string.language_field, FieldKey.LANGUAGE),
    Lyricist(R.string.lyricist_field, FieldKey.LYRICIST),
    Lyrics(R.string.lyrics_field, FieldKey.LYRICS),
    LyricsSiteUrl(R.string.lyrics_site_url_field, FieldKey.URL_LYRICS_SITE),
    Media(R.string.media_field, FieldKey.MEDIA),
    Mixer(R.string.mixer_field, FieldKey.MIXER),
    Mood(R.string.mood_field, FieldKey.MOOD),
    MusicBrainzArtistId(R.string.musicbrainz_artist_id_field, FieldKey.MUSICBRAINZ_ARTISTID),
    MusicBrainzDiscId(R.string.musicbrainz_disc_id_field, FieldKey.MUSICBRAINZ_DISC_ID),
    MusicBrainzOriginalReleaseId(R.string.musicbrainz_original_release_id_field, FieldKey.MUSICBRAINZ_ORIGINAL_RELEASE_ID),
    MusicBrainzReleaseArtistId(R.string.musicbrainz_release_artist_id_field, FieldKey.MUSICBRAINZ_RELEASEARTISTID),
    MusicBrainzReleaseGroupId(R.string.musicbrainz_release_group_id_field, FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID),
    MusicBrainzReleaseId(R.string.musicbrainz_release_id_field, FieldKey.MUSICBRAINZ_RELEASEID),
    MusicBrainzTrackId(R.string.musicbrainz_track_id_field, FieldKey.MUSICBRAINZ_TRACK_ID),
    MusicBrainzWorkId(R.string.musicbrainz_work_id_field, FieldKey.MUSICBRAINZ_WORK_ID),
    Occasion(R.string.occasion_field, FieldKey.OCCASION),
    OfficialArtistSiteUrl(R.string.official_artist_site_url_field, FieldKey.URL_OFFICIAL_ARTIST_SITE),
    OfficialReleaseSiteUrl(R.string.official_release_site_url_field, FieldKey.URL_OFFICIAL_RELEASE_SITE),
    OriginalAlbum(R.string.original_album_field, FieldKey.ORIGINAL_ALBUM),
    OriginalArtist(R.string.original_artist_field, FieldKey.ORIGINAL_ARTIST),
    OriginalLyricist(R.string.original_lyricist_field, FieldKey.ORIGINAL_LYRICIST),
    OriginalReleaseDate(R.string.original_release_date_field, FieldKey.ORIGINALRELEASEDATE),
    Producer(R.string.producer_field, FieldKey.PRODUCER),
    Quality(R.string.quality_field, FieldKey.QUALITY),
    Rating(R.string.rating_field, FieldKey.RATING),
    ReleaseCountry(R.string.release_country_field, FieldKey.MUSICBRAINZ_RELEASE_COUNTRY),
    ReleaseStatus(R.string.release_status_field, FieldKey.MUSICBRAINZ_RELEASE_STATUS),
    ReleaseType(R.string.release_type_field, FieldKey.MUSICBRAINZ_RELEASE_TYPE),
    Remixer(R.string.remixer_field, FieldKey.REMIXER),
    Script(R.string.script_field, FieldKey.SCRIPT),
    Tags(R.string.tags_field, FieldKey.TAGS),
    Tempo(R.string.tempo_field, FieldKey.TEMPO),
    TitleSortOrder(R.string.title_sort_order_field, FieldKey.TITLE_SORT),
    TotalDiscs(R.string.total_discs_field, FieldKey.DISC_TOTAL),
    TotalTracks(R.string.total_tracks_field, FieldKey.TRACK_TOTAL),
    WikipediaArtistSiteUrl(R.string.wikipedia_artist_site_url_field, FieldKey.URL_WIKIPEDIA_ARTIST_SITE),
    WikipediaReleaseSiteUrl(R.string.wikipedia_release_site_url_field, FieldKey.URL_WIKIPEDIA_RELEASE_SITE),
}