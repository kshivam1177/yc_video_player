
package com.yc.video.player.yc_video_player;
// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import static com.google.android.exoplayer2.Player.REPEAT_MODE_ALL;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.Listener;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;

import io.flutter.plugin.common.EventChannel;
import io.flutter.view.TextureRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class YcVideoPlayer {

    private static final String FORMAT_SS = "ss";
    private static final String FORMAT_DASH = "dash";
    private static final String FORMAT_HLS = "hls";
    private static final String FORMAT_OTHER = "other";

    private SimpleExoPlayer exoPlayer;

    private Surface surface;

    private final TextureRegistry.SurfaceTextureEntry textureEntry;

    private YcQueuingEventSink eventSink = new YcQueuingEventSink();

    private final EventChannel eventChannel;

    private boolean isInitialized = false;

    private final YcVideoPlayerOptions options;


    private Context context;
    private String dataSource;
    private String formatHint;
    private Map<String, String> httpHeaders;

    private JSONArray _items = new JSONArray();


    YcVideoPlayer(Context context,
                  EventChannel eventChannel,
                  TextureRegistry.SurfaceTextureEntry textureEntry,
                  String dataSource,
                  String formatHint,
                  Map<String, String> httpHeaders,
                  YcVideoPlayerOptions options) {

        this.context = context;
        this.eventChannel = eventChannel;
        this.textureEntry = textureEntry;
        this.options = options;
        this.dataSource = dataSource;
        this.formatHint = formatHint;
        this.httpHeaders = httpHeaders;

        try {
            _items = new JSONArray("[\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fb64a23e25fbd14cd10b5f9\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fb64a23e25fbd14cd10b5f9\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_400,640x360_700,640x360_1000,950x540_1500,.f4v.csmil/master.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fb64a23e25fbd14cd10b5f9\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/10_coffee_painting_flower_2015_v2_nidhi_maam/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fc20612e25fbd14cd10b78d\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1202_v2_3dmoddeling_robot/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fc1fbdfe25fbd14cd10b775\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1204_v2_moddeling_fruit_and_vegetable_making_1/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fc1fa04e25fbd14cd10b772\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1213_shipra_mam_7_papercrumpling_ice_cream/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fc1fa53e25fbd14cd10b773\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1215_shipra_mam_8_papercruumpling_birthday_cap_1/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fc1faa7e25fbd14cd10b774\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1216_shipra_mam/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fdc53014b69f4739c8195eb\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1405_shagun_mam_v2/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6009192866c59b7f73b7fe73\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1410_15121_minion_fonts_a_to_p/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"600914f066c59b7f73b7fe51\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1418_smiley_cartoon_fonts_with_shagun_maam_t_to_z_4754/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fc1fc4ae25fbd14cd10b776\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1608/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fc1fcf8e25fbd14cd10b777\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1609/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fc1fd23e25fbd14cd10b778\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1610/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5ff6a3db883c9679aeae50e7\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1618/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fbb64bde25fbd14cd10b712\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1903_1manisha_groove_to_coca_cola/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5ff85e8fd586871f2ecc7636\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1919_1_updated_link_below_1_hour_groove_to_main_deewana_tera/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"601b8d2cd9297255c85b4c62\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1927_groove_to_folk_dance_punjabi_ohho_ohho_remix_bhangra_with_manisha_maam/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"601b8d73d9297255c85b4c63\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1928_groove_to_folk_dance_rajasthani_ghoomar_with_manisha_maam/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"601b8d8cd9297255c85b4c64\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1929_groove_to_folk_dance_goan_ya_ya_mayaya_with_manisha_maam/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"601b8db9d9297255c85b4c65\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1930_groove_to_folk_dance_puppet_luk_chup_na_jao_ji_with_manisha_maam/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"601b8ddcd9297255c85b4c66\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/1931_groove_to_cham_cham_cham_rain_dance_with_manisha_maam/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fae3b91ab3bcd0a4d996184\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2013_paint_dense_palm_forest_with_coffee/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5ff99f7ad586871f2ecc77b4\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2067_f_water_doodling_trees_final_updated_link/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fdc5ecf4b69f4739c819603\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2067_nidhi_mam_na2067/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fdc5f0e4b69f4739c819604\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2068_na2068/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5ff97af8d586871f2ecc777e\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2102_farm_thememp4_2/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6014f7c322c2e6223265cfb3\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2232_new_medha_mam_get_curious_learn_about_the_wonders_of_our_world/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6014fb8022c2e6223265cfcf\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2249_program_finale_general_knowledge_qanda_session/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fb648e8e25fbd14cd10b5f7\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2404_shruti_russian_salad_sandwich_and_potato_salad/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"600a97e666c59b7f73b80733\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2427_republic_day_shruti_maam/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fd4509940e39b5e7a83be44\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2604_renuka_mam_v2/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9fa592d24afa22f7ba2d83\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2_11_20_session_5pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9e544ad24afa22f7ba2629\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2_11_20_session_6pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fdc64834b69f4739c81960f\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/2_drawing_drawing_with_triangles/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60029c4224e23921f2443975\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/3309/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6013d702f8aa2d2f691d9d17\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/3312/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"600aa04266c59b7f73b80756\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/3314_art_sessions_drawing_a_hamster/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60027c7d24e23921f24438f8\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/3322/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9d3d9bf1dc9b096de0032e\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/3_11_20_session_5pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9bcf45870a0406e9d9e2fd\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/3_11_20_session_6pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fb6478ae25fbd14cd10b5f4\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/3_pottery_bird_nest_apeksha_maam/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9bd990870a0406e9d9e379\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/4_11_20_session_5pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9e1196f1dc9b096de01067\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/4_11_20_session_6pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9bdc4e870a0406e9d9e3a1\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/5_11_20_session_5pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9bc8a4870a0406e9d9e2dc\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/5_11_20_session_6pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9d3f34f1dc9b096de00340\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/6_11_20_session_5pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9e45fbd24afa22f7ba25cf\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/6_11_20_session_5pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9bbe35870a0406e9d9e2a8\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/6_11_20_session_6pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5fb6479ee25fbd14cd10b5f5\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/6_gk_vegetatable_medha_maam/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9e1380f1dc9b096de01071\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/7_11_20_session_5pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5f9be070870a0406e9d9e3ea\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/7_11_20_session_6pm/hls_session/session_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"5ff947efd586871f2ecc7713\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/7_yoga_birthday_final_birthday_theme/hls_class/class_video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606d5854057645053dfd7bd3\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1111learn_a_night_scenery_painting_with_rupal_maam403600/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606d5e8c057645053dfd7beb\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1122learn_how_to_paint_using_potato_with_rupal_maam583800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605b3fc5abc082365aa9d7a3\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1149_introduction_to_warli_art_with_borders_session_1_5513/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605b4208abc082365aa9d7bb\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1151_learn_to_make_animals_and_plants_through_warli_art_session_3_5733/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605b420aabc082365aa9d7bc\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1152_program_finale_learn_to_make_a_scenery_through_warli_art_session_4_5306/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605b420babc082365aa9d7bd\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1153_program_finale_learn_to_make_a_scenery_through_warli_art_session_5_4614/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6083bf661ea9c46a3cd6bf73\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1316ggaming_fun_with_ggrandma_letter_g504700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6083c2881ea9c46a3cd6bf84\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1317ollie_the_octopus_fun_with_letter_o421100/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6083e51a1ea9c46a3cd6c11c\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1320_v2folding_fun_with_fantastic_f_f514500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071532445a8a82deccea3af\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1415_shagun_mam_newadvanced_cartoon_fonts_with_shagun_maama_to_m452000/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071573945a8a82deccea40e\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1420_2advanced_cartoon_fonts_with_shagun_maamv_to_z470300/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605b471aabc082365aa9d7fe\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1422_shagun_mam_new_lets_learn_poster_making_session_2_4838/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605b45d3abc082365aa9d7e5\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1423_shagun_v2_lets_learn_poster_making_session_3_4515/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6075930045a8a82decceb8dd\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1612storytime_with_aditi_maam_its_a_ladoo_party380800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6075902f45a8a82decceb8c7\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1614storytime_with_aditi_maam_kottavi_raja_and_his_sleepy_kingdom394400/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606ebe84057645053dfd80c4\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1802abracadabra_its_magic_class_with_mayank_sleight_of_hand_trick_and_appearing_match_stick_magic291800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606ed142057645053dfd8138\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1804abracadabra_its_magic_class_with_mayank_magic_trick_with_match_box_and_magnet381600/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606ed28d057645053dfd8149\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1807abracadabra_its_magic_class_balancing_curreny_note_trick_and_jumping_card_trick480600/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606ea372057645053dfd8017\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1974groove_to_milegi_milegi_with_manisha_maam520800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606ea872057645053dfd802d\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1978groove_to_slowly_slowly_with_manisha_maam510000/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606d4d09057645053dfd7b9a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1986groove_to_dance_basanti520700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606d66ce057645053dfd7c0a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/1987groove_to_top_tucker512700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6070363145a8a82decce9e35\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/201_8421_1agamograph_art533900/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6070372645a8a82decce9e49\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/202_1tessellation_art_make_your_own_puzzle542800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6087f65490726d362185b975\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/2034_learn_with_nidhi_maam_draw_pictures_using_numbers_5817/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607037e945a8a82decce9e50\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/203beach_fun_in_flipflop510400/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6076de3f45a8a82deccebfa3\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/204_14421landscape_painting_session_1555700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6079342745a8a82deccecc8e\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/206_1_landscape_painting_session_3_533400/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60703cd345a8a82decce9e8d\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/207_7421lets_groove_to_nadiyon_paar445600/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6070394f45a8a82decce9e5b\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/208_1lets_groove_to_ni_nachleh530700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077a8e845a8a82deccec237\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/209_14421_v2_fun_and_fitness_with_zumba_session_1_4700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6073e0bd45a8a82decceaf81\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/210fun_and_fitness_with_zumba401700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60703a2245a8a82decce9e5f\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/211learn_yoga_domestic_animals_theme_with_payal_maam563000/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60703b0045a8a82decce9e77\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/212_8421learn_yoga_2d_and_3d_shapes_theme_with_payal_maam582200/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6076d98145a8a82deccebf6a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/213_14421cook_cucumber_gaspacho_and_creamy_fruity_canapes_with_shruti_maam425200/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6076dad345a8a82deccebf72\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/214_14421cook_banana_sushi_and_creamy_yoghurt_chia_seed_pudding_with_shruti_mam454400/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607813ee45a8a82deccec5fa\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/2240_medha_mamget_curious_learn_about_the_important_facts_of_human_body_with_medha_maam422000/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6076b47745a8a82deccebde7\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/2414_27321cook_mug_cake_and_fruits_oats_smoothie_with_shruti_maam461500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606eb7d6057645053dfd808a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/2422cook_italian_vegetarian_chopped_salad_and_japanese_fruit_sando_with_shruti_maam445200/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606eb8c5057645053dfd808c\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/2423cook_oats_and_fruits_yogurt_parfait_and_tahini_beet_hummus_with_shruti_maam435000/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606eb94b057645053dfd8093\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/2424cook_mac_and_cheese_and_virgin_mojito_with_shruti_maam444900/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606ebab6057645053dfd80a4\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/2426_24321little_taste_makers_kolkatas_jhaal_muri_and_mexican_cheesy_nachos580500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607182eb45a8a82deccea599\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/301_6421gk_all_about_beaches424200/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607183a945a8a82deccea5a9\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/302_7421gk_all_about_united_nations521500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6079698d45a8a82deccecee2\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/303_1monday_class_3_gk_all_about_the_land_borders441400/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071850c45a8a82deccea5e7\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/304_6421_lets_stack_the_cubes550800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607185c745a8a82deccea5e8\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/305_6421_learn_math_art_through_spirolaterals455500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071877045a8a82deccea5f5\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/306_8421_make_your_own_game_trominoes474100/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071888945a8a82deccea5fb\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/307_8421_lets_make_elevator_pulley_system551400/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071897245a8a82deccea662\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/308_6421lets_animate_the_world_with_your_own_flip_book461800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60718a1545a8a82deccea665\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/309_2scintillating_science_secrets551100/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60718ac845a8a82deccea66a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/310_1_time_to_become_young_scientist484800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6087a89f90726d362185b578\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/311tuesday_class_11_session_1_introduction_to_blogging374300/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6087ad1d90726d362185b5b7\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/312wednesday_class_12_session_2_setting_up_your_blog_site474200/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6087b19f90726d362185b615\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/313thursday_class_13_session_3_adding_content_on_your_blog400300/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6087b26a90726d362185b61a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/314friday_class_14_session_4_publishing_your_blog_post391000/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602e19eee5522562ecc74c65\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/3522_lets_boogie_woogie_dance_pe_chance/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607eaadb4cd6480f68d431ad\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/401tuesday_class_1_community_helpers_at_work_jobs_they_do590600/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607eac5d4cd6480f68d431c7\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/402_12421wednesday_class_2_number_fun_math_fables514300/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607ead7c4cd6480f68d431d5\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/403_8421thursday_class_3_shapes_surround_us_fun_with_shape_monster512300/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607eae9b4cd6480f68d431f1\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/404_10221friday_class_4_colourful_under_water_world511800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607eafe24cd6480f68d43222\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/405_10221saturday_class_5_welcome_to_the_jungle483500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607eb0f04cd6480f68d43236\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/406sunday_class_6_once_upon_a_time_in_fantasy_land411600/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607eb2064cd6480f68d43244\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/407_12421monday_class_7_all_about_incredible_india523300/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607eb32f4cd6480f68d43253\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/408_12421tuesday_class_8_magic_of_colours535200/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607eb48c4cd6480f68d43267\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/409_2wednesday_class_9_around_the_world_learn_about_countries524200/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607fae31ae5226684ffde74a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/410_3thursday_class_10_go_green_lets_learn_about_plants_and_trees500100/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6086be4e1ea9c46a3cd6d5f4\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/4_men_all_blind/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077e62b45a8a82deccec3ef\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/502_2love_you_mom_card_and_dance_fun451400/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077e73a45a8a82deccec3f5\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/503_9421sea_creatures_collage_and_dance_masti534500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077e90b45a8a82deccec401\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/504_1pop_up_tricolour_card_and_patriotic_dance_fun481700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6087f31c90726d362185b95c\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/504_pop_up_tricolour_card_and_patriotic_dance_fun_4810/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077ea3c45a8a82deccec406\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/505craft_and_dance_on_vacation_theme463500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077ebda45a8a82deccec40f\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/506art_craft_and_dance_on_space_theme491700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077eda845a8a82deccec426\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/507_1jungle_fun_with_art_and_dance482600/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077ef2145a8a82deccec42b\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/508_2musical_instrument_craft_ninja_turtle_tambourine441800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607e70364cd6480f68d42f1a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/509_1sunday_class_9_draw_and_lets_do_yoga_with_peppa_and_her_frenz574800/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607e99874cd6480f68d4310d\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/510_19421monday_class_10_draw_and_form_yoga_poses_with_your_favourite_birds451000/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60810c3dae5226684ffdef7f\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/511_1_mergedtuesday_class_11_draw_and_drive_vehicles_with_yoga_fun505700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077f21d45a8a82deccec45f\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/512_2learn_about_body_parts_and_become_fit_with_zumba552500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607e74304cd6480f68d42f69\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/513_16421thursday_class_13_learn_some_facts_and_groove_to_few_states_and_capitals_dance_forms475700/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6077f2f545a8a82deccec468\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/514_1explore_drama_fun_with_dance502500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60461b2e1608ef558af899ba\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/a_boy_named_billy/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"604f2a6127bd690cf36ddb0f\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/acid_base_indicator_science_experiment/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6088fe9a322d6e110ecc6aa2\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/adi1621_2introduction_to_storywriting_with_aditi_maam_session_1393900/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60890175322d6e110ecc6ac0\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/adi1622_22421storywriting_with_aditi_maam_session_2402200/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60890529322d6e110ecc6b2b\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/adi1623_22421storywriting_with_aditi_maam_session_3421000/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60890a14322d6e110ecc6ba4\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/adi1625_21421storywriting_with_aditi_maam_session_5455600/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60890c79322d6e110ecc6bc6\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/adi1626_27421progam_finale_storywriting_with_aditi_maam_session_6380500/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60890802322d6e110ecc6b83\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/adi_1624_22421storywriting_with_aditi_maam_session_4412400/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6041df4b310fdd06c447f914\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/aditi_learnt_a_lesson/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605f420286c86f7d2901caf0\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/air_water_path_changing_experiment/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607dbeeb4cd6480f68d42dc8\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/akbar_and_birbal_akbars_five_questions/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60476f3b1608ef558af8a1a3\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/akbar_and_the_four_fools/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606b09c66c68c22bc2a998a3\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/akbarbirbal_and_cows_milk/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6040949d310fdd06c447f3cd\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/aladdin_and_magicians_revenge/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6024daa46decae570a75a799\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/aladdin_and_the_magic_lamp/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607154ae45a8a82deccea3c3\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/ambedkar_jayanti_decor/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071ad1245a8a82deccea7f5\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/ambedkar_jayanti_storyhindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6040940a310fdd06c447f3cc\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/animal_face_penstand/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"603cea44310fdd06c447e377\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/annaas_story/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"603ce03f310fdd06c447e2d5\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/annas_story/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605b386dabc082365aa9d6ef\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/ant_and_its_anthill/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605f3fdc86c86f7d2901cab2\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/ant_and_the_grasshopper/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60714e4045a8a82deccea377\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/ant_and_the_grasshopper_podcast/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"603f53f9310fdd06c447edd2\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/arjuns_concentration/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"603f2cde310fdd06c447ec94\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/art_sessions_drawing_a_flying_bird_using_hands_ka_3372mp4/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071ac9c45a8a82deccea7ed\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/baisakhi_storyhindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60375ff1310fdd06c447ca9c\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/bhakt_prahlad/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"603793a0310fdd06c447cbc4\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/bhakt_prahladd/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607dbe584cd6480f68d42dc5\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/birbal_catches_a_thief/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60461bcd1608ef558af899bb\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/bird_house_painting_hindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071599545a8a82deccea43a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/blue_jackal/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605f423d86c86f7d2901caf1\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/bundle_of_sticks/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"604cead11608ef558af8c35a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/cherry_thumb_painting_hindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605c741e2dce5604ca0b174c\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/chika_and_his_homework/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605ca6642dce5604ca0b190c\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/chintu_and_raj/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607dc10e4cd6480f68d42dd7\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/choose_to_save_a_life/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605894ababc082365aa9c858\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/colorful_paper_hat/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607d13314cd6480f68d4248c\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/comb_painting_landscape/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"604cea081608ef558af8c359\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/count_wisely/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"604cd0911608ef558af8c2c2\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/crocodile_and_hen_hindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607dc0874cd6480f68d42dd3\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/crystal_ball/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6071593e45a8a82deccea437\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/cute_notepad/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60250fe56decae570a75a8dd\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_activity_i_saraswati_puja_painting_i_diy_for_kids/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602512076decae570a75a8e8\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_activity_i_vinegarbaking_soda_experiment_i_diy_for_kids_hindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6030d6e0bbfea342aca88a20\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_craft_i_cupcake_lining_dinosaur_i_diy_for_kids/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602511236decae570a75a8e3\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_craft_i_handprint_pony_i_diy_for_kids/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602a4f88c0c17704a6dab437\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_craft_i_origami_i_penguin_i_diy_for_kids/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602a474dc0c17704a6dab422\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_craft_i_origami_i_tulip_i_diy_for_kids/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6033338fbbfea342aca89192\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_craft_i_paper_plate_cloud_tree_i_diy_for_kids_hindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602a6ee4c0c17704a6dab558\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_craft_i_photo_frame_i_diy_for_kids/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"603104d5bbfea342aca88b83\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_crafts_i_3d_elephant_i_activity_for_kids/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602a5ebfc0c17704a6dab4ac\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_crafts_i_slime_and_clay_i_activity_for_kids/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"60250aa56decae570a75a89f\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_crafts_i_teddy_bear_with_no_8_i_activity_for_kids_hindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602a5cb7c0c17704a6dab499\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_for_kids_or_flower_painting_or_beautiful_back_bottle_flower/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602a6999c0c17704a6dab4fb\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_painting_i_spiral_flower_painting_i_diy_for_kids_hindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"602a6f6ac0c17704a6dab559\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_stories_for_kids_i_silly_story_of_bondapalli_i_stories_for_kids_hindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6030d80bbbfea342aca88a26\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/diy_stories_i_salimthe_knife_sharpener_i_moral_stories_for_kids/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607d74ca4cd6480f68d42a0b\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/do_billiyan_aur_chalaak_bandar/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607d85f24cd6480f68d42b13\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/dog_and_the_well/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6086bee11ea9c46a3cd6d5fd\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/dog_loses_the_bone/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"605f45ad86c86f7d2901cb0e\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/doodle_artopen_and_closed_book_hindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607dbf604cd6480f68d42dca\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/doodling_cactus/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606ebb51057645053dfd80ae\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/dummy_video/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"606a8b346c68c22bc2a99467\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/easter_candy_rabbithindi/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6066fc24af647619ae2400da\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/easter_story/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"6065cab6af647619ae23fdee\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/easters_special_origami_carrot/hls/video.m3u8\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": {\n" +
                    "      \"oid\": \"607d6f8a4cd6480f68d4298a\"\n" +
                    "    },\n" +
                    "    \"videoUrl\": \"https://video.yellowclass.com/CLASS/easy_butterfly_tops/hls/video.m3u8\"\n" +
                    "  }\n" +
                    "]\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.initExoPlayerInstance();


    }

    private void initExoPlayerInstance() {

        exoPlayer = new SimpleExoPlayer.Builder(this.context).build();



        Uri uri = Uri.parse(this.dataSource);

        DataSource.Factory dataSourceFactory;
        if (isHTTP(uri)) {

            DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setUserAgent("ExoPlayer")
                    .setAllowCrossProtocolRedirects(true);

            if (httpHeaders != null && !httpHeaders.isEmpty()) {
                httpDataSourceFactory.setDefaultRequestProperties(httpHeaders);
            }
            dataSourceFactory = httpDataSourceFactory;
        } else {
            dataSourceFactory = new DefaultDataSourceFactory(context, "ExoPlayer");
        }

        MediaSource mediaSource = buildMediaSource(uri, dataSourceFactory, formatHint, context);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();

        setupVideoPlayer(eventChannel, textureEntry);
    }

    private static boolean isHTTP(Uri uri) {
        if (uri == null || uri.getScheme() == null) {
            return false;
        }
        String scheme = uri.getScheme();
        return scheme.equals("http") || scheme.equals("https");
    }

    private MediaSource buildMediaSource(Uri uri, DataSource.Factory mediaDataSourceFactory, String formatHint, Context context) {
        int type;
        if (formatHint == null) {
            type = Util.inferContentType(uri.getLastPathSegment());
        } else {
            switch (formatHint) {
                case FORMAT_SS:
                    type = C.TYPE_SS;
                    break;
                case FORMAT_DASH:
                    type = C.TYPE_DASH;
                    break;
                case FORMAT_HLS:
                    type = C.TYPE_HLS;
                    break;
                case FORMAT_OTHER:
                    type = C.TYPE_OTHER;
                    break;
                default:
                    type = -1;
                    break;
            }
        }
        switch (type) {

            case C.TYPE_SS:

                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        new DefaultDataSourceFactory(context, null, mediaDataSourceFactory))
                        .createMediaSource(MediaItem.fromUri(uri));

            case C.TYPE_DASH:

                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        new DefaultDataSourceFactory(context, null, mediaDataSourceFactory))
                        .createMediaSource(MediaItem.fromUri(uri));

            case C.TYPE_HLS:

                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(uri));

            case C.TYPE_OTHER:

                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(uri));

            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private void setupVideoPlayer(EventChannel eventChannel, TextureRegistry.SurfaceTextureEntry textureEntry) {

        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink sink) {
                eventSink.setDelegate(sink);
            }

            @Override
            public void onCancel(Object o) {
                eventSink.setDelegate(null);
            }
        });

        surface = new Surface(textureEntry.surfaceTexture());
        exoPlayer.setVideoSurface(surface);

        setAudioAttributes(exoPlayer, options.mixWithOthers);
        exoPlayer.addListener(new Listener() {

            private boolean isBuffering = false;

            public void setBuffering(boolean buffering) {

                if (isBuffering != buffering) {

                    isBuffering = buffering;
                    Map<String, Object> event = new HashMap<>();
                    event.put("event", isBuffering ? "bufferingStart" : "bufferingEnd");
                    eventSink.success(event);

                }
            }


            @Override
            public void onPlaybackStateChanged(final int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    setBuffering(true);
                    sendBufferingUpdate();
                } else if (playbackState == Player.STATE_READY) {
                    if (!isInitialized) {
                        isInitialized = true;
                        sendInitialized();
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("event", "completed");
                    eventSink.success(event);
                }

                if (playbackState != Player.STATE_BUFFERING) {
                    setBuffering(false);
                }
            }

            @Override
            public void onPlayerError(@NonNull final ExoPlaybackException error) {
                Log.e("SHIVAM", "onPlayerError: " + error.toString());

                setBuffering(false);
                if (eventSink != null) {

//                    eventSink.error("VideoError", "Video player had error " + error, null);

                    Map<String, Object> event = new HashMap<>();
                    event.put("event", "playerError");
                    event.put("errorType", error.rendererName);
                    event.put("errorDetail", error.toString());

                    eventSink.success(event);

                    Log.e("SHIVAM", "onPlayerError: DISPOSING...... " + dataSource);

                    YcVideoPlayer.this.softDispose();

                    Log.e("SHIVAM", "onPlayerError: DISPOSED and calling initExoPlayerInstance...... " + dataSource);

                    YcVideoPlayer.this.initExoPlayerInstance();

                    Log.e("SHIVAM", "onPlayerError: initExoPlayerInstance done...... " + dataSource);
                }

            }

        });

    }

    void sendBufferingUpdate() {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "bufferingUpdate");
        List<? extends Number> range = Arrays.asList(0, exoPlayer.getBufferedPosition());
        // iOS supports a list of buffered ranges, so here is a list with a single range.
        event.put("values", Collections.singletonList(range));
        eventSink.success(event);
    }

    @SuppressWarnings("deprecation")
    private static void setAudioAttributes(SimpleExoPlayer exoPlayer, boolean isMixMode) {
        exoPlayer.setAudioAttributes(
                new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MOVIE).build(), !isMixMode);
    }

    void play() {
        exoPlayer.setPlayWhenReady(true);
    }

    void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    void setLooping(boolean value) {
        exoPlayer.setRepeatMode(value ? REPEAT_MODE_ALL : REPEAT_MODE_OFF);
    }

    void setVolume(double value) {
        float bracketedValue = (float) Math.max(0.0, Math.min(1.0, value));
        exoPlayer.setVolume(bracketedValue);
    }

    void setPlaybackSpeed(double value) {
        // We do not need to consider pitch and skipSilence for now as we do not handle them and
        // therefore never diverge from the default values.
        final PlaybackParameters playbackParameters = new PlaybackParameters(((float) value));

        exoPlayer.setPlaybackParameters(playbackParameters);
    }

    void seekTo(int location) {
        exoPlayer.seekTo(location);
    }

    long getPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void sendInitialized() {

        if (isInitialized) {

            Map<String, Object> event = new HashMap<>();
            event.put("event", "initialized");
            event.put("duration", exoPlayer.getDuration());

            if (exoPlayer.getVideoFormat() != null) {

                Format videoFormat = exoPlayer.getVideoFormat();

                int width = videoFormat.width;
                int height = videoFormat.height;
                int rotationDegrees = videoFormat.rotationDegrees;

                // Switch the width/height if video was taken in portrait mode

                if (rotationDegrees == 90 || rotationDegrees == 270) {
                    width = exoPlayer.getVideoFormat().height;
                    height = exoPlayer.getVideoFormat().width;
                }

                event.put("width", width);
                event.put("height", height);
            }
            eventSink.success(event);
        }
    }

    void dispose() {

        if (isInitialized) {
            exoPlayer.stop();
        }
        textureEntry.release();
        eventChannel.setStreamHandler(null);
        if (surface != null) {
            surface.release();
        }
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }


    void softDispose() {
        if (isInitialized) {
            exoPlayer.stop();
        }
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }
}
