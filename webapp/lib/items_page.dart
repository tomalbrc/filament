import 'package:flutter/material.dart';
import 'package:multi_split_view/multi_split_view.dart';
import 'package:flutter_json_view/flutter_json_view.dart';
import 'package:webapp/mod_form.dart';
import 'package:webapp/text_with_background.dart';

class ItemsPage extends StatefulWidget {
  const ItemsPage({super.key, required this.title});

  final String title;

  @override
  State<ItemsPage> createState() => _ItemsPageState();
}

class _ItemsPageState extends State<ItemsPage> {
  final MultiSplitViewController _controller = MultiSplitViewController();
  final bool _pushDividers = true;

  String _generatedJson = "{}";
  void _onJsonGenerated(String json) {
    setState(() {
      _generatedJson = json; // Update state with the new JSON
    });
  }

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
                    _generatedJson,
                  ),
                "form" => Column(
                    children: [const TextWithBackground(text: "filament items"), Expanded(child: ModForm(onJsonGenerated: _onJsonGenerated,))],
                  ),
                Object() => const Text("unknown"),
                null => const Text("unknown"),
              });

      content = MultiSplitViewTheme(
          data: MultiSplitViewThemeData(
              dividerPainter: DividerPainters.background()),
          child: multiSplitView);
    } else {
      content = const Center(child: Text('Empty'));
    }

    return Container(child: content);
  }
}
