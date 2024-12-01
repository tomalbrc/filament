import 'package:flutter/material.dart';
import 'package:multi_split_view/multi_split_view.dart';
import 'package:flutter_json_view/flutter_json_view.dart';


class BlocksPage extends StatefulWidget {
  const BlocksPage({super.key, required this.title});

  final String title;

  @override
  State<BlocksPage> createState() => _BlocksPageState();
}

class _BlocksPageState extends State<BlocksPage> {
  final MultiSplitViewController _controller = MultiSplitViewController();

  bool _pushDividers = false;

  @override
  void initState() {
    super.initState();
    _controller.areas = [
      Area(flex: 1, data: "form"),
      Area(flex: 1, data: "code"),
    ];
    _controller.addListener(_rebuild);
  }

  void _rebuild() {
    setState(() {
      // rebuild to update empty text and buttons
    });
  }

  _onDividerTap(int dividerIndex) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      duration: const Duration(seconds: 1),
      content: Text("Tap on divider: $dividerIndex"),
    ));
  }

  _onDividerDoubleTap(int dividerIndex) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      duration: const Duration(seconds: 1),
      content: Text("Double tap on divider: $dividerIndex"),
    ));
  }

  @override
  Widget build(BuildContext context) {
    Widget? content;
    if (_controller.areasCount != 0) {
      MultiSplitView multiSplitView = MultiSplitView(
          onDividerTap: _onDividerTap,
          onDividerDoubleTap: _onDividerDoubleTap,
          controller: _controller,
          pushDividers: _pushDividers,
          builder: (BuildContext context, Area area) => switch (area.data) {
            "code" => JsonView.string(
              "{\"code\": \"dsdss\"}",
            ),
            "form" => const Padding(padding: EdgeInsets.all(8), child: Text("Form")),
            Object() => const Text("unknown"),
            null => const Text("unknown"),
          });

      content = MultiSplitViewTheme(
              data: MultiSplitViewThemeData(
                  dividerPainter: DividerPainters.grooved2()),
              child: multiSplitView);
    } else {
      content = const Center(child: Text('Empty'));
    }

    return content;
  }
}