# Simple and complex decorations

All decorations with behaviours, dye-able vanilla items or with a `blocks` configuration larger that a single block, are considered to be "complex".

The difference between simple and complex decorations gameplay wise is that
complex decorations are not pushable by pistons.

When it comes to the implementation, complex decorations use block entities to store data, while simple decorations are made up of a single, "simple", block.
