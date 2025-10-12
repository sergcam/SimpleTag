package org.jaudiotagger.tag.vorbiscomment;

import org.jaudiotagger.tag.reference.Tagger;

import java.util.EnumSet;

/**
 * Vorbis Comment Field Names
 *
 *
 *
 * This partial list is derived fom the following sources:
 * <ul>
 * <li>http://xiph.org/vorbis/doc/v-comment.html</li>
 * <li>http://wiki.musicbrainz.org/PicardQt/TagMapping</li>
 * <li>http://legroom.net/2009/05/09/ogg-vorbis-and-flac-comment-field-recommendations</li>
 * </ul>
 */
public enum VorbisCommentFieldKey
{
    ACOUSTID_FINGERPRINT("ACOUSTID_FINGERPRINT",EnumSet.of(Tagger.PICARD)),
    ACOUSTID_ID("ACOUSTID_ID",EnumSet.of(Tagger.PICARD)),
    ALBUM("ALBUM", EnumSet.of(Tagger.XIPH,Tagger.PICARD,Tagger.JAIKOZ, Tagger.ROON)),
    ALBUMARTIST("ALBUMARTIST",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    ALBUMARTISTSORT("ALBUMARTISTSORT",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    ALBUMARTISTS("ALBUM_ARTISTS",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    ALBUMARTISTSSORT("ALBUM_ARTISTS_SORT",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    ALBUMARTIST_JRIVER("ALBUM ARTIST",EnumSet.of(Tagger.JRIVER)),
    ALBUMSORT("ALBUMSORT",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    ALBUM_ARTIST("ALBUM_ARTIST",EnumSet.of(Tagger.MEDIA_MONKEY)),
    ALBUM_YEAR("ALBUM_YEAR",EnumSet.of(Tagger.JAIKOZ)),
    ARRANGER("ARRANGER",EnumSet.of(Tagger.PICARD)),
    ARRANGER_SORT("ARRANGER_SORT",EnumSet.of(Tagger.JAIKOZ)),
    ARTIST("ARTIST", EnumSet.of(Tagger.XIPH,Tagger.PICARD,Tagger.JAIKOZ)),
    ARTISTS("ARTISTS", EnumSet.of(Tagger.JAIKOZ)),
    ARTISTSORT("ARTISTSORT",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    ARTISTS_SORT("ARTISTS_SORT",EnumSet.of(Tagger.JAIKOZ)),
    AUDIO_ENGINEER("AUDIO_ENGINEER",EnumSet.of(Tagger.SONGKONG)),
    AUDIO_ENGINEER_SORT("AUDIO_ENGINEER_SORT",EnumSet.of(Tagger.SONGKONG)),
    ASIN("ASIN",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    BALANCE_ENGINEER("BALANCE_ENGINEER",EnumSet.of(Tagger.SONGKONG)),
    BALANCE_ENGINEER_SORT("BALANCE_ENGINEER_SORT",EnumSet.of(Tagger.SONGKONG)),
    BARCODE("BARCODE",EnumSet.of(Tagger.JAIKOZ,Tagger.ROON)),
    BPM("BPM",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    CATALOGNUMBER("CATALOGNUMBER",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    CHOIR("CHOIR",EnumSet.of(Tagger.JAIKOZ)),
    CHOIR_SORT("CHOIR_SORT",EnumSet.of(Tagger.JAIKOZ)),
    CLASSICAL_CATALOG("CLASSICAL_CATALOG",EnumSet.of(Tagger.JAIKOZ)),
    CLASSICAL_NICKNAME("CLASSICAL_NICKNAME",EnumSet.of(Tagger.JAIKOZ)),
    COMMENT("COMMENT",EnumSet.of(Tagger.PICARD)),
    COMPILATION("COMPILATION",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    COMPOSER("COMPOSER",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ,Tagger.ROON)),
    COMPOSERSORT("COMPOSERSORT",EnumSet.of(Tagger.JAIKOZ)),
    CONDUCTOR("CONDUCTOR",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ,Tagger.ROON)),
    CONDUCTOR_SORT("CONDUCTOR_SORT",EnumSet.of(Tagger.JAIKOZ)),
    CONTACT("CONTACT",EnumSet.of(Tagger.XIPH)),
    COPYRIGHT("COPYRIGHT",EnumSet.of(Tagger.XIPH,Tagger.PICARD,Tagger.JAIKOZ)),
    COUNTRY("COUNTRY",EnumSet.of(Tagger.PICARD)),
    COVERART("COVERART",EnumSet.of(Tagger.JAIKOZ)),
    COVERARTMIME("COVERARTMIME",EnumSet.of(Tagger.JAIKOZ)),
    CREDITS("CREDITS",EnumSet.of(Tagger.SONGKONG)),
    CUSTOM1("CUSTOM1",EnumSet.of(Tagger.MEDIA_MONKEY)),
    CUSTOM2("CUSTOM2",EnumSet.of(Tagger.MEDIA_MONKEY)),
    CUSTOM3("CUSTOM3",EnumSet.of(Tagger.MEDIA_MONKEY)),
    CUSTOM4("CUSTOM4",EnumSet.of(Tagger.MEDIA_MONKEY)),
    CUSTOM5("CUSTOM5",EnumSet.of(Tagger.MEDIA_MONKEY)),
    DATE("DATE",EnumSet.of(Tagger.XIPH,Tagger.PICARD,Tagger.JAIKOZ)),
    DESCRIPTION("DESCRIPTION",EnumSet.of(Tagger.XIPH)),
    DISCNUMBER("DISCNUMBER",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    DISCSUBTITLE("DISCSUBTITLE",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    DISCTOTAL("DISCTOTAL",EnumSet.of(Tagger.XIPH,Tagger.PICARD)),
    DJMIXER("DJMIXER",EnumSet.of(Tagger.PICARD)),
    DJMIXER_SORT("DJMIXER_SORT",EnumSet.of(Tagger.SONGKONG)),
    ENCODEDBY("ENCODEDBY",EnumSet.of(Tagger.PICARD)),
    ENCODER("ENCODER"),
    ENGINEER("ENGINEER",EnumSet.of(Tagger.PICARD,Tagger.ROON)),
    ENGINEER_SORT("ENGINEER_SORT",EnumSet.of(Tagger.SONGKONG)),
    ENSEMBLE("ENSEMBLE",EnumSet.of(Tagger.MEDIA_MONKEY, Tagger.JAIKOZ,Tagger.ROON)),
    ENSEMBLE_SORT("ENSEMBLE_SORT",EnumSet.of(Tagger.JAIKOZ)),
    FBPM("FBPM",EnumSet.of(Tagger.BEATUNES)),
    GENRE("GENRE",EnumSet.of(Tagger.XIPH,Tagger.PICARD,Tagger.JAIKOZ)),
    GROUP("GROUP",EnumSet.of(Tagger.JAIKOZ, Tagger.MINIMSERVER)),
    GROUPING("GROUPING",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    INSTRUMENT("INSTRUMENT",EnumSet.of(Tagger.JAIKOZ)),
    INVOLVEDPEOPLE("INVOLVEDPEOPLE", EnumSet.of(Tagger.ROON)),
    IPI("IPI",EnumSet.of(Tagger.JAIKOZ)),
    ISRC("ISRC",EnumSet.of(Tagger.XIPH,Tagger.PICARD,Tagger.JAIKOZ)),
    ISWC("ISWC",EnumSet.of(Tagger.XIPH,Tagger.PICARD,Tagger.JAIKOZ)),
    IS_CLASSICAL("IS_CLASSICAL",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    IS_GREATEST_HITS("IS_GREATEST_HITS",EnumSet.of(Tagger.JAIKOZ)),
    IS_HD("IS_HD",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    IS_LIVE("LIVE",EnumSet.of(Tagger.ROON)),
    IS_SOUNDTRACK("IS_SOUNDTRACK",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    JAIKOZ_ID("JAIKOZ_ID",EnumSet.of(Tagger.JAIKOZ)),
    KEY("KEY"),
    LABEL("LABEL",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ,Tagger.ROON)),
    LANGUAGE("LANGUAGE"),
    LICENSE("LICENSE",EnumSet.of(Tagger.XIPH)),
    LOCATION("LOCATION",EnumSet.of(Tagger.XIPH)),
    LYRICIST("LYRICIST",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ,Tagger.ROON)),
    LYRICIST_SORT("LYRICIST_SORT",EnumSet.of(Tagger.JAIKOZ)),
    LYRICS("LYRICS",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MEDIA("MEDIA",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MASTERING("MASTERING",EnumSet.of(Tagger.SONGKONG)),
    MASTERING_SORT("MASTERING_SORT",EnumSet.of(Tagger.SONGKONG)),
    METADATA_BLOCK_PICTURE("METADATA_BLOCK_PICTURE",EnumSet.of(Tagger.XIPH)),
    MIXER("MIXER",EnumSet.of(Tagger.PICARD)),
    MIXER_SORT("MIXER_SORT",EnumSet.of(Tagger.SONGKONG)),
    MOOD("MOOD",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MOOD_ACOUSTIC("MOOD_ACOUSTIC", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_AGGRESSIVE("MOOD_AGGRESSIVE", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_AROUSAL("MOOD_AROUSAL", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_DANCEABILITY("MOOD_DANCEABILITY", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_ELECTRONIC("MOOD_ELECTRONIC", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_HAPPY("MOOD_HAPPY", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_INSTRUMENTAL("MOOD_INSTRUMENTAL", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_PARTY("MOOD_PARTY", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_RELAXED("MOOD_RELAXED", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_SAD("MOOD_SAD", EnumSet.of(Tagger.JAIKOZ)),
    MOOD_VALENCE("MOOD_VALENCE", EnumSet.of(Tagger.JAIKOZ)),
    MOVEMENT("MOVEMENT", EnumSet.of(Tagger.JAIKOZ)),
    MOVEMENT_NO("MOVEMENT_NO", EnumSet.of(Tagger.JAIKOZ)),
    MOVEMENT_TOTAL("MOVEMENT_TOTAL", EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_ALBUMARTISTID("MUSICBRAINZ_ALBUMARTISTID",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MUSICBRAINZ_ALBUMID("MUSICBRAINZ_ALBUMID",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MUSICBRAINZ_ALBUMSTATUS("MUSICBRAINZ_ALBUMSTATUS",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MUSICBRAINZ_ALBUMTYPE("MUSICBRAINZ_ALBUMTYPE",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MUSICBRAINZ_ARTISTID("MUSICBRAINZ_ARTISTID",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MUSICBRAINZ_DISCID("MUSICBRAINZ_DISCID",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MUSICBRAINZ_ORIGINAL_ALBUMID("MUSICBRAINZ_ORIGINALALBUMID",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_RELEASEGROUPID("MUSICBRAINZ_RELEASEGROUPID",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MUSICBRAINZ_RELEASETRACKID("MUSICBRAINZ_RELEASETRACKID",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_TRACKID("MUSICBRAINZ_TRACKID",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK("MUSICBRAINZ_WORK", EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORKID("MUSICBRAINZ_WORKID",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    MUSICBRAINZ_RECORDING_WORK("MUSICBRAINZ_RECORDING_WORK",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_RECORDING_WORK_ID("MUSICBRAINZ_RECORDING_WORK_ID",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL1("MUSICBRAINZ_WORK_PART_LEVEL1",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL1_ID("MUSICBRAINZ_WORK_PART_LEVEL1_ID",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL1_TYPE("MUSICBRAINZ_WORK_PART_LEVEL1_TYPE",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL2("MUSICBRAINZ_WORK_PART_LEVEL2",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL2_ID("MUSICBRAINZ_WORK_PART_LEVEL2_ID",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL2_TYPE("MUSICBRAINZ_WORK_PART_LEVEL2_TYPE",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL3("MUSICBRAINZ_WORK_PART_LEVEL3",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL3_ID("MUSICBRAINZ_WORK_PART_LEVEL3_ID",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL3_TYPE("MUSICBRAINZ_WORK_PART_LEVEL3_TYPE",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL4_ID("MUSICBRAINZ_WORK_PART_LEVEL4_ID",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL4("MUSICBRAINZ_WORK_PART_LEVEL4",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL4_TYPE("MUSICBRAINZ_WORK_PART_LEVEL4_TYPE",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL5("MUSICBRAINZ_WORK_PART_LEVEL5",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL5_ID("MUSICBRAINZ_WORK_PART_LEVEL5_ID",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL5_TYPE("MUSICBRAINZ_WORK_PART_LEVEL5_TYPE",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL6("MUSICBRAINZ_WORK_PART_LEVEL6",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL6_ID("MUSICBRAINZ_WORK_PART_LEVEL6_ID",EnumSet.of(Tagger.JAIKOZ)),
    MUSICBRAINZ_WORK_PART_LEVEL6_TYPE("MUSICBRAINZ_WORK_PART_LEVEL6_TYPE",EnumSet.of(Tagger.JAIKOZ)),
    MUSICIAN("MUSICIAN", EnumSet.of(Tagger.JAIKOZ)),
    MUSICIP_PUID("MUSICIP_PUID",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    OCCASION("OCCASION",EnumSet.of(Tagger.MEDIA_MONKEY)),
    OPUS("OPUS",EnumSet.of(Tagger.JAIKOZ)),
    ORCHESTRA("ORCHESTRA",EnumSet.of(Tagger.JAIKOZ)),
    ORCHESTRA_SORT("ORCHESTRA_SORT",EnumSet.of(Tagger.JAIKOZ)),
    ORGANIZATION("ORGANIZATION",EnumSet.of(Tagger.XIPH)),  //   Name of the organization producing the track (i.e. the 'record label')
    ORIGINAL_ALBUM("ORIGINAL ALBUM",EnumSet.of(Tagger.JAIKOZ,Tagger.MEDIA_MONKEY)),
    ORIGINALRELEASEDATE("ORIGINALRELEASEDATE",EnumSet.of(Tagger.ROON)),
    ORIGINAL_ARTIST("ORIGINAL ARTIST",EnumSet.of(Tagger.JAIKOZ,Tagger.MEDIA_MONKEY)),
    ORIGINAL_LYRICIST("ORIGINAL LYRICIST",EnumSet.of(Tagger.MEDIA_MONKEY)),
    ORIGINAL_YEAR("ORIGINAL YEAR",EnumSet.of(Tagger.JAIKOZ,Tagger.MEDIA_MONKEY)),
    OVERALL_WORK("OVERALL_WORK",EnumSet.of(Tagger.JAIKOZ)),
    PART("PART",EnumSet.of(Tagger.JAIKOZ,Tagger.ROON)),
    PART_NUMBER("PARTNUMBER",EnumSet.of(Tagger.XIPH)),
    PART_TYPE("PART_TYPE",EnumSet.of(Tagger.JAIKOZ)),
    PERFORMER("PERFORMER",EnumSet.of(Tagger.XIPH,Tagger.PICARD)),
    PERFORMER_NAME("PERFORMER_NAME",EnumSet.of(Tagger.JAIKOZ)),
    PERFORMER_NAME_SORT("PERFORMER_NAME_SORT",EnumSet.of(Tagger.JAIKOZ)),
    PERIOD("PERIOD",EnumSet.of(Tagger.MUSICHI)),
    PRODUCER("PRODUCER",EnumSet.of(Tagger.PICARD,Tagger.ROON)),
    PRODUCER_SORT("PRODUCER_SORT",EnumSet.of(Tagger.SONGKONG)),
    PRODUCTNUMBER("PRODUCTNUMBER",EnumSet.of(Tagger.XIPH)),
    QUALITY("QUALITY",EnumSet.of(Tagger.MEDIA_MONKEY)),
    RANKING("RANKING",EnumSet.of(Tagger.JAIKOZ)),
    RATING("RATING",EnumSet.of(Tagger.MEDIA_MONKEY)),
    RECORDING_ENGINEER("RECORIDNG_ENGINEER",EnumSet.of(Tagger.SONGKONG)),
    RECORDING_ENGINEER_SORT("RECORDING_ENGINEER_SORT",EnumSet.of(Tagger.SONGKONG)),
    RECORDINGDATE("RECORDINGDATE",EnumSet.of(Tagger.ROON)),
    RECORDINGSTARTDATE("RECORDINGSTARTDATE",EnumSet.of(Tagger.ROON)),
    RECORDINGENDDATE("RECORDINGENDDATE",EnumSet.of(Tagger.ROON)),
    RECORDINGLOCATION("RECORDINGLOCATION",EnumSet.of(Tagger.ROON)),
    RELEASECOUNTRY("RELEASECOUNTRY",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    REMIXER("REMIXER",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    ROONALBUMTAG("ROONALBUMTAG",EnumSet.of(Tagger.ROON)),
    ROONTRACKTAG("ROONTRACKTAG",EnumSet.of(Tagger.ROON)),
    SCRIPT("SCRIPT",EnumSet.of(Tagger.JAIKOZ)),
    SECTION("SECTION",EnumSet.of(Tagger.ROON)),
    SINGLE_DISC_TRACK_NO("SINGLE_DISC_TRACK_NO",EnumSet.of(Tagger.JAIKOZ)),
    SONGKONG_ID("SONGKONG_ID",EnumSet.of(Tagger.SONGKONG)),
    SOUND_ENGINEER("SOUND_ENGINEER",EnumSet.of(Tagger.SONGKONG)),
    SOUND_ENGINEER_SORT("SOUNDO_ENGINEER_SORT",EnumSet.of(Tagger.SONGKONG)),
    SOURCEMEDIA("SOURCEMEDIA",EnumSet.of(Tagger.XIPH)),
    SUBTITLE("SUBTITLE",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    TAGS("TAGS",EnumSet.of(Tagger.JAIKOZ)),
    TEMPO("TEMPO",EnumSet.of(Tagger.MEDIA_MONKEY)),
    TIMBRE("TIMBRE_BRIGHTNESS", EnumSet.of(Tagger.JAIKOZ)),
    TITLE("TITLE", EnumSet.of(Tagger.XIPH,Tagger.PICARD,Tagger.JAIKOZ,Tagger.ROON)),
    TITLE_MOVEMENT("TITLE_MOVEMENT", EnumSet.of(Tagger.JAIKOZ)),
    TITLESORT("TITLESORT",EnumSet.of(Tagger.PICARD,Tagger.JAIKOZ)),
    TONALITY("TONALITY", EnumSet.of(Tagger.JAIKOZ)),
    TRACKNUMBER("TRACKNUMBER",EnumSet.of(Tagger.XIPH,Tagger.PICARD,Tagger.JAIKOZ)),
    TRACKTOTAL("TRACKTOTAL",EnumSet.of(Tagger.XIPH,Tagger.PICARD)),
    URL_BANDCAMP_ARTIST_SITE("URL_BANDCAMP_ARTIST_SITE",EnumSet.of(Tagger.SONGKONG)),
    URL_BANDCAMP_RELEASE_SITE("URL_BANDCAMP_RELEASE_SITE",EnumSet.of(Tagger.SONGKONG)),
    URL_DISCOGS_ARTIST_SITE("URL_DISCOGS_ARTIST_SITE",EnumSet.of(Tagger.JAIKOZ)),
    URL_DISCOGS_RELEASE_SITE("URL_DISCOGS_RELEASE_SITE",EnumSet.of(Tagger.JAIKOZ)),
    URL_LYRICS_SITE("URL_LYRICS_SITE",EnumSet.of(Tagger.JAIKOZ)),
    URL_OFFICIAL_ARTIST_SITE("URL_OFFICIAL_ARTIST_SITE",EnumSet.of(Tagger.JAIKOZ)),
    URL_OFFICIAL_RELEASE_SITE("URL_OFFICIAL_RELEASE_SITE",EnumSet.of(Tagger.JAIKOZ)),
    URL_WIKIPEDIA_ARTIST_SITE("URL_WIKIPEDIA_ARTIST_SITE",EnumSet.of(Tagger.JAIKOZ)),
    URL_WIKIPEDIA_RELEASE_SITE("URL_WIKIPEDIA_RELEASE_SITE",EnumSet.of(Tagger.JAIKOZ)),
    VENDOR("VENDOR"),
    VERSION("VERSION", EnumSet.of(Tagger.XIPH, Tagger.ROON)),// The version field may be used to differentiate multiple versions of the same track title in a single collection. (e.g. remix info)
    WORK("WORK",EnumSet.of(Tagger.JAIKOZ)),
    WORK_TYPE("WORK_TYPE",EnumSet.of(Tagger.JAIKOZ)),
    ;


    private String fieldName;
    private EnumSet<Tagger> taggers;
    private String realFieldName;

    VorbisCommentFieldKey(String fieldName)
    {
        this.fieldName = fieldName;
    }

    VorbisCommentFieldKey(String fieldName, EnumSet<Tagger> taggers)
    {
        this.fieldName = fieldName;
        this.taggers = taggers;
    }

    VorbisCommentFieldKey(String fieldName, String realFieldName)
    {
        this.fieldName = fieldName;
        this.realFieldName = realFieldName;
    }

    VorbisCommentFieldKey(String fieldName, String realFieldName, EnumSet<Tagger> taggers)
    {
        this.fieldName = fieldName;
        this.realFieldName = realFieldName;
        this.taggers = taggers;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getRealFieldName()
    {
        return realFieldName;
    }

    /**
     * List of taggers using this field, concentrates primarily on the original tagger to start using a field.
     * Tagger.XIPH means the field is either part  of the Vorbis Standard or a Vorbis proposed extension to the
     * standard
     *
     * @return
     */
    public EnumSet<Tagger> getTaggers()
    {
        return taggers;
    }
}
