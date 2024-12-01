import 'package:flutter/services.dart';
import 'package:sidebarx/sidebarx.dart';

import 'package:flutter/material.dart';
import 'package:webapp/editor_page.dart';

class MainPage extends StatefulWidget {
  const MainPage({super.key, required this.title});

  final String title;

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  final List<Widget> _pages = const [
    EditorPage(
      title: 'filament items',
    ),
    EditorPage(
      title: 'filament blocks',
    ),
    EditorPage(
      title: 'filament decorations',
    ),
  ];

  final _controller = SidebarXController(selectedIndex: 0, extended: true);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        floatingActionButton: FloatingActionButton(
          onPressed: () {
            Clipboard.setData(ClipboardData(text: _pages[_controller.selectedIndex].key?.state?._generatedJson))
          },
          tooltip: 'Copy JSON',
          child: const Icon(Icons.copy),
        ),
        body: Row(
          children: [
            SidebarX(
              controller: _controller,
              footerDivider: divider,
              headerBuilder: (context, extended) {
                return const SizedBox(
                  height: 80,
                  child: Padding(
                    padding: EdgeInsets.all(16.0),
                    child: Icon(Icons.lightbulb, color: Colors.amber,),
                  ),
                );
              },
              theme: SidebarXTheme(
                margin: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: canvasColor,
                  borderRadius: BorderRadius.circular(20),
                ),
                hoverColor: scaffoldBackgroundColor,
                textStyle: TextStyle(color: Colors.white.withOpacity(0.7)),
                selectedTextStyle: const TextStyle(color: Colors.white),
                hoverTextStyle: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w500,
                ),
                itemTextPadding: const EdgeInsets.only(left: 30),
                selectedItemTextPadding: const EdgeInsets.only(left: 30),
                itemDecoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: canvasColor),
                ),
                selectedItemDecoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(
                    color: actionColor.withOpacity(0.37),
                  ),
                  gradient: const LinearGradient(
                    colors: [accentCanvasColor, canvasColor],
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.28),
                      blurRadius: 30,
                    )
                  ],
                ),
                iconTheme: IconThemeData(
                  color: Colors.white.withOpacity(0.7),
                  size: 20,
                ),
                selectedIconTheme: const IconThemeData(
                  color: Colors.white,
                  size: 20,
                ),
              ),
              extendedTheme: const SidebarXTheme(
                width: 200,
                decoration: BoxDecoration(
                  color: canvasColor,
                ),
              ),
              items: [
                SidebarXItem(
                    label: "Items",
                    icon: Icons.insert_emoticon,
                    onTap: () {
                      setState(() {});
                    }),
                SidebarXItem(
                    label: "Blocks",
                    icon: Icons.block,
                    onTap: () {
                      setState(() {});
                    }),
                SidebarXItem(
                    label: "Decorations",
                    icon: Icons.chair,
                    onTap: () {
                      setState(() {});
                    }),
              ],
            ),
            Expanded(child: _pages[_controller.selectedIndex])
          ],
        ));
  }
}

const primaryColor = Color(0xFF685BFF);
const canvasColor = Color(0xFF2E2E48);
const scaffoldBackgroundColor = Color(0xFF464667);
const accentCanvasColor = Color(0xFF3E3E61);
const white = Colors.white;
final actionColor = const Color(0xFF5F5FA7).withOpacity(0.6);
final divider = Divider(color: white.withOpacity(0.3), height: 1);
