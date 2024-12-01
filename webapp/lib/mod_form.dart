import 'package:flutter/material.dart';
import 'dart:convert';

class ModForm extends StatefulWidget {
  final void Function(String) onJsonGenerated;

  const ModForm({super.key, required this.onJsonGenerated});

  @override
  _ModFormState createState() => _ModFormState();
}

class _ModFormState extends State<ModForm> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _typeController = TextEditingController();
  final TextEditingController _rarityController = TextEditingController();
  bool _isCraftable = false;
  String _rarity = "common";

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Form(
        key: _formKey,
        child: ListView(
          children: [
            TextFormField(
              controller: _nameController,
              decoration: const InputDecoration(
                labelText: 'Id',
                border: UnderlineInputBorder(),
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Please enter the item name.';
                }
                return null;
              },
              enableSuggestions: true,
            ),
            const SizedBox(height: 16),

            TextFormField(
              controller: _typeController,
              decoration: const InputDecoration(
                labelText: 'Item Type',
                border: OutlineInputBorder(),
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Please enter the item type.';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),

            TextFormField(
              controller: _rarityController,
              decoration: const InputDecoration(
                labelText: 'Rarity (e.g., Common, Rare)',
                border: OutlineInputBorder(),
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Please enter the item rarity.';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),

            CheckboxListTile(
              title: const Text('Is Craftable'),
              value: _isCraftable,
              onChanged: (bool? value) {
                setState(() {
                  _isCraftable = value ?? false;
                });
              },
            ),
            const SizedBox(height: 16),

            DropdownButtonFormField<String>(
              decoration: const InputDecoration(
                labelText: 'Select Rarity',
                border: OutlineInputBorder(),
              ),
              items: ['Common', 'Uncommon', 'Rare', 'Epic']
                  .map((rarity) => DropdownMenuItem(
                        value: rarity.toLowerCase(),
                        child: Text(rarity),
                      ))
                  .toList(),
              onChanged: (value) {
                _rarity = value!;
              },
              validator: (value) =>
                  value == null ? 'Please select a rarity.' : null,
            ),
            const SizedBox(height: 16),

            ElevatedButton(
              onPressed: () {
                if (_formKey.currentState?.validate() ?? false) {
                  final Map<String, dynamic> modData = {
                    'name': _nameController.text,
                    'type': _typeController.text,
                    'rarity': _rarityController.text,
                    'rarity2': _rarity,
                    'isCraftable': _isCraftable,
                  };

                  final String jsonOutput = jsonEncode(modData);
                  widget.onJsonGenerated(jsonOutput);
                }
              },
              child: const Text('Generate JSON'),
            ),
          ],
        ),
      ),
    );
  }
}
