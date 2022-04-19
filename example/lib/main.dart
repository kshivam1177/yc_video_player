// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// ignore_for_file: public_member_api_docs

/// An example of using the plugin, controlling lifecycle and playback of the
/// video.

import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:yc_video_player/yc_video_player.dart';

void main() {
  runApp(
    MaterialApp(
      home: _App(),
    ),
  );

  // FlutterError.onError = (FlutterErrorDetails details) {
  //   debugPrint("SHIVAM-DartError ${details.toString()}");
  // };
}

class _App extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        key: const ValueKey<String>('home_page'),
        appBar: AppBar(
          title: const Text('Video player example'),
          actions: <Widget>[
            IconButton(
              key: const ValueKey<String>('push_tab'),
              icon: const Icon(Icons.navigation),
              onPressed: () {
                Navigator.push<_PlayerVideoAndPopPage>(
                  context,
                  MaterialPageRoute<_PlayerVideoAndPopPage>(
                    builder: (BuildContext context) => _PlayerVideoAndPopPage(),
                  ),
                );
              },
            )
          ],
          bottom: const TabBar(
            isScrollable: true,
            tabs: <Widget>[
              Tab(icon: Icon(Icons.insert_drive_file), text: "Asset"),
              Tab(icon: Icon(Icons.list), text: "List example"),
            ],
          ),
        ),
        body: TabBarView(
          children: <Widget>[
            _ButterFlyAssetVideo(""),
            _ButterFlyRemoteVideoInList(),
          ],
        ),
      ),
    );
  }
}

class _ButterFlyRemoteVideoInList extends StatelessWidget {
  Future<List<String>> videoList(BuildContext context) async {
    final String fileContents = await DefaultAssetBundle.of(context)
        .loadString('assets/video_data.json');

    List<dynamic> _decoded = jsonDecode(fileContents);

    return List.generate(
        _decoded.length, (index) => _decoded[index]["videoUrl"]);
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<String>>(
        initialData: const <String>[],
        future: videoList(context),
        builder: (context, snapshot) {
          if (snapshot.data?.isNotEmpty ?? false) {
            return PageView.builder(
              itemCount: 100,
              itemBuilder: (itemBuilder, index) {
                return _ExampleCard(remoteURL: snapshot.data?[index] ?? "");
              },
              // gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              //   crossAxisCount: 2,
              //   childAspectRatio: 3 / 2,
              // ),
            );
          } else {
            return const Center(child: CircularProgressIndicator());
          }
        });
  }
}

/// A filler card to show the video in a list of scrolling contents.
class _ExampleCard extends StatelessWidget {
  const _ExampleCard({Key? key, required this.remoteURL}) : super(key: key);

  final String remoteURL;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: _ButterFlyAssetVideo(remoteURL),
    );
  }
}

class _ButterFlyAssetVideo extends StatefulWidget {
  final String remoteURL;

  const _ButterFlyAssetVideo(this.remoteURL);

  @override
  _ButterFlyAssetVideoState createState() => _ButterFlyAssetVideoState();
}

class _ButterFlyAssetVideoState extends State<_ButterFlyAssetVideo> {
  late VideoPlayerController _controller;

  @override
  void initState() {
    super.initState();
    _controller = widget.remoteURL.isEmpty
        ? VideoPlayerController.asset('assets/Butterfly-209.mp4')
        : VideoPlayerController.network(widget.remoteURL);

    _controller.addListener(() {
      // setState(() {});
      // print("receiveBroadcastStream-listner->")
    });
    _controller.setLooping(true);
    _controller.initialize().then((_) => setState(() {}));
    _controller.play();

    _controller.onErrorReceived((videoPlayerError) {
      debugPrint("\n\n  onErrorReceived"
          "-> ${videoPlayerError.errorType.name} "
          "-> ${videoPlayerError.errorDetail} \n\n");
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        children: <Widget>[
          Container(
            padding: const EdgeInsets.all(2),
            child: AspectRatio(
              aspectRatio: _controller.value.isInitialized
                  ? _controller.value.aspectRatio
                  : 3 / 1.8,
              child: Stack(
                alignment: Alignment.bottomCenter,
                children: <Widget>[
                  VideoPlayer(_controller),
                  _ControlsOverlay(controller: _controller),
                  // VideoProgressIndicator(_controller, allowScrubbing: true),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ControlsOverlay extends StatelessWidget {
  const _ControlsOverlay({Key? key, required this.controller})
      : super(key: key);

  static const _examplePlaybackRates = [
    0.25,
    0.5,
    1.0,
    1.5,
    2.0,
    3.0,
    5.0,
    10.0,
  ];

  final VideoPlayerController controller;

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        AnimatedSwitcher(
          duration: Duration(milliseconds: 50),
          reverseDuration: Duration(milliseconds: 200),
          child: controller.value.isPlaying
              ? SizedBox.shrink()
              : Container(
                  color: Colors.black26,
                  child: Center(
                    child: Icon(
                      Icons.play_arrow,
                      color: Colors.white,
                      size: 100.0,
                      semanticLabel: 'Play',
                    ),
                  ),
                ),
        ),
        GestureDetector(
          onTap: () {
            controller.value.isPlaying ? controller.pause() : controller.play();
          },
        ),
        Align(
          alignment: Alignment.topRight,
          child: PopupMenuButton<double>(
            initialValue: controller.value.playbackSpeed,
            tooltip: 'Playback speed',
            onSelected: (speed) {
              controller.setPlaybackSpeed(speed);
            },
            itemBuilder: (context) {
              return [
                for (final speed in _examplePlaybackRates)
                  PopupMenuItem(
                    value: speed,
                    child: Text('${speed}x'),
                  )
              ];
            },
            child: Padding(
              padding: const EdgeInsets.symmetric(
                // Using less vertical padding as the text is also longer
                // horizontally, so it feels like it would need more spacing
                // horizontally (matching the aspect ratio of the video).
                vertical: 12,
                horizontal: 16,
              ),
              child: Text('${controller.value.playbackSpeed}x'),
            ),
          ),
        ),
      ],
    );
  }
}

class _PlayerVideoAndPopPage extends StatefulWidget {
  @override
  _PlayerVideoAndPopPageState createState() => _PlayerVideoAndPopPageState();
}

class _PlayerVideoAndPopPageState extends State<_PlayerVideoAndPopPage> {
  late VideoPlayerController _videoPlayerController;
  bool startedPlaying = false;

  @override
  void initState() {
    super.initState();

    _videoPlayerController =
        VideoPlayerController.asset('assets/Butterfly-209.mp4');
    _videoPlayerController.addListener(() {
      if (startedPlaying && !_videoPlayerController.value.isPlaying) {
        Navigator.pop(context);
      }
    });
  }

  @override
  void dispose() {
    _videoPlayerController.dispose();
    super.dispose();
  }

  Future<bool> started() async {
    await _videoPlayerController.initialize();
    await _videoPlayerController.play();
    startedPlaying = true;
    return true;
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      elevation: 0,
      child: Center(
        child: FutureBuilder<bool>(
          future: started(),
          builder: (BuildContext context, AsyncSnapshot<bool> snapshot) {
            if (snapshot.data == true) {
              return AspectRatio(
                aspectRatio: _videoPlayerController.value.aspectRatio,
                child: VideoPlayer(_videoPlayerController),
              );
            } else {
              return const Text('waiting for video to load');
            }
          },
        ),
      ),
    );
  }
}
